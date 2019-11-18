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
@Deprecated
public class ASUM {

    public double apply(int n, DataPointer x) {
        if (n <1) {
            return 0;
        }
        int xinc=x.inc();
        double rslt = 0;
        if (xinc == 1) {
            int jmax = x.pos + n;
            for (int j = x.pos; j < jmax; ++j) {
                rslt += Math.abs(x.p[j]);
            }
        } else {
            int jmax = x.pos + n * xinc;
            for (int j = x.pos; j != jmax; j += xinc) {
                rslt += Math.abs(x.p[j]);
            }
        }
        return rslt;
    }

}
