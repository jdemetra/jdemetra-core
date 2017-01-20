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
import static ec.satoolkit.GenericSaProcessingFactory.DECOMPOSITION;
import ec.satoolkit.benchmarking.SaBenchmarkingResults;
import ec.satoolkit.x11.Mstatistics;
import ec.satoolkit.x11.X11Results;
import ec.satoolkit.x13.X11Decomposer;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.algorithm.*;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.x13.BasicSpec;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class X13ProcessingFactory extends GenericSaProcessingFactory implements IProcessingFactory<X13Specification, TsData, CompositeResults> {

    public static final String METHOD = "x13";
    public static final String VERSION = "0.1.0.0";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, METHOD, VERSION);
    public static final String MSTATISTICS = "m-statistics";

    public static final X13ProcessingFactory instance = new X13ProcessingFactory();

    protected X13ProcessingFactory() {
    }

    private static SequentialProcessing<TsData> create(X13Specification xspec, ProcessingContext context) {
        SequentialProcessing processing = new SequentialProcessing();
        BasicSpec basic = xspec.getRegArimaSpecification().getBasic();
        addInitialStep(basic.getSpan(), basic.isPreliminaryCheck(), processing);
        if (xspec.getRegArimaSpecification().getBasic().isPreprocessing()) {
            addPreprocessingStep(xspec.getRegArimaSpecification().build(context), processing);
        }
        DefaultPreprocessingFilter filter = new DefaultPreprocessingFilter();
        filter.setForecastHorizon(xspec.getX11Specification().getForecastHorizon());
        addDecompositionStep(new X11Decomposer(xspec.getX11Specification()), filter, processing);
        addFinalStep(filter, processing);
        addDiagnosticsStep(processing);
        addBenchmarkingStep(xspec.getBenchmarkingSpecification(), processing);
        return processing;
    }

    private static void addDiagnosticsStep(SequentialProcessing processing) {
        processing.add(new IProcessingNode<TsData>() {
            @Override
            public String getName() {
                return MSTATISTICS; //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public String getPrefix() {
                return MSTATISTICS; //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public IProcessing.Status process(TsData input, Map<String, IProcResults> results) {
                // gets the X11 decomposition
                IProcResults decomp = results.get(DECOMPOSITION);
                if (decomp == null || !(decomp instanceof X11Results)) {
                    return IProcessing.Status.Unprocessed;
                }
                X11Results x11 = (X11Results) decomp;
                Mstatistics mstats = Mstatistics.computeFromX11(x11.getSeriesDecomposition().getMode(), x11.getInformation());
                results.put(MSTATISTICS, mstats);
                return IProcessing.Status.Valid;
            }
        });
    }

    @Override
    public SequentialProcessing<TsData> generateProcessing(X13Specification xspec, ProcessingContext context) {
        return create(xspec, context);
    }

    public SequentialProcessing<TsData> generateProcessing(X13Specification xspec) {
        return create(xspec, null);
    }

    public static CompositeResults process(TsData s, X13Specification xspec) {
        SequentialProcessing<TsData> processing = create(xspec, null);
        return processing.process(s);
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
        return spec instanceof X13Specification;
    }

    @Override
    public Map<String, Class> getSpecificationDictionary(Class<X13Specification> specClass) {
        HashMap<String, Class> dic = new HashMap<>();
        X13Specification.fillDictionary(null, dic);
        return dic;
    }

    @Override
    public Map<String, Class> getOutputDictionary(boolean compact) {
        HashMap<String, Class> dic = new HashMap<>();
        PreprocessingModel.fillDictionary(null, dic, compact);
        X11Results.fillDictionary(DECOMPOSITION, dic, compact);
        Mstatistics.fillDictionary(MSTATISTICS, dic, compact);
        DefaultSeriesDecomposition.fillDictionary(null, dic, compact);
        SaBenchmarkingResults.fillDictionary(BENCHMARKING, dic, compact);
        return dic;
    }
}
