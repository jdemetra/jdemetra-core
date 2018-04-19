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
import ec.satoolkit.benchmarking.SaBenchmarkingResults;
import ec.satoolkit.seats.SeatsResults;
import ec.satoolkit.tramoseats.SeatsDecomposer;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tstoolkit.algorithm.*;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.Singleton;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.TransformSpec;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@Singleton
@Development(status = Development.Status.Alpha)
public class TramoSeatsProcessingFactory extends GenericSaProcessingFactory implements IProcessingFactory<TramoSeatsSpecification, TsData, CompositeResults> {

    public static final String METHOD = "tramoseats";
    public static final String VERSION = "0.1.0.0";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, METHOD, VERSION);

    private static SequentialProcessing<TsData> create(TramoSeatsSpecification xspec, ProcessingContext context) {
        SequentialProcessing processing = new SequentialProcessing();
        DefaultPreprocessingFilter filter = new DefaultPreprocessingFilter();
        TransformSpec transform = xspec.getTramoSpecification().getTransform();
        addInitialStep(transform.getSpan(), transform.isPreliminaryCheck(), processing);
        addPreprocessingStep(xspec.getTramoSpecification().build(context), xspec.getSeatsSpecification().getPredictionLength(), processing);
        addDecompositionStep(new SeatsDecomposer(xspec.getSeatsSpecification()), filter, processing);
        addFinalStep(filter, processing);
        addBenchmarkingStep(xspec.getBenchmarkingSpecification(), processing);
        return processing;
    }
    public static final TramoSeatsProcessingFactory instance = new TramoSeatsProcessingFactory();

    protected TramoSeatsProcessingFactory() {
    }

    public static CompositeResults process(TsData s, TramoSeatsSpecification xspec, ProcessingContext context) {
        SequentialProcessing<TsData> processing = create(xspec, context);
        return processing.process(s);
    }

    public static CompositeResults process(TsData s, TramoSeatsSpecification xspec) {
        return process(s, xspec, null);
    }

    @Override
    public SequentialProcessing<TsData> generateProcessing(TramoSeatsSpecification xspec, ProcessingContext context) {
        return create(xspec, context);
    }

    public SequentialProcessing<TsData> generateProcessing(TramoSeatsSpecification xspec) {
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
        return spec instanceof TramoSeatsSpecification;
    }

    @Override
    public Map<String, Class> getSpecificationDictionary(Class<TramoSeatsSpecification> specClass) {
        HashMap<String, Class> dic = new HashMap<>();
        TramoSeatsSpecification.fillDictionary(null, dic);
        return dic;
    }

    @Override
    public Map<String, Class> getOutputDictionary(boolean compact) {
        LinkedHashMap<String, Class> dic = new LinkedHashMap<>();
        PreprocessingModel.fillDictionary(null, dic, compact);
        SeatsResults.fillDictionary(DECOMPOSITION, dic, compact);
        DefaultSeriesDecomposition.fillDictionary(null, dic, compact);
        SaBenchmarkingResults.fillDictionary(BENCHMARKING, dic, compact);
        return dic;
    }
}
