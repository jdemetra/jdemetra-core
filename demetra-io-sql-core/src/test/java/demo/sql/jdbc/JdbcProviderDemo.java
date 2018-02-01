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
package demo.sql.jdbc;

import demetra.demo.ProviderDemo;
import demetra.sql.jdbc.JdbcBean;
import demetra.sql.jdbc.JdbcProvider;
import demetra.timeseries.TsDataTable;
import demetra.tsprovider.DataSource;
import java.io.IOException;
import java.sql.DriverManager;
import java.util.Arrays;

/**
 *
 * @author Philippe Charles
 */
public class JdbcProviderDemo {

    public static void main(String[] args) throws IOException {
        // 1. create and configure the provider
        try (JdbcProvider provider = new JdbcProvider()) {
            provider.setConnectionSupplier(o -> DriverManager.getConnection("jdbc:hsqldb:res:mydb", "sa", ""));

            // 2. create and configure a bean
            JdbcBean bean = provider.newBean();
            bean.setDatabase("mydb");
            bean.setTable("Table2");
            bean.setDimColumns(Arrays.asList("Sector", "Region"));
            bean.setPeriodColumn("Table2.Period");
            bean.setValueColumn("Rate");

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
