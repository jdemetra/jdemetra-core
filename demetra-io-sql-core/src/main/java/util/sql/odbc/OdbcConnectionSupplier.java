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
package util.sql.odbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ServiceLoader;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import util.sql.SqlConnectionSupplier;

/**
 *
 * @author Philippe Charles
 */
@lombok.extern.java.Log
@lombok.AllArgsConstructor(staticName = "of")
public final class OdbcConnectionSupplier implements SqlConnectionSupplier {

    @Nonnull
    public static OdbcConnectionSupplier ofServiceLoader() {
        for (OdbcConnectionSupplierSpi o : ServiceLoader.load(OdbcConnectionSupplierSpi.class)) {
            if (o.isAvailable()) {
                return new OdbcConnectionSupplier(o);
            }
        }
        return noOp();
    }

    @Nonnull
    public static OdbcConnectionSupplier noOp() {
        return new OdbcConnectionSupplier(NoOp.INSTANCE);
    }

    @lombok.NonNull
    private final OdbcConnectionSupplierSpi spi;

    @Nonnull
    @SuppressWarnings("null")
    public String getName() {
        String result;

        try {
            result = spi.getName();
        } catch (RuntimeException ex) {
            log.log(Level.WARNING, "Unexpected exception while getting name for '" + spi.getClass().getName() + "'", ex);
            return spi.getClass().getName();
        }

        if (result == null) {
            log.log(Level.WARNING, "Unexpected null while getting name for ''{0}''", spi.getClass().getName());
            return spi.getClass().getName();
        }

        return result;
    }

    @Override
    public Connection getConnection(String connectionString) throws SQLException {
        Connection result;

        try {
            result = spi.getConnection(connectionString);
        } catch (RuntimeException ex) {
            throw new SQLException("Unexpected exception while getting connection for '" + connectionString + "'", ex);
        }

        if (result == null) {
            throw new SQLException("Unexpected null while getting connection for '" + connectionString + "'");
        }

        return result;
    }

    private enum NoOp implements OdbcConnectionSupplierSpi {
        INSTANCE;

        @Override
        public String getName() {
            return "noOp";
        }

        @Override
        public boolean isAvailable() {
            return true;
        }

        @Override
        public Connection getConnection(String connectionString) throws SQLException {
            return SqlConnectionSupplier.noOp().getConnection(connectionString);
        }
    }
}
