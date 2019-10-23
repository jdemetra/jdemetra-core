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
package internal.sql;

import demetra.sql.HasSqlProperties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import nbbrd.sql.jdbc.SqlConnectionSupplier;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public final class SqlPropertiesSupport implements HasSqlProperties {

    private final Supplier<SqlConnectionSupplier> defaultSupplier;
    private final AtomicReference<SqlConnectionSupplier> supplier;
    private final Runnable onSupplierChange;

    @Override
    public SqlConnectionSupplier getConnectionSupplier() {
        return supplier.get();
    }

    @Override
    public void setConnectionSupplier(SqlConnectionSupplier connectionSupplier) {
        SqlConnectionSupplier old = this.supplier.get();
        if (this.supplier.compareAndSet(old, connectionSupplier != null ? connectionSupplier : defaultSupplier.get())) {
            onSupplierChange.run();
        }
    }
}
