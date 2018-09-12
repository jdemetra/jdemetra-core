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
package demetra.maths.polynomials;

import demetra.design.Development;
import demetra.maths.Complex;
import demetra.maths.polynomials.spi.RootsSolver;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class Laguerre implements RootsSolver {

    private final static int MR = 8, MT = 10, MAXIT = MR * MT;

    private final static double EPS = 1e-12, EPSS = 1e-14;

    private boolean m_bPolish = true;

    private Complex m_x;

    private final static double[] m_frac = new double[] { 0.0, 0.5, 0.25, 0.75,
	    0.13, 0.38, 0.62, 0.88, 1.0 };
    private Complex[] m_roots;
    private Polynomial m_remainder;

    /**
     * Default constructor
     */
    public Laguerre() {
	m_x = Complex.ZERO;
    }

    public void clear() {
	m_roots = null;
	m_remainder = null;
    }

    @Override
    public boolean factorize(final Polynomial p) {
	// if (p == null)
	// throw new ArgumentNullException("p");
	try {
	    int d = p.degree();
	    m_roots = new Complex[d];
	    Complex[] a = new Complex[d + 1];
	    for (int u = 0; u <= d; ++u)
		a[u] = Complex.cart(p.get(u), 0);
	    zroots(a, m_roots, m_bPolish);
	    m_remainder = Polynomial.valueOf(p.get(p.degree()));
	    return true;
	} catch (PolynomialException e) {
	    m_remainder = p;
	    m_roots = null;
	    return false;
	}
    }

    void laguer(final Complex[] a) throws PolynomialException {
	for (int iter = 1; iter <= MAXIT; ++iter) {
	    // Loop over iterations up to allowed maximum.
	    int m = a.length - 1;
	    Complex b = a[m];
	    double err = b.abs();
	    Complex d = Complex.ZERO, f = Complex.ZERO;
	    double abx = m_x.abs();
	    for (int j = m - 1; j >= 0; j--) {
		// computation of the polynomial and
		// its first two derivatives.
		f = m_x.times(f).plus(d);
		d = m_x.times(d).plus(b);
		b = m_x.times(b).plus(a[j]);
		err = b.abs() + abx * err;
	    }

	    err *= EPSS;
	    // Estimate of roundo error in evaluating polynomial.
	    if (b.abs() <= err)
		return; // We are on the root.
	    Complex g = d.div(b); // The generic case: use Laguerre's
	    // formula.
	    Complex g2 = g.times(g);
	    Complex h = g2.minus(f.div(b).times(2.0));
	    Complex sq = h.times(m).minus(g2).times(m - 1).sqrt();
	    Complex gp = g.plus(sq);
	    Complex gm = g.minus(sq);
	    double abp = gp.abs();
	    double abm = gm.abs();
	    if (abp < abm)
		gp = gm;
	    Complex dx = (Math.max(abp, abm) > 0.0) ? Complex.cart(m).div(gp)
		    : Complex.cart(Math.cos(iter), Math.sin(iter)).times(
			    1 + abx);
	    Complex x1 = m_x.minus(dx);
	    if (m_x.equals(x1))
		return; // Converged.
	    if (iter % MT != 0)
		m_x = x1;
	    else
		m_x = m_x.minus(dx.times((m_frac[iter / MT])));
	}
	throw new PolynomialException("Laguerre failed");
	// Very unusual | can occur only for Complex roots. Try a di erent
	// starting guess for the root.
    }

    @Override
    public Polynomial remainder() {
	return m_remainder;
    }

    @Override
    public Complex[] roots() {
	return m_roots;
    }
    
    void zroots(final Complex[] a, final Complex[] roots, final boolean polish) {
	// Given the degree m and the m+1 Complex coefficients a[0..m] of the
	// polynomial
	// P
	// m
	// i=0 a(i)x i ,
	// this routine successively calls laguer and nds all m Complex roots in
	// roots[1..m]. The
	// boolean variable polish should be input as true (1) if polishing
	// (also by Laguerre's method)
	// is desired, false (0) if the roots will be subsequently polished by
	// other means.

	int m = a.length - 1;
	Complex[] ad = a.clone(); // Copy of coefficients for
	// successive de iteration.
	for (int j = m - 1; j >= 0; j--) {
	    // Loop over each root to be found.
	    m_x = Complex.cart(0d);
	    // Start at zero to favor convergence to smallest remaining root,
	    // and nd the root.
	    Complex[] adv = new Complex[j + 2];
	    for (int jj = 0; jj < j + 2; ++jj)
		adv[jj] = ad[jj];
	    laguer(adv);
	    if (Math.abs(m_x.getIm()) <= 2.0 * EPS * Math.abs(m_x.getRe()))
		m_x = Complex.cart(m_x.getRe());
	    roots[j] = m_x;
	    Complex b = ad[j + 1]; // Forward de ation.
	    for (int jj = j; jj >= 0; jj--) {
		Complex c = ad[jj];
		ad[jj] = b;
		b = m_x.times(b).plus(c);
	    }
	}
	if (polish)
	    for (int j = 0; j < m; j++) // Polish the roots using the unde ated
	    // coefficients.
	    {
		m_x = roots[j];
		laguer(a);
	    }

	for (int j = 1; j < m; j++) {
	    // Sort roots by their real parts by straight insertion.
	    m_x = roots[j];
	    int i = j - 1;
	    for (; i >= 0; i--) {
		if (roots[i].getRe() <= m_x.getRe())
		    break;
		roots[i + 1] = roots[i];
	    }
	    roots[i + 1] = m_x;
	}
    }
}
