/*
 * Copyright 2020 National Bank of Belgium
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
package demetra.time;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import org.assertj.core.api.Assertions;
import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.api.ThrowableTypeAssert;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class IsoIntervalConverterTest {

    private final Period P2D = Period.ofDays(2);
    private final Period P2M = Period.ofMonths(2);

    private final LocalDate startDate = LocalDate.of(2010, 2, 15);
    private final MockedPeriod days = new MockedPeriod(startDate, P2D);
    private final MockedPeriod months = new MockedPeriod(startDate, P2M);
    private final MockedPeriod monthsUp = new MockedPeriod(LocalDate.of(2011, 1, 1).minus(P2M), P2M);

    @Test
    public void testStartEnd() {
        IsoIntervalConverter<MockedPeriod> l = new IsoIntervalConverter.StartEnd<>(LocalDateConverter.LOCAL_DATE, false, MockedPeriod::startEnd);
        IsoIntervalConverter<MockedPeriod> b = new IsoIntervalConverter.StartEnd<>(LocalDateConverter.BASIC_DATE, false, MockedPeriod::startEnd);
        IsoIntervalConverter<MockedPeriod> o = new IsoIntervalConverter.StartEnd<>(LocalDateConverter.ORDINAL_DATE, false, MockedPeriod::startEnd);
        IsoIntervalConverter<MockedPeriod> w = new IsoIntervalConverter.StartEnd<>(LocalDateConverter.WEEK_DATE, false, MockedPeriod::startEnd);

        assertThat(l.format(days)).isEqualTo("2010-02-15/2010-02-17");
        assertThat(b.format(days)).isEqualTo("20100215/20100217");
        assertThat(o.format(days)).isEqualTo("2010-046/2010-048");
        assertThat(w.format(days)).isEqualTo("2010-W07-1/2010-W07-3");

        assertThat(l.format(months)).isEqualTo("2010-02-15/2010-04-15");
        assertThat(b.format(months)).isEqualTo("20100215/20100415");
        assertThat(o.format(months)).isEqualTo("2010-046/2010-105");
        assertThat(w.format(months)).isEqualTo("2010-W07-1/2010-W15-4");

        assertThat(l.format(monthsUp)).isEqualTo("2010-11-01/2011-01-01");
        assertThat(b.format(monthsUp)).isEqualTo("20101101/20110101");
        assertThat(o.format(monthsUp)).isEqualTo("2010-305/2011-001");
        assertThat(w.format(monthsUp)).isEqualTo("2010-W44-1/2010-W52-6"); //FIXME?

        assertThat(l.parse("2010-02-15/2010-02-17")).isEqualTo(days);
        assertThat(b.parse("20100215/20100217")).isEqualTo(days);
        assertThat(o.parse("2010-046/2010-048")).isEqualTo(days);
        assertThat(w.parse("2010-W07-1/2010-W07-3")).isEqualTo(days);

        assertThat(l.parse("2010-02-15/2010-04-15")).isEqualTo(months);
        assertThat(b.parse("20100215/20100415")).isEqualTo(months);
        assertThat(o.parse("2010-046/2010-105")).isEqualTo(months);
        assertThat(w.parse("2010-W07-1/2010-W15-4")).isEqualTo(months);

        assertThat(l.parse("2010-11-01/2011-01-01")).isEqualTo(monthsUp);
        assertThat(b.parse("20101101/20110101")).isEqualTo(monthsUp);
        assertThat(o.parse("2010-305/2011-001")).isEqualTo(monthsUp);
        assertThat(w.parse("2010-W44-1/2010-W52-6")).isEqualTo(monthsUp); //FIXME?

        // Should fail on missing separator
        assertThatParseException().isThrownBy(() -> l.parse("2010-02-152010-02-17"));
        assertThatParseException().isThrownBy(() -> b.parse("2010021520100217"));
        assertThatParseException().isThrownBy(() -> o.parse("2010-0462010-048"));
        assertThatParseException().isThrownBy(() -> w.parse("2010-W07-12010-W07-3"));

        // Should fail on invalid separator
        assertThatParseException().isThrownBy(() -> l.parse("2010-02-15\\2010-02-17"));
        assertThatParseException().isThrownBy(() -> b.parse("20100215\\20100217"));
        assertThatParseException().isThrownBy(() -> o.parse("2010-046\\2010-048"));
        assertThatParseException().isThrownBy(() -> w.parse("2010-W07-1\\2010-W07-3"));

        // Should fail on leading spaces
        assertThatParseException().isThrownBy(() -> l.parse(" 2010-02-15/2010-02-17"));
        assertThatParseException().isThrownBy(() -> b.parse(" 20100215/20100217"));
        assertThatParseException().isThrownBy(() -> o.parse(" 2010-046/2010-048"));
        assertThatParseException().isThrownBy(() -> w.parse(" 2010-W07-1/2010-W07-3"));

        // Should fail on trailing spaces
        assertThatParseException().isThrownBy(() -> l.parse("2010-02-15/2010-02-17 "));
        assertThatParseException().isThrownBy(() -> b.parse("20100215/20100217 "));
        assertThatParseException().isThrownBy(() -> o.parse("2010-046/2010-048 "));
        assertThatParseException().isThrownBy(() -> w.parse("2010-W07-1/2010-W07-3 "));

        // Should fail on missing field
        assertThatParseException().isThrownBy(() -> l.parse("2010-02-15/2010-02-"));
        assertThatParseException().isThrownBy(() -> b.parse("20100215/201002"));
        assertThatParseException().isThrownBy(() -> o.parse("2010-046/2010"));
        assertThatParseException().isThrownBy(() -> w.parse("2010-W07-1/2010-W07"));

        // Should fail on invalid field
        assertThatParseException().isThrownBy(() -> l.parse("2010-02-15/xxxx-02-17"));
        assertThatParseException().isThrownBy(() -> b.parse("20100215/xxxx0217"));
        assertThatParseException().isThrownBy(() -> o.parse("2010-046/xxxx-048"));
        assertThatParseException().isThrownBy(() -> w.parse("2010-W07-1/xxxx-W07-3"));
    }

    @Test
    public void testStartEnd_concise() {
        IsoIntervalConverter<MockedPeriod> l = new IsoIntervalConverter.StartEnd<>(LocalDateConverter.LOCAL_DATE, true, MockedPeriod::startEnd);
        IsoIntervalConverter<MockedPeriod> b = new IsoIntervalConverter.StartEnd<>(LocalDateConverter.BASIC_DATE, true, MockedPeriod::startEnd);
        IsoIntervalConverter<MockedPeriod> o = new IsoIntervalConverter.StartEnd<>(LocalDateConverter.ORDINAL_DATE, true, MockedPeriod::startEnd);
        IsoIntervalConverter<MockedPeriod> w = new IsoIntervalConverter.StartEnd<>(LocalDateConverter.WEEK_DATE, true, MockedPeriod::startEnd);

        assertThat(l.format(days)).isEqualTo("2010-02-15/17");
//        assertThat(b.format(days)).isEqualTo("20100215/17");
        assertThat(o.format(days)).isEqualTo("2010-046/048");
        assertThat(w.format(days)).isEqualTo("2010-W07-1/3");

        assertThat(l.format(months)).isEqualTo("2010-02-15/04-15");
//        assertThat(b.format(months)).isEqualTo("20100215/0415");
        assertThat(o.format(months)).isEqualTo("2010-046/105");
//        assertThat(w.format(months)).isEqualTo("2010-W07-1/W15-4");

        assertThat(l.format(monthsUp)).isEqualTo("2010-11-01/2011-01-01");
//        assertThat(b.format(monthsUp)).isEqualTo("20101101/20110101");
        assertThat(o.format(monthsUp)).isEqualTo("2010-305/2011-001");
//        assertThat(w.format(monthsUp)).isEqualTo("2010-W44-1/W52-6");

        assertThat(l.parse("2010-02-15/2010-02-17")).isEqualTo(days);
        assertThat(b.parse("20100215/20100217")).isEqualTo(days);
        assertThat(o.parse("2010-046/2010-048")).isEqualTo(days);
        assertThat(w.parse("2010-W07-1/2010-W07-3")).isEqualTo(days);

        assertThat(l.parse("2010-02-15/2010-04-15")).isEqualTo(months);
        assertThat(b.parse("20100215/20100415")).isEqualTo(months);
        assertThat(o.parse("2010-046/2010-105")).isEqualTo(months);
        assertThat(w.parse("2010-W07-1/2010-W15-4")).isEqualTo(months);

        assertThat(l.parse("2010-11-01/2011-01-01")).isEqualTo(monthsUp);
        assertThat(b.parse("20101101/20110101")).isEqualTo(monthsUp);
        assertThat(o.parse("2010-305/2011-001")).isEqualTo(monthsUp);
        assertThat(w.parse("2010-W44-1/2010-W52-6")).isEqualTo(monthsUp); //FIXME?

        assertThat(l.parse("2010-02-15/17")).isEqualTo(days);
        assertThat(b.parse("20100215/17")).isEqualTo(days);
        assertThat(o.parse("2010-046/048")).isEqualTo(days);
        assertThat(w.parse("2010-W07-1/3")).isEqualTo(days);

        assertThat(l.parse("2010-02-15/04-15")).isEqualTo(months);
        assertThat(b.parse("20100215/0415")).isEqualTo(months);
        assertThat(o.parse("2010-046/105")).isEqualTo(months);
        assertThat(w.parse("2010-W07-1/W15-4")).isEqualTo(months);
    }

    @Test
    public void testStartDuration() {
        IsoIntervalConverter<MockedPeriod> l = new IsoIntervalConverter.StartDuration<>(LocalDateConverter.LOCAL_DATE, IsoConverter.PERIOD, MockedPeriod::startDuration);
        IsoIntervalConverter<MockedPeriod> b = new IsoIntervalConverter.StartDuration<>(LocalDateConverter.BASIC_DATE, IsoConverter.PERIOD, MockedPeriod::startDuration);
        IsoIntervalConverter<MockedPeriod> o = new IsoIntervalConverter.StartDuration<>(LocalDateConverter.ORDINAL_DATE, IsoConverter.PERIOD, MockedPeriod::startDuration);
        IsoIntervalConverter<MockedPeriod> w = new IsoIntervalConverter.StartDuration<>(LocalDateConverter.WEEK_DATE, IsoConverter.PERIOD, MockedPeriod::startDuration);

        assertThat(l.format(days)).isEqualTo("2010-02-15/P2D");
        assertThat(b.format(days)).isEqualTo("20100215/P2D");
        assertThat(o.format(days)).isEqualTo("2010-046/P2D");
        assertThat(w.format(days)).isEqualTo("2010-W07-1/P2D");

        assertThat(l.format(months)).isEqualTo("2010-02-15/P2M");
        assertThat(b.format(months)).isEqualTo("20100215/P2M");
        assertThat(o.format(months)).isEqualTo("2010-046/P2M");
        assertThat(w.format(months)).isEqualTo("2010-W07-1/P2M");

        assertThat(l.parse("2010-02-15/P2D")).isEqualTo(days);
        assertThat(b.parse("20100215/P2D")).isEqualTo(days);
        assertThat(o.parse("2010-046/P2D")).isEqualTo(days);
        assertThat(w.parse("2010-W07-1/P2D")).isEqualTo(days);

        assertThat(l.parse("2010-02-15/P2M")).isEqualTo(months);
        assertThat(b.parse("20100215/P2M")).isEqualTo(months);
        assertThat(o.parse("2010-046/P2M")).isEqualTo(months);
        assertThat(w.parse("2010-W07-1/P2M")).isEqualTo(months);
    }

    @Test
    public void testDurationEnd() {
        IsoIntervalConverter<MockedPeriod> l = new IsoIntervalConverter.DurationEnd<>(IsoConverter.PERIOD, LocalDateConverter.LOCAL_DATE, MockedPeriod::durationEnd);
        IsoIntervalConverter<MockedPeriod> b = new IsoIntervalConverter.DurationEnd<>(IsoConverter.PERIOD, LocalDateConverter.BASIC_DATE, MockedPeriod::durationEnd);
        IsoIntervalConverter<MockedPeriod> o = new IsoIntervalConverter.DurationEnd<>(IsoConverter.PERIOD, LocalDateConverter.ORDINAL_DATE, MockedPeriod::durationEnd);
        IsoIntervalConverter<MockedPeriod> w = new IsoIntervalConverter.DurationEnd<>(IsoConverter.PERIOD, LocalDateConverter.WEEK_DATE, MockedPeriod::durationEnd);

        assertThat(l.format(days)).isEqualTo("P2D/2010-02-17");
        assertThat(b.format(days)).isEqualTo("P2D/20100217");
        assertThat(o.format(days)).isEqualTo("P2D/2010-048");
        assertThat(w.format(days)).isEqualTo("P2D/2010-W07-3");

        assertThat(l.format(months)).isEqualTo("P2M/2010-04-15");
        assertThat(b.format(months)).isEqualTo("P2M/20100415");
        assertThat(o.format(months)).isEqualTo("P2M/2010-105");
        assertThat(w.format(months)).isEqualTo("P2M/2010-W15-4");

        assertThat(l.parse("P2D/2010-02-17")).isEqualTo(days);
        assertThat(b.parse("P2D/20100217")).isEqualTo(days);
        assertThat(o.parse("P2D/2010-048")).isEqualTo(days);
        assertThat(w.parse("P2D/2010-W07-3")).isEqualTo(days);

        assertThat(l.parse("P2M/2010-04-15")).isEqualTo(months);
        assertThat(b.parse("P2M/20100415")).isEqualTo(months);
        assertThat(o.parse("P2M/2010-105")).isEqualTo(months);
        assertThat(w.parse("P2M/2010-W15-4")).isEqualTo(months);
    }

    @Test
    public void testDuration() {
        IsoIntervalConverter<MockedPeriod> x = new IsoIntervalConverter.Duration<>(IsoConverter.PERIOD, z -> MockedPeriod.duration(z, startDate));
        assertThat(x.format(days)).isEqualTo("P2D");
        assertThat(x.format(months)).isEqualTo("P2M");

        assertThat(x.parse("P2D")).isEqualTo(days);
        assertThat(x.parse("P2M")).isEqualTo(months);
    }

    private static ThrowableTypeAssert<DateTimeParseException> assertThatParseException() {
        return Assertions.assertThatExceptionOfType(DateTimeParseException.class);
    }

    @lombok.Value
    private static final class MockedPeriod implements IsoInterval<LocalDate, Period> {

        static MockedPeriod startEnd(LocalDate start, LocalDate end) {
            return new MockedPeriod(start, Period.between(start, end));
        }

        static MockedPeriod startDuration(LocalDate start, Period duration) {
            return new MockedPeriod(start, duration);
        }

        static MockedPeriod durationEnd(Period duration, LocalDate end) {
            return new MockedPeriod(end.minus(duration), duration);
        }

        static MockedPeriod duration(Period duration, LocalDate context) {
            return new MockedPeriod(context, duration);
        }

        @lombok.NonNull
        private final LocalDate start;

        @lombok.NonNull
        private final Period duration;

        @Override
        public LocalDate start() {
            return start;
        }

        @Override
        public LocalDate end() {
            return start.plus(duration);
        }

        @Override
        public Period getDuration() {
            return duration;
        }

        @Override
        public boolean contains(LocalDate element) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String toISO8601() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public MockedPeriod at(int year, int month, int dayOfMonth) {
            return new MockedPeriod(LocalDate.of(year, month, dayOfMonth), duration);
        }
    }
}
