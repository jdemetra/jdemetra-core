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
package ec.tss.tsproviders.common.txt;

import ec.tss.tsproviders.utils.DataFormat;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class TxtLoaderTest {

    //<editor-fold defaultstate="collapsed" desc="Shortcuts">
    static TxtSource loadResource(String name, TxtBean bean) throws IOException {
        try (InputStream stream = TxtLoaderTest.class.getResourceAsStream(name)) {
            return TxtLoader.load(stream, bean);
        }
    }
    //</editor-fold>

    @Test
    public void testDefaultBean() throws IOException {
        TxtBean bean = new TxtBean();

        TxtSource source = loadResource("/Insee1.txt", bean);

        assertEquals(15, source.items.size());
        assertEquals(225, source.readLines);
        assertEquals(0, source.invalidLines);

        TxtSeries series0 = source.items.get(0);
        assertEquals(0, series0.index);
        assertEquals("S854628", series0.name);

        TsData data0 = series0.data.get();
        assertEquals(224, data0.getLength());
        assertEquals(224, data0.getObsCount());
        assertEquals(new TsPeriod(TsFrequency.Monthly, 1990, 0), data0.getStart());
        assertEquals(new TsPeriod(TsFrequency.Monthly, 2008, 7), data0.getLastPeriod());
        assertEquals(89.8, data0.get(0), 0);

        TxtSeries series7 = source.items.get(7);
        assertEquals(7, series7.index);
        assertEquals("S854614", series7.name);

        TsData data7 = series7.data.get();
        assertEquals(224, data7.getLength());
        assertEquals(224, data7.getObsCount());
        assertEquals(new TsPeriod(TsFrequency.Monthly, 1990, 0), data7.getStart());
        assertEquals(new TsPeriod(TsFrequency.Monthly, 2008, 7), data7.getLastPeriod());
        assertEquals(97, data7.get(0), 0);
    }

    @Test
    public void testGermanCsvWithComments() throws IOException {
        TxtBean bean = new TxtBean();
        bean.setCharset(StandardCharsets.UTF_8);
        bean.setDataFormat(new DataFormat(Locale.GERMAN, "yyyy-MM", null));
        bean.setDelimiter(TxtBean.Delimiter.SEMICOLON);
        bean.setHeaders(false);
        bean.setSkipLines(5);

        TxtSource source = loadResource("/bbk_SU0503.csv", bean);

        assertEquals(3, source.items.size());
        assertEquals(81, source.readLines);
        assertEquals(1, source.invalidLines);

        TxtSeries series0 = source.items.get(0);
        assertEquals(0, series0.index);
        assertEquals("Column 1", series0.name);

        TsData data0 = series0.data.get();
        assertEquals(80, data0.getLength());
        assertEquals(80, data0.getObsCount());
        assertEquals(new TsPeriod(TsFrequency.Monthly, 1996, 10), data0.getStart());
        assertEquals(new TsPeriod(TsFrequency.Monthly, 2003, 5), data0.getLastPeriod());
        assertEquals(11.25, data0.get(13), 0);

        TsData data1 = source.items.get(1).data.get().cleanExtremities();
        assertEquals(0, data1.getLength());
        assertEquals(0, data1.getObsCount());
        assertEquals(new TsPeriod(TsFrequency.Monthly, 1996, 10), data1.getStart());
        // FIXME: is this the right behavior?
        assertEquals(new TsPeriod(TsFrequency.Monthly, 1996, 9), data1.getLastPeriod());
    }

    @Test
    public void testAggregation() throws IOException {
        TxtBean bean = new TxtBean();
        bean.setFrequency(TsFrequency.Yearly);
        bean.setAggregationType(TsAggregationType.First);

        TxtSource source = loadResource("/Insee1.txt", bean);

        assertEquals(15, source.items.size());
        assertEquals(225, source.readLines);
        assertEquals(0, source.invalidLines);

        TxtSeries series0 = source.items.get(0);
        assertEquals(0, series0.index);
        assertEquals("S854628", series0.name);

        // new behavior in aggregation only takes full years => 19->18 and 2008->2007
        TsData data0 = series0.data.get();
        assertEquals(18, data0.getLength());
        assertEquals(18, data0.getObsCount());
        assertEquals(new TsPeriod(TsFrequency.Yearly, 1990, 0), data0.getStart());
        assertEquals(new TsPeriod(TsFrequency.Yearly, 2007, 0), data0.getLastPeriod());
        assertEquals(89.8, data0.get(0), 0);
        assertEquals(86.4, data0.get(1), 0);

        TxtSeries series7 = source.items.get(7);
        assertEquals(7, series7.index);
        assertEquals("S854614", series7.name);

        TsData data7 = series7.data.get();
        assertEquals(18, data7.getLength());
        assertEquals(18, data7.getObsCount());
        assertEquals(new TsPeriod(TsFrequency.Yearly, 1990, 0), data7.getStart());
        assertEquals(new TsPeriod(TsFrequency.Yearly, 2007, 0), data7.getLastPeriod());
        assertEquals(97, data7.get(0), 0);
        assertEquals(96.4, data7.get(1), 0);
    }
}
