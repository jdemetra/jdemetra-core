/*
 * Copyright 2022 National Bank of Belgium
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
package demetra.benchmarking.r;

import demetra.calendarization.CalendarizationResults;
import demetra.calendarization.CalendarizationSpec;
import demetra.timeseries.CalendarTimeSeries;
import demetra.timeseries.TsUnit;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import jdplus.calendarization.CalendarizationProcessor;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Calendarization {

    public CalendarizationResults process(CalendarTimeSeries ts, int period, String start, String end, double[] weights, boolean stde){
        LocalDate s=start == null ? null : LocalDate.parse(start, DateTimeFormatter.ISO_DATE);
        LocalDate e=end == null ? null : LocalDate.parse(end, DateTimeFormatter.ISO_DATE).plusDays(1);
        TsUnit unit;
        if (period <= 0)
            unit=TsUnit.UNDEFINED;
        else if (period != 52)
            unit=TsUnit.ofAnnualFrequency(period);
        else
            unit=TsUnit.WEEK;
        CalendarizationSpec spec=CalendarizationSpec.builder()
                .start(s)
                .end(e)
                .dailyWeights(weights)
                .aggregationUnit(unit)
                .stdev(stde)
                .build();
        return CalendarizationProcessor.PROCESSOR.process(ts, spec);
    }
}
