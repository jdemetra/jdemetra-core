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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value(staticConstructor = "of")
public class Days implements TimeSeriesDomain<Day> {

    @lombok.NonNull
    LocalDate[] days;

    @Override
    public int length() {
        return days.length;
    }

    @Override
    public Day get(int index) throws IndexOutOfBoundsException {
        return Day.of(days[index]);
    }

    @Override
    public LocalDateTime start() {
        return days[0].atStartOfDay();
    }

    @Override
    public LocalDateTime end() {
        return days[days.length - 1].plusDays(1).atStartOfDay();
    }

    @Override
    public boolean contains(LocalDateTime date) {
        LocalDate day = date.toLocalDate();
        return Arrays.binarySearch(days, day) >= 0;
    }

    @Override
    public int indexOf(LocalDateTime date) {
        LocalDate day = date.toLocalDate();
        return Arrays.binarySearch(days, day);
    }

    public static Days of(List<LocalDate> values) {
        return of(values.toArray(new LocalDate[values.size()]));
    }

    @Override
    public int indexOf(Day point) {
        return Arrays.binarySearch(days, point.getDay());
    }

    @Override
    public boolean contains(Day period) {
        return Arrays.binarySearch(days, period.getDay()) >= 0;
    }

    @Override
    public Iterator<Day> iterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TimeSeriesDomain<Day> select(TimeSelector selector) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
