/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package internal.util.sql.adodb;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@FunctionalInterface
interface PropertyLoader {

    @Nullable
    default String load(@Nonnull String name) throws IOException {
        return loadAll().get(Objects.requireNonNull(name));
    }

    @Nonnull
    Map<String, String> loadAll() throws IOException;

    @Nonnull
    default PropertyLoader memoize() {
        return new PropertyLoaders.MemoizingPropertyLoader(this);
    }

    @Nonnull
    default PropertyLoader memoizeWithExpiration(long duration, @Nonnull TimeUnit unit, @Nonnull LongSupplier clock) {
        return new PropertyLoaders.ExpiringMemoizingPropertyLoader(this, unit.toNanos(duration), clock);
    }
}
