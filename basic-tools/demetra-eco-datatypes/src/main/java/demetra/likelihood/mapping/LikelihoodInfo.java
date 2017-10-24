/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.likelihood.mapping;

import demetra.information.InformationMapping;
import demetra.likelihood.LikelihoodStatistics;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class LikelihoodInfo {

    private final String LL = "ll", LLC = "adjustedll", SSQ = "ssqerr", AIC = "aic", BIC = "bic", AICC = "aicc", BICC = "bicc", BIC2 = "bic2", HQ = "hannanquinn",
            NPARAMS = "nparams", NOBS = "nobs", NEFFECTIVEOBS = "neffective";

    private final InformationMapping<LikelihoodStatistics> MAPPING = new InformationMapping<>(LikelihoodStatistics.class);

    static {
        MAPPING.set(AIC, Double.class, source -> source.getAIC());
        MAPPING.set(AICC, Double.class, source -> source.getAICC());
        MAPPING.set(BIC, Double.class, source -> source.getBIC());
        MAPPING.set(BICC, Double.class, source -> source.getBICC());
        MAPPING.set(BIC2, Double.class, source -> source.getBIC2());
        MAPPING.set(HQ, Double.class, source -> source.getHannanQuinn());
        MAPPING.set(LL, Double.class, source -> source.getLogLikelihood());
        MAPPING.set(LLC, Double.class, source -> source.getAdjustedLogLikelihood());
        MAPPING.set(SSQ, Double.class, source -> source.getSsqErr());

        MAPPING.set(NPARAMS, Integer.class, source -> source.getEstimatedParametersCount());
        MAPPING.set(NOBS, Integer.class, source -> source.getObservationsCount());
        MAPPING.set(NEFFECTIVEOBS, Integer.class, source -> source.getEffectiveObservationsCount());

    }

    public InformationMapping<LikelihoodStatistics> getMapping() {
        return MAPPING;
    }
}
