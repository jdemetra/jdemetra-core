/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.jdr.mapping;

import demetra.algorithm.IProcResults;
import demetra.information.InformationMapping;
import ec.tstoolkit.jdr.regarima.RegArimaInfo;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class PreprocessingInfo {

    static final InformationMapping<PreprocessingModel> MAPPING = new InformationMapping<>(PreprocessingModel.class);

    static {
        MAPPING.delegate("model", RegArimaInfo.getMapping(), source -> source);
        MAPPING.delegate("arima", SarimaInfo.getMapping(), source -> source.estimation.getArima());
        MAPPING.delegate("likelihood", LikelihoodStatisticsInfo.getMapping(), source -> source.estimation.getStatistics());
        MAPPING.delegate("residuals", ResidualsInfo.getMapping(), source -> source.estimation.getNiidTests());
    }

    public InformationMapping<PreprocessingModel> getMapping() {
        return MAPPING;
    }

}
