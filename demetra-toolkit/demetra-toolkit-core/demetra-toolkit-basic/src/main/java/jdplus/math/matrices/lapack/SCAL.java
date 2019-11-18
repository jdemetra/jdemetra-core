/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class SCAL {

    public void apply(int n, double a, double[] x, int start, int inc) {
        if (n == 0 || a == 1) {
            return;
        }
        if (inc == 1) {
            int imax = start + n;
            for (int i = start; i < imax; ++i) {
                x[i] *= a;
            }
        } else {
            int imax = start + n * inc;
            for (int i = start; i != imax; i += inc) {
                x[i] *= a;
            }

        }

    }

    public void apply(int n, double a, DataPointer x) {
        if (n == 0 || a == 1) {
            return;
        }
        if (a == 0) {
            x.set(n, 0);
        } else if (a == -1) {
            x.chs(n);
        } else {
            x.mul(n, a);
        }
    }

}
