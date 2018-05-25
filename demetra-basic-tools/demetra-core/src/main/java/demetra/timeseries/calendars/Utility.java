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
import internal.timeseries.InternalFixme;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsException;
import demetra.timeseries.TsUnit;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.Holiday;
import demetra.timeseries.calendars.Holidays;
import demetra.timeseries.calendars.IHolidayInfo;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import static java.time.temporal.ChronoUnit.DAYS;
import java.util.Iterator;
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.experimental.UtilityClass
public class Utility {

    public void fillDays(Holidays hl, final Matrix D, final LocalDate start, int n) {
        LocalDate end = start.plusDays(n);
        int col = 0;
        for (Holiday item : hl.elements()) {
            Iterator<IHolidayInfo> iter = item.getDay().getIterable(TsUnit.DAY, start, end).iterator();
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

    public void fillPreviousWorkingDays(Holidays hl, final Matrix D, final LocalDate start, int n, final int del) {
        LocalDate nstart = start.plusDays(del);
        LocalDate end = start.plusDays(n);
        int col = 0;
        for (Holiday item : hl.elements()) {
            Iterator<IHolidayInfo> iter = item.getDay().getIterable(TsUnit.DAY, nstart, end).iterator();
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

    public void fillNextWorkingDays(Holidays hl, final Matrix D, final LocalDate start, int n, final int del) {
        LocalDate nstart = start.minusDays(del);
        LocalDate end = nstart.plusDays(n);
        int col = 0;
        for (Holiday item : hl.elements()) {
            Iterator<IHolidayInfo> iter = item.getDay().getIterable(TsUnit.DAY, nstart, end).iterator();
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

    /**
     *
     * @param domain
     * @return
     */
    public int[] daysCount(@Nonnull TsDomain domain) {
        int n = domain.length();
        int[] rslt = new int[n];
        LocalDate[] start = new LocalDate[n + 1]; // id of the first day for each period
        for (int i = 0; i < n; ++i) {
            start[i] = domain.get(i).start().toLocalDate();
        }
        start[n]=domain.getEndPeriod().start().toLocalDate();
        for (int i = 0; i < n; ++i) {
            // int dw0 = (start[i] - 4) % 7;
            int ni = (int) start[i].until(start[i + 1], ChronoUnit.DAYS);
            rslt[i] = ni;
        }
        return rslt;
    }

//    /**
//     *
//     * @param domain
//     * @param day
//     * @return
//     */
//    public static int[] daysCount(TsDomain domain, DayOfWeek day) {
//        int n = domain.getLength();
//        int[] rslt = new int[n];
//        int[] start = new int[n + 1]; // id of the first day for each period
//        TsPeriod d0 = domain.getStart();
//        int conv = 12 / d0.getFrequency().intValue();
//        TsPeriod month = new TsPeriod(TsFrequency.Monthly);
//        month.set(d0.getYear(), d0.getPosition() * conv);
//        for (int i = 0; i < start.length; ++i) {
//            start[i] = Day.calc(month.getYear(), month.getPosition(), 0);
//            month.move(conv);
//        }
//
//        for (int i = 0; i < n; ++i) {
//            int dw0 = (start[i] - 4) % 7;
//            int ni = start[i + 1] - start[i];
//            if (dw0 < 0) {
//                dw0 += 7;
//            }
//            int j = day.intValue();
//            int j0 = j - dw0;
//            if (j0 < 0) {
//                j0 += 7;
//            }
//            rslt[i] = 1 + (ni - 1 - j0) / 7;
//        }
//        return rslt;
//    }
//
    /**
     * Return the first Day in the given month of the given year which is a
     * specified day of week
     *
     * @param day Day of week
     * @param year
     * @param month
     * @return
     */
    LocalDate firstWeekDay(DayOfWeek day, int year, int month) {
        TsPeriod m = TsPeriod.monthly(year, month - 1);
        LocalDate start = m.start().toLocalDate();
        int iday = day.getValue();
        int istart = start.getDayOfWeek().getValue();
        int n = iday - istart;
        if (n < 0) {
            n += 7;
        }
        if (n != 0) {
            start = start.plusDays(n);
        }
        return start;
    }

//    /**
//     * monday=0, ..., sunday=6.
//     *
//     * @param domain
//     * @return
//     */
//    public static int[] lastDay(TsDomain domain) {
//        int n = domain.length();
//        int[] rslt = new int[n];
//
//        TsPeriod d1 = domain.getStart();
//        d1.move(1);
//        int conv = 12 / d1.getFrequency().intValue();
//        TsPeriod month = new TsPeriod(TsFrequency.Monthly);
//        month.set(d1.getYear(), d1.getPosition() * conv);
//        // rslt contains the id of the last day of each period
//        for (int i = 0; i < rslt.length; ++i) {
//            int id = Day.calc(month.getYear(), month.getPosition(), 0);
//            id = (id - 5) % 7;
//            if (id < 0) {
//                id += 7;
//            }
//            rslt[i] = id;
//            month.move(conv);
//        }
//        return rslt;
//    }
//
    int calc(int year, final int month, final int day) {

        boolean bLeapYear = isLeap(year);

        // make Jan 1, 1AD be 0
        int nDate = year * 365 + year / 4 - year / 100 + year / 400
                + getCumulatedMonthDays(month - 1) + day;

        // If leap year and it's before March, subtract 1:
        if ((month < 3) && bLeapYear) {
            --nDate;
        }
        return nDate - 719528; // number of days since 0
    }

    /**
     * Number of days from begin 1970.
     *
     * @param year
     * @param ndays
     * @return
     */
    int calcDays(int year, final int ndays) {
        if ((year < 0) || (year > 3000)) {
            throw new TsException(TsException.INVALID_YEAR);
        }

        if (year < 30) {
            year += 2000; // 29 == 2029
        } else if (year < 100) {
            year += 1900; // 30 == 1930
        }
        boolean bLeapYear = isLeap(year);
        int np = bLeapYear ? 366 : 365;

        if ((ndays < 0) || (ndays >= np)) {
            throw new TsException(TsException.INVALID_DAY);
        }

        // make Jan 1, 1AD be 0
        int rslt = year * 365 + year / 4 - year / 100 + year / 400 + ndays
                - 719527;
        // correction for leap year
        if (bLeapYear) {
            return rslt - 1;
        } else {
            return rslt;
        }
    }

    /**
     * true if year is leap
     *
     * @param year
     * @return
     */
    public boolean isLeap(final int year) {
        return (year % 4 == 0) && (((year % 100) != 0) || ((year % 400) == 0));
    }

    /**
     * Returns the number of days for the month before or equal to the given
     * month. We consider that there are 28 days in February
     *
     * @param month 1-based index of the month
     * @return
     */
    public static int getCumulatedMonthDays(int month) {
        return CUMULATEDMONTHDAYS[month];
    }

    /**
     * Gets the number of days by month (1-based indexed).
     *
     * @param year Considered year (meaningful only for February).
     * @param month Considered (1-based) month.
     * @return Number of days in the considered month
     */
    public static int getNumberOfDaysByMonth(final int year, final int month) {
        if ((month == 2) && isLeap(year)) {
            return 29;
        }
        return MONTHDAYS[month-1];
    }

    /**
     * Number of days by month (if no leap year)
     */
    private static final int[] MONTHDAYS = {31, 28, 31, 30, 31, 30, 31, 31, 30,
        31, 30, 31};
   /**
     * Cumulative number of days (if no leap year). CumulatedMonthDays[2] =
     * number of days from 1/1 to 28/2.
     */
    private static final int[] CUMULATEDMONTHDAYS = {0, 31, 59, 90, 120, 151,
        181, 212, 243, 273, 304, 334, 365};
}
