/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demetra.timeseries;

import demetra.time.ISO_8601;
import demetra.time.TimeIntervalAccessor;
import demetra.time.TimeIntervalFormatter;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeParseException;

/**
 * @author Jean Palate
 */
@ISO_8601
@RepresentableAsString
@lombok.Value(staticConstructor = "of")
public class Day implements TimeSeriesInterval<Period> {

    @lombok.NonNull
    LocalDate day;

    @Override
    public LocalDateTime start() {
        return day.atStartOfDay();
    }

    @Override
    public LocalDateTime end() {
        return day.plusDays(1).atStartOfDay();
    }

    @Override
    public boolean contains(LocalDateTime element) {
        return element.toLocalDate().equals(day);
    }

    @Override
    public Period getDuration() {
        return Period.ofDays(1);
    }

    @Override
    public String toString() {
        return TimeIntervalFormatter.StartDuration.ISO_LOCAL_DATE.format(this);
    }

    @StaticFactoryMethod
    @NonNull
    public static Day parse(@NonNull CharSequence text) throws DateTimeParseException {
        return TimeIntervalFormatter.StartDuration.ISO_LOCAL_DATE.parse(text, Day::from);
    }

    @StaticFactoryMethod
    @NonNull
    public static Day from(@NonNull TimeIntervalAccessor timeInterval) {
        return Day.of(LocalDate.from(timeInterval.start()));
    }
}
