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

import demetra.bridge.FromDataSourceProvider;
import demetra.timeseries.TsInformationType;
import demetra.tsprovider.DataSource;
import demetra.timeseries.TsMoniker;
import ec.tss.tsproviders.IDataSourceProviderAssert;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class PocProviderTest {

    @Test
    public void testTspCompliance() {
        IDataSourceProviderAssert.Sampler<FromDataSourceProvider<PocProvider>> sampler = new IDataSourceProviderAssert.Sampler<FromDataSourceProvider<PocProvider>>() {
            @Override
            public Optional<ec.tss.tsproviders.DataSource> dataSource(FromDataSourceProvider<PocProvider> p) {
                return p.getDataSources().stream().findFirst();
            }

            @Override
            public Optional<ec.tss.tsproviders.DataSet> tsDataSet(FromDataSourceProvider<PocProvider> p) {
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
            public Optional<ec.tss.tsproviders.DataSet> tsCollectionDataSet(FromDataSourceProvider<PocProvider> p) {
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
        IDataSourceProviderAssert.assertCompliance(() -> new FromDataSourceProvider(new PocProvider()), sampler);
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
                assertThat(o.getMoniker().getSource()).isEqualTo(PocProvider.NAME);
                assertThat(o.getData()).hasSize(25);
            });

        }
    }
}
