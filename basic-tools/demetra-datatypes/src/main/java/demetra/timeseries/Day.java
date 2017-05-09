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
import java.time.LocalDateTime;

@Immutable
@lombok.EqualsAndHashCode
public final class Day implements IDatePeriod {

    public static Day of(LocalDate day) {
        return new Day(day);
    }

    private final LocalDate day;

    private Day(LocalDate day) {
        this.day = day;
    }

    @Override
    public boolean contains(LocalDate dt) {
        return day.equals(dt);
    }

    @Override
    public LocalDate firstDay() {
        return day;
    }

    @Override
    public LocalDate lastDay() {
        return day;
    }

    @Override
    public boolean contains(LocalDateTime dt) {
        return dt.toLocalDate().equals(day);
    }

    @Override
    public String toString() {
        return day.toString();
    }
}
