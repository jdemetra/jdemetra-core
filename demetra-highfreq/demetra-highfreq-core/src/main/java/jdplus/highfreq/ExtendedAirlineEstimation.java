/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.highfreq;

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.highfreq.ExtendedAirline;
import jdplus.stats.likelihood.LikelihoodStatistics;
import demetra.math.matrices.Matrix;
import demetra.modelling.OutlierDescriptor;
import demetra.information.GenericExplorable;

/**
 * Low-level results. Should be refined
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder
public class ExtendedAirlineEstimation implements GenericExplorable {

    double[] y;
    Matrix x; //user-def reg var, outlier

    ExtendedAirline model;

    OutlierDescriptor[] outliers;

    DoubleSeq coefficients; //mean, user-def-var,outlier
    Matrix coefficientsCovariance;

    private DoubleSeq parameters, score;
    private Matrix parametersCovariance;

    LikelihoodStatistics likelihood;
    DoubleSeq residuals;

    /**
     *
     * @return Y-coef*x
     */
    public double[] linearized() {

        double[] l = y.clone();
        DoubleSeqCursor acur = coefficients.cursor();
        if (model.isMean()) {
            acur.getAndNext();
        };
        for (int j = 0; j < x.getColumnsCount(); ++j) {
            double a = acur.getAndNext();
            if (a != 0) {
                DoubleSeqCursor cursor = x.column(j).cursor();
                for (int k = 0; k < l.length; ++k) {
                    l[k] -= a * cursor.getAndNext();
                }
            }
        }
        return l;
    }

    public double[] component_mean() {
        double[] l = new double[y.length];
        if (model.isMean()) {
            for (int k = 0; k < l.length; k++) {
                l[k] = coefficients.get(0);
            }
        }
        return l;
    }

    public double[] component_userdef_reg_variables() {

        double[] l = new double[y.length];
        DoubleSeqCursor acur = coefficients.cursor();
        if (model.isMean()) {
            acur.getAndNext();
        };
        for (int j = 0; j < x.getColumnsCount() - outliers.length; ++j) {
            double a = acur.getAndNext();
            if (a != 0) {
                DoubleSeqCursor cursor = x.column(j).cursor();
                for (int k = 0; k < l.length; ++k) {
                    l[k] += a * cursor.getAndNext();
                }
            }
        }

        return l;
    }

    public double[] component_outliers() {

        double[] l = new double[y.length];
        DoubleSeqCursor acur = coefficients.cursor();
        if (model.isMean()) {
            acur.getAndNext();
        };
        for (int i = 1; i < x.getColumnsCount() - outliers.length+1; i++) {
            acur.getAndNext();
        }

        for (int j = x.getColumnsCount() - outliers.length; j < x.getColumnsCount(); ++j) {

            double a = acur.getAndNext();
            if (a != 0) {
                DoubleSeqCursor cursor = x.column(j).cursor();
                for (int k = 0; k < l.length; ++k) {
                    l[k] += a * cursor.getAndNext();
                }
            }
        }

        return l;
    }

    public double[] component_ao() {
        return component_outlier("AO");
    }

    public double[] component_wo() {
        return component_outlier("WO");
    }

    public double[] component_ls() {
        return component_outlier("LS");
    }

    /**
     * @return sum (coefficients*regression variable) if ao
     *
     */
    private double[] component_outlier(String outlierTyp) {

        double[] l = new double[y.length];
        DoubleSeqCursor acur = coefficients.cursor();
        if (model.isMean()) {
            acur.getAndNext();
        };
        for (int i = 1; i < x.getColumnsCount() - outliers.length+1; i++) {
            acur.getAndNext();
        }

        for (int j = x.getColumnsCount() - outliers.length; j < x.getColumnsCount(); ++j) {
                     double a = acur.getAndNext();
            if (outlierTyp.equalsIgnoreCase(outliers[j - x.getColumnsCount() + outliers.length].getCode())) {
       
                if (a != 0) {
                    DoubleSeqCursor cursor = x.column(j).cursor();
                    for (int k = 0; k < l.length; ++k) {
                        l[k] += a * cursor.getAndNext();
                    }
                }
            }
        }

        return l;
    }

    public double[] tstats() {
        double[] t = coefficients.toArray();
        if (t == null) {
            return null;
        }
        DoubleSeqCursor v = coefficientsCovariance.diagonal().cursor();
        for (int i = 0; i < t.length; ++i) {
            t[i] /= Math.sqrt(v.getAndNext());
        }
        return t;
    }

    public int getNx() {
        return coefficients == null ? 0 : coefficients.length();
    }
}
