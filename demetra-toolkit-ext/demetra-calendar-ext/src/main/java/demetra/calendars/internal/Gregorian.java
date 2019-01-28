package demetra.calendars.internal;

import static demetra.calendars.internal.DateUtility.*;

@lombok.Value
public class Gregorian implements Date {

    public long year;
    public int month;
    public int day;

    //
    // constants
    //
    /*- gregorian-epoch -*/
    // TYPE fixed-date
    // Fixed date of start of the (proleptic) Gregorian
    // calendar.
    public static final long EPOCH = 1;

    //
    // date conversion methods
    //
    /*- fixed-from-gregorian -*/
    // TYPE gregorian-date -> fixed-date
    // Fixed date equivalent to the Gregorian date.
    public static long toFixed(long year, int month, int day) {
        return EPOCH - 1
                + 365 * (year - 1)
                + quotient(year - 1, 4)
                - quotient(year - 1, 100)
                + quotient(year - 1, 400)
                + quotient(367 * month - 362, 12)
                + (month <= 2 ? 0 : (isLeapYear(year) ? -1 : -2))
                + day;
    }

    @Override
    public long toFixed() {
        return toFixed(year, month, day);
    }

    /*- gregorian-from-fixed -*/
    // TYPE fixed-date -> gregorian-date
    // Gregorian (year month day) corresponding to fixed $date$.
    public static Gregorian fromFixed(long date) {
        long year = yearFromFixed(date);
        long priorDays = date - toFixed(year, JANUARY, 1);
        int correction = date < toFixed(year, MARCH, 1) ? 0
                : (isLeapYear(year) ? 1 : 2);
        int month = (int) quotient(12 * (priorDays + correction) + 373, 367);
        int day = (int) (date - toFixed(year, month, 1) + 1);
        return new Gregorian(year, month, day);
    }


    /*- alt-fixed-from-gregorian -*/
    // TYPE gregorian-date -> fixed-date
    // Alternative calculation of fixed date equivalent to the
    // Gregorian date.
    public static long altFixedFromGregorian(long year, int month, int day) {
        long m = adjustedMod(month - 2, 12);
        long y = year + quotient(month + 9, 12);
        return EPOCH - 1
                - 306
                + 365 * (y - 1)
                + quotient(y - 1, 4)
                - quotient(y - 1, 100)
                + quotient(y - 1, 400)
                + quotient(3 * m - 1, 5)
                + 30 * (m - 1)
                + day;
    }


    /*- alt-gregorian-from-fixed -*/
    // TYPE fixed-date -> gregorian-date
    // Alternative calculation of Gregorian (year month day)
    // corresponding to fixed $date$.
    public static Gregorian altGregorianFromFixed(long date) {
        long y = yearFromFixed((EPOCH - 1) + date + 306);
        long priorDays = date - toFixed(y - 1, 3, 1);
        int month = (int) adjustedMod(quotient(5 * priorDays + 155, 153) + 2, 12);
        long year = y - quotient(month + 9, 12);
        int day = (int) (1 + date - toFixed(year, month, 1));
        return new Gregorian(year, month, day);
    }

    //
    // support methods
    //
    /*- gregorian-leap-year? -*/
    // TYPE gregorian-year -> boolean
    // True if $g-year$ is a leap year on the Gregorian
    // calendar.
    public static boolean isLeapYear(long gYear) {
        boolean result = false;

        if (mod(gYear, 4) == 0) {
            long n = mod(gYear, 400);
            if (n != 100 && n != 200 && n != 300) {
                result = true;
            }
        }

        return result;
    }


    /*- gregorian-year-from-fixed -*/
    // TYPE fixed-date -> gregorian-year
    // Gregorian year corresponding to the fixed $date$.
    public static long yearFromFixed(long date) {
        long l0 = date - EPOCH;
        long n400 = quotient(l0, 146097);
        long d1 = mod(l0, 146097);
        long n100 = quotient(d1, 36524);
        long d2 = mod(d1, 36524);
        long n4 = quotient(d2, 1461);
        long d3 = mod(d2, 1461);
        long n1 = quotient(d3, 365);
        long year = 400 * n400 + 100 * n100 + 4 * n4 + n1;
        return n100 == 4 || n1 == 4 ? year : year + 1;
    }


    /*- alt-gregorian-year-from-fixed -*/
    // TYPE fixed-date -> gregorian-year
    // Gregorian year corresponding to the fixed $date$.
    public static long altGregorianYearFromFixed(long date) {
        long approx = quotient(date - EPOCH + 2, 146097.0 / 400.0);
        long start = EPOCH
                + 365 * approx
                + quotient(approx, 4)
                - quotient(approx, 100)
                + quotient(approx, 400);
        return date < start ? approx : approx + 1;
    }

    //
    // auxiliary methods
    //
    // Last day of Gregorian month
    public int lastDayOfMonth() {

        switch (month) {
            case FEBRUARY:
                if (isLeapYear(year)) {
                    return 29;
                } else {
                    return 28;
                }
            case APRIL:
            case JUNE:
            case SEPTEMBER:
            case NOVEMBER:
                return 30;
            default:
                return 31;
        }
    }

    /*- day-number -*/
    // TYPE gregorian-date -> positive-integer
    // Day number in year of Gregorian date $g-date$.
    public long dayNumber() {
        return difference(toFixed(year - 1, DECEMBER, 31), toFixed());
    }


    /*- days-remaining -*/
    // TYPE gregorian-date -> non-negative-integer
    // Days remaining in year after Gregorian date $g-date$.
    public long daysRemaining() {
        return difference(toFixed(), toFixed(year, DECEMBER, 31));
    }


    /*- independence-day -*/
    // TYPE gregorian-year -> fixed-date
    // Fixed date of United States Independence Day in
    // Gregorian year.
    public static long independenceDay(long gYear) {
        return toFixed(gYear, JULY, 4);
    }


    /*- labor-day -*/
    // TYPE gregorian-year -> fixed-date
    // Fixed date of United States Labor Day in Gregorian
    // year--the first Monday in September.
    public static long laborDay(long gYear) {
        return firstKDay(MONDAY, toFixed(gYear, SEPTEMBER, 1));
    }


    /*- memorial-day -*/
    // TYPE gregorian-year -> fixed-date
    // Fixed date of United States Memorial Day in Gregorian
    // year--the last Monday in May.
    public static long memorialDay(long gYear) {
        return lastKDay(MONDAY, toFixed(gYear, MAY, 31));
    }


    /*- election-day -*/
    // TYPE gregorian-year -> fixed-date
    // Fixed date of United States Election Day in Gregorian
    // year--the Tuesday after the first Monday in November.
    public static long electionDay(long gYear) {
        return firstKDay(TUESDAY, toFixed(gYear, NOVEMBER, 2));
    }


    /*- daylight-saving-start -*/
    // TYPE gregorian-year -> fixed-date
    // Fixed date of the start of United States daylight saving
    // time in Gregorian year--the first Sunday in April.
    public static long daylightSavingStart(long gYear) {
        return firstKDay(SUNDAY, toFixed(gYear, APRIL, 1));
    }

    /*- daylight-saving-end -*/
    // TYPE gregorian-year -> fixed-date
    // Fixed date of the end of United States daylight saving
    // time in Gregorian year--the last Sunday in October.
    public static long daylightSavingEnd(long gYear) {
        return lastKDay(SUNDAY, toFixed(gYear, OCTOBER, 31));
    }


    /*- christmas -*/
    // TYPE gregorian-year -> fixed-date
    // Fixed date of Christmas in Gregorian year.
    public static long christmas(long gYear) {
        return toFixed(gYear, DECEMBER, 25);
    }


    /*- advent -*/
    // TYPE gregorian-year -> fixed-date
    // Fixed date of Advent in Gregorian year
    // --the Sunday closest to November 30.
    public static long advent(long gYear) {
        return kDayNearest(toFixed(gYear, NOVEMBER, 30), SUNDAY);
    }


    /*- epiphany -*/
    // TYPE gregorian-year -> fixed-date
    // Fixed date of Epiphany in U.S. in Gregorian year
    // --the first Sunday after January 1.
    public static long epiphany(long gYear) {
        return firstKDay(SUNDAY, toFixed(gYear, JANUARY, 2));
    }

 }
