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
package ec.tss.tsproviders.utils;

import ec.tss.tsproviders.utils.OptionalTsData.BuilderCause;
import static ec.tss.tsproviders.utils.OptionalTsData.BuilderCause.INVALID_AGGREGATION;
import static ec.tss.tsproviders.utils.OptionalTsData.BuilderCause.DUPLICATION_WITHOUT_AGGREGATION;
import static ec.tss.tsproviders.utils.OptionalTsData.BuilderCause.GUESS_DUPLICATION;
import static ec.tss.tsproviders.utils.OptionalTsData.BuilderCause.GUESS_SINGLE;
import static ec.tss.tsproviders.utils.OptionalTsData.BuilderCause.NO_DATA;
import ec.tstoolkit.timeseries.TsAggregationType;
import static ec.tstoolkit.timeseries.TsAggregationType.None;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import static ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly;
import static ec.tstoolkit.timeseries.simplets.TsFrequency.Quarterly;
import static ec.tstoolkit.timeseries.simplets.TsFrequency.Undefined;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import static java.util.EnumSet.complementOf;
import java.util.GregorianCalendar;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class OptionalTsDataTest {

    private static final Date JAN2010 = new GregorianCalendar(2010, Calendar.JANUARY, 1).getTime();
    private static final Date FEB2010 = new GregorianCalendar(2010, Calendar.FEBRUARY, 1).getTime();
    private static final Date APR2010 = new GregorianCalendar(2010, Calendar.APRIL, 1).getTime();

    private static void assertCause(BuilderCause expected, OptionalTsData actual) {
        assertEquals(expected.getMessage(), actual.getCause());
    }

    private static void assertData(TsData expected, OptionalTsData actual) {
        assertEquals(expected, actual.get());
    }

    private static TsData data(TsFrequency freq, int firstyear, int firstperiod, double... data) {
        return new TsData(freq, firstyear, firstperiod, data, false);
    }

    @Test
    public void testPresent() {
        assertData(data(Monthly, 2010, 0, 10), OptionalTsData.present(1, 1, data(Monthly, 2010, 0, 10)));
        assertEquals(OptionalTsData.present(1, 1, data(Monthly, 2010, 0, 10)), OptionalTsData.present(1, 1, data(Monthly, 2010, 0, 10)));
        assertNotEquals(OptionalTsData.present(1, 1, data(Monthly, 2010, 0, 10)), OptionalTsData.present(1, 1, data(Monthly, 2010, 0, 10, 20)));
        assertNotEquals(OptionalTsData.present(1, 1, data(Monthly, 2010, 0, 10)), OptionalTsData.present(1, 1, data(Quarterly, 2010, 0, 10)));
    }

    @Test
    public void testAbsent() {
        assertEquals("Some reason", OptionalTsData.absent(1, 1, "Some reason").getCause());
        assertEquals(OptionalTsData.absent(1, 1, "Some reason"), OptionalTsData.absent(1, 1, "Some reason"));
        assertNotEquals(OptionalTsData.absent(1, 1, "Some reason"), OptionalTsData.absent(1, 1, "Other"));
    }

    @Test
    public void testBuilderPresent() {
        OptionalTsData.Builder b;

        for (TsFrequency freq : complementOf(EnumSet.of(Undefined))) {
            b = new OptionalTsData.Builder(freq, None).add(JAN2010, 10);
            assertData(data(freq, 2010, 0, 10), b.build());
            assertEquals(b.build(), b.build());
        }

        b = new OptionalTsData.Builder(Monthly, None).add(JAN2010, 10).add(APR2010, 40);
        assertData(data(Monthly, 2010, 0, 10, Double.NaN, Double.NaN, 40), b.build());
        assertEquals(b.build(), b.build());

        b = new OptionalTsData.Builder(Quarterly, None).add(JAN2010, 10).add(APR2010, 40);
        assertData(data(Quarterly, 2010, 0, 10, 40), b.build());
        assertEquals(b.build(), b.build());

        b = new OptionalTsData.Builder(Undefined, None).add(JAN2010, 10).add(FEB2010, 20);
        assertData(data(Monthly, 2010, 0, 10, 20), b.build());
        assertEquals(b.build(), b.build());

        b = new OptionalTsData.Builder(Undefined, None).add(JAN2010, 10).add(APR2010, 40);
        assertData(data(Quarterly, 2010, 0, 10, 40), b.build());
        assertEquals(b.build(), b.build());

        for (TsFrequency freq : complementOf(EnumSet.of(Undefined))) {
            b = new OptionalTsData.Builder(freq, TsAggregationType.Last).add(JAN2010, 10).add(JAN2010, 20);
            assertData(data(freq, 2010, 0, 20), b.build());
            assertEquals(b.build(), b.build());
        }
    }

    @Test
    public void testBuilderAbsent() {
        OptionalTsData.Builder b;

        for (TsFrequency freq : TsFrequency.values()) {
            b = new OptionalTsData.Builder(freq, None);
            assertCause(NO_DATA, b.build());
            assertEquals(b.build(), b.build());
        }

        for (TsAggregationType aggregation : complementOf(EnumSet.of(None))) {
            b = new OptionalTsData.Builder(Undefined, aggregation).add(JAN2010, 10);
            assertCause(INVALID_AGGREGATION, b.build());
            assertEquals(b.build(), b.build());
        }

        b = new OptionalTsData.Builder(Undefined, None).add(JAN2010, 10);
        assertCause(GUESS_SINGLE, b.build());
        assertEquals(b.build(), b.build());

        b = new OptionalTsData.Builder(Undefined, None).add(JAN2010, 10).add(JAN2010, 20);
        assertCause(GUESS_DUPLICATION, b.build());
        assertEquals(b.build(), b.build());

        for (TsFrequency freq : complementOf(EnumSet.of(Undefined))) {
            b = new OptionalTsData.Builder(freq, None).add(JAN2010, 10).add(JAN2010, 20);
            assertCause(DUPLICATION_WITHOUT_AGGREGATION, b.build());
            assertEquals(b.build(), b.build());
        }
    }

}
