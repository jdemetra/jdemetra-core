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
import demetra.maths.Constants;
import demetra.maths.polynomials.spi.RootsSolver;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class GrantHitchinsSolver implements RootsSolver {

    private static final double ONE = 1.0, TWO = 2.0, ZERO = 0.0, TEN = 10.0,
	    P8 = 0.8, A8 = 8.0, A1P5 = 1.5, P4Z1 = 0.00001, P5 = 0.5,
	    P2Z1 = 0.001, P1 = 0.1, P3Z2 = 0.0002, FOUR = 4.0;
    private static final int g_max = 100;

    private double m_x, m_y, m_r, m_j, m_rx, m_jx;
    private double m_tol;
    // temporary buffers
    private double[] m_b, m_c;
    // scaling factor
    private double m_fac;
    // tolerance
    private static final double EPS = Constants.getEpsilon(), SMALL = 1e-15,
	    CMAX = Math.sqrt(Double.MAX_VALUE), EPS2 = EPS * 1e4;
    private Polynomial m_remainder;

    private Complex[] m_roots;

    /**
     *
     */
    public GrantHitchinsSolver() {
	m_tol = EPS;
    }

    /**
     * Evaluates R,RX,J,JX at the point X+IY and applies the Adam's test.
     * @param a
     * @param n
     * @param tol
     * @return True if the test is satisfied
     */
    private boolean evaluate(double[] a, int n, double tol) {
	double p = -TWO * m_x;
	double q = m_x * m_x + m_y * m_y;
	double t = Math.sqrt(q);
	double a2 = ZERO, b2 = ZERO, b1 = a[0], a1 = b1;
	double a3, b3;
	double c = Math.abs(a1) * P8;
	n -= 2;
	for (int k = 1; k < n; ++k) {
	    a3 = a2;
	    a2 = a1;
	    a1 = a[k] - p * a2 - q * a3;
	    c = t * c + Math.abs(a1);
	    b3 = b2;
	    b2 = b1;
	    b1 = a1 - p * b2 - q * b3;
	}
	n += 2;

	a3 = a2;
	a2 = a1;
	a1 = a[n - 2] - p * a2 - q * a3;
	m_r = a[n - 1] + m_x * a1 - q * a2;
	m_j = a1 * m_y;
	m_rx = a1 - TWO * b2 * m_y * m_y;
	m_jx = TWO * m_y * (b1 - m_x * b2);
	c = t * (t * c + Math.abs(a1)) + Math.abs(m_r);
	double eps = (TEN * c - A8 * (Math.abs(m_r) + Math.abs(a1) * t) + TWO
		* Math.abs(m_x * a1))
		* tol;
	return Math.sqrt(m_r * m_r + m_j * m_j) < eps;
    }

    public void clear() {
	m_remainder = null;
	m_roots = null;
    }

    private void compositedeflation(double[] a, int n, double[] rez,
	    double[] imz) {
	// MATCHING POINT FOR COMPOSITE DEFLATION
	double tol2 = Math.pow(m_tol, A1P5);
	double fun = ONE / tol2;
	int k = -1;
	for (int i = 0; i < n; ++i) {
	    double nfun = Math.abs(m_b[i]) + Math.abs(m_c[i]);
	    if (nfun > m_tol) {
		nfun = Math.abs(m_b[i] - m_c[i]) / nfun;
		if (nfun < fun) {
		    fun = nfun;
		    k = i;
		}
	    }
	}

	for (int i = 0; i < k; ++i)
	    a[i] = m_b[i];
	a[k] = P5 * (m_b[k] + m_c[k]);
	for (int i = k + 1; i < n; ++i)
	    a[i] = m_c[i];
    }

    private int deflate(double[] a, int n, double[] rez, double[] imz,
	    double eps) {
	// check for real root

	if (isrealroot(a, n, eps))
	    n = deflaterealroot(a, n, rez, imz);
	else
	    n = deflatecomplexroot(a, n, rez, imz);
	compositedeflation(a, n, rez, imz);
	return n;
    }

    private int deflatecomplexroot(double[] a, int n, double[] rez, double[] imz) {
	// COMPLEX ROOT ACCEPTED AND BOTH BACKWARD AND FORWARD
	// DEFLATIONS ARE PERFORMED WITH QUADRATIC FACTOR
	n -= 2;
	rez[n] = m_x * m_fac;
	rez[n - 1] = m_x * m_fac;
	imz[n] = m_y * m_fac;
	imz[n - 1] = -imz[n];

	m_r = TWO * m_x;
	m_j = -(m_x * m_x + m_y * m_y);
	m_b[0] = a[0];
	m_b[1] = a[1] + m_r * m_b[0];
	m_c[n - 1] = -a[n + 1] / m_j;
	m_c[n - 2] = -(a[n] + m_r * m_c[n - 1]) / m_j;
	boolean cbig = false;
	for (int i = 2; i < n; ++i) {
	    m_b[i] = a[i] + m_r * m_b[i - 1] + m_j * m_b[i - 2];
	    int j = n - i;
	    if (!cbig) {
		m_c[j] = -(a[j + 2] - m_c[j + 2] + m_r * m_c[j + 1]) / m_j;
		if (Math.abs(m_c[j]) <= CMAX)
		    continue;
		cbig = true;
	    }
	    m_c[j] = CMAX;

	}
	return n;
    }

    private int deflaterealroot(double[] a, int n, double[] rez, double[] imz) {
	// BOTH BACKWARD AND FORWARD DEFLATIONS
	// ARE PERFORMED WITH LINEAR FACTOR
	--n;
	rez[n - 1] = m_x * m_fac;
	imz[n - 1] = ZERO;
	m_b[0] = a[0];
	m_c[n - 1] = -a[n] / m_x;
	boolean cbig = false;
	for (int i = 1; i < n; ++i) {
	    m_b[i] = a[i] + m_x * m_b[i - 1];
	    int j = n - i;
	    if (!cbig) {
		m_c[j] = (m_c[j + 1] - a[j + 1]) / m_x;
		if (Math.abs(m_c[j]) <= CMAX)
		    continue;
		cbig = true;
	    }
	    m_c[j] = CMAX;
	}
	return n;
    }

    @Override
    public boolean factorize(Polynomial p) {
	int degree = p.getDegree();
	while ((degree > 0) && (p.get(degree) == 0))
	    --degree;
	if (degree == 0)
	    return false;
	double[] a = new double[degree + 1];
	for (int i = 0; i <= degree; ++i)
	    a[i] = p.get(degree - i);
	double[] rez = new double[degree];
	double[] imz = new double[degree];
	try {
	    if (this.solve(a, a.length, rez, imz)) {
		// create roots...
		m_roots = new Complex[degree];
		for (int i = 0; i < degree; ++i)
		    m_roots[i] = Complex
			    .cart(rez[i], imz[i]);
		m_remainder = Polynomial.valueOf(p.get(p.getDegree()));
		return true;
	    } else
		return false;
	} catch (PolynomialException ex) {
	    return false;
	}
    }

    /**
     * 
     * @return
     */
    public double getTol() {
	return m_tol;
    }

    private boolean isrealroot(double[] a, int n, double tol) {
	double tmp = m_y;
	m_y = ZERO;
	boolean rslt = evaluate(a, n, tol);
	// restore y
	m_y = tmp;
	return rslt;
    }

    @Override
    public Polynomial remainder() {
	return m_remainder;
    }
    
    private void rescale_a(double[] a, int n) {
	double scale = ONE;
	int i = n - 1;
	while (--i >= 0) {
	    scale *= m_fac;
	    a[i] /= scale;
	}
    }

    @Override
    public Complex[] roots() {
	return m_roots;
    }

    private void scale_a(double[] a, int n) {
	double scale = ZERO;
	for (int i = 0; i < n; ++i) {
	    double ai = Math.abs(a[i]);
	    if (ai >= P4Z1)
		scale += Math.log(ai);
	}
	int q = (int) (scale / (n * Math.log(TWO)) + P5);
	scale = Math.pow(TWO, -q);
	for (int i = 0; i < n; ++i)
	    a[i] *= scale;
    }

    private boolean search(double[] a, int n, double tol) {
	evaluate(a, n, tol);
	double fun = m_r * m_r + m_j * m_j;
	int j = 0;
	double tol2 = Math.pow(m_tol, A1P5);

	while (++j < g_max) {
	    double g = m_rx * m_rx + m_jx * m_jx;
	    if (g < fun * tol2)
		return false;
	    double s1 = -(m_r * m_rx + m_j * m_jx) / g;
	    double s2 = (m_r * m_jx - m_j * m_rx) / g;
	    double sig = P3Z2;
	    double s = Math.sqrt(s1 * s1 + s2 * s2);
	    if (s > ONE) {
		s1 /= s;
		s2 /= s;
		sig /= s;
	    }
	    // VALID DIRECTION OF SEARCH HAS BEEN DETERMINED, NOW
	    // PROCEED TO DETERMINE SUITABLE STEP
	    m_x += s1;
	    m_y += s2;
	    double nfun = 0;
	    int k = 0;
	    while (++k < g_max) {
		if (evaluate(a, n, tol))
		    return true;
		nfun = m_r * m_r + m_j * m_j;
		if (fun - nfun >= sig * fun)
		    break;
		s1 = P5 * s1;
		s2 = P5 * s2;
		if (Math.abs(s1) <= EPS * Math.abs(m_x)
			&& Math.abs(s2) <= EPS * Math.abs(m_y))
		    return false;
		s *= P5;
		sig *= P5;
		m_x -= s1;
		m_y -= s2;
	    }
	    if (k == g_max)
		throw new PolynomialException(
			"Infinite loop in GrantHitchinsSolver");
	    fun = nfun;
	}
	if (j == g_max)
	    throw new PolynomialException(
		    "Infinite loop in GrantHitchinsSolver");
	return true;
    }

    /**
     * 
     * @param tol
     */
    public void setTol(double tol) {
	if (tol < EPS)
	    throw new PolynomialException(
		    "tol is too small");
	m_tol = tol;
    }

    private boolean solve(double[] a, int n, double[] rez, double[] imz) {
	n = solve(a, n, rez, imz, false, m_tol);
	if (n != 1)
	    n = solve(a, n, rez, imz, true, EPS2);
	if (n <= 1)
	    return true;
	else
	    return false;
    }

    private int solve(double[] a, int n, double[] rez, double[] imz,
	    boolean reentry, double eps) {
	// THIS ROUTINE ATTEMPTS TO SOLVE A REAL POLYNOMIAL EQUATION
	// HAVING N COEFFICIENTS (DEGREE EQUALS N-1) USING THE SEARCH
	// ALGORITHM PROPOSED IN GRANT AND HITCHINS (1971) TO
	// LIMITING MACHINE PRECISION. ON ENTRY THE COEFFICIENTS
	// OF THE POLYNOMIAL ARE HELD IN THE ARRAY A(N), WITH A(0)
	// HOLDING THE COEFFICIENT OF THE HIGHEST POWER. ON NORMAL
	// ENTRY THE PARAMETER IFAIL HAS VALUE 0 (HARD FAIL) OR 1
	// (SOFT FAIL) AND WILL BE ZERO ON SUCCESFUL EXIT WITH
	// THE CALCULATED ESTIMATES OF THE ROOTS HELD AS
	// REZ(K)+I*IMZ(K), K EQUALS N-1, IN APPROXIMATE DECREASING
	// ORDER OF MODULUS. THE VALUE OF TOL IS OBTAINED BY
	// CALLING THE ROUTINE X02AJF.
	// ABNORMAL EXITS WILL BE INDICATED BY IFAIL HAVING
	// VALUE 1 OR 2. THE FORMER IMPLIES THAT EITHER A(1) EQUALS 0
	// OR N.LT.2 OR N.GT.100. FOR IFAIL EQUALS 2, A POSSIBLE
	// SADDLE POINT HAS BEEN DETECTED. THE NUMBER OF COEFFICIENTS
	// OF THE REDUCED POLYNOMIAL IS STORED IN N AND ITS
	// COEFFICIENTS ARE STORED IN A(1) TO A(N), THE ROOTS
	// THUS FAR BEING STORED IN THE ARRAYS REZ AND IMZ
	// STARTING WITH REZ(N)+I*IMZ(N). AN IMMEDIATE RE-ENTRY
	// IS POSSIBLE WITH IFAIL UNCHANGED AND WITH A NEW
	// STARTING POINT FOR THE SEARCH HELD IN REZ(1)+IIMZ(1).
	// REF - J.I.M.A., VOL.8., PP122-129 (1971).
	// .. Parameters ..
	// Implicits ..
	// Formal Arguments ..
	// In/Out Status: Read, Maybe Written ..
	// integer n
	// In/Out Status: Maybe Read, Maybe Written ..
	// double precision a(n)
	// In/Out Status: Maybe Read, Maybe Written ..
	// double precision rez(n)
	// In/Out Status: Maybe Read, Maybe Written ..
	// double precision imz(n)
	// In/Out Status: Read, Maybe Written ..
	// double precision tol
	// In/Out Status: Read, Overwritten ..
	// nothing to do
	if (n < 2)
	    return n;
	if (Math.abs(a[0]) <= SMALL)
	    throw new PolynomialException(
		    "invalid polynomial");

	// .. Executable Statements ..
	// double cmax = Math.sqrt(m_tol);
	m_fac = ONE;

	// remove ZERO roots
	while (a[n - 1] == ZERO) {
	    --n;
	    rez[n - 1] = ZERO;
	    imz[n - 1] = ZERO;
	}
	while (n > 1)
	    // test for low order polynomials
	    // rescaling should not be necessary in such cases
	    if (n == 2) {
		solve1(a, rez, imz);
		return 1;
	    } else if (n == 3) {
		solve2(a, rez, imz);
		return 1;
	    } else // general case
	    {
		int nn = solven(a, n, rez, imz, reentry, eps);
		if (nn == n)
		    return n;
		else
		    n = nn;
	    }
	return 1;
    }

    private void solve1(double[] a, double[] rez, double[] imz) {
	rez[0] = -a[1] / a[0] * m_fac;
	imz[0] = ZERO;
    }

    private void solve2(double[] a, double[] rez, double[] imz) {
	m_r = a[1] * a[1] - FOUR * a[0] * a[2];
	if (m_r > ZERO) {
	    imz[0] = ZERO;
	    imz[1] = ZERO;
	    if (a[1] < ZERO)
		rez[0] = P5 * (-a[1] + Math.sqrt(m_r)) / a[0] * m_fac;
	    else if (a[1] == ZERO)
		rez[0] = -P5 * Math.sqrt(m_r) / a[0] * m_fac;
	    else
		rez[0] = P5 * (-a[1] - Math.sqrt(m_r)) / a[0] * m_fac;
	    rez[1] = a[2] / (rez[0] * a[0]) * m_fac * m_fac;
	} else {
	    rez[1] = -P5 * a[1] / a[0] * m_fac;
	    rez[0] = rez[1];
	    imz[1] = P5 * Math.sqrt(-m_r) / a[0] * m_fac;
	    imz[0] = -imz[1];
	}
    }

    private int solven(double[] a, int n, double[] rez, double[] imz,
	    boolean reentry, double eps) {
	scale_a(a, n);
	m_b = a.clone();
	m_c = new double[n];
	step1(a, n);
	if (!reentry) {
	    m_x = P2Z1;
	    m_y = P1;
	} else {
	    m_x = rez[0];
	    m_y = imz[0] + 2 * eps;
	    reentry = false;
	}
	if (!search(a, n, eps)) {
	    rescale_a(a, n);
	    // saddle point ?
	    return n;
	}
	return deflate(a, n, rez, imz, eps);
    }

    private void step1(double[] a, int n) {
	while (true) {
	    for (int i = n - 1; i > 0; --i) {
		if (m_b[i] == ZERO)
		    return;
		double t = m_b[0] / m_b[i];
		if (Math.abs(t) >= ONE)
		    return;
		for (int k = 1; k <= i; ++k)
		    m_c[k - 1] = m_b[k] - t * m_b[i - k];
		for (int k = 0; k < i; ++k)
		    m_b[k] = m_c[k];
	    }
	    m_fac *= TWO;
	    double scale = ONE;
	    for (int i = n - 2; i >= 0; --i) {
		scale *= TWO;
		a[i] *= scale;
		m_b[i] = a[i];
	    }
	}
    }
}
