/*
 * Copyright 2017 National Bank of Belgium
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
import demetra.spreadsheet.SpreadSheetBean;
import demetra.spreadsheet.SpreadSheetProvider;
import demetra.timeseries.TsDataTable;
import demetra.tsprovider.DataSource;
import demetra.design.Demo;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Philippe Charles
 */
public class SpreadSheetProviderDemo {

    @Demo
    public static void main(String[] args) throws IOException {
        File file = ProviderDemo.resourceAsFile(SpreadSheetProviderDemo.class, "/Top5Browsers.xlsx");

        // 1. create and configure the provider
        try (SpreadSheetProvider provider = new SpreadSheetProvider()) {

            // 2. create and configure a bean
            SpreadSheetBean bean = provider.newBean();
            bean.setFile(file);

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
