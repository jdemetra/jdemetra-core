/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

/**
 *
 * Computes y = a * x + y
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
@Deprecated
public class AXPY {

    public void apply(int n, double a, DataPointer x, DataPointer y) {
        if (n == 0 || a == 0) {
            return;
        }
        int xinc=x.inc(), yinc=y.inc();
        if (yinc == 1) {
            int yend = y.pos + n;
            if (xinc == 1) {
                for (int i = y.pos, j = x.pos; i < yend; ++i, ++j) {
                    y.p[i] += a * x.p[j];
                }
            } else {
                for (int i = y.pos, j = x.pos; i < yend; ++i, j += xinc) {
                    y.p[i] += a * x.p[j];
                }
            }
        } else {
            if (xinc == 1) {
                int xend = x.pos + n;
                for (int i = y.pos, j = x.pos; j < xend; i += yinc, ++j) {
                    y.p[i] += a * x.p[j];
                }
            } else {
                int yend = y.pos + n * yinc;
                for (int i = y.pos, j = x.pos; i != yend; i += yinc, j += xinc) {
                    y.p[i] += a * x.p[j];
                }
            }
        }

    }
}
