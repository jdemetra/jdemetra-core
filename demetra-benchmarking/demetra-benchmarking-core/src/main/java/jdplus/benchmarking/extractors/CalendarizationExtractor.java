/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.benchmarking.extractors;

import demetra.calendarization.CalendarizationResults;
import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import demetra.timeseries.TsData;
import java.time.format.DateTimeFormatter;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(InformationExtractor.class)
public class CalendarizationExtractor extends InformationMapping<CalendarizationResults> {

    public final String AGG = "agg", EAGG = "eagg", DAYS="days", EDAYS="edays", START="start";

    public CalendarizationExtractor() {
        set(AGG, TsData.class, source -> source.getAggregatedSeries());
        set(EAGG, TsData.class, source -> source.getStdevAggregatedSeries());
        set(DAYS, double[].class, source -> source.getDailyData());
        set(EDAYS, double[].class, source -> source.getDailyStdev());
        set(START, String.class, source -> source.getStart().format(DateTimeFormatter.ISO_DATE));
 }

    @Override
    public Class getSourceClass() {
        return CalendarizationResults.class;
    }

}
