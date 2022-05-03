/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demetra.benchmarking.r;

import demetra.calendarization.CalendarizationResults;
import demetra.calendarization.CalendarizationSpec;
import demetra.data.DoubleSeq;
import demetra.timeseries.CalendarPeriod;
import demetra.timeseries.CalendarPeriodObs;
import demetra.timeseries.CalendarTimeSeries;
import demetra.timeseries.TsData;
import demetra.timeseries.TsUnit;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import jdplus.calendarization.CalendarizationProcessor;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Calendarization {

    public CalendarTimeSeries of(String[] start, double[] data) {
        if (start.length != data.length + 1) {
            throw new IllegalArgumentException();
        }
        List<CalendarPeriodObs> entries = new ArrayList<>();
        LocalDate e = LocalDate.parse(start[0], DateTimeFormatter.ISO_DATE), s = null;
        for (int i = 0; i < data.length; ++i) {
            s = e;
            entries.add(CalendarPeriodObs.of(s, e, data[i]));
        }
        return CalendarTimeSeries.of(entries);
    }

    public CalendarTimeSeries of(String[] start, String[] ends, double[] data) {
        if (start.length != data.length || ends.length != data.length) {
            throw new IllegalArgumentException();
        }
        List<CalendarPeriodObs> entries = new ArrayList<>();

        for (int i = 0; i < data.length; ++i) {
            LocalDate s = LocalDate.parse(start[0], DateTimeFormatter.ISO_DATE),
                    e = LocalDate.parse(ends[i], DateTimeFormatter.ISO_DATE);
            entries.add(CalendarPeriodObs.of(s, e, data[i]));
        }
        return CalendarTimeSeries.of(entries);
    }
    
    public CalendarizationResults process(CalendarTimeSeries ts, int period, String start, String end, double[] weights, boolean stde){
        LocalDate s=start == null ? null : LocalDate.parse(start, DateTimeFormatter.ISO_DATE);
        LocalDate e=start == null ? null : LocalDate.parse(end, DateTimeFormatter.ISO_DATE);
        TsUnit unit;
        if (period != 52)
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
