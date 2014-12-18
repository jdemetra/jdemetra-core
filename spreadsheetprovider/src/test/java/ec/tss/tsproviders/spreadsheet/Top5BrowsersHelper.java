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

import com.google.common.io.Resources;
import ec.tss.tsproviders.spreadsheet.engine.SpreadSheetCollection;
import ec.tss.tsproviders.spreadsheet.engine.SpreadSheetSeries;
import ec.tss.tsproviders.spreadsheet.engine.SpreadSheetSource;
import ec.tss.tsproviders.spreadsheet.facade.Book;
import ec.tss.tsproviders.utils.DataFormat;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.io.IOException;
import static java.lang.Double.NaN;
import java.net.URL;
import org.junit.Assert;

/**
 *
 * @author Philippe Charles
 */
public final class Top5BrowsersHelper {

    private Top5BrowsersHelper() {
        // static class
    }

    public static URL getURL(String type) {
        return Resources.getResource("Top5Browsers." + type);
    }
    static final double DELTA = 0;

    public static void testContent(SpreadSheetSource top5Browsers) {
        Assert.assertNotNull(top5Browsers);
        Assert.assertEquals(3, top5Browsers.collections.size());
        testMonthlyVertical(top5Browsers);
        testQuarterlyHorizontal(top5Browsers);
        testDataTest(top5Browsers);
    }
    static final TsFrequency CHROME_FREQ = TsFrequency.Monthly;
    static final double[] CHROME_DATA = {0, 0, 1.03, 1.02, 0.93, 1.21, 1.38, 1.52, 1.73, 2.07, 2.42, 2.82, 3.01, 3.38, 3.69, 4.17, 4.66, 5.45, 6.04, 6.72, 7.29, 8.06, 8.61, 9.24, 9.88, 10.76, 11.54, 12.39, 13.35, 14.85, 15.68, 16.54, 17.37, 18.29, 19.36, 20.65, 22.14, 23.16, 23.61, 25, 25.65};
    static final TsPeriod CHROME_DOMAIN_FIRST = new TsPeriod(CHROME_FREQ, 2008, 6);
    static final TsPeriod CHROME_DOMAIN_LAST = new TsPeriod(CHROME_FREQ, 2011, 10);

    static void testMonthlyVertical(SpreadSheetSource top5Browsers) {
        SpreadSheetCollection wsMonthly = top5Browsers.collections.get("Top 5 Browsers - Monthly");
        Assert.assertEquals("Top 5 Browsers - Monthly", wsMonthly.sheetName);
        Assert.assertEquals(6, wsMonthly.series.size());

        for (SpreadSheetSeries o : wsMonthly.series) {
            Assert.assertEquals(CHROME_FREQ, o.data.get().getFrequency());
            Assert.assertEquals(CHROME_DOMAIN_FIRST, o.data.get().getStart());
            Assert.assertEquals(CHROME_DOMAIN_LAST, o.data.get().getLastPeriod());
        }

        SpreadSheetSeries chromeMonthly = wsMonthly.series.get(2);
        Assert.assertEquals("Chrome", chromeMonthly.seriesName);
        Assert.assertArrayEquals(CHROME_DATA, chromeMonthly.data.get().getValues().internalStorage(), DELTA);
    }
    static final TsFrequency SAFARI_FREQ = TsFrequency.Quarterly;
    static final double[] SAFARI_DATA = {3.05, 2.56, 2.63, 2.78, 3.19, 3.54, 4.03, 4.15, 4.24, 4.69, 5.06, 5.04, 5.31, 5.92};
    static final TsPeriod SAFARI_DOMAIN_FIRST = new TsPeriod(SAFARI_FREQ, 2008, 2);
    static final TsPeriod SAFARI_DOMAIN_LAST = new TsPeriod(SAFARI_FREQ, 2011, 3);

    static void testQuarterlyHorizontal(SpreadSheetSource top5Browsers) {
        SpreadSheetCollection wsQuarterly = top5Browsers.collections.get("Top 5 Browsers - Quarterly");
        Assert.assertEquals("Top 5 Browsers - Quarterly", wsQuarterly.sheetName);
        Assert.assertEquals(6, wsQuarterly.series.size());

        for (SpreadSheetSeries o : wsQuarterly.series) {
            Assert.assertEquals(SAFARI_FREQ, o.data.get().getFrequency());
            Assert.assertEquals(SAFARI_DOMAIN_FIRST, o.data.get().getStart());
            Assert.assertEquals(SAFARI_DOMAIN_LAST, o.data.get().getLastPeriod());
        }

        SpreadSheetSeries safariQuarterly = wsQuarterly.series.get(3);
        Assert.assertEquals("Safari", safariQuarterly.seriesName);
        Assert.assertArrayEquals(SAFARI_DATA, safariQuarterly.data.get().getValues().internalStorage(), DELTA);
    }
    static final TsFrequency DT_FREQ = TsFrequency.Monthly;
    static final double[][] DT_DATA = new double[][]{
        {67.16, 67.68, 68.14, 67.84, 65.41, 64.43, 62.52, 61.88},
        {26.14, 26.08, 25.77, 25.54, 25.27, 25.23, 27.03, 27.85},
        {0, 0, 1.03, NaN, NaN, NaN, NaN, 1.52, 1.73, 2.07},
        {2.99, 3, 2.91, 2.49, 2.41, 2.57, 2.59, 2.73, 2.75},
        {1.78, 1.83, 2.86, 2.69, 3.01, 2.83, 2.92, 2.95, 2.94}
    };
    static final TsPeriod DT_DOMAIN_FIRST = new TsPeriod(DT_FREQ, 2008, 6);
    static final TsPeriod DT_DOMAIN_LAST = new TsPeriod(DT_FREQ, 2009, 3);

    static void testDataTest(SpreadSheetSource top5Browsers) {
        SpreadSheetCollection ws = top5Browsers.collections.get("DataTest");
        Assert.assertEquals("DataTest", ws.sheetName);
        Assert.assertEquals(5, ws.series.size());

        for (int i = 0; i < ws.series.size(); i++) {
            Assert.assertEquals(DT_FREQ, ws.series.get(i).data.get().getFrequency());
            Assert.assertArrayEquals(DT_DATA[i], ws.series.get(i).data.get().getValues().internalStorage(), DELTA);
        }

        Assert.assertEquals(new TsPeriod(DT_FREQ, 2008, 8), ws.series.get(0).data.get().getStart());
        Assert.assertEquals(DT_DOMAIN_LAST, ws.series.get(0).data.get().getLastPeriod());
        Assert.assertEquals(DT_DOMAIN_FIRST, ws.series.get(1).data.get().getStart());
        Assert.assertEquals(new TsPeriod(DT_FREQ, 2009, 1), ws.series.get(1).data.get().getLastPeriod());
        Assert.assertEquals(DT_DOMAIN_FIRST, ws.series.get(2).data.get().getStart());
        Assert.assertEquals(DT_DOMAIN_LAST, ws.series.get(2).data.get().getLastPeriod());
        Assert.assertEquals(new TsPeriod(DT_FREQ, 2008, 7), ws.series.get(3).data.get().getStart());
        Assert.assertEquals(DT_DOMAIN_LAST, ws.series.get(3).data.get().getLastPeriod());
        Assert.assertEquals(DT_DOMAIN_FIRST, ws.series.get(4).data.get().getStart());
        Assert.assertEquals(new TsPeriod(DT_FREQ, 2009, 2), ws.series.get(4).data.get().getLastPeriod());
    }

    public static void testLoadStream(String type, Book.Factory bookFactory) throws IOException {
        SpreadSheetBean bean = new SpreadSheetBean();
        bean.setDataFormat(DataFormat.DEFAULT);
        try (Book book = bookFactory.load(getURL(type))) {
            testContent(SpreadSheetSource.load(book, bean.dataFormat, bean.frequency, bean.aggregationType, bean.cleanMissing));
        }
    }
}
