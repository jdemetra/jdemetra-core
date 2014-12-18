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
import ec.tstoolkit.timeseries.regression.EasterVariable;
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
@Development(status = Development.Status.Preliminary)
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

    private static final Map<Integer, Day> g_dic = new HashMap<>();
    private static final int[] g_days = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    public final int offset;
    private final double weight;

    /**
     * Creates a new Easter related day, with 0 offset. Corresponds to easter
     */
    public EasterRelatedDay() {
        this(0, 1);
    }

    public EasterRelatedDay(int offset) {
        this(offset, 1);
    }

    public EasterRelatedDay(int offset, double weight) {
        this.weight = weight;
        this.offset = offset;
    }

    public EasterRelatedDay reweight(double nweight) {
        if (nweight == weight) {
            return this;
        }
        return new EasterRelatedDay(offset, nweight);
    }

    public EasterRelatedDay plus(int ndays) {
        return new EasterRelatedDay(offset + ndays, weight);
    }

    @Override
    public double getWeight() {
        return weight;
    }
    public static final EasterRelatedDay AshWednesday = new EasterRelatedDay(-46),
            Easter = new EasterRelatedDay(0),
            EasterMonday = new EasterRelatedDay(1),
            EasterFriday = new EasterRelatedDay(-2),
            EasterThursday = new EasterRelatedDay(-3),
            Ascension = new EasterRelatedDay(39),
            Pentecost = new EasterRelatedDay(49),
            PentecostMonday = new EasterRelatedDay(50);

    public Day calcDay(int year) {
        Day d = easter(year);
        if (offset != 0) {
            d = d.plus(offset);
        }
        return d;
    }

    private Day easter(int year) {
        synchronized (g_dic) {
            Day e = g_dic.get(year);
            if (e == null) {
                e = Utilities.easter(year);
                g_dic.put(year, e);

            }
            // e is a struct. Returns a clone version in a Java implementation !!!
            return e;
        }
    }

    /**
     *
     * @param month 0-based
     * @param day 0-based
     * @return
     */
    public static double probEaster(int month, int day) {
        if (month == 2) {
            if (day < 21) {
                return 0;
            } else if (day < 27) {
                return (day - 20) / (7 * EasterVariable.LUNARY);
            } else {
                return 1 / EasterVariable.LUNARY;
            }
        } else if (month == 3) {
            if (day < 18) {
                return 1 / EasterVariable.LUNARY;
            } else if (day < 25) {
                return (25 - day + EasterVariable.DEC_LUNARY) / (7 * EasterVariable.LUNARY);
            } else {
                return 0;
            }

        } else {
            return 0;
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
            return (pos + 1) / (7 * EasterVariable.LUNARY);
        } else if (pos < 28) {
            return 1 / EasterVariable.LUNARY;
        } else {
            return (35 - pos + EasterVariable.DEC_LUNARY) / (7 * EasterVariable.LUNARY);
        }
    }

    @Override
    public Iterable<IDayInfo> getIterable(TsFrequency freq, Day start, Day end) {
        return new EasterDayList(freq, offset, start, end);
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

        // Easter always falls between March, 22 and April, 25 (inclusive). The probability to get a specific day is defined by probEaster.
        // We don't take into account leap year. So, the solution is slightly wrong for offset
        // <= -50.
        // The considered day falls between ...

        int d0 = 31 + 28 + 21 + offset, d1 = 31 + 28 + 31 + 25 + offset; // d1 excluded

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
                    c1 += g_days[i * c + j];
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
        Day sday = Utilities.easter(pstart.getYear()).plus(offset);
        if (start.isAfter(sday)) {
            pstart.move(1);
        }
        Day eday = Utilities.easter(pend.getYear()).plus(offset);
        if (end.isBefore(eday)) {
            pend.move(-1);
        }
        int n = pend.minus(pstart) + 1;
        return new TsDomain(pstart, Math.max(0, n));
    }

    static class EasterDayInfo implements IDayInfo {

        public EasterDayInfo(TsFrequency freq, int year, int offset) {
            Day easter = Utilities.easter(year);
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

    static class EasterDayList extends AbstractList<IDayInfo> {

        public EasterDayList(TsFrequency freq, int offset, Day fstart, Day fend) {
            int ystart = fstart.getYear(), yend = fend.getYear();
            Day xday = Utilities.easter(ystart).plus(offset);
            Day yday = Utilities.easter(yend).plus(offset);

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
