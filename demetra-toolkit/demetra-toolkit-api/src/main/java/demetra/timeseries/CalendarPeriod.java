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

import demetra.data.Range;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *
 * @author Jean Palate
 */
@lombok.Value(staticConstructor = "of")
public class CalendarPeriod implements Range<LocalDateTime>, Comparable<CalendarPeriod> {

    @lombok.NonNull
    LocalDate start, end;

    @Override
    public LocalDateTime start() {
        return start.atStartOfDay();
    }

    @Override
    public LocalDateTime end() {
        return end.atStartOfDay();
    }

    @Override
    public boolean contains(LocalDateTime element) {
        return element.isBefore(end.atStartOfDay()) && (! element.isBefore(start.atStartOfDay()));
    }

    @Override
    public int compareTo(CalendarPeriod t) {
        if (start.equals(t.start) && end.isAfter(t.end))
            return 0;
        if (! end.isAfter(t.start))
            return -1;
        if (! t.end.isAfter(start))
            return 1;
        throw new TsException(TsException.INCOMPATIBLE_PERIOD);
    }
}
