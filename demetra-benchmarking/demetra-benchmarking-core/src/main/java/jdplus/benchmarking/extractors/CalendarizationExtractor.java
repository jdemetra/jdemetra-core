/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.benchmarking.extractors;

import demetra.calendarization.CalendarizationResults;
import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import jdplus.stats.likelihood.LikelihoodStatistics;
import demetra.timeseries.TsData;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(InformationExtractor.class)
public class CalendarizationExtractor extends InformationMapping<CalendarizationResults> {

    public final String AGG = "agg", EAGG = "eagg";

    public CalendarizationExtractor() {
        set(AGG, TsData.class, source -> source.getAggregatedSeries());
        set(EAGG, TsData.class, source -> source.getStdevAggregatedSeries());
    }

    @Override
    public Class getSourceClass() {
        return CalendarizationResults.class;
    }

}
