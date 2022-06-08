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
package ec.tss.tsproviders.common.random;

import demetra.bridge.ToDataSourceLoader;
import demetra.tsprovider.HasDataSourceBean;
import demetra.tsprovider.tck.DataSourceLoaderAssert;
import ec.tss.*;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.TsProviders;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Jean Palate
 */
public class RandomProviderTest {

    public RandomProviderTest() {
        TsFactory.instance.add(new RandomProvider());
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testCollection() {
        RandomBean bean = new RandomBean();
        DataSource dataSource = bean.toDataSource(RandomProvider.SOURCE, RandomProvider.VERSION);
        TsMoniker moniker = TsProviders.lookup(RandomProvider.class, dataSource).get().toMoniker(dataSource);
        TsCollection collection = TsFactory.instance.createTsCollection("test", moniker, TsInformationType.All);
        for (Ts s : collection) {
            assertTrue(s.hasData() == TsStatus.Valid);
        }
    }

    @Test
    public void testCompliance() {
        DataSourceLoaderAssert.assertCompliance(() -> ToDataSourceLoader.toDataSourceLoader(new RandomProvider()), HasDataSourceBean::newBean);
    }
}
