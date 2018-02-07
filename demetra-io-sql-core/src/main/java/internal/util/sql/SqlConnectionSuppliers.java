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
package internal.util.sql;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Objects;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import util.sql.SqlConnectionSupplier;
import util.sql.SqlFunc;

/**
 * Package-private supporting class for {@link SqlConnectionSupplier}.
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public final class SqlConnectionSuppliers {

    public boolean isDriverAvailable(String driverClassName) {
        return isDriverLoadable(driverClassName)
                && isDriverRegistered(driverClassName);
    }

    private static boolean isDriverLoadable(String driverClassName) {
        try {
            return Driver.class.isAssignableFrom(Class.forName(driverClassName));
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    private static boolean isDriverRegistered(String driverClassName) {
        return Collections
                .list(DriverManager.getDrivers())
                .stream()
                .map(o -> o.getClass().getName())
                .anyMatch(driverClassName::equals);
    }

    /**
     * A connection supplier that uses {@link DriverManager}.
     */
    @lombok.AllArgsConstructor
    public static final class DriverBasedSupplier implements SqlConnectionSupplier {

        @lombok.NonNull
        private final String driverClassName;

        @lombok.NonNull
        private final SqlFunc<String, String> toUrl;

        @Override
        public Connection getConnection(String connectionString) throws SQLException {
            Objects.requireNonNull(connectionString);
            if (!isDriverLoadable(driverClassName)) {
                throw new SQLException("Can't load jdbc driver '" + driverClassName + "'");
            }
            return toUrl.andThen(DriverManager::getConnection).applyWithSql(connectionString);
        }
    }

    /**
     * A connection supplier that uses {@link javax.sql.DataSource}.
     */
    @lombok.AllArgsConstructor
    public static final class DataSourceBasedSupplier implements SqlConnectionSupplier {

        @lombok.NonNull
        private final SqlFunc<String, javax.sql.DataSource> toDataSource;

        @Override
        public Connection getConnection(String connectionString) throws SQLException {
            Objects.requireNonNull(connectionString);
            return toDataSource.andThen(DataSource::getConnection).applyWithSql(connectionString);
        }
    }

    public enum CustomSuppliers implements SqlConnectionSupplier {

        JNDI {
            @Override
            public Connection getConnection(String connectionString) throws SQLException {
                Objects.requireNonNull(connectionString);
                return lookup(connectionString).getConnection();
            }

            private DataSource lookup(String connectionString) throws SQLException {
                try {
                    Context ctx = new InitialContext();
                    return (DataSource) ctx.lookup(connectionString);
                } catch (NamingException | ClassCastException ex) {
                    throw new SQLException("Cannot retrieve javax.sql.DataSource for '" + connectionString + "'", ex);
                }
            }
        },
        NO_OP {
            @Override
            public Connection getConnection(String connectionString) throws SQLException {
                Objects.requireNonNull(connectionString);
                throw new SQLException("No connection for '" + connectionString + "'");
            }
        };

    }
}
