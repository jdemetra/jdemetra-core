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
package ec.tstoolkit.timeseries;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import ec.tstoolkit.design.Immutable;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.NewObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Representation of a day.
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
public final class Day implements IPeriod, Comparable<Day> {
    // /////////////////////////////////////////////////////////////////////////

    /**
     * constant identifying the first Monday in 1970. 1/1/1970 is a Thursday (0
     * = Thursday, 3=Sunday)
     */
    static final int dayofweek_start = 3;
    /**
     * Number of days by month (if no leap year)
     */
    private static final int[] monthDays = {31, 28, 31, 30, 31, 30, 31, 31, 30,
        31, 30, 31};
    /**
     * Cumulative number of days (if no leap year). CumulatedMonthDays[2] =
     * number of days from 1/1 to 28/2.
     */
    private static final int[] cumulatedMonthDays = {0, 31, 59, 90, 120, 151,
        181, 212, 243, 273, 304, 334, 365};
    /**
     *
     */
    public static final Day BEG = new Day(1000, Month.January, 0);
    /**
     * Represents the last day supported by the framework. By convention, it is
     * the 31th of December 2999.
     */
    public static final Day END = new Day(2999, Month.December, 30);

    // util...
    public static int getMonthDays(int month) {
        return monthDays[month];
    }
    
    public static int getCumulatedMonthDays(int month) {
        return cumulatedMonthDays[month];
    }
    
    private static int calc(final Date date) {
        Calendar cal = CALENDAR_THREAD_LOCAL.get();
        cal.setTime(date);
        return calc(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH) - 1);
    }

    /**
     * Number of days from begin 1970. month and day are 0-base indexed.
     * 
     * @param year
     * @param month
     * @param day
     * @return
     */
    public static int calc(int year, final int month, final int day) {
        year = verify(year);
        boolean bLeapYear = verify(year, month, day);

        // make Jan 1, 1AD be 0
        int nDate = year * 365 + year / 4 - year / 100 + year / 400
                + cumulatedMonthDays[month] + day;

        // If leap year and it's before March, subtract 1:
        if ((month < 2) && bLeapYear) {
            --nDate;
        }
        return nDate - 719527; // number of days since 0
    }

    /**
     * Number of days from begin 1970. day is 0-base indexed.
     * 
     * @param year
     * @param ndays
     * @return
     */
    public static int calcDays(int year, final int ndays) {
        year = verify(year);
        if ((year < 0) || (year > 3000)) {
            throw new TsException(TsException.INVALID_YEAR);
        }

        if (year < 30) {
            year += 2000; // 29 == 2029
        }
        else if (year < 100) {
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
        }
        else {
            return rslt;
        }
    }

    /**
     * 
     * @param d0
     * @param d1
     * @return
     */
    public static int subtract(final Day d0, final Day d1) {
        return d0.day_ - d1.day_;
    }

    /**
     * Creates a new object that represents today
     * 
     * @return Today
     */
    public static Day toDay() {
        return new Day(new Date());
    }

    static int verify(int year) {
//        if ((year < 0) || (year > 3000)) {
//            throw new TsException(TsException.INVALID_YEAR);
//        }
        if (year < 30) {
            year += 2000; // 29 == 2029
        }
        else if (year < 100) {
            year += 1900; // 30 == 1930
        }
        return year;
    }

    /**
     * Verifies the validity of (year, month, day, where month, day are 0-base
     * indexed).
     * 
     * @return true if leap year, false otherwise.
     * @throws TSException
     */
    static boolean verify(int year, final int month, final int day) {
        year = verify(year);
        if ((month < 0) || (month > 11)) {
            throw new TsException(TsException.INVALID_MONTH);
        }
        // Check for leap year and set the number of days in the month
        boolean bLeapYear = ((year & 3) == 0)
                && (((year % 100) != 0) || ((year % 400) == 0));
        int nDaysInMonth = monthDays[month];
        if (bLeapYear && (day == 28) && (month == 1)) {
            ++nDaysInMonth;
        }
        if ((day < 0) || (day >= nDaysInMonth)) {
            throw new TsException(TsException.INVALID_DAY);
        }
        return bLeapYear;
    }

     // 0 = 1/1/70
    private final int day_;
    private static final long G_DAY0 = new GregorianCalendar(1970, Calendar.JANUARY, 1).getTimeInMillis();

    /**
     * Gets the number of days by month (0-base indexed).
     * 
     * @param year
     *            Considered year (meaningful only for February).
     * @param month
     *            Considered (0-based) month.
     * @return Number of days in the considered month
     */
    public static int getNumberOfDaysByMonth(final int year, final int month) {
        if ((month == 1) && isLeap(year)) {
            return 29;
        }
        return monthDays[month];
    }

    /**
     * true if year is leap
     * 
     * @param year
     * @return
     */
    public static boolean isLeap(final int year) {
        return (year % 4 == 0) && (((year % 100) != 0) || ((year % 400) == 0));
    }

    /**
     * 
     * @param d
     */
    public Day(final Date d) {
        day_ = calc(d);
    }

    /**
     * 
     * @param day
     */
    Day(final int day) {
        day_ = day;
    }

    /**
     * 
     * @param year Year
     * @param month Month (integer value of month is 0-based
     * @param day 0-based day
     */
    public Day(final int year, final Month month, final int day) {
        day_ = calc(year, month.intValue(), day);
    }

    @Override
    public int compareTo(final Day o) {
        return Integer.compare(day_, o.day_);
    }

    /**
     * 
     * @param dt
     * @return
     */
    @Override
    public boolean contains(final Date dt) {
        return day_ == calc(dt);
    }

    /**
     * 
     * @param d1
     * @return
     */
    public int difference(final Day d1) {
        return day_ - d1.day_;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof Day && equals((Day) obj));
    }
    
    private boolean equals(Day other) {
        return day_ == other.day_;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + this.day_;
        return hash;
    }

    /**
     * 
     * @return
     */
    @Override
    public Day firstday() {
        return this;
    }

    /**
     * 
     * @return
     */
    public DayOfWeek getDayOfWeek() {
        int d = (day_ - dayofweek_start) % 7;
        if (d < 0) {
            d += 7;
        }
        return DayOfWeek.valueOf(d);
    }

    /**
     * Gets the identifier of the day.
     * 
     * @return The identifier of the day, which is the number of days after (or
     *         before if id is negative) the first of January 1970 (id of
     *         1/1/1970 equals 0).
     */
    public int getId() {
        return day_;
    }

    /**
     * 
     * @return
     */
    public int getMonth() {
        return toInternalCalendar().get(Calendar.MONTH);
    }

    /**
     * 
     * @return
     */
    public int getYear() {
        return toInternalCalendar().get(Calendar.YEAR);
    }

    /**
     * Is this day strictly after a given day?
     * 
     * @param d1
     * @return this > d1
     */
    public boolean isAfter(final Day d1) {
        return day_ > d1.day_;
    }

    /**
     * Is this day strictly before a given day?
     * 
     * @param d1
     * @return this < d1
     */
    public boolean isBefore(final Day d1) {
        return day_ < d1.day_;
    }

    /**
     * Is this day equal or before a given day?
     * 
     * @param d1
     * @return this <= d1
     */
    public boolean isNotAfter(final Day d1) {
        return day_ <= d1.day_;
    }

    /**
     * Is this day equal or after a given day?
     * 
     * @param d1
     * @return this >= d1
     */
    public boolean isNotBefore(final Day d1) {
        return day_ >= d1.day_;
    }

    /**
     * 
     * @return
     */
    public boolean isWorkingDay() {
        int d = (day_ - dayofweek_start) % 7;
        if (d < 0) {
            d += 7;
        }
        return (d != 5) && (d != 6);
    }

    @Override
    public Day lastday() {
        return this;
    }

    /**
     * 
     * @param ndays
     * @return
     */
    public Day minus(final int ndays) {
        return new Day(day_ - ndays);
    }

    /**
     * 
     * @param ndays
     * @return
     */
    public Day plus(final int ndays) {
        return new Day(day_ + ndays);
    }

    /**
     * Returns a <code>Date</code> object representing this
     * <code>Day</code>'s time value (millisecond offset from the <a
     * href="#Epoch">Epoch</a>").
     *
     * @return a <code>Date</code> representing the time value.
    */    
    @NewObject
    public Date getTime() {
        return new Date(getTimeInMillis());
    }

    public long getTimeInMillis() {
        return toInternalCalendar().getTimeInMillis();
    }
    
    /**
     * Returns a <code>GregorianCalendar</code> object set to this
     * <code>Day</code>'s time value (millisecond offset from the <a
     * href="#Epoch">Epoch</a>").
     * 
     * @return a new <code>GregorianCalendar</code> set to the time value.
     * @see #getTime() 
     */
    @NewObject
    public GregorianCalendar toCalendar() {
        // result might be used in another thread => cannot use CALENDAR_THREAD_LOCAL
        return initCalendar(new GregorianCalendar());
    }

    private GregorianCalendar toInternalCalendar() {
        return initCalendar(CALENDAR_THREAD_LOCAL.get());
    }

    private GregorianCalendar initCalendar(GregorianCalendar cal) {
        cal.setTimeInMillis(G_DAY0);
        cal.add(Calendar.DAY_OF_MONTH, day_);
        return cal;
    }
    
    @Override
    public String toString() {
        return DATE_FORMAT_THREAD_LOCAL.get().format(toInternalCalendar().getTime());
    }
    
    public static Day fromString(String s) throws ParseException{
        return new Day(DATE_FORMAT_THREAD_LOCAL.get().parse(s));
    }

    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT_THREAD_LOCAL = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };
    /**
     * Calendar.getInstance() creates a new instance of GregorianCalendar and its 
     * constructor triggers a lot of internal synchronized code.
     * => We use ThreadLocal to avoid this overhead
     */
    private static final ThreadLocal<GregorianCalendar> CALENDAR_THREAD_LOCAL = new ThreadLocal<GregorianCalendar>() {
        @Override
        protected GregorianCalendar initialValue() {
            return new GregorianCalendar();
        }
    };

}
