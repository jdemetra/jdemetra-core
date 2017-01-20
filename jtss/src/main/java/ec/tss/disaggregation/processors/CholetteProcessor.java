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

import ec.benchmarking.simplets.TsCholette;
import ec.tss.disaggregation.documents.BenchmarkingResults;
import ec.tss.disaggregation.documents.UniCholetteSpecification;
import ec.tstoolkit.algorithm.AlgorithmDescriptor;
import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.algorithm.IProcessing;
import ec.tstoolkit.algorithm.IProcessingFactory;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jean
 */
public class CholetteProcessor implements IProcessingFactory<UniCholetteSpecification, TsData[], BenchmarkingResults> {

    public static final String FAMILY = "Benchmarking";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, "Cholette", null);

    public static final CholetteProcessor instance = new CholetteProcessor();

    protected CholetteProcessor() {
    }

    @Override
    public void dispose() {
    }

    @Override
    public AlgorithmDescriptor getInformation() {
        return DESCRIPTOR;
    }

    @Override
    public boolean canHandle(IProcSpecification spec) {
        return spec instanceof UniCholetteSpecification;
    }

    @Override
    public IProcessing<TsData[], BenchmarkingResults> generateProcessing(final UniCholetteSpecification spec, ProcessingContext context) {
        return new DefaultProcessing(spec);
    }

    public IProcessing<TsData[], BenchmarkingResults> generateProcessing(final UniCholetteSpecification spec) {
        return new DefaultProcessing(spec);
    }

    @Override
    public Map<String, Class> getSpecificationDictionary(Class<UniCholetteSpecification> specClass) {
        Map<String, Class> dic = new HashMap<>();
        UniCholetteSpecification.fillDictionary(null, dic);
        return dic;
    }

    @Override
    public Map<String, Class> getOutputDictionary(boolean compact) {
        Map<String, Class> dic = new HashMap<>();
        BenchmarkingResults.fillDictionary(null, dic, compact);
        return dic;
    }

    public static class DefaultProcessing implements IProcessing<TsData[], BenchmarkingResults> {

        private final UniCholetteSpecification spec;

        public DefaultProcessing(UniCholetteSpecification spec) {
            this.spec = spec.clone();
        }

        @Override
        public BenchmarkingResults process(TsData[] input) {
            TsData s = input[0], c = input[1];
            BenchmarkingResults rslts = new BenchmarkingResults();
            if (s != null && c != null) {
                TsCholette cholette = new TsCholette();
                cholette.setRho(spec.getRho());
                cholette.setLambda(spec.getLambda());
                TsFrequency agg = spec.getAggregationFrequency();
                if (agg != TsFrequency.Undefined && s.getFrequency().intValue() > agg.intValue()) {
                    s = s.changeFrequency(agg, spec.getAggregationType(), true);
                }
                cholette.setAggregationType(spec.getAggregationType());
                TsData bench = cholette.process(s, c);
                // input
                rslts.set(s, input[1], bench);
            }
            return rslts;
        }

    }
}
