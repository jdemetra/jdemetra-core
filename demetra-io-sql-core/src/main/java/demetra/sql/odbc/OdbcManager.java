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
package demetra.sql.odbc;

import com.google.common.base.StandardSystemProperty;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import sql.util.SqlConnectionSupplier;

/**
 *
 * @author Philippe Charles
 */
@lombok.extern.java.Log
@lombok.AllArgsConstructor
final class OdbcManager {

    @Nonnull
    public static OdbcManager getDefault() {
        return new OdbcManager(is64bit(), OdbcManager::isDriverAvailable);
    }

    private final boolean amd64;
    private final Predicate<String> driverAvailable;

    @Nonnull
    public SqlConnectionSupplier getConnectionSupplier() {
        if (!amd64 && driverAvailable.test(JDBC_ODBC_DRIVER_NAME)) {
            log.info("Using Sun's odbc driver");
            return SqlConnectionSupplier.usingDriverManager(JDBC_ODBC_DRIVER_NAME, o -> JDBC_ODBC_DRIVER_PREFIX + o);
        }

        if (driverAvailable.test(ADO_DRIVER_NAME)) {
            log.info("Using ADO driver");
            return SqlConnectionSupplier.usingDriverManager(ADO_DRIVER_NAME, o -> ADO_DRIVER_PREFIX + o);
        }

        log.info("Using NoOp driver");
        return OdbcManager::noOpConnection;
    }

    private static final String JDBC_ODBC_DRIVER_NAME = "sun.jdbc.odbc.JdbcOdbcDriver";
    private static final String JDBC_ODBC_DRIVER_PREFIX = "jdbc:odbc:";
    private static final String ADO_DRIVER_NAME = "internal.sql.adodb.AdoDriver";
    private static final String ADO_DRIVER_PREFIX = "jdbc:adodb:";

    private static boolean isDriverAvailable(String className) {
        return isClassLoadable(Driver.class, className)
                && isDriverRegistered(className);
    }

    private static boolean is64bit() {
        return "amd64".equals(StandardSystemProperty.OS_ARCH.value());
    }

    private static boolean isClassLoadable(Class<?> type, String className) {
        try {
            return Driver.class.isAssignableFrom(Class.forName(className));
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    private static boolean isDriverRegistered(String className) {
        return Collections.list(DriverManager.getDrivers()).stream()
                .anyMatch(o -> className.equals(o.getClass().getName()));
    }

    private static Connection noOpConnection(String connectionString) throws SQLException {
        throw new SQLException("Cannot create connection for '" + connectionString + "'");
    }
}
