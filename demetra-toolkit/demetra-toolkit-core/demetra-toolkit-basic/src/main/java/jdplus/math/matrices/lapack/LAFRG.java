/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

import demetra.math.Constants;

/**
 * DLARFG generates a real elementary reflector H of order n, such that
 *
 * H * ( alpha ) = ( beta ), H**T * H = I. 
 *      (   x  )   (   0  )
 *
 * where alpha and beta are scalars, and x is an (n-1)-element real vector. H is
 * represented in the form
 *
 * H = I - tau * ( 1 ) * ( 1 v**T ) , ( v )
 *
 * where tau is a real scalar and v is a real (n-1)-element vector.
 *
 * If the elements of x are all zero, then tau = 0 and H is taken to be the unit
 * matrix.
 *
 * Otherwise 1 &le tau &le 2.
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class LAFRG {

    @lombok.Data
    public static class Reflector {

        double alpha, tau;
        final double[] px;
        final int xstart; // start of the x-part of the vector 
        final int n; // length of the full vector (including alpha,beta
        final int incx; // increment between x
        
        public Reflector(int n, DataPointer x){
            this.n=n;
            this.px=x.p;
            this. incx=x.inc();
            this.xstart=x.pos+incx;
            this.alpha=x.value();
        }
    }

    /**
     *
     * @param r
     */
    public void apply(Reflector r) {
        r.tau = 0;
        if (r.n <= 1) {
            return;
        }
        double alpha = r.alpha;
        double xnorm = LapackUtility.nrm2(r.n - 1, r.px, r.xstart, r.incx);
        if (xnorm == 0) {
            return;
        }
        double beta = -Math.copySign(LapackUtility.lapy2(alpha, xnorm), alpha);
        double eps = Constants.getEpsilon();
        double safemin = Constants.getSafeMin() / eps;
        int k = 0;
        if (Math.abs(beta) < safemin) {
            double rsafemin = 1 / safemin;
            do {
                SCAL.apply(r.n - 1, rsafemin, r.px, r.xstart, r.incx);
                alpha *= rsafemin;
                beta *= rsafemin;
            } while (Math.abs(beta) < safemin && ++k < 4);
            xnorm = LapackUtility.nrm2(r.n - 1, r.px, r.xstart, r.incx);
            beta = -Math.copySign(LapackUtility.lapy2(alpha, xnorm), alpha);
        }
        r.tau = (beta - alpha) / beta;
        SCAL.apply(r.n - 1, 1/(alpha - beta), r.px, r.xstart, r.incx);
        for (int j = 0; j < k; ++j) {
            beta *= safemin;
        }
        r.alpha = beta;
    }
}
