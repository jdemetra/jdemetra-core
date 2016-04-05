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
package ec.tstoolkit.timeseries.calendars;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.DayOfWeek;
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class Utilities {

    /**
     *
     * @param domain
     * @return
     */
    public static int[] daysCount(TsDomain domain) {
	// if (domain == null)
        // throw new ArgumentNullException("domain");
        int n = domain.getLength();
        int[] rslt = new int[n];
        int[] start = new int[n + 1]; // id of the first day for each period
        TsPeriod d0 = domain.getStart();
        int conv = 12 / d0.getFrequency().intValue();
        TsPeriod month = new TsPeriod(TsFrequency.Monthly);
        month.set(d0.getYear(), d0.getPosition() * conv);
        for (int i = 0; i < start.length; ++i) {
            start[i] = Day.calc(month.getYear(), month.getPosition(), 0);
            month.move(conv);
        }
        for (int i = 0; i < n; ++i) {
            // int dw0 = (start[i] - 4) % 7;
            int ni = start[i + 1] - start[i];
            rslt[i] = ni;
        }
        return rslt;
    }

    /**
     *
     * @param domain
     * @param day
     * @return
     */
    public static int[] daysCount(TsDomain domain, DayOfWeek day) {
        int n = domain.getLength();
        int[] rslt = new int[n];
        int[] start = new int[n + 1]; // id of the first day for each period
        TsPeriod d0 = domain.getStart();
        int conv = 12 / d0.getFrequency().intValue();
        TsPeriod month = new TsPeriod(TsFrequency.Monthly);
        month.set(d0.getYear(), d0.getPosition() * conv);
        for (int i = 0; i < start.length; ++i) {
            start[i] = Day.calc(month.getYear(), month.getPosition(), 0);
            month.move(conv);
        }

        for (int i = 0; i < n; ++i) {
            int dw0 = (start[i] - 4) % 7;
            int ni = start[i + 1] - start[i];
            if (dw0 < 0) {
                dw0 += 7;
            }
            int j = day.intValue();
            int j0 = j - dw0;
            if (j0 < 0) {
                j0 += 7;
            }
            rslt[i] = 1 + (ni - 1 - j0) / 7;
        }
        return rslt;
    }

    /**
     * Meeus/Jones/Butcher's algorithm for computing Easter The easter and
     * easter2 methods give the same results up to 4150.
     *
     * @param y Year
     * @return The Easter day for the given year
     */
    public static Day easter(int y) {
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
        return new Day(y, Month.valueOf(month - 1), day - 1);
    }

    /**
     * Ron Mallen's algorithm for computing Easter. Valid till 4000. The easter
     * and easter2 methods give the same results up to 4150
     *
     * @param y Year
     * @return The Easter day for the given year
     */
    public static Day easter2(int y) {
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
            return new Day(y, Month.April, d - 32);
        } else {
            return new Day(y, Month.March, d - 1);
        }
    }

    /**
     * Returns the Julian Easter Meeus algorithm
     *
     * @param year Considered year
     * @param gregorian Gregorian (true) or Julian (false) day
     * @return Easter day
     */
    public static Day julianEaster(int year, boolean gregorian) {
        int a = year % 19;
        int b = year % 4;
        int c = year % 7;
        int d = (19 * a + 15) % 30;
        int e = (2 * b + 4 * c + 6 * d + 6) % 7;
        int z=4+d+e;
        int month, day; 
        int de=d+e;
        if (de<10){
            month=3; 
            day=22+de;
        }else{
            month=4;
            day=de-9;
        }
        Day easter = new Day(year, Month.valueOf(month-1), day-1);
        if (gregorian) {
            return easter.plus(13);
        } else {
            return easter;
        }
    }

    public static Day julianEaster2(int year, boolean gregorian) {
        int g = year % 19;
        int i = (19 * g + 15) % 30;
        int j = (year + year / 4 + i) % 7;
        int l = i - j;
        int month = 3 + (l + 40) / 44;
        int day = l + 28 - 31 * (month / 4);
        Day easter = new Day(year, Month.valueOf(month - 1), day - 1);
        if (gregorian) {
            return easter.plus(13);
        } else {
            return easter;
        }
    }

    /**
     * Return the first Day in the given month of the given year which is a
     * specified day of week
     *
     * @param day Day of week
     * @param year
     * @param month
     * @return
     */
    public static Day firstWeekDay(DayOfWeek day, int year, Month month) {
        TsPeriod m = new TsPeriod(TsFrequency.Monthly);
        m.set(year, month.intValue());
        Day start = m.firstday();
        int iday = day.intValue();
        int istart = start.getDayOfWeek().intValue();
        int n = iday - istart;
        if (n < 0) {
            n += 7;
        }
        if (n != 0) {
            start = start.plus(n);
        }
        return start;
    }

    /**
     * monday=0, ..., sunday=6.
     *
     * @param domain
     * @return
     */
    public static int[] lastDay(TsDomain domain) {
        int n = domain.getLength();
        int[] rslt = new int[n];

        TsPeriod d1 = domain.getStart();
        d1.move(1);
        int conv = 12 / d1.getFrequency().intValue();
        TsPeriod month = new TsPeriod(TsFrequency.Monthly);
        month.set(d1.getYear(), d1.getPosition() * conv);
        // rslt contains the id of the last day of each period
        for (int i = 0; i < rslt.length; ++i) {
            int id = Day.calc(month.getYear(), month.getPosition(), 0);
            id = (id - 5) % 7;
            if (id < 0) {
                id += 7;
            }
            rslt[i] = id;
            month.move(conv);
        }
        return rslt;
    }

    /**
     *
     * @param start
     * @param buffer
     */
    public static void leapYear(TsPeriod start, DataBlock buffer) {
        buffer.set(0);
        TsDomain domain = new TsDomain(start, buffer.getLength());
        int period = 0;
        int freq = start.getFrequency().intValue();
        if (freq == 12) {
            period = 1;
        }
        int idx = period - start.getPosition();
        if (idx < 0) {
            idx += freq;
        }
        while (idx < buffer.getLength()) {
            if (Day.isLeap(domain.get(idx).getYear())) {
                buffer.set(idx, .75);
            } else {
                buffer.set(idx, -.25);
            }
            idx += freq;
        }
    }

    /**
     *
     * @param start
     * @param buffer
     */
    public static void lengthofPeriod(TsPeriod start, DataBlock buffer) {
        int[] ndays = daysCount(new TsDomain(start, buffer.getLength()));
        double m = 365.25 / start.getFrequency().intValue();
        for (int i = 0; i < ndays.length; ++i) {
            buffer.set(i, ndays[i] - m);
        }
    }

    /**
     *
     * @param kind
     * @param domain
     * @param back
     * @return
     */
    public static double logJacobian(LengthOfPeriodType kind, TsDomain domain,
            boolean back) {
        double lj = 0;
        if (kind == LengthOfPeriodType.LengthOfPeriod) {
            int[] ndays = daysCount(domain);
            double m = 365.25 / domain.getFrequency().intValue();
            if (back) {
                for (int i = 0; i < ndays.length; ++i) {
                    double c = ndays[i] / m;
                    lj += Math.log(c);
                }
            } else {
                for (int i = 0; i < ndays.length; ++i) {
                    double c = m / ndays[i];
                    lj += Math.log(c);
                }
            }
        } else {
            int period = 0;
            int freq = domain.getFrequency().intValue();
            if (freq == 12) {
                period = 1;
            }
            int idx = period - domain.getStart().getPosition();
            if (idx < 0) {
                idx += freq;
            }
            int ndays = 0;
            if (freq == 12) {
                ndays = 28;
            } else {
                int nm = 12 / freq;
                for (int i = 0; i < nm; ++i) {
                    ndays += Day.getMonthDays(i);
                }
            }
            double leap = (ndays + 1) / (ndays + .25);
            double nleap = ndays / (ndays + .25);
            double lleap = Math.log(leap);
            double lnleap = Math.log(nleap);

            if (back) {
                while (idx < domain.getLength()) {
                    if (Day.isLeap(domain.get(idx).getYear())) {
                        lj += lleap;
                    } else {
                        lj += lnleap;
                    }
                    idx += freq;
                }
            } else {
                while (idx < domain.getLength()) {
                    if (Day.isLeap(domain.get(idx).getYear())) {
                        lj -= lleap;
                    } else {
                        lj -= lnleap;
                    }
                    idx += freq;
                }
            }
        }
        return lj;
    }

    // / <summary>
    // / Arrays with the number of monday, ..., sunday
    // / </summary>
    // / <param name="domain"></param>
    // / <returns></returns>
    /**
     *
     * @param domain
     * @return
     */
    public static int[][] tradingDays(TsDomain domain) {
        int[][] rslt = new int[7][];

        int n = domain.getLength();
        int[] start = new int[n + 1]; // id of the first day for each period
        TsPeriod d0 = domain.getStart();
        int conv = 12 / d0.getFrequency().intValue();
        TsPeriod month = new TsPeriod(TsFrequency.Monthly);
        month.set(d0.getYear(), d0.getPosition() * conv);
        for (int i = 0; i < start.length; ++i) {
            start[i] = Day.calc(month.getYear(), month.getPosition(), 0);
            month.move(conv);
        }

        for (int j = 0; j < 7; ++j) {
            rslt[j] = new int[n];
        }

        for (int i = 0; i < n; ++i) {
            int dw0 = (start[i] - 4) % 7;
            int ni = start[i + 1] - start[i];
            if (dw0 < 0) {
                dw0 += 7;
            }
            for (int j = 0; j < 7; ++j) {
                int j0 = j - dw0;
                if (j0 < 0) {
                    j0 += 7;
                }
                rslt[j][i] = 1 + (ni - 1 - j0) / 7;
            }
        }
        return rslt;
    }

    /**
     *
     * @param kind
     * @param tsdata
     * @param back
     */
    public static void transform(LengthOfPeriodType kind, TsData tsdata,
            boolean back) {
        if (kind == LengthOfPeriodType.LengthOfPeriod) {
            int[] ndays = daysCount(tsdata.getDomain());
            double m = 365.25 / tsdata.getFrequency().intValue();
            double[] data = tsdata.internalStorage();
            if (back) {
                for (int i = 0; i < ndays.length; ++i) {
                    data[i] *= ndays[i] / m;
                }
            } else {
                for (int i = 0; i < ndays.length; ++i) {
                    data[i] *= m / ndays[i];
                }
            }
        } else {
            TsDomain domain = tsdata.getDomain();
            int period = 0;
            int freq = tsdata.getFrequency().intValue();
            if (freq == 12) {
                period = 1;
            }
            int idx = period - domain.getStart().getPosition();
            if (idx < 0) {
                idx += freq;
            }
            int ndays = 0;
            if (freq == 12) {
                ndays = 28;
            } else {
                int nm = 12 / freq;
                for (int i = 0; i < nm; ++i) {
                    ndays += Day.getMonthDays(i);
                }
            }
            double leap = (ndays + 1) / (ndays + .25);
            double nleap = ndays / (ndays + .25);
            double[] data = tsdata.internalStorage();
            if (back) {
                while (idx < domain.getLength()) {
                    if (Day.isLeap(domain.get(idx).getYear())) {
                        data[idx] *= leap;
                    } else {
                        data[idx] *= nleap;
                    }
                    idx += freq;
                }
            } else {
                while (idx < domain.getLength()) {
                    if (Day.isLeap(domain.get(idx).getYear())) {
                        data[idx] /= leap;
                    } else {
                        data[idx] /= nleap;
                    }
                    idx += freq;
                }
            }
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Easter-related methods">
    public static final double LUNARY = 29.53059, DEC_LUNARY = .53059;
    /**
     * The probability that Easter falls on April,4 + K (or March, 22 + K) is
     * defined by PROB[K]/CYCLE
     */
    static final int CYCLE = 532;
    static final int[] PROB = new int[]{
        4, 8, 8, 12, 16, 16, 20, 16, 16, 20, 16, 16, 20, 16, 20, 20, 16, 20, 16, 16, 20, 16, 16, 20, 16, 20, 16, 16, 20, 16, 12, 12, 8, 8, 4
    };

    /**
     * Computes the probability for a given Easter date, in the Gregorian
     * calendar.
     *
     * @param pos Pos is the position between 4/4 and 8/5 (from 0 to 35
     * (excluded)
     * @return The requested probability. For instance, probJulianEaster(0)
     * gives the probability that the Julian Easter falls on April, 4
     */
    public static double probJulianEaster(int pos) {
        if (pos < 0 || pos >= 35) {
            return 0;
        } else {
            double denom = CYCLE;
            return PROB[pos] / denom;
        }
    }

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
    public static double probEaster(int pos) {
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
