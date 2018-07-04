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
import demetra.maths.matrices.Matrix;
import demetra.util.Comparator;
import java.time.DayOfWeek;
import java.time.LocalDate;
import static java.time.temporal.ChronoUnit.DAYS;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class Holidays {

    private final List<Holiday> holidays = new ArrayList<>();
    private final IHoliday.Context context;

    public Holidays() {
        this(true, false);
    }

    public Holidays(boolean mean, boolean julianeaster) {
        context = new IHoliday.Context(mean, julianeaster);
    }

    public IHoliday.Context getContext() {
        return context;
    }

    public boolean add(IHoliday fday) {
        if (fday.match(context)) {
            Holiday ev = new Holiday(fday);
            if (holidays.contains(ev)) {
                return false;
            } else {
                holidays.add(ev);
                return true;
            }
        } else {
            return false;
        }
    }

    public int getCount() {
        return holidays.size();
    }

    public boolean add(Holiday sd) {
        if (!sd.getDay().match(context)) {
            return false;
        }
        for (Holiday ev : holidays) {
            if (ev.equals(sd)) {
                return false;
            }
        }
        holidays.add(sd);
        return true;
    }

    public void addAll(Collection<Holiday> c) {
        for (Holiday nev : c) {
            if (!nev.getDay().match(context)) {
                continue;
            }
            boolean used = false;
            for (Holiday ev : holidays) {
                if (ev.equals(nev)) {
                    used = true;
                    break;
                }
            }
            if (!used) {
                holidays.add(nev);
            }
        }
    }

    public Holiday get(int idx) {
        return holidays.get(idx);
    }

    public void clear() {
        holidays.clear();
    }

    public Holiday[] toArray() {
        return holidays.toArray(new Holiday[holidays.size()]);
    }

    public Collection<Holiday> elements() {
        return Collections.unmodifiableList(holidays);
    }

    public boolean contentEquals(Holidays other) {
        return Objects.deepEquals(context, other.context) && Comparator.equals(holidays, other.holidays);
    }
    
    public void fillDays(final Matrix D, final LocalDate start) {
        LocalDate end = start.plusDays(D.getRowsCount());
        int col = 0;
        for (Holiday item : elements()) {
            Iterator<IHolidayInfo> iter = item.getDay().getIterable(start, end).iterator();
            while (iter.hasNext()) {
                LocalDate date = iter.next().getDay();
                if (date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                    long pos = start.until(date, DAYS);
                    D.set((int) pos, col, 1);
                }
            }
            if (D.getColumnsCount() > 1) {
                ++col;
            }
        }
    }

    public void fillPreviousWorkingDays(final Matrix D, final LocalDate start, final int del) {
        int n=D.getRowsCount();
        LocalDate nstart = start.plusDays(del);
        LocalDate end = start.plusDays(n);
        int col = 0;
        for (Holiday item : elements()) {
            Iterator<IHolidayInfo> iter = item.getDay().getIterable(nstart, end).iterator();
            while (iter.hasNext()) {
                LocalDate date = iter.next().getDay().minusDays(del);
                date = IHolidayInfo.getPreviousWorkingDate(date);
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

    public void fillNextWorkingDays(final Matrix D, final LocalDate start, final int del) {
        int n=D.getRowsCount();
        LocalDate nstart = start.minusDays(del);
        LocalDate end = nstart.plusDays(n);
        int col = 0;
        for (Holiday item : elements()) {
            Iterator<IHolidayInfo> iter = item.getDay().getIterable(nstart, end).iterator();
            while (iter.hasNext()) {
                LocalDate date = iter.next().getDay().plusDays(del);
                date = IHolidayInfo.getNextWorkingDate(date);
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
