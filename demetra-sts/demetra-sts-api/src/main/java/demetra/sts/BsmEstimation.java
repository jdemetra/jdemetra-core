/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.sts;

import demetra.data.DoubleSeq;
import demetra.likelihood.MissingValueEstimation;
import demetra.likelihood.ParametersEstimation;
import demetra.math.matrices.MatrixType;

/**
 *
 * @author PALATEJ
 */
public interface BsmEstimation {
       DoubleSeq getY();
        MatrixType getX();
        DoubleSeq getCoefficients();
        MatrixType getCoefficientsCovariance();
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
