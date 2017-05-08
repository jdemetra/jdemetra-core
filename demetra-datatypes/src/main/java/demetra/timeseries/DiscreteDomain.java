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
import java.time.Period;
import java.util.Arrays;

/**
 *
 * @author Jean Palate
 */
@Immutable
@lombok.EqualsAndHashCode
public final class DiscreteDomain implements ITimeDomain<TimePoint> {

    private final LocalDateTime[] dates;

    public static DiscreteDomain of(LocalDateTime[] dates) {
        LocalDateTime[] ndates = dates.clone();
        Arrays.parallelSort(ndates);
        return new DiscreteDomain(ndates);
    }

    private DiscreteDomain(LocalDateTime[] dates) {
        this.dates = dates;
    }

    @Override
    public int length() {
        return dates.length;
    }

    @Override
    public TimePoint get(int index) {
        return TimePoint.of(dates[index]);
    }

    @Override
    public int search(LocalDateTime time) {
        return Arrays.binarySearch(dates, time);
    }

    @Override
    public Period toPeriod() {
        return null;
    }

    @Override
    public boolean isContinuous() {
        return false;
    }
}
