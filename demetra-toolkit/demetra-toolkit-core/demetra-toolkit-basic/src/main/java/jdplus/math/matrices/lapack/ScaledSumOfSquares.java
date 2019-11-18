/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

/**
 * Sum of squares in scaled form
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Data
public class ScaledSumOfSquares {

    private double sumsq, scale;

    public ScaledSumOfSquares() {
        sumsq = 0;
        scale = 1;
    }

    public double value() {
        return sumsq * scale * scale;
    }

    public double sqrt() {
        return scale * Math.sqrt(sumsq);
    }

    public void add(double x) {
        double absxi = Math.abs(x);
        if (Double.isNaN(absxi)) {
            scale = Double.NaN;
            sumsq = Double.NaN;
            return;
        }
        if (absxi > 0) {
            if (scale < absxi) {
                double tmp = scale / absxi;
                sumsq = 1 + sumsq * tmp * tmp;
                scale = absxi;
            } else {
                double tmp = absxi / scale;
                sumsq += tmp * tmp;
            }
        }
    }

    /**
     * DLASSQ
     *
     * @param x
     * @param start
     * @param n
     * @param inc
     */
    public void add(double[] x, int start, int n, int inc) {
        if (n > 0) {
            int imax = start + n * inc;
            for (int i = start; i < imax; i += inc) {
                double absxi = Math.abs(x[i]);
                if (Double.isNaN(absxi)) {
                    scale = Double.NaN;
                    sumsq = Double.NaN;
                    return;
                }
                if (absxi > 0) {
                    if (scale < absxi) {
                        double tmp = scale / absxi;
                        sumsq = 1 + sumsq * tmp * tmp;
                        scale = absxi;
                    } else {
                        double tmp = absxi / scale;
                        sumsq += tmp * tmp;
                    }
                }
            }
        }
    }
}
