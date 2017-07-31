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
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;

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
}
