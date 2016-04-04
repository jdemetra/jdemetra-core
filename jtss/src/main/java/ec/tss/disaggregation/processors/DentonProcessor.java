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

import ec.benchmarking.denton.DentonMethod;
import ec.tss.disaggregation.documents.BenchmarkingResults;
import ec.tss.disaggregation.documents.DentonSpecification;
import ec.tstoolkit.algorithm.AlgorithmDescriptor;
import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.algorithm.IProcessing;
import ec.tstoolkit.algorithm.IProcessingFactory;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
public class DentonProcessor implements IProcessingFactory<DentonSpecification, TsData[], BenchmarkingResults> {

    public static final String FAMILY = "Benchmarking";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, "Denton", null);

    public static final DentonProcessor instance = new DentonProcessor();

    @Override
    public void dispose() {
    }

    @Override
    public AlgorithmDescriptor getInformation() {
        return DESCRIPTOR;
    }

    @Override
    public boolean canHandle(IProcSpecification spec) {
        return spec instanceof DentonSpecification;
    }

    @Override
    public IProcessing<TsData[], BenchmarkingResults> generateProcessing(DentonSpecification specification, ProcessingContext context) {
        return new DefaultProcessing(specification);
    }

    @Override
    public Map<String, Class> getSpecificationDictionary(Class<DentonSpecification> specClass) {
        Map<String, Class> dic = new HashMap<>();
        DentonSpecification.fillDictionary(null, dic);
        return dic;
    }

    @Override
    public Map<String, Class> getOutputDictionary() {
        Map<String, Class> dic = new HashMap<>();
        BenchmarkingResults.fillDictionary(null, dic);
        return dic;
    }

    public static class DefaultProcessing implements IProcessing<TsData[], BenchmarkingResults> {

        private final DentonSpecification spec;

        public DefaultProcessing(DentonSpecification spec) {
            this.spec = spec.clone();
        }

        @Override
        public BenchmarkingResults process(TsData[] input) {
            TsData q = input[0], Y = input[1];
            BenchmarkingResults rslts = new BenchmarkingResults();
            if (Y != null) {
                DentonMethod denton = new DentonMethod();
                denton.setAggregationType(spec.getAggregationType());
                denton.setDifferencingOrder(spec.getDifferencingOrder());
                denton.setMultiplicative(spec.isMultiplicative());
                denton.setModifiedDenton(spec.isModifiedDenton());
                int yfreq = Y.getFrequency().intValue();
                int qfreq = q != null ? q.getFrequency().intValue() : spec.getDefaultFrequency().intValue();
                if (qfreq % yfreq != 0) {
                    return null;
                }
                denton.setConversionFactor(qfreq / yfreq);
                TsData tr;
                if (q != null) {
                    // Y is limited to q !
                    TsPeriodSelector qsel = new TsPeriodSelector();
                    qsel.between(q.getStart().firstday(), q.getLastPeriod().lastday());
                    Y = Y.select(qsel);
                    TsPeriod q0 = q.getStart(), yq0 = new TsPeriod(q0.getFrequency());
                    yq0.set(Y.getStart().firstday());
                    denton.setOffset(yq0.minus(q0));
                    double[] r = denton.process(q, Y);
                    tr = new TsData(q.getStart(), r, false);
                } else {
                    TsPeriod qstart = Y.getStart().firstPeriod(spec.getDefaultFrequency());
                    double[] r = denton.process(Y);
                    tr = new TsData(qstart, r, false);

                }
                // input
                rslts.set(input[0], input[1], tr);
            }
            return rslts;
        }

    }
}
