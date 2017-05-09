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

import demetra.data.Range;
import demetra.design.Development;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * interface that defines a time period. Time periods are defined by a starting
 * datetime and an ending datetime. By convention, the start belongs to the
 * period and the end doesn't belong to the period, except for point periods. A
 * point in the time is defined by a period with the same start and end
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface ITimePeriod extends Range<LocalDateTime> {

    default boolean isPoint() {
        return start().isEqual(end());
    }

    /**
     * Checks that the period contains a given date
     *
     * @param dt The considered date.
     * @return true if the date is inside the period, false otherwise.
     */
    @Override
    default boolean contains(LocalDateTime dt) {
        LocalDateTime start = start();
        if (dt.isEqual(start)) {
            return true;
        }
        LocalDateTime end = end();
        return dt.isAfter(start) && dt.isBefore(end);
    }

    default Duration duration() {
        return Duration.between(start(), end());
    }
}
