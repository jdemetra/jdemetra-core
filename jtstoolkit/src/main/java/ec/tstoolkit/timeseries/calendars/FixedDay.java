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

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class FixedDay implements ISpecialDay {
    
    public final int day;
    public final Month month;
    private final double weight;
    
    public FixedDay(int day, Month month) {
        this(day, month, 1);
    }
    
    public FixedDay(int day, Month month, double weight) {
        this.day = day;
        this.month = month;
        this.weight = weight;
    }
    
    public FixedDay reweight(double nweight) {
        if (weight == this.weight) {
            return this;
        }
        return new FixedDay(day, month, nweight);
    }
    
    @Override
    public double getWeight() {
        return weight;
    }
    
    @Override
    public boolean match(Context context){
        return true;
    }
    
    public Day calcDay(int year) {
        return new Day(year, month, day);
    }
    public static final FixedDay Christmas = new FixedDay(24, Month.December), NewYear = new FixedDay(0, Month.January),
            Assumption = new FixedDay(14, Month.August), MayDay = new FixedDay(0, Month.May),
            AllSaintsDay = new FixedDay(0, Month.November), Halloween = new FixedDay(30, Month.October);
    
    @Override
    public Iterable<IDayInfo> getIterable(TsFrequency freq, Day start, Day end) {
        return new FixedDayIterable(freq, this, start, end);
    }
    
    @Override
    public double[][] getLongTermMeanEffect(int freq) {
        int c = 12 / freq;
        int imonth = month.intValue();
        int p = imonth / c;
        double[] m = new double[7];
        for (int i = 0; i < 6; ++i) {
            m[i] = weight / 7;
        }
        m[6] = -weight * 6 / 7;
        
        double[][] rslt = new double[freq][];
        rslt[p] = m;
        return rslt;
    }
    
    public FixedDay plus(int offset) {
        if (offset == 0) {
            return this;
        }
        int imonth = month.intValue();
        int pos = Day.getCumulatedMonthDays(imonth) + day;
        pos += offset;
        if (pos < 0 || pos >= 365) {
            return null;
        }
        int nmonth = 0;
        while (pos >= Day.getCumulatedMonthDays(nmonth + 1)) {
            ++nmonth;
        }
        int nday = pos - Day.getCumulatedMonthDays(nmonth);
        // avoid leap year
        if (imonth <= 1 && nmonth >= 2) {
            return null;
        }
        return new FixedDay(nday, Month.valueOf(nmonth), weight);
    }
    
    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof FixedDay && equals((FixedDay) obj));
    }
    
    private boolean equals(FixedDay other) {
        return other.day == day && other.month == month && other.weight == weight;
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 31 * hash + this.day;
        hash = 31 * hash + Objects.hashCode(this.month);
        return hash;
    }
    
    @Override
    public TsDomain getSignificantDomain(TsFrequency freq, Day start, Day end) {
        TsPeriod pstart = new TsPeriod(freq, start), pend = new TsPeriod(freq, end);
        Day sday = new Day(pstart.getYear(), month, day);
        if (start.isAfter(sday)) {
            pstart.move(1);
        }
        Day eday = new Day(pend.getYear(), month, day);
        if (end.isBefore(eday)) {
            pend.move(-1);
        }
        int n = pend.minus(pstart) + 1;
        return new TsDomain(pstart, Math.max(0, n));
    }
    
    static class FixedDayInfo implements IDayInfo {
        
        FixedDayInfo(TsPeriod period, FixedDay fday) {
            m_fday = fday;
            m_period = period.clone();
        }
        
        @Override
        public Day getDay() {
            return new Day(m_period.getYear(), m_fday.month, m_fday.day);
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
        final FixedDay m_fday;
    }
    
    static class FixedDayIterable implements Iterable<IDayInfo> {
        
        FixedDayIterable(TsFrequency freq, FixedDay fday, Day fstart, Day fend) {
            this.fday = fday;
            int ystart = fstart.getYear(), yend = fend.getYear();
            Day xday = new Day(ystart, fday.month, fday.day);
            Day yday = new Day(yend, fday.month, fday.day);
            
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
        private final FixedDay fday;
        private final TsPeriod pstart;
        private final int m_n;
        
        @Override
        public Iterator<IDayInfo> iterator() {
            return new Iterator<IDayInfo>() {
                FixedDayInfo m_info = new FixedDayInfo(pstart, fday);
                int m_cur = -1;
                
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
