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

import demetra.time.IsoConverter;
import demetra.time.IsoIntervalConverter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Jean Palate
 */
@lombok.Value(staticConstructor = "of")
public class TimePeriod implements TimeSeriesInterval<Duration>, Comparable<TimePeriod> {

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
    public Duration getDuration() {
        return Duration.between(start, end);
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

    @Override
    public String toString() {
        return toISO8601();
    }

    @Override
    public String toISO8601() {
        return CONVERTER.format(this).toString();
    }

    @NonNull
    public static TimePeriod parse(@NonNull CharSequence text) throws DateTimeParseException {
        return CONVERTER.parse(text);
    }

    private static TimePeriod make(LocalDateTime start, LocalDateTime end) {
        return TimePeriod.of(start, end);
    }

    private static final IsoIntervalConverter<TimePeriod> CONVERTER
            = new IsoIntervalConverter.StartEnd<>(IsoConverter.LOCAL_DATE_TIME, false, TimePeriod::make);
}
