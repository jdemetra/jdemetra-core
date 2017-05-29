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
import java.time.LocalDateTime;

/**
 *
 * @author Jean Palate
 */
@Immutable
@lombok.EqualsAndHashCode
public final class TimePeriod implements ITimePeriod {

    public static TimePeriod of(LocalDateTime start, LocalDateTime end) {
        if (end.isAfter(start)) {
            return new TimePeriod(start, end);
        } else {
            throw new IllegalArgumentException("TimePeriod: end before start");
        }
    }

    private final LocalDateTime start, end;

    private TimePeriod(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public boolean contains(LocalDateTime dt) {
        if (start.isAfter(dt)) {
            return false;
        }
        return end.isAfter(dt);
    }

    @Override
    public LocalDateTime start() {
        return start;
    }

    @Override
    public LocalDateTime end() {
        return end;
    }
}
