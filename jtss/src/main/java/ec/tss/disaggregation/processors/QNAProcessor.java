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
import ec.tss.disaggregation.documents.QNAModel;
import ec.tss.disaggregation.documents.QNAModelSpecification;
import ec.tstoolkit.algorithm.AlgorithmDescriptor;
import ec.tstoolkit.algorithm.CompositeResults;
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
public class QNAProcessor implements IProcessingFactory<QNAModelSpecification, QNAModel, CompositeResults> {

    public static final String FAMILY = "National Accounts";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, "QNA", "0.1.0");

    public static final QNAProcessor instance = new QNAProcessor();

    @Override
    public void dispose() {
    }

    @Override
    public AlgorithmDescriptor getInformation() {
        return DESCRIPTOR;
    }

    @Override
    public boolean canHandle(IProcSpecification spec) {
        return spec instanceof QNAModelSpecification;
    }

    @Override
    public IProcessing<QNAModel, CompositeResults> generateProcessing(QNAModelSpecification specification, ProcessingContext context) {
        return new DefaultProcessing(specification);
    }

    @Override
    public Map<String, Class> getSpecificationDictionary(Class<QNAModelSpecification> specClass) {
        Map<String, Class> dic = new HashMap<>();
//        QNAModelSpecification.fillDictionary(null, dic);
        return dic;
    }

    @Override
    public Map<String, Class> getOutputDictionary() {
        Map<String, Class> dic = new HashMap<>();
        // TODO
        return dic;
    }

    public static class DefaultProcessing implements IProcessing<QNAModel, CompositeResults> {

        private final QNAModelSpecification spec;

        public DefaultProcessing(QNAModelSpecification spec) {
            this.spec = spec.clone();
        }

        @Override
        public CompositeResults process(QNAModel input) {
            return null;
        }

    }
}
