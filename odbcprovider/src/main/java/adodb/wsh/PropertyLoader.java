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
package adodb.wsh;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@FunctionalInterface
interface PropertyLoader {

    @Nullable
    default String load(@NonNull String name) throws IOException {
        return loadAll().get(Objects.requireNonNull(name));
    }

    @NonNull
    Map<String, String> loadAll() throws IOException;

    @NonNull
    default PropertyLoader memoize() {
        return new PropertyLoaders.MemoizingPropertyLoader(this);
    }

    @NonNull
    default PropertyLoader memoizeWithExpiration(long duration, @NonNull TimeUnit unit, @NonNull LongSupplier clock) {
        return new PropertyLoaders.ExpiringMemoizingPropertyLoader(this, unit.toNanos(duration), clock);
    }
}
