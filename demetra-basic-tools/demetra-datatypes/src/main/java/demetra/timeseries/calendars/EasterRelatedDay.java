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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
public class EasterRelatedDay implements IHoliday {

    /*
     * Raw estimation of the probability to get Easter at a specific date is defined below:
     * 22/3 (1/7)*1/LUNARY
     * 23/3 (2/7)*1/LUNARY
     * ...
     * 27/3 (6/7)*1/LUNARY
     * 28/3 1/LUNARY
     * ...
     * 18/4 1/LUNARY
     * 19/4 1/LUNARY + (1/7) * DEC_LUNARY/LUNARY = (7 + 1 * DEC_LUNARY)/(7 * LUNARY)
     * 20/4 (6/7)*1/LUNARY + (1/7) * DEC_LUNARY/LUNARY= (6 + 1 * DEC_LUNARY)/(7 * LUNARY)
     * 21/4 (5/7)*1/LUNARY + (1/7) * DEC_LUNARY/LUNARY
     * 22/4 (4/7)*1/LUNARY + (1/7) * DEC_LUNARY/LUNARY
     * 23/4 (3/7)*1/LUNARY + (1/7) * DEC_LUNARY/LUNARY
     * 24/4 (2/7)*1/LUNARY + (1/7) * DEC_LUNARY/LUNARY
     * 25/4 (1/7)*1/LUNARY + (1/7) *DEC_LUNARY/LUNARY
     */
    private static final Map<Integer, LocalDate> DIC = new HashMap<>();
    private static final Map<Integer, LocalDate> JDIC = new HashMap<>();
    private static final int[] DAYS = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private int offset;
    private double weight;
    private boolean julian;
    
    public static EasterRelatedDay gregorian(int offset, double weight){
        return new EasterRelatedDay(offset, weight, false);
    }

    public static EasterRelatedDay gregorian(int offset){
        return new EasterRelatedDay(offset, 1, false);
    }

    public static EasterRelatedDay julian(int offset, double weight){
        return new EasterRelatedDay(offset, weight, true);
    }

    public static EasterRelatedDay julian(int offset){
        return new EasterRelatedDay(offset, 1, true);
    }

    private EasterRelatedDay(int offset, double weight, boolean julian) {
        this.weight = weight;
        this.offset = offset;
        this.julian = julian;
    }

    public EasterRelatedDay reweight(double nweight) {
        if (nweight == weight) {
            return this;
        }
        return new EasterRelatedDay(offset, nweight, julian);
    }

    public EasterRelatedDay plus(int ndays) {
        return new EasterRelatedDay(offset + ndays, weight, julian);
    }

    @Override
    public double getWeight() {
        return weight;
    }

    public static final EasterRelatedDay SHROVEMONDAY = gregorian(-48),
            SHROVETUESDAY = gregorian(-47),
            ASHWEDNESDAY = gregorian(-46),
            EASTER = gregorian(0),
            EASTERMONDAY = gregorian(1),
            EASTERFRIDAY = gregorian(-2),
            EASTERTHURSDAY = gregorian(-3),
            ASCENSION = gregorian(39),
            PENTECOST = gregorian(49),
            WHITMONDAY = gregorian(50),
            CORPUSCHRISTI = gregorian(60),
            JULIAN_SHROVEMONDAY = julian(-48),
            JULIAN_SHROVETUESDAY = julian(-47),
            JULIAN_ASHWEDNESDAY = julian(-46),
            JULIAN_EASTER = julian(0),
            JULIAN_EASTERMONDAY = julian(1),
            JULIAN_EASTERFRIDAY = julian(-2),
            JULIAN_EASTERTHURSDAY = julian(-3),
            JULIAN_ASCENSION = julian(39),
            JULIAN_PENTECOST = julian(49),
            JULIAN_WHITMONDAY = julian(50),
            JULIAN_CORPUSCHRISTI = julian(60);

    public LocalDate calcDay(int year) {
        LocalDate d = easter(year);
        if (offset != 0) {
            d = d.plusDays(offset);
        }
        return d;
    }

    private LocalDate easter(int year) {
        return easter(year, julian);
    }

    private static LocalDate easter(int year, boolean jul) {
        if (jul) {
            synchronized (JDIC) {
                LocalDate e = JDIC.get(year);
                if (e == null) {
                    e = Easter.julianEaster(year, true);
                    JDIC.put(year, e);
                }
                return e;
            }
        } else {
            synchronized (DIC) {
                LocalDate e = DIC.get(year);
                if (e == null) {
                    e = Easter.easter(year);
                    DIC.put(year, e);
                }
                return e;
            }
        }
    }

    private double probEaster(int del) {
        return julian ? Easter.probJulianEaster(del)
                : Easter.probEaster(del);
    }

    @Override
    public Iterable<HolidayInfo> getIterable(LocalDate start, LocalDate end) {
        return new EasterDayList(offset, start, end, julian);
    }

    private static int START = 80, JSTART = 90, DEL = 35, JDEL = 43;
    // 31+28+21=80, 31+28+31=90

    @Override
    public double[][] longTermMean(int freq) {
        // week day

        int w = offset % 7;
        if (w == 0) {
            w=7; // Sunday
        }
        if (w < 0) {
            w += 7;
        }
        // monday must be 0...
        --w;

        // Easter always falls between March, 22 and April, 25 (inclusive). The probability to get a specific day is defined by probEaster.
        // We don't take into account leap year. So, the solution is slightly wrong for offset
        // <= -50.
        // The considered day falls between ...
        int d0, d1;
        if (julian) {
            d0 = JSTART + offset;
            d1 = d0 + JDEL;
        } else {
            d0 = START + offset;
            d1 = d0 + DEL;
        }
        // d1 excluded

        int ifreq = (int) freq;
        int c = 12 / ifreq;

        int c0 = 0, c1 = 0;
        for (int i = 0; i < c; ++i) {
            c1 += DAYS[i];
        }

        double[][] rslt = new double[ifreq][];
        for (int i = 0; i < ifreq;) {
            if (d0 < c1 && d1 > c0) {
                double[] m = new double[7];
                double x = 0;
                for (int j = Math.max(d0, c0); j < Math.min(d1, c1); ++j) {
                    x += probEaster(j - d0);
                }
                m[w] = x * weight;
                rslt[i] = m;
            }
            // update c0, c1;
            c0 = c1;
            if (++i < ifreq) {
                for (int j = 0; j < c; ++j) {
                    c1 += DAYS[i * c + j];
                }
            }
        }
        return rslt;
    }

    static class EasterDayInfo implements HolidayInfo {

        final LocalDate day;

        public EasterDayInfo(int year, int offset, boolean julian) {
            LocalDate easter = easter(year, julian);
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
    }

    static class EasterDayList extends AbstractList<HolidayInfo> {

        private final int startyear, n, offset;
        private final boolean julian;

        public EasterDayList(int offset, LocalDate fstart, LocalDate fend, boolean julian) {
            this.offset = offset;
            this.julian = julian;
            int ystart = fstart.getYear(), yend = fend.getYear();
            LocalDate xday = easter(ystart, julian).plusDays(offset);
            LocalDate yday = easter(yend, julian).plusDays(offset);

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

    /**
     * @return the offset
     */
    public int getOffset() {
        return offset;
    }
}
