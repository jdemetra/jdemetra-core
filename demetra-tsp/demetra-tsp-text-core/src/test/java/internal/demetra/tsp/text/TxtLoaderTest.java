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
package internal.demetra.tsp.text;

import demetra.data.AggregationType;
import demetra.data.Data;
import demetra.timeseries.*;
import demetra.timeseries.util.ObsGathering;
import demetra.tsp.text.TxtBean;
import demetra.tsprovider.HasFilePaths;
import demetra.tsprovider.util.ObsFormat;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * @author Philippe Charles
 */
public class TxtLoaderTest {

    private final TxtLoader loader = new TxtLoader(HasFilePaths.of());

    private File getFile(String resource) throws URISyntaxException {
        URI uri = TxtLoaderTest.class.getResource(resource).toURI();
        return new File(uri);
     }

    @Test
    public void testDefaultBean() throws IOException, URISyntaxException {
        TxtBean bean = new TxtParam.V1().getDefaultValue();
        bean.setFile(getFile("/Insee1.txt"));

        TsCollection source = loader.load(bean);

        assertEquals(15, source.getItems().size());

        Ts series0 = source.get(0);
        assertEquals("S854628", series0.getName());

        TsData data0 = series0.getData();
        assertEquals(224, data0.length());
        assertEquals(TsPeriod.monthly(1990, 1), data0.getStart());
        assertEquals(TsPeriod.monthly(2008, 9), data0.getEnd());
        assertEquals(89.8, data0.getValues().get(0), 0);

        Ts series7 = source.get(7);
        assertEquals("S854614", series7.getName());

        TsData data7 = series7.getData();
        assertEquals(224, data7.size());
        assertEquals(TsPeriod.monthly(1990, 1), data7.getStart());
        assertEquals(TsPeriod.monthly(2008, 9), data7.getEnd());
        assertEquals(97, data7.getValue(0), 0);
    }

    @Test
    public void testGermanCsvWithComments() throws IOException, URISyntaxException {
        TxtBean bean = new TxtParam.V1().getDefaultValue();
        bean.setFile(getFile("/bbk_SU0503.csv"));
        bean.setCharset(StandardCharsets.UTF_8);
        bean.setObsFormat(ObsFormat.of(Locale.GERMAN, "yyyy-MM", null));
        bean.setDelimiter(TxtBean.Delimiter.SEMICOLON);
        bean.setHeaders(false);
        bean.setSkipLines(5);

        TsCollection source = loader.load(bean);

        assertEquals(1, source.size());

        Ts series0 = source.get(0);
        assertEquals("Column 1", series0.getName());

        TsData data0 = series0.getData();
        assertEquals(80, data0.size());
        assertEquals(TsPeriod.monthly(1996, 11), data0.getStart());
        assertEquals(TsPeriod.monthly(2003, 7), data0.getEnd());
        assertEquals(11.25, data0.getValue(13), 0);
    }

    @Test
    public void testAggregation() throws IOException, URISyntaxException {
        TxtBean bean = new TxtParam.V1().getDefaultValue();
        bean.setFile(getFile("/Insee1.txt"));
        bean.setObsGathering(ObsGathering.builder().unit(TsUnit.YEAR).aggregationType(AggregationType.First).build());

        TsCollection source = loader.load(bean);

        assertEquals(15, source.size());

        Ts series0 = source.get(0);
        assertEquals("S854628", series0.getName());

        // new behavior in aggregation only takes full years => 19->18 and 2008->2007
        TsData data0 = series0.getData();
        assertEquals(18, data0.size());
        assertEquals(TsPeriod.yearly(1990), data0.getStart());
        assertEquals(TsPeriod.yearly(2008), data0.getEnd());
        assertEquals(89.8, data0.getValue(0), 0);
        assertEquals(86.4, data0.getValue(1), 0);

        Ts series7 = source.get(7);
        assertEquals("S854614", series7.getName());

        TsData data7 = series7.getData();
        assertEquals(18, data7.size());
        assertEquals(TsPeriod.yearly(1990), data7.getStart());
        assertEquals(TsPeriod.yearly(2008), data7.getEnd());
        assertEquals(97, data7.getValues().get(0), 0);
        assertEquals(96.4, data7.getValues().get(1), 0);
    }
}
