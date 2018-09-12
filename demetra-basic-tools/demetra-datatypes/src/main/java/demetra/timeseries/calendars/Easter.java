/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.timeseries.calendars;

import demetra.design.Development;
import java.time.LocalDate;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
@Development(status = Development.Status.Beta)
public class Easter {

    /**
     * Meeus/Jones/Butcher's algorithm for computing Easter The easter and
     * easter2 methods give the same results up to 4150.
     *
     * @param y Year
     * @return The Easter day for the given year
     */
    public LocalDate easter(int y) {
        int a = y % 19;
        int b = y / 100;
        int c = y % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = (h + l - 7 * m + 114) % 31 + 1;
        return LocalDate.of(y, month, day);
    }

    /**
     * Ron Mallen's algorithm for computing Easter. Valid till 4000. The easter
     * and easter2 methods give the same results up to 4150
     *
     * @param y Year
     * @return The Easter day for the given year
     */
    public LocalDate easter2(int y) {
        int firstdig = y / 100;
        int remain19 = y % 19;
        // calculate PFM date (Paschal Full Moon)
        int temp = (firstdig - 15) / 2 + 202 - 11 * remain19;
        if (firstdig == 21 || firstdig == 24 || firstdig == 25
                || (firstdig >= 27 && firstdig <= 32) || firstdig == 34
                || firstdig == 35 || firstdig == 38) {
            --temp;
        } else if (firstdig == 33 || firstdig == 36 || firstdig == 37
                || firstdig == 39 || firstdig == 40) {
            temp -= 2;
        }
        temp = temp % 30;
        int ta = temp + 21;
        if (temp == 29) {
            --ta;
        }
        if (temp == 28 && remain19 > 10) {
            --ta;
        }
        // find the next sunday
        int tb = (ta - 19) % 7;
        int tc = (40 - firstdig) % 4;
        if (tc == 3) {
            ++tc;
        }
        if (tc > 1) {
            ++tc;
        }
        temp = y % 100;
        int td = (temp + temp / 4) % 7;
        int te = ((20 - tb - tc - td) % 7) + 1;
        int d = ta + te;
        // return the date
        if (d > 31) {
            return LocalDate.of(y, 4, d - 31);
        } else {
            return LocalDate.of(y, 3, d);
        }
    }

    /**
     * Converts a Julian date into a Julian day number
     *
     * @param year
     * @param month From 1 to 12
     * @param day From 1 to 31
     * @return The Julian day
     */
    public int julianDate2JDN(int year, int month, int day) {
        int a = (14 - month) / 12;
        int y = year + 4800 - a;
        int m = month + (12 * a) - 3;
        return day + ((153 * m + 2) / 5) + 365 * y + y / 4 - 32083;
    }

    /**
     * Converts a Gregorian date into a Julian day number
     *
     * @param year
     * @param month From 1 to 12
     * @param day From 1 to 31
     * @return The Julian day
     */
    public int gregorianDate2JDN(int year, int month, int day) {
        int a = (14 - month) / 12;
        int y = year + 4800 - a;
        int m = month + (12 * a) - 3;
        return day + ((153 * m + 2) / 5) + 365 * y + y / 4 - y / 100 + y / 400 - 32045;
    }

    /**
     * Converts a Julian day number into a Gregorian date
     *
     * @param jdn The Julian date number
     * @return
     */
    public LocalDate JDN2GregorianDate(int jdn) {
        final int y = 4716, v = 3, j = 1401, u = 5, m = 2, s = 153, n = 12, w = 2, r = 4, B = 274277, p = 1461, C = -38;

        int f = jdn + j + (((4 * jdn + B) / 146097) * 3) / 4 + C;
        int e = r * f + v;
        int g = (e % p) / r;
        int h = u * g + w;
        int D = (h % s) / u;
        int M = ((h / s + m) % n);
        int Y = (e / p) - y + (n + m - M) / n;
        return LocalDate.of(Y, M + 1, D + 1);
    }

    /**
     * Returns the Julian Easter (Delambre's algorithm)
     *
     * @param year Considered year
     * @param gregorian Gregorian (true) or Julian (false) day
     * @return Easter day
     */
    public LocalDate julianEaster(int year, boolean gregorian) {
        int a = year % 19;
        int b = year % 4;
        int c = year % 7;
        int d = (19 * a + 15) % 30;
        int e = (2 * b + 4 * c - d + 34) % 7;
        int f = d + e + 114;
        /*
       * month and day give the Easter date in the Julian calendar
         */
        int month = f / 31;
        int day = (f % 31) + 1;
        if (gregorian) {
            return JDN2GregorianDate(julianDate2JDN(year, month, day));
        } else {
            return LocalDate.of(year, month, day);
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Easter-related methods">
    public final double LUNARY = 29.53059, DEC_LUNARY = .53059;
    /**
     * The probability that Easter falls on April,4 + K (or March, 22 + K) is
     * defined by PROB[K]/CYCLE
     */
    static int CYCLE = 532, TWOCYCLE = CYCLE << 1;
    static int[] PROB = new int[]{
        4, 8, 8, 12, 16, 16, 20, 16, 16, 20, 16, 16, 20, 16, 20, 20, 16, 20, 16, 16, 20, 16, 16, 20, 16, 20, 16, 16, 20, 16, 12, 12, 8, 8, 4
    };

    /**
     * Computes the probability for a given Easter date, in the Gregorian
     * calendar.
     *
     * @param pos Pos is the position between 1/4 and 13/5 (from 0 to 43
     * (excluded)
     * @return The requested probability. For instance, probJulianEaster(0)
     * gives the probability that the Julian Easter falls on April, 4
     */
    public double probJulianEaster(int pos) {
        if (pos < 0 || pos >= 43) {
            return 0;
        } else {
            double denom = TWOCYCLE;
            return JD[pos] / denom;
        }
    }

    private static final int[] JD = {
        1, 1, 3, 9, 15, 11, 14, 27, 36, 28, 24, 32, 40, 39, 33, 31,
        34, 36, 42, 39, 33, 31, 33, 42, 38, 33, 30, 32, 39, 40,
        33, 29, 26, 31, 33, 22, 7, 15, 12, 7, 1, 1, 1
    };

    /**
     * Computes the probability for a given Easter date. The current
     * implementation returns a raw estimation of that probability. It could be
     * replaced by a more accurate estimation (for example computed by means of
     * one of the available algorithms on a complete cycle of Easter calendar =
     * 5700000 years). Anyway, the differences will be marginal.
     *
     * @param pos Pos is the position between 22/3 and 25/4 (from 0 to 35
     * (excluded)
     * @return
     */
    public double probEaster(int pos) {
        if (pos < 0 || pos >= 35) {
            return 0;
        }
        if (pos < 6) {
            return (pos + 1) / (7 * LUNARY);
        } else if (pos < 28) {
            return 1 / LUNARY;
        } else {
            return (35 - pos + DEC_LUNARY) / (7 * LUNARY);
        }
    }

    //</editor-fold>
}
