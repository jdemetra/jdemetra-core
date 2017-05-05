/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.timeseries;

import demetra.design.Immutable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;

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
    public DailyPeriod elementAt(int index) {
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
    public Period toPeriod() {
        return PERIOD;
    }
}
