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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import javax.annotation.Nonnegative;

/**
 *
 * @author Jean Palate
 */
@Immutable
@lombok.EqualsAndHashCode
public final class Weeks implements IDateDomain<DailyPeriod> {

    public static Weeks of(LocalDate start, int nweeks) {
        return new Weeks(start, nweeks);
    }

    public static Weeks between(DayOfWeek firstDay, LocalDate start, LocalDate last) {
        DayOfWeek sdw = start.getDayOfWeek();
        int del = 1 + (int) start.until(last, ChronoUnit.DAYS);

        if (sdw == firstDay) {
            return new Weeks(start, del / 7);
        } else {
            int corr = firstDay.getValue() - sdw.getValue();
            if (corr < 0) {
                corr = 7 + corr;
            }
            return new Weeks(start.plusDays(corr), (del - corr) / 7);
        }
    }

    private static final Period PERIOD = Period.ofWeeks(1);

    private final LocalDate firstDay;
    private final int nweeks;

    private Weeks(LocalDate firstDay, int nweeks) {
        this.firstDay = firstDay;
        this.nweeks = nweeks;
    }

    @Override
    public int length() {
        return nweeks;
    }

    @Override
    public DailyPeriod get(int index) {
        return DailyPeriod.of(firstDay.plusDays(7 * index), 7);
    }

    @Override
    public int search(LocalDate d) {
        int del = (int) firstDay.until(d, ChronoUnit.DAYS);
        if (del < 0) {
            return -1;
        }
        if (del >= nweeks * 7) {
            return -nweeks;
        }
        return del / 7;
    }

    @Override
    public boolean isContinuous() {
        return true;
    }

    @Override
    public Period getPeriod() {
        return PERIOD;
    }

    @Override
    public Weeks range(@Nonnegative int firstPeriod, @Nonnegative int lastPeriod) {
        int l = lastPeriod - firstPeriod;
        if (l < 0) {
            l = 0;
        }
        return Weeks.of(get(firstPeriod).firstDay(), l);
    }

    @Override
    public DailyPeriod getStart() {
        return get(0);
    }

    @Override
    public DailyPeriod getEnd() {
        return get(nweeks);
    }

    @Override
    public DailyPeriod getLast() {
        return get(nweeks - 1);
    }

    @Override
    public Weeks intersection(IDateDomain<DailyPeriod> d2) {
        if (this == d2) {
            return this;
        }

        if (!getPeriod().equals(d2.getPeriod())) {
            throw new CalendarTsException(CalendarTsException.INCOMPATIBLE_FREQ);
        }

        LocalDate start1 = getStart().firstDay(), start2 = d2.getStart().firstDay();
        LocalDate end1 = getLast().lastDay(), end2 = d2.getLast().lastDay();

        LocalDate s = start1.isBefore(start2) ? start2 : start1;
        LocalDate e = end1.isBefore(end2) ? end1 : end2;
        return Weeks.of(s, (int) s.until(e, ChronoUnit.WEEKS));
    }

    @Override
    public IDateDomain<DailyPeriod> union(final IDateDomain<DailyPeriod> d2) {
        throw new UnsupportedOperationException("union()");
    }

    @Override
    public Weeks select(TsPeriodSelector selector) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Weeks lag(int nperiods) {
        return Weeks.of(firstDay.plusWeeks(nperiods), nweeks);
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
