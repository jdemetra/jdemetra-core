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
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.Iterator;
import java.util.Objects;

@Development(status = Development.Status.Preliminary)
public class FixedWeekDay implements ISpecialDay {

    public final DayOfWeek dayOfWeek;
    public final int week;
    public final Month month;
    private final double weight;

    public FixedWeekDay(int week, DayOfWeek day, Month month) {
        this.week = week;
        this.dayOfWeek = day;
        this.month = month;
        weight = 1;
    }

    public FixedWeekDay(int week, DayOfWeek day, Month month, double weight) {
        this.week = week;
        this.dayOfWeek = day;
        this.month = month;
        this.weight = weight;
    }

    public FixedWeekDay reweight(double nweight) {
        return new FixedWeekDay(week, dayOfWeek, month, nweight);
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public boolean match(Context context) {
        return true;
    }

    public Day calcDay(int year) {
        Day d = Utilities.firstWeekDay(dayOfWeek, year, month);
        if (week > 0) {
            d = d.plus(week * 7);
        }
        return d;
    }

    public static FixedWeekDay add(FixedWeekDay fd, int offset) {
        if (offset == 0) {
            return fd;
        }
        int pos = fd.week * 7 + offset + fd.dayOfWeek.intValue();
        if (pos < 0 || pos >= 28) {
            return null;
        }
        return new FixedWeekDay(pos / 7, DayOfWeek.valueOf(pos % 7), fd.month, fd.weight);
    }
    public static final FixedWeekDay LaborDay = new FixedWeekDay(0, DayOfWeek.Monday, Month.September), ThanksGiving = new FixedWeekDay(3, DayOfWeek.Thursday, Month.November);

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof FixedWeekDay && equals((FixedWeekDay) obj));
    }

    private boolean equals(FixedWeekDay other) {
        return other.dayOfWeek == dayOfWeek && other.month == month && other.week == week && other.weight == weight;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.dayOfWeek);
        hash = 89 * hash + this.week;
        hash = 89 * hash + Objects.hashCode(this.month);
        return hash;
    }

    @Override
    public Iterable<IDayInfo> getIterable(TsFrequency freq, Day start, Day end) {
        return new FixedWeekDayIterable(freq, this, start, end);
    }

    @Override
    public double[][] getLongTermMeanEffect(int freq) {
        int w = dayOfWeek.intValue(); // Sunday = 0
        if (w == 0) {
            return null;
        }
        --w;
        int c = 12 / freq;
        int imonth = month.intValue();
        int p = imonth / c;
        double[] m = new double[7];
        m[w] = weight;
        m[6] = -weight;

        double[][] rslt = new double[freq][];
        rslt[p] = m;
        return rslt;
    }

    @Override
    public TsDomain getSignificantDomain(TsFrequency freq, Day start, Day end) {
        TsPeriod pstart = new TsPeriod(freq, start), pend = new TsPeriod(freq, end);
        Day sday = Utilities.firstWeekDay(dayOfWeek, pstart.getYear(), month);
        if (week > 0) {
            sday = sday.plus(week * 7);
        }
        if (start.isAfter(sday)) {
            pstart.move(1);
        }
        Day eday = Utilities.firstWeekDay(dayOfWeek, pend.getYear(), month);
        if (week > 0) {
            eday = eday.plus(week * 7);
        }
        if (end.isBefore(eday)) {
            pend.move(-1);
        }
        int n = pend.minus(pstart) + 1;
        return new TsDomain(pstart, Math.max(0, n));
    }

    static class FixedWeekDayInfo implements IDayInfo {

        FixedWeekDayInfo(TsPeriod period, FixedWeekDay fday) {
            m_fday = fday;
            m_period = period.clone();
        }

        @Override
        public Day getDay() {
            Day d = Utilities.firstWeekDay(m_fday.dayOfWeek, m_period.getYear(), m_fday.month);
            if (m_fday.week > 0) {
                d = d.plus(m_fday.week * 7);
            }
            return d;
        }

        @Override
        public TsPeriod getPeriod() {
            return m_period;
        }

        @Override
        public DayOfWeek getDayOfWeek() {
            return getDay().getDayOfWeek();
        }

        public void move(int n) {
            m_period.move(n * m_period.getFrequency().intValue());
        }
        final TsPeriod m_period;
        final FixedWeekDay m_fday;
    }

    static class FixedWeekDayIterable implements Iterable<IDayInfo> {

        FixedWeekDayIterable(TsFrequency freq, FixedWeekDay fday, Day fstart, Day fend) {
            this.fday = fday;
            int ystart = fstart.getYear(), yend = fend.getYear();
            Day xday = fday.calcDay(ystart);
            Day yday = fday.calcDay(yend);

            pstart = new TsPeriod(freq);
            pstart.set(xday);
            TsPeriod pend = new TsPeriod(freq);
            pend.set(yday);

            // pstart is the last period strictly before the valid time span
            if (xday.isNotBefore(fstart)) {
                pstart.move(-freq.intValue());
            }

            // pstart is the last valid period
            if (yday.isAfter(fend)) {
                pend.move(-freq.intValue());
            }

            m_n = pend.getYear() - pstart.getYear();
        }
        private final FixedWeekDay fday;
        private final TsPeriod pstart;
        private final int m_n;

        @Override
        public Iterator<IDayInfo> iterator() {
            return new Iterator<IDayInfo>() {
                private int m_cur = -1;
                private FixedWeekDayInfo m_info = new FixedWeekDayInfo(pstart, fday);

                @Override
                public boolean hasNext() {
                    return m_cur < m_n - 1;
                }

                @Override
                public IDayInfo next() {
                    m_info.move(1);
                    ++m_cur;
                    return m_info;
                }
            };
        }
    }
}
