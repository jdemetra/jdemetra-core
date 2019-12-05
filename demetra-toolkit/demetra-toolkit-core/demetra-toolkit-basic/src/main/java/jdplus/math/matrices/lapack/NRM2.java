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
public class NRM2 {
    
    public double apply(int n, DataPointer x) {
        if (n < 1) {
            return 0;
        } else if (n == 1) {
            return Math.abs(x.value());
        } else {
            int xinc=x.inc();
            int imax = x.pos + n * xinc;
            double scale = 0;
            double ssq = 1;
            for (int i = x.pos; i < imax; i += xinc) {
                double xcur = x.p[i];
                if (xcur != 0) {
                    double absxi = Math.abs(xcur);
                    if (scale < absxi) {
                        double tmp = scale / absxi;
                        ssq = 1 + ssq * tmp * tmp;
                        scale = absxi;
                    } else {
                        double tmp = absxi / scale;
                        ssq += tmp * tmp;
                    }
                }
            }
            return scale * Math.sqrt(ssq);
        }
    }

}
