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
package ec.tss.tsproviders.common.uscb;

import demetra.bridge.ToDataSourceProvider;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.tck.DataSourceProviderAssert;
import org.junit.Test;

import java.util.Optional;

/**
 * @author Philippe Charles
 */
public class UscbProviderTest {

    @Test
    public void testCompliance() {
        DataSourceProviderAssert.assertCompliance(() -> ToDataSourceProvider.toDataSourceProvider(new UscbProvider()), new DataSourceProviderAssert.Sampler<ToDataSourceProvider>() {
            @Override
            public Optional<DataSource> dataSource(ToDataSourceProvider p) {
                return Optional.empty();
            }

            @Override
            public Optional<DataSet> tsDataSet(ToDataSourceProvider p) {
                return Optional.empty();
            }

            @Override
            public Optional<DataSet> tsCollectionDataSet(ToDataSourceProvider p) {
                return Optional.empty();
            }
        });
    }
}
