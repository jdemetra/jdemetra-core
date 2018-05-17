/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.jdr.sa;

import demetra.algorithm.IProcResults;
import demetra.information.InformationMapping;
import ec.satoolkit.ISeriesDecomposition;
import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.satoolkit.seats.SeatsResults;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.jdr.mapping.PreprocessingInfo;
import ec.tstoolkit.jdr.seats.SeatsInfo;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
public class TramoSeatsResults implements IProcResults {
    
    CompositeResults results;
    SaDiagnostics diagnostics;
    
    public ec.tstoolkit.jdr.regarima.Processor.Results regarima(){
        return new ec.tstoolkit.jdr.regarima.Processor.Results(model());
    }

    PreprocessingModel model() {
        return results == null ? null : results.get(TramoSeatsProcessingFactory.PREPROCESSING, PreprocessingModel.class);
    }

    ISeriesDecomposition finals() {
        return results == null ? null : results.get(TramoSeatsProcessingFactory.FINAL, ISeriesDecomposition.class);
    }

    SeatsResults seats() {
        return results == null ? null : results.get(TramoSeatsProcessingFactory.DECOMPOSITION, SeatsResults.class);
    }

    static final InformationMapping<TramoSeatsResults> MAPPING = new InformationMapping<>(TramoSeatsResults.class);

    static {
        MAPPING.delegate(null, SaDecompositionInfo.getMapping(), source -> source.finals());
        MAPPING.delegate("preprocessing", PreprocessingInfo.getMapping(), source -> source.model());
        MAPPING.delegate("decomposition", SeatsInfo.getMapping(), source -> source.seats());
        MAPPING.delegate("diagnostics", SaDiagnostics.getMapping(), source -> source.diagnostics);
    }

    public InformationMapping<TramoSeatsResults> getMapping() {
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
