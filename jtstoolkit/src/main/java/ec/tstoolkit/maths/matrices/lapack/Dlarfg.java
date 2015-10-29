/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
*/

package ec.tstoolkit.maths.matrices.lapack;

import ec.tstoolkit.maths.Constants;

/**
 * 
 * @author Jean Palate
 */
public class Dlarfg {
    private final static double ZERO = 0.0, ONE = 1.0;
    private double m_beta, m_tau, m_alpha;

    /**
     * 
     * @return
     */
    public final double beta() {
	return m_beta;
    }
    
    public final double scale(){
        return m_alpha;
    }

    /**
     * DLARFG generates a real elementary reflector H of order n, such that
     * H * ( alpha | x' )' = ( beta | 0 )', H' * H = I.
     * where alpha and beta are scalars, and x is an (n-1)-element real
     * vector. H is represented in the form
     * H = I - tau * ( 1 | v')' * ( 1 | v' ) ,
     * where tau is a real scalar and v is a real (n-1)-element
     * vector.
     * If the elements of x are all zero, then tau = 0 and H is taken to be
     * the unit matrix.
     * Otherwise 1 <= tau <= 2.    
     * @param n The order of the elementary reflector.
     * @param alpha The value alpha (scaling factor)
     * @param x Array containing the vector x (on entry). The items corresponding to
     * x are overwritten with the vector v on exit.
     * @param ix Position of the first item of x (or v)
     * @param incx Increment between elements of x (or v).
     */
    public final void fn(final int n, final double alpha, final double[] x,
	    final int ix, final int incx) {

        m_alpha=alpha;
	m_beta = m_alpha;
	m_tau = ZERO;
	if (n <= 1)
	    return;
	double xnorm = Dnrm2.fn(n - 1, x, ix, incx);
	if (xnorm == ZERO)
	    // H = I
	    return;
	// general case
	m_beta = -Dlapy2.fn(m_alpha, xnorm) * Math.signum(m_alpha);
	double safmin = Constants.getSafeMinimum() / Constants.getEpsilon();
	if (Math.abs(m_beta) < safmin) {
	    // XNORM, BETA may be inaccurate; scale X and recompute them
	    double rsafmin = ONE / safmin;
	    int knt = 0;
	    do {
		++knt;
		Dscal.fn(n - 1, rsafmin, x, ix, incx);
		m_beta *= rsafmin;
		m_alpha *= rsafmin;
	    } while (Math.abs(m_beta) < safmin);
	    // New BETA is at most 1, at least SAFMIN
	    xnorm = Dnrm2.fn(n - 1, x, ix, incx);
	    m_beta = -Dlapy2.fn(m_alpha, xnorm) * Math.signum(m_alpha);
	    m_tau = (m_beta - m_alpha) / m_beta;
	    Dscal.fn(n - 1, ONE / (m_alpha - m_beta), x, ix, incx);
	    // If ALPHA is subnormal, it may lose relative accuracy
	    for (int j = 0; j < knt; ++j)
		m_beta *= safmin;
	} else {
	    m_tau = (m_beta - m_alpha) / m_beta;
	    Dscal.fn(n - 1, ONE / (m_alpha - m_beta), x, ix, incx);
	}
    }

    /**
     * 
     * @return
     */
    public final double tau() {
	return m_tau;
    }
}