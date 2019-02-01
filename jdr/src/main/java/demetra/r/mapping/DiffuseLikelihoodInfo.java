/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.r.mapping;

import demetra.information.InformationMapping;
import demetra.likelihood.LikelihoodStatistics;
import demetra.maths.MatrixType;
import demetra.sarima.SarimaModel;
import demetra.likelihood.DiffuseConcentratedLikelihood;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class DiffuseLikelihoodInfo {

    private final String LL = "ll", SSQ = "ssqerr", SER = "ser", SIGMA = "sigma", COEF = "coeff", VAR = "cvar", RES="residuals";

    private final InformationMapping<DiffuseConcentratedLikelihood> MAPPING = new InformationMapping<>(DiffuseConcentratedLikelihood.class);

    static {
        MAPPING.set(SER, Double.class, source -> source.ser());
        MAPPING.set(SIGMA, Double.class, source -> source.sigma());
        MAPPING.set(COEF, double[].class, source -> source.coefficients().toArray());
        MAPPING.set(VAR, MatrixType.class, source -> source.unscaledCovariance());
        MAPPING.set(LL, Double.class, source -> source.logLikelihood());
        MAPPING.set(SSQ, Double.class, source -> source.ssq());
        MAPPING.set(RES, double[].class, source -> source.e().toArray());
    }

    public InformationMapping<DiffuseConcentratedLikelihood> getMapping() {
        return MAPPING;
    }
}
