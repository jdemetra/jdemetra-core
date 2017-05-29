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

import demetra.design.Development;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

/**
 * interface that defines a date period. The atomic time unit for a IDatePeriod
 * is the day.
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface IDatePeriod extends ITimePeriod {

    /**
     * Checks that the period contains a given date
     *
     * @param dt The considered date.
     * @return true if the date is inside the period, false otherwise.
     */
    default boolean contains(LocalDate dt) {
        LocalDate start = firstDay();
        if (dt.isEqual(start)) {
            return true;
        }
        LocalDate last = lastDay();
        return dt.isAfter(start) && !dt.isAfter(last);
    }

    /**
     * Gets the first day of the period
     *
     * @return The first day of the period.
     */
    LocalDate firstDay();

    /**
     * Gets the last day of the period
     *
     * @return The last day of the period
     */
    LocalDate lastDay();

    @Override
    default LocalDateTime start() {
        return firstDay().atStartOfDay();
    }

    @Override
    default LocalDateTime end() {
        return lastDay().plusDays(1).atStartOfDay();
    }

    @Override
    default boolean contains(LocalDateTime time) {
        return contains(time.toLocalDate());
    }

    default Period length() {
        return firstDay().until(lastDay());
    }
}
