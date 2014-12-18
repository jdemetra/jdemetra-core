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
package ec.tstoolkit.maths.matrices;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.maths.Fft;
import ec.tstoolkit.utilities.Arrays2;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public final class Toeplitz
{

    /**
     * Durbin algorithm to solve the Yule-Walker equations: <br>
     * Ty=-r, where T[i, j] = r[abs(i-j)](Toeplitz matrix). <br>
     * Reference G.H.Golub and C.F.Van Loan. Matrix Computations, third edition,
     * pages 194-195. ISBN 0-8018-5414-8.
     * 
     * @param r
     * @return
     */
    public static double[] solveDurbinSystem(final double[] r) {
	int n = r.length - 1;
	double[] y = new double[n];
	double alpha = -r[1] / r[0], beta = r[0];
	y[0] = alpha;
	for (int k = 0; k < n - 1; ++k) {
	    beta *= (1 - alpha * alpha);
	    double tmp = 0;
	    for (int i = k; i >= 0; --i)
		tmp += r[k - i + 1] * y[i];
	    alpha = -(r[k + 2] + tmp) / beta;
	    int k2 = (1 + k) / 2;
	    for (int i = 0; i < k2; ++i) {
		double yi = y[i];
		y[i] += alpha * y[k - i];
		y[k - i] += alpha * yi;
	    }
	    if (k % 2 == 0)
		y[k2] *= (1 + alpha);
	    y[k + 1] = alpha;
	}
	return y;
    }

    /**
     * 
     * @param r
     * @param b
     * @return
     */
    public static double[] solveLevinsonSystem(final double[] r,
	    final double[] b) {
	int n = r.length - 1;
	double[] y = new double[n - 1];
	double[] x = new double[n];
	double alpha = -r[1] / r[0], beta = r[0];
	y[0] = alpha;
	x[0] = b[0] / r[0];
	for (int k = 0; k < n - 1; ++k) {
	    beta *= (1 - alpha * alpha);
	    double tmp = 0;
	    for (int i = k; i >= 0; --i)
		tmp += r[k - i + 1] * x[i];
	    double mu = (b[k + 1] - tmp) / beta;
	    for (int i = 0; i <= k; ++i)
		x[i] += mu * y[k - i];
	    x[k + 1] = mu;

	    if (k < n - 2) {
		tmp = 0;
		for (int i = k; i >= 0; --i)
		    tmp += r[k - i + 1] * y[i];
		alpha = -(r[k + 2] + tmp) / beta;
		int k2 = (1 + k) / 2;
		for (int i = 0; i < k2; ++i) {
		    double yi = y[i];
		    y[i] += alpha * y[k - i];
		    y[k - i] += alpha * yi;
		}
		if (k % 2 == 0)
		    y[k2] *= (1 + alpha);
		y[k + 1] = alpha;
	    }
	}
	return x;
    }

    private final double[] m_r;

    /**
     * 
     * @param r
     */
    public Toeplitz(final double[] r) {
	m_r = r.clone();
    }

    /**
     * 
     * @param t
     */
    public Toeplitz(final Toeplitz t) {
	m_r = t.m_r.clone();
    }

    /**
     * Trench algorithm for the inversion of a symmetric Toeplitz matrix.
     * 
     * @return
     */
    public Matrix inverse() {
	int n = m_r.length, nc = n - 1;
	Matrix m = new Matrix(n, n);
	// double[] rc = new double[nc];
	// Array.Copy(m_r, rc, nc);
	double[] y = solveDurbinSystem(m_r);
	double tmp = m_r[0];
	for (int i = 0; i < nc; ++i)
	    tmp += m_r[i + 1] * y[i];
	double gamma = 1 / tmp;
	m.set(0, 0, gamma);
	m.set(nc, nc, gamma);
	for (int i = 0; i < nc; ++i) {
	    y[i] *= gamma;
	    m.set(0, i + 1, y[i]);
	    m.set(nc - i - 1, nc, y[i]);
	}
	Arrays2.reverse(y);
	int imax = (n + 1) / 2;
	for (int i = 1; i < imax; ++i)
	    for (int j = i; j < n - i; ++j) {
		double x = m.get(i - 1, j - 1)
			+ (y[nc - i] * y[nc - j] - y[i - 1] * y[j - 1]) / gamma;
		m.set(i, j, x);
		m.set(nc - j, nc - i, x);
	    }
	SymmetricMatrix.fromUpper(m);
	return m;
    }

    /**
     * 
     * @param x
     */
    public void mul(final DataBlock x) {
	int n = m_r.length;
	if (n == 1)
	    x.mul(0, m_r[0]);
	else {
	    // By FFT.
	    // circulant matrix
	    int nc = 2 * n - 2;
	    // DFT length:
	    int q = 2;
	    while (q < nc)
		q <<= 1;
	    Complex[] fx = new Complex[q];
	    Complex[] fr = new Complex[q];
	    for (int i = 0; i < n; ++i) {
		fx[i] = Complex.cart(x.get(i));
		fr[i] = Complex.cart(m_r[i]);
	    }
	    for (int i = n - 2, j = n; i > 0; --i, ++j)
		fr[j] = Complex.cart(m_r[i]);
	    Fft fft = new Fft();
	    fft.transform(fx);
	    fft.transform(fr);
	    for (int i = 0; i < q; ++i)
		fx[i] = fx[i].times(fr[i]);
	    fft.backTransform(fx);
	    for (int i = 0; i < n; ++i)
		x.set(i, fx[i].getRe());
	}

    }
}
