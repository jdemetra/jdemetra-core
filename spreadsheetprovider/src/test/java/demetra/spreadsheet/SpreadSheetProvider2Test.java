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

import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tss.TsInformationType;
import ec.tss.TsMoniker;
import static ec.tss.tsproviders.Assertions.assertThat;
import ec.tss.tsproviders.DataSet;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.IFileLoaderAssert;
import ec.tss.tsproviders.spreadsheet.SpreadSheetBean;
import ec.tss.tsproviders.spreadsheet.SpreadSheetProvider;
import ec.tss.tsproviders.spreadsheet.SpreadSheetProviderTest;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.io.IOException;
import java.net.URL;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class SpreadSheetProvider2Test {

    private static final URL SAMPLE = SpreadSheetProviderTest.class.getResource("/Top5Browsers.xlsx");

    @Test
    public void testEquivalence() throws IOException {
        assertThat(new SpreadSheetProvider2())
                .isEquivalentTo(new SpreadSheetProvider(), this::getSampleDataSource);
    }

    @Test
    public void testTspCompliance() {
        IFileLoaderAssert.assertCompliance(SpreadSheetProvider2::new, this::getSampleBean);
    }

    @Test
    public void testMonikerLegacy() {
        String legacy = "<<Insee.xlsx>><<'FRANCE Alim# et tabac$'>><<Industries alimentaires 001563038>>";

        DataSource source = DataSource.of("XCLPRVDR", "20111201", "file", "Insee.xlsx");

        DataSet expected = DataSet.builder(source, DataSet.Kind.SERIES)
                .put("seriesName", "Industries alimentaires 001563038")
                .put("sheetName", "FRANCE Alim. et tabac")
                .build();

        try (SpreadSheetProvider2 p = new SpreadSheetProvider2()) {
            assertThat(p.toDataSet(new TsMoniker("XCLPRVDR", legacy))).isEqualTo(expected);
        }
    }

    @Test
    public void testMonikerUri() {
        String uri = "demetra://tsprovider/XCLPRVDR/20111201/SERIES?aggregationType=Last&cleanMissing=false&datePattern=dd%2FMM%2Fyyyy&file=Insee.xlsx&frequency=Monthly&locale=fr&numberPattern=%23.%23#seriesName=Textiles+001563047&sheetName=FRANCE+Textile";

        DataSource source = DataSource.builder("XCLPRVDR", "20111201")
                .put("aggregationType", "Last")
                .put("cleanMissing", "false")
                .put("datePattern", "dd/MM/yyyy")
                .put("locale", "fr")
                .put("numberPattern", "#.#")
                .put("file", "Insee.xlsx")
                .put("frequency", "Monthly")
                .build();

        DataSet expected = DataSet.builder(source, DataSet.Kind.SERIES)
                .put("seriesName", "Textiles 001563047")
                .put("sheetName", "FRANCE Textile")
                .build();

        try (SpreadSheetProvider2 p = new SpreadSheetProvider2()) {
            assertThat(p.toDataSet(new TsMoniker("XCLPRVDR", uri))).isEqualTo(expected);
        }
    }

    @Test
    public void testSample() throws IOException {
        try (SpreadSheetProvider2 p = new SpreadSheetProvider2()) {
            SpreadSheetBean2 bean = p.newBean();
            bean.setFile(IFileLoaderAssert.urlAsFile(SAMPLE));

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
                        assertThat(o.moniker).isEqualTo(p.toMoniker(leaf));
                        assertThat(o.name).isEqualTo("Top 5 Browsers - Monthly\nChrome");
                        assertThat(o.metaData)
                                .containsEntry("series.aligntype", "VERTICAL")
                                .containsEntry("series.name", "Chrome")
                                .containsEntry("series.ordering", "2")
                                .containsEntry("sheet.name", "Top 5 Browsers - Monthly")
                                .hasSize(4);
                        assertThat(o.data).isEqualTo(new TsData(TsFrequency.Monthly, 2008, 6, new double[]{0.0, 0.0, 1.03, 1.02, 0.93, 1.21, 1.38, 1.52, 1.73, 2.07, 2.42, 2.82, 3.01, 3.38, 3.69, 4.17, 4.66, 5.45, 6.04, 6.72, 7.29, 8.06, 8.61, 9.24, 9.88, 10.76, 11.54, 12.39, 13.35, 14.85, 15.68, 16.54, 17.37, 18.29, 19.36, 20.65, 22.14, 23.16, 23.61, 25.0, 25.65}, false));
                        assertThat(o.type).isEqualTo(TsInformationType.All);
                        assertThat(o.invalidDataCause).isEqualTo(null);
                    });

            assertThat(p.close(dataSource)).isTrue();
            assertThat(p.getDataSources()).isEmpty();
        }
    }

    private SpreadSheetBean2 getSampleBean(SpreadSheetProvider2 o) {
        SpreadSheetBean2 bean = o.newBean();
        bean.setFile(IFileLoaderAssert.urlAsFile(SAMPLE));
        return bean;
    }

    private DataSource getSampleDataSource(SpreadSheetProvider o) {
        SpreadSheetBean bean = o.newBean();
        bean.setFile(IFileLoaderAssert.urlAsFile(SAMPLE));
        bean.setFrequency(TsFrequency.Quarterly);
        bean.setAggregationType(TsAggregationType.Average);
        bean.setCleanMissing(false);
        return o.encodeBean(bean);
    }
}
