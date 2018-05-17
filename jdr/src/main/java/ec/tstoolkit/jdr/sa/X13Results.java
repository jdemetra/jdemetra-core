/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.jdr.sa;

import demetra.algorithm.IProcResults;
import demetra.information.InformationMapping;
import ec.satoolkit.ISeriesDecomposition;
import ec.satoolkit.algorithm.implementation.X13ProcessingFactory;
import ec.satoolkit.x11.Mstatistics;
import ec.satoolkit.x11.X11Results;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.jdr.mapping.PreprocessingInfo;
import ec.tstoolkit.jdr.x11.X11DecompositionInfo;
import ec.tstoolkit.jdr.x11.MstatisticsInfo;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
public class X13Results implements IProcResults {

    CompositeResults results;
    SaDiagnostics diagnostics;

    public ec.tstoolkit.jdr.regarima.Processor.Results regarima() {
        return new ec.tstoolkit.jdr.regarima.Processor.Results(model());
    }

    PreprocessingModel model() {
        return results == null ? null : results.get(X13ProcessingFactory.PREPROCESSING, PreprocessingModel.class);
    }

    ISeriesDecomposition finals() {
        return results == null ? null : results.get(X13ProcessingFactory.FINAL, ISeriesDecomposition.class);
    }

    X11Results x11() {
        return results == null ? null : results.get(X13ProcessingFactory.DECOMPOSITION, X11Results.class);
    }

    Mstatistics mstats() {
        return results == null ? null : results.get(X13ProcessingFactory.MSTATISTICS, Mstatistics.class);
    }
    static final InformationMapping<X13Results> MAPPING = new InformationMapping<>(X13Results.class);

    static {
        MAPPING.delegate(null, SaDecompositionInfo.getMapping(), source -> source.finals());
        MAPPING.delegate("preprocessing", PreprocessingInfo.getMapping(), source -> source.model());
        MAPPING.delegate("mstats", MstatisticsInfo.getMapping(), source -> source.mstats());
        MAPPING.delegate("decomposition", X11DecompositionInfo.getMapping(), source -> source.x11());
        MAPPING.delegate("diagnostics", SaDiagnostics.getMapping(), source -> source.diagnostics);
    }

    public InformationMapping<X13Results> getMapping() {
        return MAPPING;
    }

    @Override
    public boolean contains(String id) {
        return MAPPING.contains(id);
    }

    @Override
    public Map<String, Class> getDictionary() {
        Map<String, Class> dic = new LinkedHashMap<>();
        MAPPING.fillDictionary(null, dic, true);
        return dic;
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        return MAPPING.getData(this, id, tclass);
    }
}
