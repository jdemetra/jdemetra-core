/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.highfreq;

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import jdplus.likelihood.LikelihoodStatistics;
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
public class FractionalAirlineEstimation implements GenericExplorable {

    double[] y;
    Matrix x;

    FractionalAirline model;

    OutlierDescriptor[] outliers;

    DoubleSeq coefficients;
    Matrix coefficientsCovariance;

    private DoubleSeq parameters, score;
    private Matrix parametersCovariance;

    LikelihoodStatistics likelihood;

    public double[] linearized() {

        double[] l = y.clone();
        DoubleSeqCursor acur = coefficients.cursor();
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
