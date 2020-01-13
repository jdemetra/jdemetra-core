/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.regarima;

import demetra.data.DoubleSeq;
import demetra.design.Development;
import demetra.likelihood.ConcentratedLikelihood;
import demetra.math.functions.ObjectiveFunctionPoint;
import demetra.linearmodel.LinearModel;
import demetra.math.matrices.MatrixType;

/**
 *
 * @author Jean Palate
 * @param <S>
 */
@Development(status = Development.Status.Alpha)
@lombok.Value
public class RegArimaModel<S> {

        @lombok.NonNull
        private double[] y;
        
        private boolean mean;
        
        private MatrixType X;

        @lombok.NonNull
        private S arima;
 

}
