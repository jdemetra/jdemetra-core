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
package ec.satoolkit.algorithm.implementation;

import ec.satoolkit.DefaultPreprocessingFilter;
import ec.satoolkit.DefaultSeriesDecomposition;
import ec.satoolkit.GenericSaProcessingFactory;
import static ec.satoolkit.GenericSaProcessingFactory.BENCHMARKING;
import static ec.satoolkit.GenericSaProcessingFactory.PREPROCESSING;
import ec.satoolkit.benchmarking.SaBenchmarkingResults;
import ec.satoolkit.benchmarking.SaBenchmarkingSpec;
import ec.satoolkit.special.GeneralizedAirlineDecomposer;
import ec.satoolkit.special.GeneralizedAirlineSpecification;
import ec.tstoolkit.algorithm.*;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.modelling.arima.Method;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@Deprecated
public class GeneralizedAirlineProcessingFactory extends GenericSaProcessingFactory implements IProcessingFactory<GeneralizedAirlineSpecification, TsData, CompositeResults> {

    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, "Generalized airline model", null);

    private static SequentialProcessing<TsData> create(GeneralizedAirlineSpecification xspec, ProcessingContext context) {
        SequentialProcessing processing = new SequentialProcessing();
        DefaultPreprocessingFilter filter = new DefaultPreprocessingFilter();
        if (xspec.getPreprocessingSpec().method != Method.None) {
            addPreprocessingStep(xspec.buildPreprocessor(context), -2, processing);
        }
        addDecompositionStep(new GeneralizedAirlineDecomposer(xspec.getDecompositionSpec()), filter, processing);
        addFinalStep(filter, processing);
        SaBenchmarkingSpec bspec = xspec.getBenchmarkingSpec();
        if (bspec != null && bspec.isEnabled()) {
            addBenchmarkingStep(bspec, processing);
        }
        return processing;
    }

    public static final GeneralizedAirlineProcessingFactory instance = new GeneralizedAirlineProcessingFactory();

    protected GeneralizedAirlineProcessingFactory() {
    }

    public static CompositeResults process(TsData s, GeneralizedAirlineSpecification xspec, ProcessingContext context) {
        SequentialProcessing<TsData> processing = create(xspec, context);
        return processing.process(s);
    }

    @Override
    public SequentialProcessing<TsData> generateProcessing(GeneralizedAirlineSpecification xspec, ProcessingContext context) {
        return create(xspec, context);
    }

    public SequentialProcessing<TsData> generateProcessing(GeneralizedAirlineSpecification xspec) {
        return create(xspec, null);
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
        return spec instanceof GeneralizedAirlineSpecification;
    }

    @Override
    public Map<String, Class> getSpecificationDictionary(Class<GeneralizedAirlineSpecification> specClass) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, Class> getOutputDictionary(boolean compact) {
        HashMap<String, Class> dic = new HashMap<>();
        PreprocessingModel.fillDictionary(null, dic, compact);
        DefaultSeriesDecomposition.fillDictionary(null, dic, compact);
        SaBenchmarkingResults.fillDictionary(BENCHMARKING, dic, compact);
        return dic;
    }
}
