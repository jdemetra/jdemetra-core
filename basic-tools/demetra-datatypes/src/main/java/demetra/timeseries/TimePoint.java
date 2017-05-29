/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.timeseries;

import demetra.design.Immutable;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 *
 * @author Jean Palate
 */
@Immutable
@lombok.EqualsAndHashCode
public final class TimePoint implements ITimePeriod {

    public static TimePoint of(LocalDateTime start) {
        return new TimePoint(start);
    }

    private final LocalDateTime pt;

    private TimePoint(LocalDateTime pt) {
        this.pt = pt;
    }

    @Override
    public boolean contains(LocalDateTime dt) {
        return dt.equals(pt);
    }

    @Override
    public LocalDateTime start() {
        return pt;
    }

    @Override
    public LocalDateTime end() {
        return pt;
    }

    @Override
    public Duration duration() {
        return Duration.ZERO;
    }
}
