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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Iterator;

/**
 *
 * @author Jean Palate
 */
@lombok.Value(staticConstructor = "of")
public class CalendarPeriods implements TimeSeriesDomain<CalendarPeriod> {

    @lombok.NonNull
    CalendarPeriod[] periods;

    @Override
    public int length() {
        return periods.length;
    }

    @Override
    public CalendarPeriod get(int index) throws IndexOutOfBoundsException {
        return periods[index];
    }

    @Override
    public LocalDateTime start() {
        return periods[0].start();
    }

    @Override
    public LocalDateTime end() {
        return periods[periods.length - 1].end();
    }

    @Override
    public boolean contains(LocalDateTime date) {
        int pos = 0;
        while (pos < periods.length && !periods[pos].start().isAfter(date)) {
            if (date.isBefore(periods[pos].end())) {
                return true;
            } else {
                ++pos;
            }
        }
        return false;
    }

    @Override
    public int indexOf(LocalDateTime date) {
        int pos = 0;
        while (pos < periods.length && !periods[pos].start().isAfter(date)) {
            if (date.isBefore(periods[pos].end())) {
                return pos;
            } else {
                ++pos;
            }
        }
        return -pos-1;
    }

    @Override
    public int indexOf(CalendarPeriod period) {
        return Arrays.binarySearch(periods, period);
    }

    @Override
    public boolean contains(CalendarPeriod period) {
        return Arrays.binarySearch(periods, period) >= 0;
    }

    @Override
    public Iterator<CalendarPeriod> iterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TimeSeriesDomain<CalendarPeriod> select(TimeSelector selector) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
