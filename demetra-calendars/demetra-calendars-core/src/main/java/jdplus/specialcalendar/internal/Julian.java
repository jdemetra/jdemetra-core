package jdplus.specialcalendar.internal;

import static jdplus.specialcalendar.internal.DateUtility.*;
import java.util.Arrays;

@lombok.Value
public class Julian implements Date {
    

    public long year;
    public int month;
    public int day;

    /*- julian-epoch -*/
    // TYPE fixed-date
    // Fixed date of start of the Julian calendar.
    public static final long EPOCH = Gregorian.toFixed(0, DECEMBER, 30);


    /*- fixed-from-julian -*/
    // TYPE julian-date -> fixed-date
    // Fixed date equivalent to the Julian date.
    public static long toFixed(long year, int month, int day) {
        long y = year < 0 ? year + 1 : year;
        return EPOCH - 1
                + 365 * (y - 1)
                + quotient(y - 1, 4)
                + quotient(367 * month - 362, 12)
                + (month <= 2 ? 0
                        : (isLeapYear(year) ? -1 : -2))
                + day;
    }

    @Override
    public long toFixed() {
        return toFixed(year, month, day);
    }

    /*- julian-from-fixed -*/
    // TYPE fixed-date -> julian-date
    // Julian (year month day) corresponding to fixed $date$.
    public static Julian fromFixed(long date) {
        long approx = quotient(4 * (date - EPOCH) + 1464, 1461);
        long year = approx <= 0 ? approx - 1 : approx;
        long priorDays = date - toFixed(year, JANUARY, 1);
        int correction = date < toFixed(year, MARCH, 1) ? 0 : (isLeapYear(year) ? 1 : 2);
        int month = (int) quotient(12 * (priorDays + correction) + 373, 367);
        int day = (int) (date - toFixed(year, month, 1) + 1);
        return new Julian(year, month, day);
    }

    //
    // support methods
    //
    /*- bce -*/
    // TYPE standard-year -> julian-year
    // Negative value to indicate a BCE Julian year.
    public static long BCE(long n) {
        return -n;
    }


    /*- ce -*/
    // TYPE standard-year -> julian-year
    // Positive value to indicate a CE Julian year.
    public static long CE(long n) {
        return n;
    }


    /*- julian-leap-year? -*/
    // TYPE julian-year -> boolean
    // True if $j-year$ is a leap year on the Julian calendar.
    public static boolean isLeapYear(long jYear) {
        return mod(jYear, 4) == (jYear > 0 ? 0 : 3);
    }

    //
    // auxiliary methods
    //
    /*- julian-in-gregorian -*/
    // TYPE (julian-month julian-day gregorian-year)
    // TYPE -> list-of-fixed-dates
    // List of the fixed dates of Julian month, day
    // that occur in Gregorian year.
    public static long[] inGregorian(int jMonth, int jDay, long gYear) {
        long jan1 = Gregorian.toFixed(gYear, JANUARY, 1);
        long dec31 = Gregorian.toFixed(gYear, DECEMBER, 31);
        long y = Julian.fromFixed(jan1).year;
        long yPrime = y == -1 ? 1 : y + 1;
        long date1 = toFixed(y, jMonth, jDay);
        long date2 = toFixed(yPrime, jMonth, jDay);
        long[] ll=new long[2];
        int lpos=0;
        if (jan1 <= date1 && date1 <= dec31) {
            ll[lpos++]=date1;
        }
        if (jan1 <= date2 && date2 <= dec31) {
            ll[lpos++]=date2;
        }
        switch (lpos){
            case 0:
                return LEMPTY;
            case 1:
                return new long[]{ll[0]};
            default:
                return ll;
        }
    }


    /*- eastern-orthodox-christmas -*/
    // TYPE gregorian-year -> list-of-fixed-dates
    // List of zero or one fixed dates of Eastern Orthodox
    // Christmas in Gregorian year.
    public static long[] easternOrthodoxChristmas(long gYear) {
        return inGregorian(DECEMBER, 25, gYear);
    }

}
