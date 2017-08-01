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
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
@Immutable
@lombok.EqualsAndHashCode
public final class DailyPeriod implements IDatePeriod {
    
    public static DailyPeriod of(LocalDate first, LocalDate last) {
        if (last.isBefore(last))
            return new DailyPeriod(first, 0);
        int del = 1 + (int) first.until(last, ChronoUnit.DAYS);
        return new DailyPeriod(first, del);
    }

    public static DailyPeriod of(LocalDate first, int lengthInDays) {
        return new DailyPeriod(first, lengthInDays);
    }

    private final LocalDate first;
    private final int n;

    private DailyPeriod(LocalDate first, int n) {
        this.first = first;
        this.n = n;
    }
    
    public boolean isEmpty(){
        return n == 0;
    }

    @Override
    public boolean contains(LocalDate d) {
        int del = (int) first.until(d, ChronoUnit.DAYS);
        return del >= 0 && del < n;
    }

    @Override
    public LocalDate firstDay() {
        return first;
    }

    @Override
    public LocalDate lastDay() {
        return first.plusDays(n - 1);
    }

    public int lengthInDays() {
        return n;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(first.toString()).append(('.')).append(lastDay().toString());
        return builder.toString();
    }
}
