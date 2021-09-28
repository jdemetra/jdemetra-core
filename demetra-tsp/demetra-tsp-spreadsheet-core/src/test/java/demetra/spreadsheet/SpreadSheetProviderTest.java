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
package demetra.spreadsheet;

import _test.SpreadSheetSamples;
import demetra.bridge.ToFileLoader;
import demetra.timeseries.TsData;
import demetra.timeseries.TsInformationType;
import demetra.timeseries.TsMoniker;
import demetra.timeseries.TsPeriod;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.tck.FileLoaderAssert;
import internal.spreadsheet.SpreadSheetSupport;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class SpreadSheetProviderTest {

    @Test
    public void testEquivalence() throws IOException {
        try (SpreadSheetProvider p = SpreadSheetSamples.TOP5.getProvider3()) {
            FileLoaderAssert
                    .assertThat(ToFileLoader.toFileLoader(SpreadSheetSamples.TOP5.getProvider2()))
                    .isEquivalentTo(p, o -> o.encodeBean(SpreadSheetSamples.TOP5.getBean3(o)));
        }
    }

    @Test
    public void testTspCompliance() {
        FileLoaderAssert.assertCompliance(SpreadSheetSamples.TOP5::getProvider3, SpreadSheetSamples.TOP5::getBean3);
    }

    @Test
    public void testMonikerLegacy() {
        String legacy = "<<Insee.xlsx>><<'FRANCE Alim# et tabac$'>><<Industries alimentaires 001563038>>";

        DataSource source = DataSource.of("XCLPRVDR", "20111201", "file", "Insee.xlsx");

        DataSet expected = DataSet.builder(source, DataSet.Kind.SERIES)
                .parameter("seriesName", "Industries alimentaires 001563038")
                .parameter("sheetName", "FRANCE Alim. et tabac")
                .build();

        try (SpreadSheetProvider p = new SpreadSheetProvider()) {
            assertThat(p.toDataSet(TsMoniker.of("XCLPRVDR", legacy))).hasValue(expected);
        }
    }

    @Test
    public void testMonikerUri() {
        String uri = "demetra://tsprovider/XCLPRVDR/20111201/SERIES?aggregationType=Last&cleanMissing=false&datePattern=dd%2FMM%2Fyyyy&file=Insee.xlsx&frequency=Monthly&locale=fr&numberPattern=%23.%23#seriesName=Textiles+001563047&sheetName=FRANCE+Textile";

        DataSource source = DataSource.builder("XCLPRVDR", "20111201")
                .parameter("aggregationType", "Last")
                .parameter("cleanMissing", "false")
                .parameter("datePattern", "dd/MM/yyyy")
                .parameter("locale", "fr")
                .parameter("numberPattern", "#.#")
                .parameter("file", "Insee.xlsx")
                .parameter("frequency", "Monthly")
                .build();

        DataSet expected = DataSet.builder(source, DataSet.Kind.SERIES)
                .parameter("seriesName", "Textiles 001563047")
                .parameter("sheetName", "FRANCE Textile")
                .build();

        try (SpreadSheetProvider p = new SpreadSheetProvider()) {
            assertThat(p.toDataSet(TsMoniker.of("XCLPRVDR", uri))).hasValue(expected);
        }
    }

    @Test
    public void testSample() throws IOException {
        try (SpreadSheetProvider p = SpreadSheetSamples.TOP5.getProvider3()) {
            SpreadSheetBean bean = SpreadSheetSamples.TOP5.getBean3(p);

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

            assertThat(p.getTsCollection(p.toMoniker(node), TsInformationType.All)).hasSize(6);

            DataSet leaf = p.children(node).get(2);
            assertThat(p.getDisplayName(leaf)).isEqualTo("Top 5 Browsers - Monthly\nChrome");
            assertThat(p.getDisplayNodeName(leaf)).isEqualTo("Chrome");

            assertThat(p.getTs(p.toMoniker(leaf), TsInformationType.All))
                    .satisfies(o -> {
                        assertThat(o.getMoniker()).isEqualTo(p.toMoniker(leaf));
                        assertThat(o.getName()).isEqualTo("Top 5 Browsers - Monthly\nChrome");
                        assertThat(o.getMeta())
                                .containsEntry(SpreadSheetSupport.SHEET_GRID_LAYOUT_META, "VERTICAL")
                                .containsEntry(SpreadSheetSupport.SERIES_NAME_META, "Chrome")
                                .containsEntry(SpreadSheetSupport.SHEET_NAME_META, "Top 5 Browsers - Monthly")
                                .hasSize(3);
                        assertThat(o.getData()).isEqualTo(TsData.ofInternal(TsPeriod.monthly(2008, 7), new double[]{0.0, 0.0, 1.03, 1.02, 0.93, 1.21, 1.38, 1.52, 1.73, 2.07, 2.42, 2.82, 3.01, 3.38, 3.69, 4.17, 4.66, 5.45, 6.04, 6.72, 7.29, 8.06, 8.61, 9.24, 9.88, 10.76, 11.54, 12.39, 13.35, 14.85, 15.68, 16.54, 17.37, 18.29, 19.36, 20.65, 22.14, 23.16, 23.61, 25.0, 25.65}));
                        assertThat(o.getType()).isEqualTo(TsInformationType.All);
                    });

            assertThat(p.close(dataSource)).isTrue();
            assertThat(p.getDataSources()).isEmpty();
        }
    }
}
