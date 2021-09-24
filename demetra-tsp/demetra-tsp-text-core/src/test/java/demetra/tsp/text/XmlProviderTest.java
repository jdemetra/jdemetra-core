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
package demetra.tsp.text;

import _test.XmlSamples;
import demetra.bridge.FromFileLoader;
import demetra.timeseries.TsCollection;
import demetra.timeseries.TsMoniker;
import demetra.timeseries.TsPeriod;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.tck.FileLoaderAssert;
import ec.tss.tsproviders.IFileLoaderAssert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class XmlProviderTest {

    @Test
    public void testEquivalence() throws IOException {
        try (XmlProvider p = XmlSamples.INSEE1.getProvider3()) {
            IFileLoaderAssert
                    .assertThat(FromFileLoader.fromFileLoader(p))
                    .isEquivalentTo(XmlSamples.INSEE1.getProvider2(), o -> o.encodeBean(XmlSamples.INSEE1.getBean2(o)));
        }
    }

    @Test
    public void testTspCompliance() {
        FileLoaderAssert.assertCompliance(XmlSamples.INSEE1::getProvider3, XmlSamples.INSEE1::getBean3);
    }

    //    @Test
    public void testMonikerLegacy() {
        String legacy = "Insee@0@0";

        XmlBean bean = new XmlBean();
        bean.setFile(new File("Insee.xml"));

        DataSource source = DataSource.of("xml", "20111201", "file", "Insee.xml");

        DataSet expected = DataSet.builder(source, DataSet.Kind.SERIES)
                .parameter("collectionIndex", "0")
                .parameter("seriesIndex", "0")
                .build();

        try (XmlProvider p = new XmlProvider()) {
            assertThat(p.toDataSet(TsMoniker.of("Xml", legacy))).hasValue(expected);
        }
    }

    @Test
    public void testMonikerUri() {
        String uri = "demetra://tsprovider/Xml/20111201/SERIES?file=Insee.xml#collectionIndex=0&seriesIndex=0";

        ec.tss.tsproviders.common.xml.XmlBean bean = new ec.tss.tsproviders.common.xml.XmlBean();
        bean.setFile(new File("Insee.xml"));

        DataSource source = DataSource.builder("Xml", "20111201")
                .parameter("file", "Insee.xml")
                .build();

        DataSet expected = DataSet.builder(source, DataSet.Kind.SERIES)
                .parameter("collectionIndex", "0")
                .parameter("seriesIndex", "0")
                .build();

        try (XmlProvider p = new XmlProvider()) {
            assertThat(p.toDataSet(TsMoniker.of("Xml", uri))).hasValue(expected);
        }
    }

    @Test
    public void testSample() throws IOException {
        try (XmlProvider p = XmlSamples.INSEE1.getProvider3()) {
            assertThat(p.getDataSources()).isEmpty();

            XmlBean bean = XmlSamples.INSEE1.getBean3(p);
            DataSource dataSource = p.encodeBean(bean);
            assertThat(p.open(dataSource)).isTrue();
            assertThat(p.getDataSources()).containsExactly(dataSource);
            assertThat(p.getDisplayName(dataSource)).isEqualTo(bean.getFile().getPath());

            List<DataSet> nodes = p.children(dataSource);
            assertThat(nodes)
                    .hasSize(2)
                    .allSatisfy(o -> {
                        assertThat(o.getKind()).isEqualTo(DataSet.Kind.COLLECTION);
                        assertThat(o.getDataSource()).isEqualTo(dataSource);
                        assertThat(o.getParameters()).containsOnlyKeys("collectionIndex");
                    });

            DataSet node0 = nodes.get(0);
            assertThat(p.getDisplayName(node0)).isEqualTo("S1");
            assertThat(p.getDisplayNodeName(node0)).isEqualTo("S1");
            assertThat(p.getTsCollection(p.toMoniker(node0), demetra.timeseries.TsInformationType.All))
                    .satisfies(o -> {
                        TsCollection col = (TsCollection) o;
                        assertThat(col.getMoniker()).isEqualTo(p.toMoniker(node0));
//                        assertThat(col.getName()).isEqualTo("S1");
                        assertThat(col.getMeta()).isEmpty();
                        assertThat(col.getItems()).hasSize(2);
                    });

            List<DataSet> leaves = p.children(node0);
            assertThat(leaves)
                    .hasSize(2)
                    .allSatisfy(o -> {
                        assertThat(o.getKind()).isEqualTo(DataSet.Kind.SERIES);
                        assertThat(o.getDataSource()).isEqualTo(dataSource);
                        assertThat(o.getParameters()).containsOnlyKeys("collectionIndex", "seriesIndex");
                    });

            DataSet leaf1 = leaves.get(1);
            assertThat(p.getDisplayName(leaf1)).isEqualTo("S1 - 000854655");
            assertThat(p.getDisplayNodeName(leaf1)).isEqualTo("000854655");
            assertThat(p.getTs(p.toMoniker(leaf1), demetra.timeseries.TsInformationType.All))
                    .satisfies(o -> {
                        assertThat(o.getMoniker()).isEqualTo(p.toMoniker(leaf1));
                        assertThat(o.getName()).isEqualTo("S1 - 000854655");
                        assertThat(o.getMeta()).isEmpty();
                        assertThat(o.getData()).isEqualTo(demetra.timeseries.TsData.ofInternal(TsPeriod.monthly(1990, 1), VALUES));
                        assertThat(o.getType()).isEqualTo(demetra.timeseries.TsInformationType.All);
                    });

            assertThat(p.close(dataSource)).isTrue();
            assertThat(p.getDataSources()).isEmpty();
        }
    }

    static final double[] VALUES = {82.7, 79.4, 85.4, 78.9, 78.9, 80.5, 79.6, 56.9, 79.1, 84.5, 78.5, 65.2, 81.1, 76.3, 82.3, 78.2, 74.7, 81, 81.9, 55.1, 81.3, 84.2, 76, 73, 84.4, 82.9, 86.3, 83, 77.8, 86.7, 81.4, 57.2, 82.4, 82.8, 76, 70.9, 77.2, 78, 84.9, 81.3, 77.5, 83.7, 79.2, 57.1, 79.3, 78.7, 77.2, 73.3, 82.8, 81.3, 89.1, 83.6, 85.3, 87.5, 83.4, 64.6, 87.9, 86.2, 85.6, 81, 91.5, 85.9, 94.6, 85.2, 88.1, 91.5, 87, 64.4, 83.9, 83, 79.6, 70.5, 87.9, 85.8, 89.6, 87, 85.8, 90.4, 91.5, 64.9, 86.4, 89.7, 81.7, 77.6, 90.4, 88.4, 94.1, 97.3, 88.8, 94.4, 96.7, 69.4, 95, 99.5, 86, 88.4, 95.9, 92.6, 100.5, 95.9, 92.6, 99.6, 100.1, 70.2, 96.4, 94.6, 90.9, 86.4, 92.8, 88.8, 103.6, 97.5, 94.9, 104.5, 100.1, 74.1, 100.1, 100.1, 97.3, 91, 100.1, 101.1, 110.2, 99.5, 105.4, 103.7, 102.7, 80, 101.9, 103.6, 100.8, 91, 104.6, 101.6, 108, 96.8, 101.4, 105.6, 105.2, 78.9, 98, 105.1, 96.6, 80.1, 99.1, 97.7, 106.1, 101.8, 99.3, 102.6, 107, 76.6, 98.9, 100.3, 92.9, 86.6, 102, 96.9, 106.4, 104.2, 95.3, 97.8, 101.4, 72.7, 101.2, 102.3, 94.3, 87.7, 99.4, 98.4, 108.1, 101, 99.4, 109.4, 106.2, 75.9, 104.2, 104.4, 97.3, 89.5, 103.6, 100.5, 108.8, 104.9, 104, 107.3, 101.4, 79.1, 105.4, 100.5, 100.4, 90, 103.5, 96.7, 110.1, 98.4, 105.1, 108.9, 99.8, 80.3, 100.8, 99.9, 98.1, 87.2, 104.5, 100.7, 110.8, 102.6, 104.1, 107.3, 107.5, 80.4, 101.4, 109.6, 99.5, 86.8, 108, 104.7, 104.2, 109.3, 100.1, 101.3, 105, 72.9};
}
