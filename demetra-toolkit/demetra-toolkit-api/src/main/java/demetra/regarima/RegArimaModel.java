/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.regarima;

import demetra.data.DoubleSeq;
import demetra.design.Development;
import demetra.likelihood.ConcentratedLikelihood;
import demetra.likelihood.MaximumLogLikelihood;
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

    @Development(status = Development.Status.Alpha)
    @lombok.Value
    public static class Data<T> {

        @lombok.NonNull
        private LinearModel model;

        @lombok.NonNull
        private T arima;

        //<editor-fold defaultstate="collapsed" desc="delegate to model">
        public DoubleSeq getY() {
            return model.getY();
        }

        public boolean isMeanCorrection() {
            return model.isMeanCorrection();
        }

        public MatrixType getX() {
            return model.getX();
        }
        //</editor-fold>
    }

    @lombok.Value
    public static class Estimation {

        /**
         * Concentrated likelihood
         */
        @lombok.NonNull
        private ConcentratedLikelihood likelihood;
        /**
         * Maximum of the likelihood. Could be missing if the arima model
         * doesn't contain any free parameter
         */
        private MaximumLogLikelihood maximumLogLikelihood;
    }
    
    private Data<S> data;
    private Estimation estimation;
}
