/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sts;

import jdplus.stats.likelihood.DiffuseLikelihoodStatistics;
import demetra.data.DoubleSeq;
import demetra.timeseries.regression.MissingValueEstimation;
import demetra.data.ParametersEstimation;
import demetra.math.matrices.Matrix;

/**
 *
 * @author PALATEJ
 */
public interface BsmEstimation {

    DoubleSeq getY();

    Matrix getX();

    DoubleSeq getCoefficients();

    Matrix getCoefficientsCovariance();

    MissingValueEstimation[] getMissing();

    /**
     * Parameters of the stochastic component.Fixed parameters are not
     * included
     *
     * @return
     */
    ParametersEstimation getParameters();

    /**
     *
     * @return
     */
    DiffuseLikelihoodStatistics getStatistics();

    DoubleSeq getResiduals();

}
