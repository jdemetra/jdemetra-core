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
import ec.satoolkit.special.MixedAirlineDecomposer;
import ec.satoolkit.special.MixedAirlineSpecification;
import ec.tstoolkit.algorithm.*;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.modelling.arima.Method;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@Deprecated
public class MixedAirlineProcessingFactory extends GenericSaProcessingFactory implements IProcessingFactory<MixedAirlineSpecification, TsData, CompositeResults> {
    
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, "Mixed airline model", null);
    
    private static SequentialProcessing<TsData> create(MixedAirlineSpecification xspec, ProcessingContext context) {
        SequentialProcessing processing = new SequentialProcessing();
        DefaultPreprocessingFilter filter = new DefaultPreprocessingFilter();
        if (xspec.getPreprocessingSpec().method != Method.None) {
            addPreprocessingStep(xspec.buildPreprocessor(context), processing);
        }
        addDecompositionStep(new MixedAirlineDecomposer(xspec.getDecompositionSpec()), filter, processing);
        addFinalStep(filter, processing);
        // TODO For test only
        SaBenchmarkingSpec bspec = xspec.getBenchmarkingSpec();
        if (bspec != null && bspec.isEnabled()) {
            addBenchmarkingStep(bspec, processing);
        }
        return processing;
    }
    
    public static final MixedAirlineProcessingFactory instance = new MixedAirlineProcessingFactory();
    
    protected MixedAirlineProcessingFactory() {
    }
    
    public static CompositeResults process(TsData s, MixedAirlineSpecification xspec) {
        SequentialProcessing<TsData> processing = create(xspec, null);
        return processing.process(s);
    }
    
    @Override
    public SequentialProcessing<TsData> generateProcessing(MixedAirlineSpecification xspec, ProcessingContext context) {
        return create(xspec, context);
    }
    
    public SequentialProcessing<TsData> generateProcessing(MixedAirlineSpecification xspec) {
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
        return spec instanceof MixedAirlineSpecification;
    }
    
    @Override
    public Map<String, Class> getSpecificationDictionary(Class<MixedAirlineSpecification> specClass) {
        HashMap<String, Class> dic = new HashMap<>();
        MixedAirlineSpecification.fillDictionary(null, dic);
        return dic;
    }
    
    @Override
    public Map<String, Class> getOutputDictionary(boolean compact) {
        HashMap<String, Class> dic = new LinkedHashMap<>();
        PreprocessingModel.fillDictionary(null, dic, compact);
        DefaultSeriesDecomposition.fillDictionary(null, dic, compact);
        SaBenchmarkingResults.fillDictionary(BENCHMARKING, dic, compact);
        return dic;
    }
}
