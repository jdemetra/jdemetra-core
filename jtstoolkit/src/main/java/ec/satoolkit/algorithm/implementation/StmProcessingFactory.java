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
import ec.satoolkit.special.StmDecomposer;
import ec.satoolkit.special.StmSpecification;
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
@Development(status = Development.Status.Alpha)
public class StmProcessingFactory extends GenericSaProcessingFactory implements IProcessingFactory<StmSpecification, TsData, CompositeResults> {

    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, "Structural model", null);

    private static SequentialProcessing<TsData> create(StmSpecification xspec, ProcessingContext context) {
        SequentialProcessing processing = new SequentialProcessing();
        DefaultPreprocessingFilter filter = new DefaultPreprocessingFilter();
        if (xspec.getPreprocessingSpec().method != Method.None) {
            addPreprocessingStep(xspec.buildPreprocessor(context), processing);
        }
        addDecompositionStep(new StmDecomposer(xspec.getDecompositionSpec()), filter, processing);
        addFinalStep(filter, processing);
        SaBenchmarkingSpec bspec = xspec.getBenchmarkingSpec();
        if (bspec != null && bspec.isEnabled()) {
            addBenchmarkingStep(bspec, processing);
        }
        return processing;
    }

    public static final StmProcessingFactory instance=new StmProcessingFactory();
    
    protected StmProcessingFactory(){}

    public static CompositeResults process(TsData s, StmSpecification xspec) {
        SequentialProcessing<TsData> processing = create(xspec, null);
        return processing.process(s);
    }

    @Override
    public SequentialProcessing<TsData> generateProcessing(StmSpecification xspec, ProcessingContext context) {
        return create(xspec, context);
    }

    public SequentialProcessing<TsData> generateProcessing(StmSpecification xspec) {
        return create(xspec, null);
    }

    @Override
    public AlgorithmDescriptor getInformation() {
        return DESCRIPTOR;
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean canHandle(IProcSpecification spec) {
        return spec instanceof StmSpecification;
    }

    @Override
    public Map<String, Class> getSpecificationDictionary(Class<StmSpecification> specClass) {
        HashMap<String, Class> dic = new HashMap<>();
        StmSpecification.fillDictionary(null, dic);
        return dic;
    }
    
    @Override
    public Map<String, Class> getOutputDictionary() {
        HashMap<String, Class> dic = new HashMap<>();
        PreprocessingModel.fillDictionary(null, dic);
        DefaultSeriesDecomposition.fillDictionary(null, dic);
        SaBenchmarkingResults.fillDictionary(BENCHMARKING, dic);
        return dic;
    }
}
