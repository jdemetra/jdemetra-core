/*
 * Copyright 2015 National Bank of Belgium
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
import java.sql.SQLException;
import java.util.function.Function;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
/**
 * A class that supplies opened connections to databases.
 *
 * @author Philippe Charles
 * @see http://docs.oracle.com/javase/tutorial/jdbc/basics/connecting.html
 */
public interface SqlConnectionSupplier {

    /**
     * Opens a connection to a database. The class that uses this connection
     * must close it after use.
     *
     * @param connectionString
     * @return A new opened connection.
     * @throws SQLException
     */
    @Nonnull
    Connection getConnection(@Nonnull String connectionString) throws SQLException;

    @Nonnull
    static SqlConnectionSupplier usingDriverManager(@Nonnull String driverClassName, @Nonnull Function<String, String> toUrl) {
        return new SqlConnectionSuppliers.DriverBasedSupplier(driverClassName, toUrl);
    }

    @Nonnull
    static SqlConnectionSupplier usingDataSource(@Nonnull SqlFunc<String, javax.sql.DataSource> toDataSource) {
        return new SqlConnectionSuppliers.DataSourceBasedSupplier(toDataSource);
    }

    @Nonnull
    static SqlConnectionSupplier usingJndi() {
        return SqlConnectionSuppliers.WITH_JNDI;
    }
}
