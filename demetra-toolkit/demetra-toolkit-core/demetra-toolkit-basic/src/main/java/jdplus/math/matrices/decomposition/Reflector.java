/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.decomposition;

import demetra.math.Constants;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.DataPointer;

/**
 *
 * @author palatej
 */
@lombok.Data
class Reflector {

    double alpha; //+-||x|| (=Hv)
    double beta;
    private final double[] px;
    private int xstart; // start of the vector 
    int n; // length of the vector 

    double x0() {
        return px[xstart];
    }

    void x0(double value) {
        px[xstart] = value;
    }

    DataPointer x() {
        return DataPointer.of(px, xstart);
    }

    DataPointer v() {
        return DataPointer.of(px, xstart + 1);
    }

    Reflector(double[] x) {
        this.px = x;
    }

    /**
     * Initialize the reflector
     *
     * @param start Starting position of the vector
     * @param n Number of elements in the vector
     */
    void set(int start, int n) {
        this.xstart = start;
        this.n = n;
    }

    void larfg() {
        if (n == 0)
            return;
        double x0 = x0();
        int m = n - 1;
        DataPointer v = v(), x=x();
//        if (n == 1){
//            alpha = Math.abs(x0);
//            beta = 0;
//            return;
//        }
//        if (v.test(m, w->w == 0)) {
//            alpha = Math.abs(x0);
//            beta = 0;
//            x0(1);
//        } else {
            double nrm = x.norm2(n);
//            double nrm = LapackUtility.lapy2(x0, vnrm);
            double eps = Constants.getEpsilon();
            double safemin = Constants.getSafeMin() / eps;
            int k = 0;
            if (nrm < safemin) {
                double rsafemin = 1 / safemin;
                do {
                    v.mul(m, rsafemin);
                    x0 *= rsafemin;
                    nrm *= rsafemin;
                } while (nrm < safemin && ++k < 4);
                nrm = x.norm2(n);
            }
            if (x0 < 0) {
                nrm = -nrm;
            }
            for (int j = 0; j < k; ++j) {
                nrm *= safemin;
            }
            beta = nrm / (nrm + x0);
            v.div(m, nrm);
            x0(1 + x0 / nrm);
            alpha = -nrm;

            // beta = -+ || x ||
//        }
    }

    void lapply(Matrix M) {
        int nc = M.getColumnsCount(), lda = M.getColumnIncrement(), m = n - 1, mstart = M.getStartPosition();
        double[] pm = M.getStorage();
        int xmax = xstart + n;
        for (int k = 0, im = mstart; k < nc; ++k, im += lda) {
            double s = pm[im] / beta;
            for (int i = xstart + 1, j = im + 1; i < xmax; ++i, ++j) {
                s += pm[j] * px[i];
            }
            if (s != 0) {
                pm[im] -= s;
                s *= -beta;
                for (int i = xstart + 1, j = im + 1; i < xmax; ++i, ++j) {
                    pm[j] += s * px[i];
                }
            }
        }
    }

}
