/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package ec.tss.tsproviders.jdbc;

import com.google.common.base.Preconditions;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A class that supplies opened connections to databases. The parameters needed
 * to establish a connection are provided by a JdbcBean.
 *
 * @author Philippe Charles
 * @see http://docs.oracle.com/javase/tutorial/jdbc/basics/connecting.html
 */
public interface ConnectionSupplier {

    /**
     * Opens a connection to a database. The class that uses this connection
     * must close it after use.
     *
     * @return A new opened connection.
     * @throws SQLException
     */
    @NonNull
    Connection getConnection(@NonNull JdbcBean bean) throws SQLException;

    /**
     * A connection supplier that uses {@link DriverManager}.
     */
    public static abstract class DriverBasedSupplier implements ConnectionSupplier {

        final boolean driverAvailable = loadDriver();

        @Override
        public Connection getConnection(JdbcBean bean) throws SQLException {
            Preconditions.checkState(driverAvailable, "Driver not available");
            return DriverManager.getConnection(getUrl(bean));
        }

        public boolean isDriverAvailable() {
            return driverAvailable;
        }

        @NonNull
        abstract protected String getUrl(@NonNull JdbcBean bean);

        abstract protected boolean loadDriver();
    }

    /**
     * A connection supplier that uses {@link javax.sql.DataSource}.
     */
    public static abstract class DataSourceBasedSupplier implements ConnectionSupplier {

        @Override
        public Connection getConnection(JdbcBean bean) throws SQLException {
            return getDataSource(bean).getConnection();
        }

        abstract protected javax.sql.@NonNull DataSource getDataSource(@NonNull JdbcBean bean) throws SQLException;
    }
}
