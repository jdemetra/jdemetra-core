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
import demetra.timeseries.calendars.Holiday;
import demetra.timeseries.calendars.HolidayInfo;
import java.time.DayOfWeek;
import java.time.LocalDate;
import static java.time.temporal.ChronoUnit.DAYS;
import java.util.Iterator;
import jdplus.math.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.experimental.UtilityClass
public class HolidaysUtility {

    
    public void fillDays(Holiday[] holidays, final Matrix D, final LocalDate start, final boolean skipSundays) {
        LocalDate end = start.plusDays(D.getRowsCount());
        int col = 0;
        for (Holiday item : holidays) {
            Iterator<HolidayInfo> iter = item.getDay().getIterable(start, end).iterator();
            while (iter.hasNext()) {
                LocalDate date = iter.next().getDay();
                if (!skipSundays || date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                    long pos = start.until(date, DAYS);
                    D.set((int) pos, col, 1);
                }
            }
            if (D.getColumnsCount() > 1) {
                ++col;
            }
        }
    }

    public void fillPreviousWorkingDays(Holiday[] holidays, final Matrix D, final LocalDate start, final int del) {
        int n = D.getRowsCount();
        LocalDate nstart = start.plusDays(del);
        LocalDate end = start.plusDays(n);
        int col = 0;
        for (Holiday item : holidays) {
            Iterator<HolidayInfo> iter = item.getDay().getIterable(nstart, end).iterator();
            while (iter.hasNext()) {
                LocalDate date = iter.next().getDay().minusDays(del);
                date = HolidayInfo.getPreviousWorkingDate(date);
                long pos = start.until(date, DAYS);
                if (pos >= 0 && pos < n) {
                    D.set((int) pos, col, 1);
                }
            }
            if (D.getColumnsCount() > 1) {
                ++col;
            }
        }
    }

    public void fillNextWorkingDays(Holiday[] holidays, final Matrix D, final LocalDate start, final int del) {
        int n = D.getRowsCount();
        LocalDate nstart = start.minusDays(del);
        LocalDate end = nstart.plusDays(n);
        int col = 0;
        for (Holiday item : holidays) {
            Iterator<HolidayInfo> iter = item.getDay().getIterable(nstart, end).iterator();
            while (iter.hasNext()) {
                LocalDate date = iter.next().getDay().plusDays(del);
                date = HolidayInfo.getNextWorkingDate(date);
                long pos = start.until(date, DAYS);
                if (pos >= 0 && pos < n) {
                    D.set((int) pos, col, 1);
                }
            }
            if (D.getColumnsCount() > 1) {
                ++col;
            }
        }
    }
}
