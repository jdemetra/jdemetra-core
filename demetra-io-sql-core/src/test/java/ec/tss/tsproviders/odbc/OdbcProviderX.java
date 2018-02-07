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
package ec.tss.tsproviders.odbc;

import ec.tss.tsproviders.jdbc.ConnectionSupplier;
import ec.tss.tsproviders.jdbc.JdbcBean;
import java.sql.Connection;
import java.sql.SQLException;
import util.sql.SqlConnectionSupplier;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class OdbcProviderX {

    public OdbcProvider create(SqlConnectionSupplier supplier) {
        return new OdbcProvider(new ConnectionSupplier.DriverBasedSupplier() {
            @Override
            public Connection getConnection(JdbcBean bean) throws SQLException {
                return supplier.getConnection(bean.getDbName());
            }

            @Override
            protected String getUrl(JdbcBean bean) {
                return "jdbc:odbc:" + bean.getDbName();
            }

            @Override
            protected boolean loadDriver() {
                return true;
            }
        });
    }
}
