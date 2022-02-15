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
package _demo;

import demetra.demo.ProviderDemo;
import demetra.sql.odbc.OdbcBean;
import demetra.sql.odbc.OdbcProvider;
import demetra.timeseries.TsDataTable;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.cube.TableAsCube;

import java.io.IOException;
import java.sql.DriverManager;

/**
 * @author Philippe Charles
 */
public class OdbcProviderDemo {

    public static void main(String[] args) throws IOException {
        // 1. create and configure the provider
        try (OdbcProvider provider = new OdbcProvider()) {
            provider.setConnectionSupplier(o -> DriverManager.getConnection("jdbc:hsqldb:res:mydb", "sa", ""));

            // 2. create and configure a bean
            OdbcBean bean = provider.newBean();
            bean.setDsn("mydb");
            bean.setTable("Table2");
            bean.setCube(TableAsCube
                    .builder()
                    .dimension("Sector")
                    .dimension("Region")
                    .timeDimension("Table2.Period")
                    .measure("Rate")
                    .build());

            // 3. create and open a DataSource from the bean
            DataSource dataSource = provider.encodeBean(bean);
            provider.open(dataSource);

            // 4. run demos
            ProviderDemo.printTree(provider, dataSource);
            ProviderDemo.printFirstSeries(provider, dataSource);
            ProviderDemo.printDataTable(provider, dataSource, TsDataTable.DistributionType.FIRST);

            // 5. close resources
            provider.close(dataSource);
        }
    }
}
