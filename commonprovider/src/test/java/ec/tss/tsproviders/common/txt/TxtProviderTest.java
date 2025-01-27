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
package ec.tss.tsproviders.common.txt;

import ec.tss.TsInformation;
import ec.tss.TsInformationType;
import ec.tss.TsMoniker;
import ec.tss.tsproviders.DataSet;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.IFileLoaderAssert;
import ec.tss.tsproviders.utils.DataFormat;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Locale;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class TxtProviderTest {

    @Test
    public void testCompliance() {
        IFileLoaderAssert.assertCompliance(TxtProvider::new, TxtProviderTest::getSampleBean);
    }

//    @Test
    public void testMonikerLegacy() {
        String legacy = "Insee1.txt@S854655";

        TxtBean bean = new TxtBean();
        bean.setFile(Paths.get("Insee1.txt").toFile());

        // FIXME: series index or name?
        DataSet expected = DataSet.builder(bean.toDataSource("Txt", "20111201"), DataSet.Kind.SERIES)
                .put("seriesIndex", 0)
                .build();

        try (TxtProvider p = new TxtProvider()) {
            assertThat(p.toDataSet(new TsMoniker("Txt", legacy))).isEqualTo(expected);
        }
    }

    @Test
    public void testMonikerUri() {
        String uri = "demetra://tsprovider/Txt/20111201/SERIES?aggregationType=Last&charset=UTF-16&cleanMissing=true&datePattern=dd%2FMM%2Fyyyy&delimiter=SEMICOLON&file=Insee1.txt&frequency=Monthly&headers=false&locale=fr&numberPattern=%23.%23&skipLines=3&textQualifier=QUOTE#seriesIndex=1";

        TxtBean bean = new TxtBean();
        bean.setCharset(StandardCharsets.UTF_16);
        bean.setCleanMissing(true);
        bean.setDataFormat(DataFormat.of(Locale.FRENCH, "dd/MM/yyyy", "#.#"));
        bean.setDelimiter(TxtBean.Delimiter.SEMICOLON);
        bean.setFile(Paths.get("Insee1.txt").toFile());
        bean.setFrequency(TsFrequency.Monthly);
        bean.setAggregationType(TsAggregationType.Last);
        bean.setHeaders(false);
        bean.setSkipLines(3);
        bean.setTextQualifier(TxtBean.TextQualifier.QUOTE);

        DataSet expected = DataSet.builder(bean.toDataSource("Txt", "20111201"), DataSet.Kind.SERIES)
                .put("seriesIndex", 1)
                .build();

        try (TxtProvider p = new TxtProvider()) {
            assertThat(p.toDataSet(new TsMoniker("Txt", uri))).isEqualTo(expected);
        }
    }

    @Test
    public void testSample() throws IOException {
        try (TxtProvider p = new TxtProvider()) {
            TxtBean bean = getSampleBean(p);
            DataSource dataSource = p.encodeBean(bean);
            assertThat(p.getDataSources()).isEmpty();
            assertThat(p.open(dataSource)).isTrue();

            assertThat(p.getDataSources()).containsExactly(dataSource);
            assertThat(p.getDisplayName(dataSource)).isEqualTo(bean.getFile().getPath());
            assertThat(p.children(dataSource)).hasSize(15);

            DataSet leaf = p.children(dataSource).get(1);
            assertThat(p.getDisplayName(leaf)).isEqualTo("S854655");
            assertThat(p.getDisplayNodeName(leaf)).isEqualTo("S854655");

            assertThat(new TsInformation("", p.toMoniker(leaf), TsInformationType.All))
                    .satisfies(o -> {
                        assertThat(p.get(o)).isTrue();
                        assertThat(o)
                                .extracting("moniker", "name", "metaData", "data", "type", "invalidDataCause")
                                .containsExactly(p.toMoniker(leaf), "S854655", null, new TsData(TsFrequency.Monthly, 1990, 0, VALUES, false), TsInformationType.All, null);
                    });

            assertThat(p.close(dataSource)).isTrue();
            assertThat(p.getDataSources()).isEmpty();
        }
    }

    private static final URL SAMPLE = TxtProviderTest.class.getResource("/Insee1.txt");
    static final double[] VALUES = {82.7, 79.4, 85.4, 78.9, 78.9, 80.5, 79.6, 56.9, 79.1, 84.5, 78.5, 65.2, 81.1, 76.3, 82.3, 78.2, 74.7, 81, 81.9, 55.1, 81.3, 84.2, 76, 73, 84.4, 82.9, 86.3, 83, 77.8, 86.7, 81.4, 57.2, 82.4, 82.8, 76, 70.9, 77.2, 78, 84.9, 81.3, 77.5, 83.7, 79.2, 57.1, 79.3, 78.7, 77.2, 73.3, 82.8, 81.3, 89.1, 83.6, 85.3, 87.5, 83.4, 64.6, 87.9, 86.2, 85.6, 81, 91.5, 85.9, 94.6, 85.2, 88.1, 91.5, 87, 64.4, 83.9, 83, 79.6, 70.5, 87.9, 85.8, 89.6, 87, 85.8, 90.4, 91.5, 64.9, 86.4, 89.7, 81.7, 77.6, 90.4, 88.4, 94.1, 97.3, 88.8, 94.4, 96.7, 69.4, 95, 99.5, 86, 88.4, 95.9, 92.6, 100.5, 95.9, 92.6, 99.6, 100.1, 70.2, 96.4, 94.6, 90.9, 86.4, 92.8, 88.8, 103.6, 97.5, 94.9, 104.5, 100.1, 74.1, 100.1, 100.1, 97.3, 91, 100.1, 101.1, 110.2, 99.5, 105.4, 103.7, 102.7, 80, 101.9, 103.6, 100.8, 91, 104.6, 101.6, 108, 96.8, 101.4, 105.6, 105.2, 78.9, 98, 105.1, 96.6, 80.1, 99.1, 97.7, 106.1, 101.8, 99.3, 102.6, 107, 76.6, 98.9, 100.3, 92.9, 86.6, 102, 96.9, 106.4, 104.2, 95.3, 97.8, 101.4, 72.7, 101.2, 102.3, 94.3, 87.7, 99.4, 98.4, 108.1, 101, 99.4, 109.4, 106.2, 75.9, 104.2, 104.4, 97.3, 89.5, 103.6, 100.5, 108.8, 104.9, 104, 107.3, 101.4, 79.1, 105.4, 100.5, 100.4, 90, 103.5, 96.7, 110.1, 98.4, 105.1, 108.9, 99.8, 80.3, 100.8, 99.9, 98.1, 87.2, 104.5, 100.7, 110.8, 102.6, 104.1, 107.3, 107.5, 80.4, 101.4, 109.6, 99.5, 86.8, 108, 104.7, 104.2, 109.3, 100.1, 101.3, 105, 72.9};

    private static TxtBean getSampleBean(TxtProvider p) {
        TxtBean result = p.newBean();
        result.setFile(IFileLoaderAssert.urlAsFile(SAMPLE));
        return result;
    }
}
