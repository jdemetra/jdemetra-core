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
package sql.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Package-private supporting class for {@link SqlConnectionSupplier}.
 *
 * @author Philippe Charles
 */
@lombok.extern.java.Log
@lombok.experimental.UtilityClass
final class SqlConnectionSuppliers {

    /**
     * A connection supplier that uses {@link DriverManager}.
     */
    static final class DriverBasedSupplier implements SqlConnectionSupplier {

        private final boolean driverAvailable;
        private final Function<String, String> toUrl;

        DriverBasedSupplier(String driverClassName, Function<String, String> toUrl) {
            this.driverAvailable = loadDriver(driverClassName);
            this.toUrl = toUrl;
        }

        private static boolean loadDriver(String driverClassName) {
            try {
                Class.forName(driverClassName);
                return true;
            } catch (ClassNotFoundException ex) {
                log.log(Level.INFO, "Can't load jdbc driver '{}'", driverClassName);
                return false;
            }
        }

        @Override
        public Connection getConnection(String connectionString) throws SQLException {
            if (!driverAvailable) {
                throw new IllegalStateException("Driver not available");
            }
            return DriverManager.getConnection(toUrl.apply(connectionString));
        }
    }

    /**
     * A connection supplier that uses {@link javax.sql.DataSource}.
     */
    static final class DataSourceBasedSupplier implements SqlConnectionSupplier {

        private final SqlFunc<String, javax.sql.DataSource> toDataSource;

        DataSourceBasedSupplier(SqlFunc<String, DataSource> toDataSource) {
            this.toDataSource = toDataSource;
        }

        @Override
        public Connection getConnection(String connectionString) throws SQLException {
            DataSource dataSource = toDataSource.applyWithSql(connectionString);
            return Objects.requireNonNull(dataSource).getConnection();
        }
    }

    static final SqlConnectionSupplier WITH_JNDI = new DataSourceBasedSupplier(o -> {
        try {
            Context ctx = new InitialContext();
            return (javax.sql.DataSource) ctx.lookup(o);
        } catch (NamingException ex) {
            throw new SQLException("Cannot retrieve javax.sql.DataSource object", ex);
        }
    });
}
