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
package ec.tss.tsproviders.jdbc.oracle;

import ec.tss.TsAsyncMode;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.db.DbAccessor;
import ec.tss.tsproviders.jdbc.ConnectionSupplier;
import ec.tss.tsproviders.jdbc.ConnectionSupplier.DriverBasedSupplier;
import ec.tss.tsproviders.jdbc.JdbcAccessor;
import ec.tss.tsproviders.jdbc.JdbcBean;
import ec.tss.tsproviders.jdbc.JdbcProvider;
import ec.tss.tsproviders.jdbc.dsm.datasource.DataSourceManager;
import ec.tss.tsproviders.jdbc.dsm.datasource.DataSourceType;
import ec.tss.tsproviders.jdbc.dsm.datasource.interfaces.IManagedDataSource;
import ec.tss.tsproviders.jdbc.dsm.identification.Account;
import ec.tss.tsproviders.jdbc.dsm.identification.AccountManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Demortier Jeremy
 */
@Deprecated
public class OracleProvider extends JdbcProvider<JdbcBean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OracleProvider.class);
    public static final String SOURCE = DataSourceType.ORACLE.getSourceQualifier();
    static final String VERSION = "20111201";
    // PROPERTIES
    private final DriverBasedSupplier connectionSupplier;

    public OracleProvider() {
        super(SOURCE, VERSION, LOGGER, TsAsyncMode.Once);
        this.connectionSupplier = new OracleSupplier();
    }

    @Override
    public boolean isAvailable() {
        return connectionSupplier.isDriverAvailable();
    }

    @Override
    public String getDisplayName() {
        return "Oracle databases";
    }

    @Override
    protected DbAccessor<JdbcBean> loadFromBean(JdbcBean bean) throws Exception {
        return new JdbcAccessor(logger, bean, connectionSupplier).memoize();
    }

    @Override
    public JdbcBean newBean() {
        return new JdbcBean();
    }

    @Override
    public JdbcBean decodeBean(DataSource dataSource) {
        return new JdbcBean(dataSource);
    }

    static class OracleSupplier extends ConnectionSupplier.DriverBasedSupplier {

        @Override
        protected String getUrl(JdbcBean bean) {
            try {
                Account acc = AccountManager.INSTANCE.getAccount(DataSourceType.ORACLE.getSourceQualifier(), bean.getDbName());
                IManagedDataSource mngDataSource = DataSourceManager.INSTANCE.getManagedDataSource(DataSourceType.ORACLE.getSourceQualifier(), bean.getDbName());
                if (acc != null && mngDataSource != null) {
                    return String.format("jdbc:oracle:thin:%s/%s@%s:%s:%s",
                            acc.getLogin(), acc.getPassword(),
                            mngDataSource.getProperty("Server"), mngDataSource.getProperty("Port"), mngDataSource.getProperty("SID"));
                }
                LOGGER.warn("Unable to retrieve either the account or the datasource information");
                return "";
            } catch (Exception ex) {
                LOGGER.warn("Unable to get connection string", ex.getMessage());
                return "";
            }
        }

        @Override
        protected boolean loadDriver() {
            try {
                Class.forName("oracle.jdbc.driver.OracleDriver");
                return true;
            } catch (ClassNotFoundException ex) {
                //LOGGER.error("Can't load MySql jdbc driver", ex);
                return false;
            }
        }
    }
}
