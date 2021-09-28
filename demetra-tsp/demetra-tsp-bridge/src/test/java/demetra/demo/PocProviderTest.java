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
package demetra.demo;

import demetra.timeseries.TsCollection;
import demetra.timeseries.TsInformationType;
import demetra.timeseries.TsMoniker;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.DataSourceProvider;
import demetra.tsprovider.tck.DataSourceProviderAssert;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class PocProviderTest {

    @Test
    public void testTspCompliance() {
        DataSourceProviderAssert.Sampler<DataSourceProvider> sampler = new DataSourceProviderAssert.Sampler<DataSourceProvider>() {
            @Override
            public Optional<DataSource> dataSource(DataSourceProvider p) {
                return p.getDataSources().stream().findFirst();
            }

            @Override
            public Optional<DataSet> tsDataSet(DataSourceProvider p) {
                return dataSource(p).map(o -> {
                    try {
                        return p.children(o).get(0);
                    } catch (IllegalArgumentException | IOException ex) {
                        Logger.getLogger(PocProviderTest.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return null;
                });
            }

            @Override
            public Optional<DataSet> tsCollectionDataSet(DataSourceProvider p) {
                return dataSource(p).map(o -> {
                    try {
                        return p.children(o).get(0);
                    } catch (IllegalArgumentException | IOException ex) {
                        Logger.getLogger(PocProviderTest.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return null;
                });
            }
        };
        DataSourceProviderAssert.assertCompliance(() -> new PocProvider(), sampler);
    }

    @Test
    public void testSample() throws IOException {
        try (PocProvider p = new PocProvider()) {
            DataSource normalSource = DataSource.of(PocProvider.NAME, "");

            assertThat(p.getDataSources())
                    .hasSize(6)
                    .element(0)
                    .isEqualTo(normalSource);

            assertThat(p.getDisplayName()).isEqualTo("Proof-of-concept");
            assertThat(p.getSource()).isEqualTo("poc");

            TsMoniker normalMoniker = p.toMoniker(normalSource);

            assertThat(p.getTsCollection(normalMoniker, TsInformationType.All)).satisfies(o -> {
                assertThat(((TsCollection) o).getMoniker().getSource()).isEqualTo(PocProvider.NAME);
                assertThat(((TsCollection) o)).hasSize(25);
            });

        }
    }
}
