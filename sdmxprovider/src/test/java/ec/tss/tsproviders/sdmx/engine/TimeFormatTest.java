/*
 * Copyright 2013 National Bank of Belgium
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
package ec.tss.tsproviders.sdmx.engine;

import ec.tss.tsproviders.utils.Parsers.Parser;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.Date;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class TimeFormatTest {

    private static final Date D_2000_01_01 = new TsPeriod(TsFrequency.Monthly, 2000, 0).firstday().getTime();
    private static final Date D_2000_02_01 = new TsPeriod(TsFrequency.Monthly, 2000, 1).firstday().getTime();
    private static final Date D_2000_07_01 = new TsPeriod(TsFrequency.Monthly, 2000, 6).firstday().getTime();
    private static final Date D_2000_09_01 = new TsPeriod(TsFrequency.Monthly, 2000, 8).firstday().getTime();
    private static final Date D_2000_10_01 = new TsPeriod(TsFrequency.Monthly, 2000, 9).firstday().getTime();

    @Test
    public void testP1Y() {
        Parser<Date> parser = TimeFormat.P1Y.getParser();
        assertEquals(D_2000_01_01, parser.parse("2000"));
        assertEquals(D_2000_01_01, parser.parse("2000-A1"));
//        assertNull(parser.parse("2000-A0"));
//        assertNull(parser.parse("2000-A2"));
        assertNull(parser.parse("hello"));
    }

    @Test
    public void testP6M() {
        Parser<Date> parser = TimeFormat.P6M.getParser();
        assertEquals(D_2000_01_01, parser.parse("2000-S1"));
        assertEquals(D_2000_07_01, parser.parse("2000-S2"));
        assertNull(parser.parse("2000-S0"));
        assertNull(parser.parse("2000-S3"));
        assertNull(parser.parse("hello"));
    }

    @Test
    public void testP4M() {
        Parser<Date> parser = TimeFormat.P4M.getParser();
        assertEquals(D_2000_01_01, parser.parse("2000-T1"));
        assertEquals(D_2000_09_01, parser.parse("2000-T3"));
        assertNull(parser.parse("2000-T0"));
        assertNull(parser.parse("2000-T4"));
        assertNull(parser.parse("hello"));
    }

    @Test
    public void testP3M() {
        Parser<Date> parser = TimeFormat.P3M.getParser();
        assertEquals(D_2000_01_01, parser.parse("2000-Q1"));
        assertEquals(D_2000_10_01, parser.parse("2000-Q4"));

        assertNull(parser.parse("hello"));
    }

    @Test
    public void testP1M() {
        Parser<Date> parser = TimeFormat.P1M.getParser();
        assertEquals(D_2000_01_01, parser.parse("2000-01"));
        assertEquals(D_2000_02_01, parser.parse("2000-02"));
        assertEquals(D_2000_01_01, parser.parse("2000-M1"));
        assertEquals(D_2000_02_01, parser.parse("2000-M2"));
        assertEquals(D_2000_01_01, parser.parse("2000M1"));
        assertEquals(D_2000_02_01, parser.parse("2000M2"));
        assertNull(parser.parse("2000-M0"));
        assertNull(parser.parse("2000-M13"));
        assertNull(parser.parse("hello"));
    }

    @Test
    public void testP7D() {
        Parser<Date> parser = TimeFormat.P7D.getParser();

        assertNull(parser.parse("hello"));
    }

    @Test
    public void testP1D() {
        Parser<Date> parser = TimeFormat.P1D.getParser();

        assertNull(parser.parse("hello"));
    }

    @Test
    public void testPT1M() {
        Parser<Date> parser = TimeFormat.PT1M.getParser();

        assertNull(parser.parse("hello"));
    }

}
