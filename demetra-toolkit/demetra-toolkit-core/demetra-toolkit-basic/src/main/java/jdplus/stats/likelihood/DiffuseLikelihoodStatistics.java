/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.stats.likelihood;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class DiffuseLikelihoodStatistics {

    double logLikelihood, transformationAdjustment;

    int observationsCount;
    int estimatedParametersCount;
    int diffuseCount;

    // decomposition of the likelihood
    double ssqErr, logDeterminant, diffuseCorrection;

    public double getAdjustedLogLikelihood() {
        return logLikelihood + transformationAdjustment;
    }

    public int getEffectiveObservationsCount() {
        return observationsCount-diffuseCount;
    }

    public double aic() {
        return 2 * estimatedParametersCount - 2 * getAdjustedLogLikelihood();
    }

    public double aicc() {
        double neff=getEffectiveObservationsCount();
        double nhp=estimatedParametersCount;
        return -2 * (getAdjustedLogLikelihood() - (nhp*neff) / (neff - nhp - 1));
    }

    public double bic() {
        double neff=getEffectiveObservationsCount();
        double nhp=estimatedParametersCount;
        return -2 * getAdjustedLogLikelihood() + nhp * Math.log(neff);
    }

     public double hannanQuinn() {
        double neff=getEffectiveObservationsCount();
        double nhp=estimatedParametersCount;
        return -2 * (getAdjustedLogLikelihood() - nhp * Math.log(Math.log(neff)));
   }
}
