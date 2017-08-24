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
import demetra.timeseries.RegularDomain;
import demetra.timeseries.TsFrequency;
import demetra.timeseries.TsPeriod;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
public class EasterRelatedDay implements ISpecialDay {

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
    public final int offset;
    private final double weight;
    private final boolean julian;

    /**
     * Creates a new Easter related day, with 0 offset. Corresponds to easter
     */
    public EasterRelatedDay() {
        this(0, 1, false);
    }

    public EasterRelatedDay(int offset) {
        this(offset, 1, false);
    }

    public EasterRelatedDay(int offset, boolean julian) {
        this(offset, 1, julian);
    }

    public EasterRelatedDay(int offset, double weight) {
        this(offset, weight, false);
    }

    public EasterRelatedDay(int offset, double weight, boolean julian) {
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

    public boolean isJulian() {
        return julian;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public boolean match(Context context) {
        return context.isJulianEaster() == julian;
    }

    public static final EasterRelatedDay SHROVEMONDAY = new EasterRelatedDay(-48),
            SHROVETUESDAY = new EasterRelatedDay(-47),
            ASHWEDNESDAY = new EasterRelatedDay(-46),
            EASTER = new EasterRelatedDay(0),
            EASTERMONDAY = new EasterRelatedDay(1),
            EASTERFRIDAY = new EasterRelatedDay(-2),
            EASTERTHURSDAY = new EasterRelatedDay(-3),
            ASCENSION = new EasterRelatedDay(39),
            PENTECOST = new EasterRelatedDay(49),
            PENTECOSTMONDAY = new EasterRelatedDay(50),
            CORPUSCHRISTI = new EasterRelatedDay(60),
            JULIAN_SHROVEMONDAY = new EasterRelatedDay(-48, true),
            JULIAN_SHROVETUESDAY = new EasterRelatedDay(-47, true),
            JULIAN_ASHWEDNESDAY = new EasterRelatedDay(-46, true),
            JULIAN_EASTER = new EasterRelatedDay(0, true),
            JULIAN_EASTERMONDAY = new EasterRelatedDay(1, true),
            JULIAN_EASTERFRIDAY = new EasterRelatedDay(-2, true),
            JULIAN_EASTERTHURSDAY = new EasterRelatedDay(-3, true),
            JULIAN_ASCENSION = new EasterRelatedDay(39, true),
            JULIAN_PENTECOST = new EasterRelatedDay(49, true),
            JULIAN_PENTECOSTMONDAY = new EasterRelatedDay(50, true),
            JULIAN_CORPUSCHRISTI = new EasterRelatedDay(60, true);

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
    public Iterable<IDayInfo> getIterable(TsFrequency freq, LocalDate start, LocalDate end) {
        return new EasterDayList(freq, offset, start, end, julian);
    }

    private static int START = 80, JSTART = 90, DEL = 35, JDEL = 43;
    // 31+28+21=80, 31+28+31=90

    @Override
    public double[][] getLongTermMeanEffect(int freq) {
        // week day

        int w = offset % 7;
        if (w == 0) {
            return null;
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
                m[6] = -m[w];
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

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof EasterRelatedDay && equals((EasterRelatedDay) obj));
    }

    private boolean equals(EasterRelatedDay other) {
        return other.offset == offset && other.weight == weight && other.julian == julian;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + this.offset;
        return hash;
    }

    @Override
    public RegularDomain getSignificantDomain(RegularDomain domain) {
        if (!domain.getStartPeriod().getFreq().getUnit().equals(ChronoUnit.DAYS)) {
            throw new IllegalArgumentException();
        }
        LocalDate first = domain.start().toLocalDate(), last = domain.end().toLocalDate().minusDays(1);
        LocalDate efirst = easter(first.getYear()).plusDays(offset);
        LocalDate elast = easter(last.getYear()).plusDays(offset);
        if (efirst.isBefore(first)) {
            efirst = easter(first.getYear() + 1).plusDays(offset);
        }
        if (elast.isAfter(last)) {
            elast = easter(last.getYear() - 1).plusDays(offset);
        }
        if (efirst.isAfter(elast)) {
            return RegularDomain.of(domain.getStartPeriod().withDate(efirst), 0);
        } else {
            return RegularDomain.of(domain.getStartPeriod().withDate(efirst), (int) ChronoUnit.DAYS.between(efirst, elast));
        }
    }

    static class EasterDayInfo implements IDayInfo {

        public EasterDayInfo(TsFrequency freq, int year, int offset, boolean julian) {
            LocalDate easter = easter(year, julian);
            day = easter.plusDays(offset);
            this.freq = freq;
        }

        @Override
        public LocalDate getDay() {
            return day;
        }

        @Override
        public TsPeriod getPeriod() {
            return TsPeriod.of(freq, day);
        }

        @Override
        public DayOfWeek getDayOfWeek() {
            return day.getDayOfWeek();
        }
        final LocalDate day;
        final TsFrequency freq;
    }

    static class EasterDayList extends AbstractList<IDayInfo> {

        public EasterDayList(TsFrequency freq, int offset, LocalDate fstart, LocalDate fend, boolean julian) {
            this.freq = freq;
            m_offset = offset;
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
        private final int startyear, n, m_offset;
        private final TsFrequency freq;
        private final boolean julian;

        @Override
        public IDayInfo get(int index) {
            return new EasterDayInfo(freq, startyear + index, m_offset, julian);
        }

        @Override
        public int size() {
            return n;
        }
    }
}
