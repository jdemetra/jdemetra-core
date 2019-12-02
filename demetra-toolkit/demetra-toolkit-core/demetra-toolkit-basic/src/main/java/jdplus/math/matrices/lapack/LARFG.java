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
 * H * x = ( beta, 0 )', H' * H = I.
 *
 * where beta is a scalar, and x is an n-element real vector. H is
 * represented in the form
 *
 * H = I - tau * ( 1, v )' * ( 1, v ) 
 *
 * where tau is a real scalar and v is a real (n-1)-element vector.
 *
 * If the n-1 last elements of x are all zero, then tau = 0 and H is taken to be the unit
 * matrix.
 *
 * Otherwise 1 &le tau &le 2.
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class LARFG {

    /**
     *
     * @param r On entry, the considered array is x
     * On exit, the reflector is H - tau*(1,v)'*(1,v)   
     * 
     * Hx = (beta,0)'
     */
    public void apply(HouseholderReflector r) {
        r.tau = 0;
        int m=r.n-1;
        if (m <= 0) {
            return;
        }
        double x0 = r.x0();
        DataPointer v=r.v();
        double xnorm = v.norm2(m);
        if (xnorm == 0) {
            return;
        }
        
        double beta = -Math.copySign(LapackUtility.lapy2(x0, xnorm), x0);
        double eps = Constants.getEpsilon();
        double safemin = Constants.getSafeMin() / eps;
        int k = 0;
        if (Math.abs(beta) < safemin) {
            double rsafemin = 1 / safemin;
            do {
                v.mul(m, rsafemin);
                x0 *= rsafemin;
                beta *= rsafemin;
            } while (Math.abs(beta) < safemin && ++k < 4);
            xnorm = v.norm2(m);
            beta = -Math.copySign(LapackUtility.lapy2(x0, xnorm), x0);
        }
        r.tau = (beta - x0) / beta;
        v.mul(m, 1 / (x0 - beta));
        for (int j = 0; j < k; ++j) {
            beta *= safemin;
        }
        r.beta(beta);
    }
}
