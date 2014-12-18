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
package ec.tss.tsproviders.utils;

import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class StrangeParsersTest {

    private static final Date D_2010_01_01 = new TsPeriod(TsFrequency.Monthly, 2010, 0).firstday().getTime();
    private static final Date D_2010_02_01 = new TsPeriod(TsFrequency.Monthly, 2010, 1).firstday().getTime();

    @Test
    public void testYearFreqPosParser() {
        Parsers.Parser<Date> parser = StrangeParsers.yearFreqPosParser();
        Assert.assertEquals(D_2010_01_01, parser.parse("2010M1"));
        Assert.assertEquals(D_2010_01_01, parser.parse("2010-M1"));
        Assert.assertEquals(D_2010_02_01, parser.parse("2010M2"));
        Assert.assertEquals(D_2010_02_01, parser.parse("2010-M2"));
        Assert.assertEquals(D_2010_01_01, parser.parse("2010Q1"));
        Assert.assertEquals(D_2010_01_01, parser.parse("2010-Q1"));
        Assert.assertNull(parser.parse("1234"));
    }

}
