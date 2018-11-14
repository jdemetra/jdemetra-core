/*
* Copyright 2013 National Bank of Belgium
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
package demetra.timeseries.calendars;

import demetra.design.Development;
import demetra.timeseries.TsPeriod;
import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public interface HolidayInfo {

    /**
     * Day corresponding to the holiday
     * @return 
     */
    LocalDate getDay();

    /**
     * Day of week of the holiday
     * @return 
     */
    default DayOfWeek getDayOfWeek() {
        return getDay().getDayOfWeek();
    }
    
    /**
     * Returns the date equal or before the given date
     * @param date
     * @return 
     */
    static LocalDate getPreviousWorkingDate(LocalDate date){
        DayOfWeek dw=date.getDayOfWeek();
        if (dw== DayOfWeek.SUNDAY)
            return date.minusDays(1);
        else
            return date;
    }
    
    /**
     * Returns the date equal or after the given date
     * @param date
     * @return 
     */
    static LocalDate getNextWorkingDate(LocalDate date){
        DayOfWeek dw=date.getDayOfWeek();
        if (dw== DayOfWeek.SUNDAY)
            return date.plusDays(1);
        else
            return date;
    }

}
