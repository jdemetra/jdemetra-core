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
package ec.tss.tsproviders.spreadsheet;

import _test.Top5Browsers;
import ec.tss.TsCollectionInformation;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class SpreadSheetProviderTest {

    @Test
    public void testCompliance(@TempDir Path temp) {
        IFileLoaderAssert.assertCompliance(SpreadSheetProvider::new, provider -> getSampleBean(provider, temp));
    }

    @Test
    public void testMonikerLegacy() {
        String legacy = "<<Insee.xlsx>><<'FRANCE Alim# et tabac$'>><<Industries alimentaires 001563038>>";

        SpreadSheetBean bean = new SpreadSheetBean();
        bean.setFile(new File("Insee.xlsx"));

        DataSet expected = DataSet.builder(bean.toDataSource("XCLPRVDR", "20111201"), DataSet.Kind.SERIES)
                .put("seriesName", "Industries alimentaires 001563038")
                .put("sheetName", "'FRANCE Alim# et tabac$'")
                .build();

        try (SpreadSheetProvider p = new SpreadSheetProvider()) {
            assertThat(p.toDataSet(new TsMoniker("XCLPRVDR", legacy))).isEqualTo(expected);
        }
    }

    @Test
    public void testMonikerUri() {
        String uri = "demetra://tsprovider/XCLPRVDR/20111201/SERIES?aggregationType=Last&cleanMissing=false&datePattern=dd%2FMM%2Fyyyy&file=Insee.xlsx&frequency=Monthly&locale=fr&numberPattern=%23.%23#seriesName=Textiles+001563047&sheetName=FRANCE+Textile";

        SpreadSheetBean bean = new SpreadSheetBean();
        bean.setAggregationType(TsAggregationType.Last);
        bean.setCleanMissing(false);
        bean.setDataFormat(DataFormat.of(Locale.FRENCH, "dd/MM/yyyy", "#.#"));
        bean.setFile(new File("Insee.xlsx"));
        bean.setFrequency(TsFrequency.Monthly);

        DataSet expected = DataSet.builder(bean.toDataSource("XCLPRVDR", "20111201"), DataSet.Kind.SERIES)
                .put("seriesName", "Textiles 001563047")
                .put("sheetName", "FRANCE Textile")
                .build();

        try (SpreadSheetProvider p = new SpreadSheetProvider()) {
            assertThat(p.toDataSet(new TsMoniker("XCLPRVDR", uri))).isEqualTo(expected);
        }
    }

    @Test
    public void testSample(@TempDir Path temp) throws IOException {
        try (SpreadSheetProvider p = new SpreadSheetProvider()) {
            SpreadSheetBean bean = getSampleBean(p, temp);
            DataSource dataSource = p.encodeBean(bean);
            assertThat(p.getDataSources()).isEmpty();
            assertThat(p.open(dataSource)).isTrue();

            assertThat(p.getDataSources()).containsExactly(dataSource);
            assertThat(p.getDisplayName(dataSource)).isEqualTo(bean.getFile().getPath());
            assertThat(p.children(dataSource)).hasSize(3);

            DataSet node = p.children(dataSource).get(0);
            assertThat(p.getDisplayName(node)).isEqualTo("Top 5 Browsers - Monthly");
            assertThat(p.getDisplayNodeName(node)).isEqualTo("Top 5 Browsers - Monthly");
            assertThat(p.children(node)).hasSize(6);

            assertThat(new TsCollectionInformation(p.toMoniker(node), TsInformationType.All))
                    .satisfies(o -> {
                        assertThat(p.get(o)).isTrue();
                        assertThat(o.items).hasSize(6);
                    });

            DataSet leaf = p.children(node).get(2);
            assertThat(p.getDisplayName(leaf)).isEqualTo("Top 5 Browsers - Monthly\nChrome");
            assertThat(p.getDisplayNodeName(leaf)).isEqualTo("Chrome");

            assertThat(new TsInformation("", p.toMoniker(leaf), TsInformationType.All))
                    .satisfies(o -> {
                        assertThat(p.get(o)).isTrue();
                        assertThat(o)
                                .extracting("moniker", "name", "metaData", "data", "type", "invalidDataCause")
                                .containsExactly(p.toMoniker(leaf), "Top 5 Browsers - Monthly\nChrome", null, new TsData(TsFrequency.Monthly, 2008, 6, VALUES, false), TsInformationType.All, null);
                    });

            assertThat(p.close(dataSource)).isTrue();
            assertThat(p.getDataSources()).isEmpty();
        }
    }

    private static final double[] VALUES = {0.0, 0.0, 1.03, 1.02, 0.93, 1.21, 1.38, 1.52, 1.73, 2.07, 2.42, 2.82, 3.01, 3.38, 3.69, 4.17, 4.66, 5.45, 6.04, 6.72, 7.29, 8.06, 8.61, 9.24, 9.88, 10.76, 11.54, 12.39, 13.35, 14.85, 15.68, 16.54, 17.37, 18.29, 19.36, 20.65, 22.14, 23.16, 23.61, 25.0, 25.65};

    private static SpreadSheetBean getSampleBean(SpreadSheetProvider p, Path temp) {
        SpreadSheetBean bean = p.newBean();
        bean.setFile(Top5Browsers.getRefFile(temp).toFile());
        return bean;
    }
}
