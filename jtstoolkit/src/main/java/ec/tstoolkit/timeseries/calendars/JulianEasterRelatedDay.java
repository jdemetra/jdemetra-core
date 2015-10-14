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

import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.DayOfWeek;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
public class JulianEasterRelatedDay implements ISpecialDay {

    private static final Map<Integer, Day> g_dic = new HashMap<>();
    private static final int[] g_days = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    public final int offset;
    private final double weight;

    /**
     * Creates a new Easter related day, with 0 offset. Corresponds to easter
     */
    public JulianEasterRelatedDay() {
        this(0, 1);
    }

    public JulianEasterRelatedDay(int offset) {
        this(offset, 1);
    }

    public JulianEasterRelatedDay(int offset, double weight) {
        this.weight = weight;
        this.offset = offset;
    }

    public JulianEasterRelatedDay reweight(double nweight) {
        if (nweight == weight) {
            return this;
        }
        return new JulianEasterRelatedDay(offset, nweight);
    }

    public JulianEasterRelatedDay plus(int ndays) {
        return new JulianEasterRelatedDay(offset + ndays, weight);
    }

    @Override
    public double getWeight() {
        return weight;
    }
    public static final JulianEasterRelatedDay 
            ShroveMonday = new JulianEasterRelatedDay(-48),
            ShroveTuesday = new JulianEasterRelatedDay(-47),
            AshWednesday = new JulianEasterRelatedDay(-46),
            Easter = new JulianEasterRelatedDay(0),
            EasterMonday = new JulianEasterRelatedDay(1),
            EasterFriday = new JulianEasterRelatedDay(-2),
            EasterThursday = new JulianEasterRelatedDay(-3),
            Ascension = new JulianEasterRelatedDay(39),
            Pentecost = new JulianEasterRelatedDay(49),
            PentecostMonday = new JulianEasterRelatedDay(50),
            CorpusChristi = new JulianEasterRelatedDay(60);

    public Day calcDay(int year) {
        Day d = julianEaster(year);
        if (offset != 0) {
            d = d.plus(offset);
        }
        return d;
    }

    private Day julianEaster(int year) {
        synchronized (g_dic) {
            Day e = g_dic.get(year);
            if (e == null) {
                e = Utilities.julianEaster(year, true);
                g_dic.put(year, e);
            }
            return e;
        }
    }

    @Override
    public Iterable<IDayInfo> getIterable(TsFrequency freq, Day start, Day end) {
        return new JulianEasterDayList(freq, offset, start, end);
    }

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

        // Easter always falls between April, 4 and May, 8 (inclusive). The probability to get a specific day is defined by probEaster.
        // The considered day falls between ...
        // 31+28+31+3=93
        // 31+28+31+30+8=128
        int d0 = 93 + offset, d1 = 128 + offset; // d1 excluded

        int ifreq = (int) freq;
        int c = 12 / ifreq;

        int c0 = 0, c1 = 0;
        for (int i = 0; i < c; ++i) {
            c1 += g_days[i];
        }

        double[][] rslt = new double[ifreq][];
        for (int i = 0; i < ifreq;) {
            if (d0 < c1 && d1 > c0) {
                double[] m = new double[7];
                double x = 0;
                for (int j = Math.max(d0, c0); j < Math.min(d1, c1); ++j) {
                    x += Utilities.probJulianEaster(j - d0);
                }
                m[w] = x * weight;
                m[6] = -m[w];
                rslt[i] = m;
            }
            // update c0, c1;
            c0 = c1;
            if (++i < ifreq) {
                for (int j = 0; j < c; ++j) {
                    c1 += g_days[i * c + j];
                }
            }
        }
        return rslt;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof JulianEasterRelatedDay && equals((JulianEasterRelatedDay) obj));
    }

    private boolean equals(JulianEasterRelatedDay other) {
        return other.offset == offset && other.weight == weight;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + this.offset;
        return hash;
    }

    @Override
    public TsDomain getSignificantDomain(TsFrequency freq, Day start, Day end) {
        TsPeriod pstart = new TsPeriod(freq, start), pend = new TsPeriod(freq, end);
        Day sday = Utilities.julianEaster(pstart.getYear(), true).plus(offset);
        if (start.isAfter(sday)) {
            pstart.move(1);
        }
        Day eday = Utilities.julianEaster(pend.getYear(), true).plus(offset);
        if (end.isBefore(eday)) {
            pend.move(-1);
        }
        int n = pend.minus(pstart) + 1;
        return new TsDomain(pstart, Math.max(0, n));
    }

    static class EasterDayInfo implements IDayInfo {

        public EasterDayInfo(TsFrequency freq, int year, int offset) {
            Day easter = Utilities.julianEaster(year, true);
            m_day = easter.plus(offset);
            m_freq = freq;
        }

        @Override
        public Day getDay() {
            return m_day;
        }

        @Override
        public TsPeriod getPeriod() {
            TsPeriod p = new TsPeriod(m_freq);
            p.set(m_day);
            return p;
        }

        @Override
        public DayOfWeek getDayOfWeek() {
            return m_day.getDayOfWeek();
        }
        final Day m_day;
        final TsFrequency m_freq;
    }

    static class JulianEasterDayList extends AbstractList<IDayInfo> {

        public JulianEasterDayList(TsFrequency freq, int offset, Day fstart, Day fend) {
            int ystart = fstart.getYear(), yend = fend.getYear();
            Day xday = Utilities.julianEaster(ystart, true).plus(offset);
            Day yday = Utilities.julianEaster(yend, true).plus(offset);

            if (xday.isBefore(fstart)) {
                ++ystart;
            }

            // pstart is the last valid period
            if (fend.isNotBefore(yday)) {
                ++yend;
            }

            m_n = yend - ystart;
            m_startyear = ystart;
            m_freq = freq;
            m_offset = offset;
        }
        private final int m_startyear, m_n, m_offset;
        private final TsFrequency m_freq;

        @Override
        public IDayInfo get(int index) {
            return new EasterDayInfo(m_freq, m_startyear + index, m_offset);
        }

        @Override
        public int size() {
            return m_n;
        }
    }
}
