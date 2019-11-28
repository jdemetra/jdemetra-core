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
package ec.satoolkit;

import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.satoolkit.algorithm.implementation.X13ProcessingFactory;
import ec.satoolkit.benchmarking.SaBenchmarkingResults;
import ec.satoolkit.diagnostics.StationaryVarianceDecomposition;
import ec.satoolkit.x11.X11Results;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.IProcDocument;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.algorithm.SingleTsData;
import ec.tstoolkit.information.InformationMapping;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author Jean Palate
 */
public class GenericSaResults implements IProcResults {

    public static SingleTsData getInput(CompositeResults rslts) {
        if (rslts == null) {
            return null;
        }
        return rslts.get(IProcDocument.INPUT, SingleTsData.class);
    }

    public static ISeriesDecomposition getFinalDecomposition(CompositeResults rslts) {
        if (rslts == null) {
            return null;
        }
        return rslts.get(GenericSaProcessingFactory.FINAL, ISeriesDecomposition.class);
    }

    public static PreprocessingModel getPreprocessingModel(CompositeResults rslts) {
        if (rslts == null) {
            return null;
        }
        return rslts.get(GenericSaProcessingFactory.PREPROCESSING, PreprocessingModel.class);
    }

    public static <S extends ISaResults> S getDecomposition(CompositeResults rslts, Class<S> sclass) {
        if (rslts == null) {
            return null;
        }
        return rslts.get(GenericSaProcessingFactory.DECOMPOSITION, sclass);
    }

    public static SaBenchmarkingResults getBenchmarking(CompositeResults rslts) {
        if (rslts == null) {
            return null;
        }
        return rslts.get(GenericSaProcessingFactory.BENCHMARKING, SaBenchmarkingResults.class);
    }

    public static GenericSaResults of(PreprocessingModel regarima, ISaResults decomposition, ISeriesDecomposition finals) {
        if (decomposition == null || finals == null) {
            return null;
        }
        return new GenericSaResults(regarima, decomposition, finals);
    }

    private final PreprocessingModel regarima;
    private final ISaResults decomposition;
    private final ISeriesDecomposition finals;

    private StationaryVarianceDecomposition varDecomposition;

    private GenericSaResults(PreprocessingModel regarima, ISaResults decomposition, ISeriesDecomposition finals) {
        this.regarima = regarima;
        this.decomposition = decomposition;
        this.finals = finals;
    }

    public StationaryVarianceDecomposition varDecomposition() {
        if (varDecomposition == null) {
            TsData O = regarima == null ? regarima.getData(ModellingDictionary.YC, TsData.class) : 
                    finals.getData(ModellingDictionary.Y, TsData.class);
            TsData T = decomposition.getData(ModellingDictionary.T_CMP, TsData.class);
            TsData S = decomposition.getData(ModellingDictionary.S_CMP, TsData.class);
            TsData I = decomposition.getData(ModellingDictionary.I_CMP, TsData.class);
            TsData Cal = regarima == null ? null : regarima.getData(ModellingDictionary.CAL, TsData.class);
            TsData D = regarima == null ? null : regarima.getData(ModellingDictionary.DET, TsData.class);
            DecompositionMode m = finals.getData(ModellingDictionary.MODE, DecompositionMode.class);
            boolean mul = m != null ? m != DecompositionMode.Additive : false;
            TsData P = mul ? TsData.divide(D, Cal) : TsData.subtract(D, Cal);
            varDecomposition = new StationaryVarianceDecomposition();
            varDecomposition.process(O, T, S, I, Cal, P, mul);
        }
        return varDecomposition;
    }

    public static void fillDictionary(String prefix, Map<String, Class> map, boolean compact) {
        MAPPING.fillDictionary(prefix, map, compact);
    }

    @Override
    public Map<String, Class> getDictionary() {
        LinkedHashMap<String, Class> map = new LinkedHashMap<>();
        fillDictionary(null, map, false);
        return map;
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        return MAPPING.getData(this, id, tclass);
    }

    @Override
    public boolean contains(String id) {
        return MAPPING.contains(id);
    }

    @Override
    public List<ProcessingInformation> getProcessingInformation() {
        return Collections.emptyList();
    }

    public static final String METHOD = "method", VARIANCE = "variancedecomposition",
            CYCLE = "cycle", SEASONAL = "seasonality", IRREGULAR = "irregular", TD = "tdh", OTHERS = "others", TOTAL = "total";

    // MAPPING
    public static InformationMapping<GenericSaResults> getMapping() {
        return MAPPING;
    }

    public static <T> void setMapping(String name, Class<T> tclass, Function<GenericSaResults, T> extractor) {
        synchronized (MAPPING) {
            MAPPING.set(name, tclass, extractor);
        }
    }

    private static final InformationMapping<GenericSaResults> MAPPING = new InformationMapping<>(GenericSaResults.class);

    static {
        MAPPING.set(METHOD, String.class, source -> source.decomposition instanceof X11Results
                ? X13ProcessingFactory.METHOD : TramoSeatsProcessingFactory.METHOD);
        MAPPING.set(InformationSet.concatenate(VARIANCE, CYCLE), Double.class, source
                -> source.varDecomposition().getVarC());
        MAPPING.set(InformationSet.concatenate(VARIANCE, SEASONAL), Double.class, source
                -> source.varDecomposition().getVarS());
        MAPPING.set(InformationSet.concatenate(VARIANCE, IRREGULAR), Double.class, source
                -> source.varDecomposition().getVarI());
        MAPPING.set(InformationSet.concatenate(VARIANCE, TD), Double.class, source
                -> source.varDecomposition().getVarTD());
        MAPPING.set(InformationSet.concatenate(VARIANCE, OTHERS), Double.class, source
                -> source.varDecomposition().getVarP());
        MAPPING.set(InformationSet.concatenate(VARIANCE, TOTAL), Double.class, source
                -> source.varDecomposition().getVarTotal());

    }
}
