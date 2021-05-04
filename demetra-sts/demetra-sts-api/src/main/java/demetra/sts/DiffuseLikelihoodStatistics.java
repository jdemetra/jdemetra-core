/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.sts;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class DiffuseLikelihoodStatistics {

    double logLikelihood, transformationAdjustment,
            adjustedLogLikelihood;
    
    int observationsCount;
    int diffuseCount;

    // decomposition of the likelihood
    double ssqErr, logDeterminant, diffuseCorrection;
}
