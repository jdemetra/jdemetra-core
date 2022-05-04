/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demetra.benchmarking.r;

import demetra.calendarization.CalendarizationResults;
import demetra.calendarization.CalendarizationSpec;
import demetra.data.AggregationType;
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
