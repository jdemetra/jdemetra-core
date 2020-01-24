/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.regarima;

import demetra.data.MissingValueEstimation;
import demetra.data.ParameterEstimation;
import demetra.data.ParametersEstimation;
import demetra.likelihood.LikelihoodStatistics;
import demetra.math.matrices.MatrixType;
import lombok.NonNull;

/**
 *
 * @author palatej
 * @param <S>
 */
@lombok.Value
@lombok.Builder(builderClassName="Builder")
public class RegArimaEstimation<S> {
    
    /**
     * Stochastic model
     */
    @NonNull 
    private S arima;
    /**
     * Estimated parameters of the stochastic model
     */
    private ParametersEstimation parameters;
    
    /**
     * Data being modelled (might contain missing values)
     */
    @NonNull 
    private double[] y;
    /**
     * Matrix of the regression variables (column i corresponding to the variable i). 
     * The matrix should not contain regression variables corresponding to missing values (additive outliers) 
     * or to mean correction.
     */
    private MatrixType X;
    
    /**
     * Mean correction, if any. The mean correction is applied on the model after differencing
     */
    private ParameterEstimation meanCorrection;
    
    /**
     * Estimated coefficients
     * The size of the estimated coefficients should correspond to the size of the regression variables (X)
     */
    private ParametersEstimation coefficients;
    
    /**
     * Estimation of the missing values, if any
     */
    private MissingValueEstimation[] missing;
    
    /**
     * Likelihood of the model.
     */
    private LikelihoodStatistics likelihood;
}
