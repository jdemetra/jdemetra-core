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

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableTypeAssert;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class TimeIntervalFormatterTest {

    private static final Period P2D = Period.ofDays(2);
    private static final Period P2M = Period.ofMonths(2);

    private static final LocalDate startDate = LocalDate.of(2010, 2, 15);
    private static final DateBasedInterval days = new DateBasedInterval(startDate, P2D);
    private static final DateBasedInterval months = new DateBasedInterval(startDate, P2M);
    private static final DateBasedInterval monthsUp = new DateBasedInterval(LocalDate.of(2011, 1, 1).minus(P2M), P2M);

    @Nested
    class StartEndTest {

        final TimeIntervalQuery<DateBasedInterval> query = accessor -> new DateBasedInterval(
                LocalDate.from(accessor.start()),
                Period.between(LocalDate.from(accessor.start()), LocalDate.from(accessor.end()))
        );

        @Test
        public void testFormat() {
            TimeIntervalFormatter l = TimeIntervalFormatter.StartEnd.ISO_LOCAL_DATE;
            TimeIntervalFormatter b = TimeIntervalFormatter.StartEnd.BASIC_ISO_DATE;
            TimeIntervalFormatter o = TimeIntervalFormatter.StartEnd.ISO_ORDINAL_DATE;
            TimeIntervalFormatter w = TimeIntervalFormatter.StartEnd.ISO_WEEK_DATE;

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
        }

        @Test
        public void testFormatConcise() {
            TimeIntervalFormatter l = TimeIntervalFormatter.StartEnd.ISO_LOCAL_DATE.withConcise(true);
            TimeIntervalFormatter b = TimeIntervalFormatter.StartEnd.BASIC_ISO_DATE.withConcise(true);
            TimeIntervalFormatter o = TimeIntervalFormatter.StartEnd.ISO_ORDINAL_DATE.withConcise(true);
            TimeIntervalFormatter w = TimeIntervalFormatter.StartEnd.ISO_WEEK_DATE.withConcise(true);

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
        }

        @Test
        public void testParse() {
            TimeIntervalFormatter l = TimeIntervalFormatter.StartEnd.ISO_LOCAL_DATE;
            TimeIntervalFormatter b = TimeIntervalFormatter.StartEnd.BASIC_ISO_DATE;
            TimeIntervalFormatter o = TimeIntervalFormatter.StartEnd.ISO_ORDINAL_DATE;
            TimeIntervalFormatter w = TimeIntervalFormatter.StartEnd.ISO_WEEK_DATE;

            assertThat(l.parse("2010-02-15/2010-02-17", query)).isEqualTo(days);
            assertThat(b.parse("20100215/20100217", query)).isEqualTo(days);
            assertThat(o.parse("2010-046/2010-048", query)).isEqualTo(days);
            assertThat(w.parse("2010-W07-1/2010-W07-3", query)).isEqualTo(days);

            assertThat(l.parse("2010-02-15/2010-04-15", query)).isEqualTo(months);
            assertThat(b.parse("20100215/20100415", query)).isEqualTo(months);
            assertThat(o.parse("2010-046/2010-105", query)).isEqualTo(months);
            assertThat(w.parse("2010-W07-1/2010-W15-4", query)).isEqualTo(months);

            assertThat(l.parse("2010-11-01/2011-01-01", query)).isEqualTo(monthsUp);
            assertThat(b.parse("20101101/20110101", query)).isEqualTo(monthsUp);
            assertThat(o.parse("2010-305/2011-001", query)).isEqualTo(monthsUp);
            assertThat(w.parse("2010-W44-1/2010-W52-6", query)).isEqualTo(monthsUp); //FIXME?

            String msg = "Cannot find interval designator";

            // Should fail on missing separator
            assertThatParseException().isThrownBy(() -> l.parse("2010-02-152010-02-17", query)).withMessage(msg);
            assertThatParseException().isThrownBy(() -> b.parse("2010021520100217", query)).withMessage(msg);
            assertThatParseException().isThrownBy(() -> o.parse("2010-0462010-048", query)).withMessage(msg);
            assertThatParseException().isThrownBy(() -> w.parse("2010-W07-12010-W07-3", query)).withMessage(msg);

            // Should fail on invalid separator
            assertThatParseException().isThrownBy(() -> l.parse("2010-02-15\\2010-02-17", query)).withMessage(msg);
            assertThatParseException().isThrownBy(() -> b.parse("20100215\\20100217", query)).withMessage(msg);
            assertThatParseException().isThrownBy(() -> o.parse("2010-046\\2010-048", query)).withMessage(msg);
            assertThatParseException().isThrownBy(() -> w.parse("2010-W07-1\\2010-W07-3", query)).withMessage(msg);

            // Should fail on leading spaces
            assertThatParseException().isThrownBy(() -> l.parse(" 2010-02-15/2010-02-17", query));
            assertThatParseException().isThrownBy(() -> b.parse(" 20100215/20100217", query));
            assertThatParseException().isThrownBy(() -> o.parse(" 2010-046/2010-048", query));
            assertThatParseException().isThrownBy(() -> w.parse(" 2010-W07-1/2010-W07-3", query));

            // Should fail on trailing spaces
            assertThatParseException().isThrownBy(() -> l.parse("2010-02-15/2010-02-17 ", query));
            assertThatParseException().isThrownBy(() -> b.parse("20100215/20100217 ", query));
            assertThatParseException().isThrownBy(() -> o.parse("2010-046/2010-048 ", query));
            assertThatParseException().isThrownBy(() -> w.parse("2010-W07-1/2010-W07-3 ", query));

            // Should fail on missing field
            assertThatParseException().isThrownBy(() -> l.parse("2010-02-15/2010-02-", query));
            assertThatParseException().isThrownBy(() -> b.parse("20100215/201002", query));
            assertThatParseException().isThrownBy(() -> o.parse("2010-046/2010", query));
            assertThatParseException().isThrownBy(() -> w.parse("2010-W07-1/2010-W07", query));

            // Should fail on invalid field
            assertThatParseException().isThrownBy(() -> l.parse("2010-02-15/xxxx-02-17", query));
            assertThatParseException().isThrownBy(() -> b.parse("20100215/xxxx0217", query));
            assertThatParseException().isThrownBy(() -> o.parse("2010-046/xxxx-048", query));
            assertThatParseException().isThrownBy(() -> w.parse("2010-W07-1/xxxx-W07-3", query));
        }

        @Test
        public void testParseConcise() {
            TimeIntervalFormatter l = TimeIntervalFormatter.StartEnd.ISO_LOCAL_DATE.withConcise(true);
            TimeIntervalFormatter b = TimeIntervalFormatter.StartEnd.BASIC_ISO_DATE.withConcise(true);
            TimeIntervalFormatter o = TimeIntervalFormatter.StartEnd.ISO_ORDINAL_DATE.withConcise(true);
            TimeIntervalFormatter w = TimeIntervalFormatter.StartEnd.ISO_WEEK_DATE.withConcise(true);

            assertThat(l.parse("2010-02-15/2010-02-17", query)).isEqualTo(days);
            assertThat(b.parse("20100215/20100217", query)).isEqualTo(days);
            assertThat(o.parse("2010-046/2010-048", query)).isEqualTo(days);
            assertThat(w.parse("2010-W07-1/2010-W07-3", query)).isEqualTo(days);

            assertThat(l.parse("2010-02-15/2010-04-15", query)).isEqualTo(months);
            assertThat(b.parse("20100215/20100415", query)).isEqualTo(months);
            assertThat(o.parse("2010-046/2010-105", query)).isEqualTo(months);
            assertThat(w.parse("2010-W07-1/2010-W15-4", query)).isEqualTo(months);

            assertThat(l.parse("2010-11-01/2011-01-01", query)).isEqualTo(monthsUp);
            assertThat(b.parse("20101101/20110101", query)).isEqualTo(monthsUp);
            assertThat(o.parse("2010-305/2011-001", query)).isEqualTo(monthsUp);
            assertThat(w.parse("2010-W44-1/2010-W52-6", query)).isEqualTo(monthsUp); //FIXME?

            assertThat(l.parse("2010-02-15/17", query)).isEqualTo(days);
            assertThat(b.parse("20100215/17", query)).isEqualTo(days);
            assertThat(o.parse("2010-046/048", query)).isEqualTo(days);
            assertThat(w.parse("2010-W07-1/3", query)).isEqualTo(days);

            assertThat(l.parse("2010-02-15/04-15", query)).isEqualTo(months);
            assertThat(b.parse("20100215/0415", query)).isEqualTo(months);
            assertThat(o.parse("2010-046/105", query)).isEqualTo(months);
            assertThat(w.parse("2010-W07-1/W15-4", query)).isEqualTo(months);
        }
    }

    @Nested
    class StartDurationTest {

        final TimeIntervalQuery<DateBasedInterval> query = accessor -> new DateBasedInterval(LocalDate.from(accessor.start()), Period.from(accessor.getDuration()));

        @Test
        public void testFormat() {
            TimeIntervalFormatter l = TimeIntervalFormatter.StartDuration.ISO_LOCAL_DATE;
            TimeIntervalFormatter b = TimeIntervalFormatter.StartDuration.BASIC_ISO_DATE;
            TimeIntervalFormatter o = TimeIntervalFormatter.StartDuration.ISO_ORDINAL_DATE;
            TimeIntervalFormatter w = TimeIntervalFormatter.StartDuration.ISO_WEEK_DATE;

            assertThat(l.format(days)).isEqualTo("2010-02-15/P2D");
            assertThat(b.format(days)).isEqualTo("20100215/P2D");
            assertThat(o.format(days)).isEqualTo("2010-046/P2D");
            assertThat(w.format(days)).isEqualTo("2010-W07-1/P2D");

            assertThat(l.format(months)).isEqualTo("2010-02-15/P2M");
            assertThat(b.format(months)).isEqualTo("20100215/P2M");
            assertThat(o.format(months)).isEqualTo("2010-046/P2M");
            assertThat(w.format(months)).isEqualTo("2010-W07-1/P2M");
        }

        @Test
        public void testParse() {
            TimeIntervalFormatter l = TimeIntervalFormatter.StartDuration.ISO_LOCAL_DATE;
            TimeIntervalFormatter b = TimeIntervalFormatter.StartDuration.BASIC_ISO_DATE;
            TimeIntervalFormatter o = TimeIntervalFormatter.StartDuration.ISO_ORDINAL_DATE;
            TimeIntervalFormatter w = TimeIntervalFormatter.StartDuration.ISO_WEEK_DATE;

            assertThat(l.parse("2010-02-15/P2D", query)).isEqualTo(days);
            assertThat(b.parse("20100215/P2D", query)).isEqualTo(days);
            assertThat(o.parse("2010-046/P2D", query)).isEqualTo(days);
            assertThat(w.parse("2010-W07-1/P2D", query)).isEqualTo(days);

            assertThat(l.parse("2010-02-15/P2M", query)).isEqualTo(months);
            assertThat(b.parse("20100215/P2M", query)).isEqualTo(months);
            assertThat(o.parse("2010-046/P2M", query)).isEqualTo(months);
            assertThat(w.parse("2010-W07-1/P2M", query)).isEqualTo(months);
        }
    }

    @Nested
    class DurationEndTest {

        final TimeIntervalQuery<DateBasedInterval> query = accessor -> {
            Period duration = Period.from(accessor.getDuration());
            return new DateBasedInterval(LocalDate.from(accessor.end()).minus(duration), duration);
        };

        @Test
        public void testFormat() {
            TimeIntervalFormatter l = TimeIntervalFormatter.DurationEnd.ISO_LOCAL_DATE;
            TimeIntervalFormatter b = TimeIntervalFormatter.DurationEnd.BASIC_ISO_DATE;
            TimeIntervalFormatter o = TimeIntervalFormatter.DurationEnd.ISO_ORDINAL_DATE;
            TimeIntervalFormatter w = TimeIntervalFormatter.DurationEnd.ISO_WEEK_DATE;

            assertThat(l.format(days)).isEqualTo("P2D/2010-02-17");
            assertThat(b.format(days)).isEqualTo("P2D/20100217");
            assertThat(o.format(days)).isEqualTo("P2D/2010-048");
            assertThat(w.format(days)).isEqualTo("P2D/2010-W07-3");

            assertThat(l.format(months)).isEqualTo("P2M/2010-04-15");
            assertThat(b.format(months)).isEqualTo("P2M/20100415");
            assertThat(o.format(months)).isEqualTo("P2M/2010-105");
            assertThat(w.format(months)).isEqualTo("P2M/2010-W15-4");
        }

        @Test
        public void testParse() {
            TimeIntervalFormatter l = TimeIntervalFormatter.DurationEnd.ISO_LOCAL_DATE;
            TimeIntervalFormatter b = TimeIntervalFormatter.DurationEnd.BASIC_ISO_DATE;
            TimeIntervalFormatter o = TimeIntervalFormatter.DurationEnd.ISO_ORDINAL_DATE;
            TimeIntervalFormatter w = TimeIntervalFormatter.DurationEnd.ISO_WEEK_DATE;

            assertThat(l.parse("P2D/2010-02-17", query)).isEqualTo(days);
            assertThat(b.parse("P2D/20100217", query)).isEqualTo(days);
            assertThat(o.parse("P2D/2010-048", query)).isEqualTo(days);
            assertThat(w.parse("P2D/2010-W07-3", query)).isEqualTo(days);

            assertThat(l.parse("P2M/2010-04-15", query)).isEqualTo(months);
            assertThat(b.parse("P2M/20100415", query)).isEqualTo(months);
            assertThat(o.parse("P2M/2010-105", query)).isEqualTo(months);
            assertThat(w.parse("P2M/2010-W15-4", query)).isEqualTo(months);
        }
    }

    @Nested
    class DurationTest {

        final TimeIntervalQuery<DateBasedInterval> query = (TimeIntervalAccessor z) -> {
            Period duration = Period.from(z.getDuration());
            return new DateBasedInterval(startDate, duration);
        };

        @Test
        public void testFormat() {
            TimeIntervalFormatter x = TimeIntervalFormatter.Duration.of(Period::parse);

            assertThat(x.format(days)).isEqualTo("P2D");
            assertThat(x.format(months)).isEqualTo("P2M");
        }

        @Test
        public void testParse() {
            TimeIntervalFormatter x = TimeIntervalFormatter.Duration.of(Period::parse);

            assertThat(x.parse("P2D", query)).isEqualTo(days);
            assertThat(x.parse("P2M", query)).isEqualTo(months);
        }
    }

    private static ThrowableTypeAssert<DateTimeParseException> assertThatParseException() {
        return Assertions.assertThatExceptionOfType(DateTimeParseException.class);
    }

    @lombok.Value
    private static class DateBasedInterval implements TimeInterval<LocalDate, Period> {

        @lombok.NonNull
        LocalDate start;

        @lombok.NonNull
        Period duration;

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
    }
}
