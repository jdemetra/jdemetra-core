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
import demetra.timeseries.Fixme;
import demetra.timeseries.RegularDomain;
import demetra.timeseries.TsUnit;
import demetra.timeseries.TsPeriod;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class FixedDay implements IHoliday {

    public final int day;
    public final int month;
    private final double weight;

    public FixedDay(int month, int day) {
        this(month, day, 1);
    }

    public FixedDay(int month, int day, double weight) {
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
    public boolean match(Context context) {
        return true;
    }

    public static final FixedDay CHRISTMAS = new FixedDay(12, 25), NEWYEAR = new FixedDay(1, 1),
            ASSUMPTION = new FixedDay(8, 15), MAYDAY = new FixedDay(5, 1),
            ALLSAINTSDAY = new FixedDay(11, 1), HALLOWEEN = new FixedDay(10, 31);

    @Override
    public Iterable<IHolidayInfo> getIterable(TsUnit freq, LocalDate start, LocalDate end) {
        return new FixedDayIterable(freq, this, start, end);
    }

    @Override
    public double[][] getLongTermMeanEffect(int freq) {
        int c = 12 / freq;
        int p = month / c;
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
        int pos = Fixme.getCumulatedMonthDays(month) + day;
        pos += offset;
        if (pos < 0 || pos >= 365) {
            return null;
        }
        int nmonth = 0;
        while (pos >= Fixme.getCumulatedMonthDays(nmonth + 1)) {
            ++nmonth;
        }
        int nday = pos - Fixme.getCumulatedMonthDays(nmonth);
        // avoid leap year
        if (month <= 1 && nmonth >= 2) {
            return null;
        }
        return new FixedDay(nmonth, nday, weight);
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
    public RegularDomain getSignificantDomain(RegularDomain domain) {
        if (!domain.getStartPeriod().getUnit().getChronoUnit().equals(ChronoUnit.DAYS)) {
            throw new IllegalArgumentException();
        }
        LocalDate first = domain.start().toLocalDate(), last = domain.end().toLocalDate().minusDays(1);
        LocalDate efirst = LocalDate.of(first.getYear(), month, day);
        LocalDate elast = LocalDate.of(last.getYear(), month, day);
        if (efirst.isBefore(first)) {
            efirst = LocalDate.of(first.getYear() + 1, month, day);
        }
        if (elast.isAfter(last)) {
            elast = LocalDate.of(last.getYear() - 1, month, day);
        }
        if (efirst.isAfter(elast)) {
            return RegularDomain.of(domain.getStartPeriod().withDate(efirst), 0);
        } else {
            return RegularDomain.of(domain.getStartPeriod().withDate(efirst), (int) ChronoUnit.DAYS.between(efirst, elast));
        }
    }

    static class FixedDayInfo implements IHolidayInfo {

        FixedDayInfo(TsPeriod period, FixedDay fday) {
            m_fday = fday;
            m_period = period;
        }

        @Override
        public LocalDate getDay() {
            return LocalDate.of(m_period.start().getYear(), m_fday.month, m_fday.day);
        }

//        @Override
//        public TsPeriod getPeriod() {
//            return m_period;
//        }

        final TsPeriod m_period;
        final FixedDay m_fday;
    }

    static class FixedDayIterable implements Iterable<IHolidayInfo> {

        FixedDayIterable(TsUnit freq, FixedDay fday, LocalDate fstart, LocalDate fend) {
            this.fday = fday;
            int ystart = fstart.getYear(), yend = fend.getYear();
            LocalDate xday = LocalDate.of(ystart, fday.month, fday.day);
            LocalDate yday = LocalDate.of(yend, fday.month, fday.day);

            TsPeriod start = TsPeriod.of(freq, xday);
            int nyears = yend - ystart;

            // pstart is the first valid period
            if (xday.isBefore(fstart)) {
                start = start.plus(Fixme.getAsInt(freq));
                --nyears;
            }

            // pend is the first invalidvalid period
            if (yday.isBefore(fend)) {
                ++nyears;
            }

            pstart = start;
            n = nyears;
        }
        private final FixedDay fday;
        private final TsPeriod pstart;
        private final int n;

        @Override
        public Iterator<IHolidayInfo> iterator() {
            return new Iterator<IHolidayInfo>() {
                int cur = 0;

                @Override
                public boolean hasNext() {
                    return cur < n;
                }

                @Override
                public IHolidayInfo next() {
                    if (cur == 0) {
                        ++cur;
                        return new FixedDayInfo(pstart, fday);
                    } else {
                        return new FixedDayInfo(pstart.plus(Fixme.getAsInt(pstart.getUnit()) * (cur++)), fday);
                    }
                }
            };
        }
    }
}
