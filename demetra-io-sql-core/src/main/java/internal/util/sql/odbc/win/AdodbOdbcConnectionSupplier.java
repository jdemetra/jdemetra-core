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
package internal.util.sql.odbc.win;

import demetra.design.DirectImpl;
import internal.util.sql.SqlConnectionSuppliers;
import java.sql.Connection;
import java.sql.SQLException;
import org.openide.util.lookup.ServiceProvider;
import util.sql.SqlConnectionSupplier;
import util.sql.odbc.OdbcConnectionSupplierSpi;

/**
 *
 * @author Philippe Charles
 */
@DirectImpl
@ServiceProvider(service = OdbcConnectionSupplierSpi.class)
public final class AdodbOdbcConnectionSupplier implements OdbcConnectionSupplierSpi {

    private static final String ADO_DRIVER_NAME = "internal.util.sql.adodb.AdoDriver";
    private static final String ADO_DRIVER_PREFIX = "jdbc:adodb:";

    private final SqlConnectionSupplier delegate = SqlConnectionSupplier.usingDriverManager(ADO_DRIVER_NAME, o -> ADO_DRIVER_PREFIX + o);

    @Override
    public String getName() {
        return ADO_DRIVER_NAME;
    }

    @Override
    public boolean isAvailable() {
        return SqlConnectionSuppliers.isDriverAvailable(ADO_DRIVER_NAME);
    }

    @Override
    public Connection getConnection(String connectionString) throws SQLException {
        return delegate.getConnection(connectionString);
    }
}
