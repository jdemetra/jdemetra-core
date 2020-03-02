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

import demetra.design.Development;
import demetra.timeseries.calendars.EasterRelatedDay;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.AbstractList;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.Value
class EasterDayInfo implements HolidayInfo {

    final LocalDate day;

    EasterDayInfo(int year, int offset, boolean julian) {
        LocalDate easter = EasterRelatedDay.easter(year, julian);
        day = easter.plusDays(offset);
    }

    @Override
    public LocalDate getDay() {
        return day;
    }

    @Override
    public DayOfWeek getDayOfWeek() {
        return day.getDayOfWeek();
    }

    static class EasterDayList extends AbstractList<HolidayInfo> {

        private final int startyear, n, offset;
        private final boolean julian;

        public EasterDayList(int offset, boolean julian, LocalDate fstart, LocalDate fend) {
            this.offset = offset;
            this.julian = julian;
            int ystart = fstart.getYear(), yend = fend.getYear();
            LocalDate xday = EasterRelatedDay.easter(ystart, julian).plusDays(offset);
            LocalDate yday = EasterRelatedDay.easter(yend, julian).plusDays(offset);

            if (xday.isBefore(fstart)) {
                ++ystart;
            }

            // pstart is the last valid period
            if (yday.isBefore(fend)) {
                ++yend;
            }

            n = yend - ystart;
            startyear = ystart;
        }

        @Override
        public HolidayInfo get(int index) {
            return new EasterDayInfo(startyear + index, offset, julian);
        }

        @Override
        public int size() {
            return n;
        }
    }

}
