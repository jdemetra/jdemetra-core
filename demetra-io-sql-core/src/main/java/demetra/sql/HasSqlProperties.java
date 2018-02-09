/*
 * Copyright 2018 National Bank of Belgium
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
package demetra.sql;

import internal.sql.SqlPropertiesSupport;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import util.sql.SqlConnectionSupplier;

/**
 *
 * @author Philippe Charles
 */
public interface HasSqlProperties {

    @Nonnull
    SqlConnectionSupplier getConnectionSupplier();

    void setConnectionSupplier(@Nullable SqlConnectionSupplier connectionSupplier);

    @Nonnull
    static HasSqlProperties of(@Nonnull Supplier<SqlConnectionSupplier> defaultSupplier, @Nonnull Runnable onSupplierChange) {
        return new SqlPropertiesSupport(
                defaultSupplier,
                new AtomicReference<>(defaultSupplier.get()),
                onSupplierChange);
    }
}
