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
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class TemporalIntervalRepresentationTest {

    @Test
    public void testLocalDatePeriod() {
        LocalDate startDate = LocalDate.of(2010, 2, 15);
        MockedPeriod days = new MockedPeriod(startDate, Period.ofDays(2));
        MockedPeriod months = new MockedPeriod(startDate, Period.ofMonths(2));

        assertThat(new TemporalIntervalRepresentation.StartEnd<LocalDate, Period>(ISO8601.Converter.LOCAL_DATE))
                .satisfies(r -> {
                    assertThat(r.format(days)).isEqualTo("2010-02-15/2010-02-17");
                    assertThat(r.format(months)).isEqualTo("2010-02-15/2010-04-15");

                    assertThat(r.formatConcise(days)).isEqualTo("2010-02-15/17");
                    assertThat(r.formatConcise(days.at(2010, 2, 1))).isEqualTo("2010-02-01/03");
                    assertThat(r.formatConcise(days.at(2010, 2, 28))).isEqualTo("2010-02-28/03-02");
                    assertThat(r.formatConcise(months)).isEqualTo("2010-02-15/04-15");
                    assertThat(r.formatConcise(months.at(2010, 9, 1))).isEqualTo("2010-09-01/11-01");
                    assertThat(r.formatConcise(months.at(2010, 11, 1))).isEqualTo("2010-11-01/2011-01-01");

                    assertThat(r.parse("2010-02-15/2010-02-17", MockedPeriod::startEnd)).isEqualTo(days);
                    assertThat(r.parse("2010-02-01/03", MockedPeriod::startEnd)).isEqualTo(days.at(2010, 2, 1));
                    assertThat(r.parse("2010-02-28/03-02", MockedPeriod::startEnd)).isEqualTo(days.at(2010, 2, 28));
                    assertThat(r.parse("2010-02-15/2010-04-15", MockedPeriod::startEnd)).isEqualTo(months);
                    assertThat(r.parse("2010-09-01/11-01", MockedPeriod::startEnd)).isEqualTo(months.at(2010, 9, 1));
                    assertThat(r.parse("2010-11-01/2011-01-01", MockedPeriod::startEnd)).isEqualTo(months.at(2010, 11, 1));
                });

        assertThat(new TemporalIntervalRepresentation.StartDuration<>(ISO8601.Converter.LOCAL_DATE, ISO8601.Converter.PERIOD))
                .satisfies(startDuration -> {
                    assertThat(startDuration.format(days)).isEqualTo("2010-02-15/P2D");
                    assertThat(startDuration.format(months)).isEqualTo("2010-02-15/P2M");

                    assertThat(startDuration.parse("2010-02-15/P2D", MockedPeriod::startDuration)).isEqualTo(days);
                    assertThat(startDuration.parse("2010-02-15/P2M", MockedPeriod::startDuration)).isEqualTo(months);
                });

        assertThat(new TemporalIntervalRepresentation.DurationEnd<>(ISO8601.Converter.PERIOD, ISO8601.Converter.LOCAL_DATE))
                .satisfies(durationEnd -> {
                    assertThat(durationEnd.format(days)).isEqualTo("P2D/2010-02-17");
                    assertThat(durationEnd.format(months)).isEqualTo("P2M/2010-04-15");

                    assertThat(durationEnd.parse("P2D/2010-02-17", MockedPeriod::durationEnd)).isEqualTo(days);
                    assertThat(durationEnd.parse("P2M/2010-04-15", MockedPeriod::durationEnd)).isEqualTo(months);
                });

        assertThat(new TemporalIntervalRepresentation.Duration<LocalDate, Period>(ISO8601.Converter.PERIOD))
                .satisfies(duration -> {
                    assertThat(duration.format(days)).isEqualTo("P2D");
                    assertThat(duration.format(months)).isEqualTo("P2M");

                    assertThat((MockedPeriod) duration.parse("P2D", x -> MockedPeriod.duration(x, startDate))).isEqualTo(days);
                    assertThat((MockedPeriod) duration.parse("P2M", x -> MockedPeriod.duration(x, startDate))).isEqualTo(months);
                });
    }

    @lombok.Value
    private static final class MockedPeriod implements TemporalInterval<LocalDate, Period> {

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
