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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Immutable
@lombok.EqualsAndHashCode
public final class Day implements IDatePeriod {

    public static final Day BEG = Day.of(LocalDate.MIN), END = Day.of(LocalDate.MAX);

    public static Day of(LocalDate day) {
        return new Day(day);
    }

    public static Day of(int year, int month, int day) {
        return new Day(LocalDate.of(year, month, day));
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

    public LocalDate toLocalDate() {
        return day;
    }

    public long difference(Day d) {
        return d.day.until(day, ChronoUnit.DAYS);
    }
    
    public Day plus(long ndays){
        return new Day(day.plus(ndays, ChronoUnit.DAYS));
    }
    
    public Day minus(long ndays){
        return new Day(day.minus(ndays, ChronoUnit.DAYS));
    }
    
    public DayOfWeek getDayOfWeek(){
        return day.getDayOfWeek();
    }

    /**
     * Returns the number of days for the month before or equal to the given month.
     * We consider that there are 28 days in February
     * @param month 1-based index of the month
     * @return 
     */
    public static int getCumulatedMonthDays(int month) {
        return CUMULATEDMONTHDAYS[month];
    }

    /**
     * Cumulative number of days (if no leap year). CumulatedMonthDays[2] =
     * number of days from 1/1 to 28/2.
     */
    private static final int[] CUMULATEDMONTHDAYS = {0, 31, 59, 90, 120, 151,
        181, 212, 243, 273, 304, 334, 365};

}
