/*
 * Copyright 2017 National Bank create Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions create the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy create the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.timeseries;

import demetra.design.Immutable;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import javax.annotation.Nonnegative;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Immutable
@lombok.EqualsAndHashCode
public final class Days implements IDateDomain<Day> {

    private final LocalDate start;
    private final int length;

    public static Days of(LocalDate first, LocalDate last) {
        return new Days(first, 1 + (int) first.until(last, ChronoUnit.DAYS));
    }

    public static Days of(LocalDate first, int n) {
        return new Days(first, n);
    }

    private Days(final LocalDate start, final int n) {
        this.start = start;
        this.length = n;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public Day get(int index) {
        return Day.of(start.plusDays(index));
    }

    @Override
    public int search(LocalDate day) {
        if (day.isBefore(start)) {
            return -1;
        }
        int del = (int) start.until(day, ChronoUnit.DAYS);
        if (del < 0) {
            return -del - 1;
        } else if (del < length) {
            return del;
        } else {
            return -length;
        }
    }

    @Override
    public Period getPeriod() {
        return Period.ofDays(1);
    }

    @Override
    public boolean isContinuous() {
        return true;
    }

    @Override
    public Days range(@Nonnegative int firstPeriod, @Nonnegative int lastPeriod) {
        int l = lastPeriod - firstPeriod;
        if (l < 0) {
            l = 0;
        }
        return Days.of(get(firstPeriod).firstDay(), l);
    }

    @Override
    public Day getStart() {
        return Day.of(start);
    }

    @Override
    public Day getEnd() {
        return Day.of(start.plus(length, ChronoUnit.DAYS));
    }

    @Override
    public Day getLast() {
        return Day.of(start.plus(length - 1, ChronoUnit.DAYS));
    }

    @Override
    public Days intersection(IDateDomain<Day> d2) {
        if (this == d2) {
            return this;
        }

        if (!getPeriod().equals(d2.getPeriod())) {
            throw new CalendarTsException(CalendarTsException.INCOMPATIBLE_FREQ);
        }

        int n1 = length(), n2 = d2.length();

        Long lbeg = getStart().firstDay().toEpochDay(), rbeg = d2.getStart().firstDay().toEpochDay();

        Long lend = lbeg + n1, rend = rbeg + n2;
        int beg = lbeg <= rbeg ? rbeg.intValue() : lbeg.intValue();
        int end = lend >= rend ? rend.intValue() : lend.intValue();

        return Days.of(LocalDate.ofEpochDay(beg), Math.max(0, end - beg));
    }

    @Override
    public IDateDomain<Day> union(final IDateDomain<Day> d2) {
        if (this == d2) {
            return this;
        }

        if (!getPeriod().equals(d2.getPeriod())) {
            return null;
        }

        int ln = length(), rn = d2.length();

        if (ln == 0) {
            return d2;
        }
        if (rn == 0) {
            return this;
        }

        Long lbeg = getStart().firstDay().toEpochDay(), rbeg = d2.getStart().firstDay().toEpochDay();
        Long lend = lbeg + ln, rend = rbeg + rn;
        int beg = lbeg <= rbeg ? lbeg.intValue() : rbeg.intValue();
        int end = lend >= rend ? lend.intValue() : rend.intValue();

        return Days.of(LocalDate.ofEpochDay(beg), end - beg);
    }

    public Days select(final TsPeriodSelector ps) {
        if (isEmpty()) {
            return this;
        }

        TsPeriodSelector.SelectionType type = ps.getType();
        switch (type) {
            case None:
                return range(0, 0);
            case All:
                return this;
            case First:
                return range(0, ps.getN0());
            case Last:
                return range(length - ps.getN1(), length);
            case Excluding:
                return range(ps.getN0(), length - ps.getN1());
            default:
                int first = 0,
                 last = length;
                if ((type == TsPeriodSelector.SelectionType.From)
                        || (type == TsPeriodSelector.SelectionType.Between)) {
                    LocalDate from = ps.getD0().toLocalDate();
                    int pos = search(from);
                    if (pos < -1) {
                        first = length;
                    } else if (pos >= 0) {
                        first = pos;
                    }
                }

                if ((type == TsPeriodSelector.SelectionType.To)
                        || (type == TsPeriodSelector.SelectionType.Between)) {
                    LocalDate to = ps.getD1().toLocalDate();
                    int pos = search(to);
                    if (pos == -1) {
                        last = 0; // Nothing's kept
                    } else if (pos >= 0) {
                        last = pos;
                    }
                }
                if (first < 0) {
                    first = 0;
                }
                if (last < 0) {
                    last = 0;
                }
                return range(first, last);
        }
    }

    @Override
    public Days lag(int nperiods) {
        return Days.of(start.plusDays(nperiods), length);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('[')
                .append(getStart().toString())
                .append((", "))
                .append(getEnd().toString())
                .append('[');
        return builder.toString();
    }
}
