/*
 * Copyright 2020 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.timeseries.calendars;

import demetra.design.Development;
import demetra.timeseries.ValidityPeriod;
import java.time.LocalDate;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.Value
public class FixedDay implements Holiday {

    private int month;
    private int day;
    private double weight;
    private ValidityPeriod validityPeriod;

    /**
     *
     * @param month Month, 1-based
     * @param day Day of the month, 1-based
     */
    public FixedDay(int month, int day) {
        this(month, day, 1, ValidityPeriod.ALWAYS);
    }

    /**
     *
     * @param month Month, 1-based
     * @param day Day of the month, 1-based
     * @param weight Weight of the holiday
     * @param validityPeriod
     */
    public FixedDay(int month, int day, double weight, ValidityPeriod validityPeriod) {
        this.day = day;
        this.month = month;
        this.weight = weight;
        this.validityPeriod = validityPeriod;
    }

    @Override
    public FixedDay reweight(double nweight) {
        if (weight == this.weight) {
            return this;
        }
        return new FixedDay(day, month, nweight, validityPeriod);
    }

    @Override
    public FixedDay forPeriod(LocalDate start, LocalDate end) {
        if (validityPeriod.getStart() != start && validityPeriod.getEnd() != end) {
            return new FixedDay(day, month, weight, ValidityPeriod.between(start, end));
        } else {
            return this;
        }
    }

    public static final FixedDay CHRISTMAS = new FixedDay(12, 25), NEWYEAR = new FixedDay(1, 1),
            ASSUMPTION = new FixedDay(8, 15), MAYDAY = new FixedDay(5, 1),
            ALLSAINTSDAY = new FixedDay(11, 1), ARMISTICE = new FixedDay(11, 11), HALLOWEEN = new FixedDay(10, 31);

    public FixedDay plus(int offset) {
        if (offset == 0) {
            return this;
        }
        // position in the year (1-based)
        int pos = CalendarUtility.getCumulatedMonthDays(month - 1) + day;
        pos += offset;
        if (pos > 365) {
            pos -= 365;
        } else if (pos <= 0) {
            pos += 365;
        }
        int nmonth = 0;
        while (pos > CalendarUtility.getCumulatedMonthDays(nmonth + 1)) {
            ++nmonth;
        }
        int nday = pos - CalendarUtility.getCumulatedMonthDays(nmonth);
        // avoid leap year
        if (month <= 2 && nmonth >= 2) {
            return null;
        }
        return new FixedDay(nmonth + 1, nday, weight, validityPeriod);
    }

}
