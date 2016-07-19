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

import static ec.tss.tsproviders.utils.OptionalTsData.DUPLICATION_WITHOUT_AGGREGATION;
import static ec.tss.tsproviders.utils.OptionalTsData.GUESS_DUPLICATION;
import static ec.tss.tsproviders.utils.OptionalTsData.GUESS_SINGLE;
import static ec.tss.tsproviders.utils.OptionalTsData.INVALID_AGGREGATION;
import static ec.tss.tsproviders.utils.OptionalTsData.NO_DATA;
import static ec.tss.tsproviders.utils.OptionalTsData.absent;
import static ec.tss.tsproviders.utils.OptionalTsData.builder;
import static ec.tss.tsproviders.utils.OptionalTsData.present;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class OptionalTsDataTest {

    private static final Date JAN2010 = new GregorianCalendar(2010, Calendar.JANUARY, 1).getTime();
    private static final Date FEB2010 = new GregorianCalendar(2010, Calendar.FEBRUARY, 1).getTime();
    private static final Date APR2010 = new GregorianCalendar(2010, Calendar.APRIL, 1).getTime();

    private static void assertData(TsData expected, OptionalTsData actual) {
        assertEquals(expected, actual.get());
    }

    private static TsData data(TsFrequency freq, int firstyear, int firstperiod, double... data) {
        return new TsData(freq, firstyear, firstperiod, data, false);
    }

    @Test
    @SuppressWarnings("null")
    public void testPresent() {
        assertThat(present(data(Monthly, 2010, 0, 10)))
                .isEqualTo(present(data(Monthly, 2010, 0, 10)))
                .isNotEqualTo(present(data(Monthly, 2010, 0, 10, 20)))
                .isNotEqualTo(present(data(Quarterly, 2010, 0, 10, 10)))
                .extracting(OptionalTsData::isPresent, OptionalTsData::get)
                .containsExactly(true, data(Monthly, 2010, 0, 10));

        assertThatThrownBy(() -> present(null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> present(data(Monthly, 2010, 0, 10)).getCause())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testAbsent() {
        assertThat(absent("Some reason"))
                .isEqualTo(absent("Some reason"))
                .isNotEqualTo(absent("Other"))
                .extracting(OptionalTsData::isPresent, OptionalTsData::getCause)
                .containsExactly(false, "Some reason");

        assertThatThrownBy(() -> absent(null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> absent("Some reason").get())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void testBuilderPresent() {
        OptionalTsData.Builder2 b;

        for (TsFrequency freq : complementOf(EnumSet.of(Undefined))) {
            b = builder(freq, None, false).add(JAN2010, 10);
            assertData(data(freq, 2010, 0, 10), b.build());
            assertEquals(b.build(), b.build());
        }

        b = builder(Monthly, None, false).add(JAN2010, 10).add(APR2010, 40);
        assertData(data(Monthly, 2010, 0, 10, Double.NaN, Double.NaN, 40), b.build());
        assertEquals(b.build(), b.build());

        b = builder(Quarterly, None, false).add(JAN2010, 10).add(APR2010, 40);
        assertData(data(Quarterly, 2010, 0, 10, 40), b.build());
        assertEquals(b.build(), b.build());

        b = builder(Undefined, None, false).add(JAN2010, 10).add(FEB2010, 20);
        assertData(data(Monthly, 2010, 0, 10, 20), b.build());
        assertEquals(b.build(), b.build());

        b = builder(Undefined, None, false).add(JAN2010, 10).add(APR2010, 40);
        assertData(data(Quarterly, 2010, 0, 10, 40), b.build());
        assertEquals(b.build(), b.build());

        for (TsFrequency freq : complementOf(EnumSet.of(Undefined))) {
            b = builder(freq, TsAggregationType.Last, false).add(JAN2010, 10).add(JAN2010, 20);
            assertData(data(freq, 2010, 0, 20), b.build());
            assertEquals(b.build(), b.build());
        }
    }

    @Test
    public void testBuilderAbsent() {
        OptionalTsData.Builder2 b;

        for (TsFrequency freq : TsFrequency.values()) {
            b = builder(freq, None, false);
            assertEquals(NO_DATA, b.build());
            assertEquals(b.build(), b.build());
        }

        for (TsAggregationType aggregation : complementOf(EnumSet.of(None))) {
            b = builder(Undefined, aggregation, false).add(JAN2010, 10);
            assertEquals(INVALID_AGGREGATION, b.build());
            assertEquals(b.build(), b.build());
        }

        b = builder(Undefined, None, false).add(JAN2010, 10);
        assertEquals(GUESS_SINGLE, b.build());
        assertEquals(b.build(), b.build());

        b = builder(Undefined, None, false).add(JAN2010, 10).add(JAN2010, 20);
        assertEquals(GUESS_DUPLICATION, b.build());
        assertEquals(b.build(), b.build());

        for (TsFrequency freq : complementOf(EnumSet.of(Undefined))) {
            b = builder(freq, None, false).add(JAN2010, 10).add(JAN2010, 20);
            assertEquals(DUPLICATION_WITHOUT_AGGREGATION, b.build());
            assertEquals(b.build(), b.build());
        }
    }
}
