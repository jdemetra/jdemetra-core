/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.timeseries;

import demetra.time.ISO8601;
import demetra.time.TemporalIntervalRepresentation;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAmount;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Jean Palate
 */
@lombok.Value(staticConstructor = "of")
public class TimePeriod implements TimeSeriesPeriod, Comparable<TimePeriod> {

    private static final TemporalIntervalRepresentation.StartEnd<LocalDateTime> REPRESENTATION
            = new TemporalIntervalRepresentation.StartEnd(ISO8601.Converter.LOCAL_DATE_TIME);

    @lombok.NonNull
    LocalDateTime start, end;

    @Override
    public LocalDateTime start() {
        return start;
    }

    @Override
    public LocalDateTime end() {
        return end;
    }

    @Override
    public boolean contains(LocalDateTime element) {
        return element.isBefore(end) && (!element.isBefore(start));
    }

    @Override
    public TemporalAmount getDuration() {
        return Duration.between(start, end);
    }

    @Override
    public TemporalIntervalRepresentation getRepresentation() {
        return REPRESENTATION;
    }

    @Override
    public int compareTo(TimePeriod t) {
        if (start.equals(t.start) && end.isAfter(t.end)) {
            return 0;
        }
        if (!end.isAfter(t.start)) {
            return -1;
        }
        if (!t.end.isAfter(start)) {
            return 1;
        }
        throw new TsException(TsException.INCOMPATIBLE_PERIOD);
    }

    @NonNull
    public static TimePeriod parse(@NonNull CharSequence text) throws DateTimeParseException {
        return REPRESENTATION.parse(text, (start, end) -> TimePeriod.of(start, end));
    }
}
