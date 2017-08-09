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
public final class ContinuousDomain implements ITimeDomain<TimePeriod> {

    public static ContinuousDomain make(LocalDateTime[] dates) {
        LocalDateTime[] ndates = dates.clone();
        Arrays.parallelSort(ndates);
        return new ContinuousDomain(ndates);
    }

    private final LocalDateTime[] dates;

    private ContinuousDomain(LocalDateTime[] dates) {
        this.dates = dates;
    }

    @Override
    public int length() {
        return dates.length - 1;
    }

    @Override
    public TimePeriod get(int index) {
        return TimePeriod.of(dates[index], dates[index + 1]);
    }

    @Override
    public int search(LocalDateTime time) {
        int pos = Arrays.binarySearch(dates, time);
        if (pos >= 0) {
            if (pos < dates.length - 1) {
                return pos;
            } else {
                return -dates.length;
            }
        } else if (pos == -1) // before
        {
            return -1;
        } else if (pos == -dates.length - 1) //after
        {
            return -dates.length;
        } else {
            return -pos - 2;
        }
    }

    @Override
    public Period getPeriod() {
        return null;
    }

    @Override
    public boolean isContinuous() {
        return true;
    }

    @Override
    public ContinuousDomain range(int first, int last) {
        if (first >= dates.length - 1 || last <= first) {
            return new ContinuousDomain(new LocalDateTime[]{dates[0], dates[0]});
        }
        return new ContinuousDomain(Arrays.copyOfRange(dates, first, last - first + 1));
    }

}
