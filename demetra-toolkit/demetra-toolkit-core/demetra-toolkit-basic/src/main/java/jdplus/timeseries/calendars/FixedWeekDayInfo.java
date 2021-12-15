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
package jdplus.timeseries.calendars;

import nbbrd.design.Development;
import demetra.timeseries.calendars.FixedWeekDay;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Iterator;

/**
 * TODO: move to basic
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
class FixedWeekDayInfo implements HolidayInfo {

    final int year;
    final FixedWeekDay fday;

    FixedWeekDayInfo(int year, FixedWeekDay fday) {
        this.fday = fday;
        this.year = year;
    }

    @Override
    public LocalDate getDay() {
        return fday.calcDate(year);
    }

    @Override
    public DayOfWeek getDayOfWeek() {
        return fday.getDayOfWeek();
    }

    static class FixedWeekDayIterable implements Iterable<HolidayInfo> {

        private final FixedWeekDay fday;
        private final int year;
        private final int n;

        FixedWeekDayIterable(FixedWeekDay fday, LocalDate fstart, LocalDate fend) {
            this.fday = fday;
            int ystart = fstart.getYear(), yend = fend.getYear();
            LocalDate xday = fday.calcDate(ystart);
            LocalDate yday = fday.calcDate(yend);

            if (xday.isBefore(fstart)) {
                ++ystart;
            }
            if (!yday.isBefore(fend)) {
                --yend;
            }
            year = ystart;
            n = yend - ystart + 1;
        }

        @Override
        public Iterator<HolidayInfo> iterator() {
            return new Iterator<HolidayInfo>() {
                int cur = 0;

                @Override
                public boolean hasNext() {
                    return cur < n;
                }

                @Override
                public HolidayInfo next() {
                    return new FixedWeekDayInfo(year + (cur++), fday);
                }
            };
        }
    }
}
