/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.likelihood;

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
    
    public double getAdjustedLogLikelihood(){
        return logLikelihood+transformationAdjustment;
    }
    
    public double aic(){
        return -2 * (getAdjustedLogLikelihood() - estimatedParametersCount);
    }

    public double aicc(){
        return -2 * (getAdjustedLogLikelihood() - (estimatedParametersCount*observationsCount)/(observationsCount-estimatedParametersCount-1));
    }

    public double bic(){
        return -2 * getAdjustedLogLikelihood() + estimatedParametersCount * Math.log(observationsCount);
    }
    
}
