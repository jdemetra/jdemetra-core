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
