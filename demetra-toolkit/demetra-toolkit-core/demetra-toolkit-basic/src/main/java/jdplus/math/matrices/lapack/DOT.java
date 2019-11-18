/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;

/**
 * Computes x*y'
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
@Deprecated
public class DOT {

    /**
     * x*y'
     *
     * @param n
     * @param x
     * @param y
     * @return
     */
    public double apply(int n, DataPointer x, DataPointer y) {
        if (n == 0) {
            return 0;
        }
        if (x == y) {
            return apply(n, x);
        }
        if (n == 1) {
            return x.value() * y.value();
        }
        double d = 0;
        int xinc=x.inc(), yinc=y.inc();
        if (xinc == 1) {
            int imax = x.pos + n;
            if (yinc == 1) {
                for (int i = x.pos, j = y.pos; i < imax; ++i, ++j) {
                    d += x.p[i] * y.p[j];
                }
            } else {
                for (int i = x.pos, j = y.pos; i < imax; ++i, j += yinc) {
                    d += x.p[i] * y.p[j];
                }
            }
        } else {
            if (yinc == 1) {
                int jmax = y.pos + n;
                for (int i = x.pos, j = y.pos; j < jmax; i += xinc, ++j) {
                    d += x.p[i] * y.p[j];
                }
            } else {
                int imax = x.pos + n * xinc;
                for (int i = x.pos, j = y.pos; i != imax; i += xinc, j += yinc) {
                    d += x.p[i] * y.p[j];
                }
            }
        }
        return d;
    }

    /**
     * x*x'
     *
     * @param n
     * @param x
     * @return
     */
    public double apply(int n, DataPointer x) {
        if (n == 0) {
            return 0;
        }
        if (n == 1) {
            double d = x.value();
            return d * d;
        }
        return x.ssq(n);
    }
}
