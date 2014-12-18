/*
 * Copyright 2013 National Bank of Belgium
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
package ec.tss.disaggregation.processors;

import ec.benchmarking.simplets.Calendarization;
import ec.benchmarking.simplets.Calendarization.PeriodObs;
import ec.tss.disaggregation.documents.CalendarizationResults;
import ec.tss.disaggregation.documents.CalendarizationSpecification;
import ec.tstoolkit.algorithm.AlgorithmDescriptor;
import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.algorithm.IProcessing;
import ec.tstoolkit.algorithm.IProcessingFactory;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Mats Maggi
 */
public class CalendarizationProcessor implements IProcessingFactory<CalendarizationSpecification, List<PeriodObs>, CalendarizationResults> {

    public static final String FAMILY = "Benchmarking";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, "Calendarization", null);

    public static final CalendarizationProcessor instance = new CalendarizationProcessor();

    @Override
    public void dispose() {
    }

    @Override
    public AlgorithmDescriptor getInformation() {
        return DESCRIPTOR;
    }

    @Override
    public boolean canHandle(IProcSpecification spec) {
        return spec instanceof CalendarizationSpecification;
    }

    @Override
    public IProcessing<List<PeriodObs>, CalendarizationResults> generateProcessing(CalendarizationSpecification specification, ProcessingContext context) {
        return new DefaultProcessing(specification);
    }

    @Override
    public Map<String, Class> getSpecificationDictionary(Class<CalendarizationSpecification> specClass) {
        Map<String, Class> dic = new HashMap<>();
        CalendarizationSpecification.fillDictionary(null, dic);
        return dic;
    }

    @Override
    public Map<String, Class> getOutputDictionary() {
        Map<String, Class> dic = new HashMap<>();
        CalendarizationResults.fillDictionary(null, dic);
        return dic;
    }

    public static class DefaultProcessing implements IProcessing<List<PeriodObs>, CalendarizationResults> {

        private final CalendarizationSpecification spec;

        public DefaultProcessing(CalendarizationSpecification spec) {
            this.spec = spec.clone();
        }

        @Override
        public CalendarizationResults process(List<PeriodObs> input) {
            CalendarizationResults rslts = new CalendarizationResults();
            Calendarization cal = new Calendarization();

            // Add observations
            for (PeriodObs p : input) {
                cal.add(p.start, p.end, p.value);
            }

            cal.setDailyWeights(spec.getWeights());
            TsFrequency freq = spec.getAggFrequency();
            TsPeriod end = new TsPeriod(freq);
            end.set(input.get(input.size() - 1).end);
            cal.setSpan(input.get(0).start, end.lastday());

            // Get data
            TsData agg = cal.getAggregates(freq);
            TsData aggStdev = cal.getAggregatesStdev(freq);
            double[] smooth = cal.getSmoothedData();
            double[] smoothStdev = cal.getSmoothedStdev();
            
            rslts.set(smooth, smoothStdev, agg, aggStdev);

            return rslts;
        }

    }

}
