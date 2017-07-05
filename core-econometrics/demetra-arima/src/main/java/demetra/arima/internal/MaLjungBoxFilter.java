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

package demetra.arima.internal;

import demetra.arima.IArimaModel;
import demetra.arima.estimation.IArmaFilter;
import demetra.data.DataBlock;
import demetra.data.Doubles;
import demetra.data.LogSign;
import demetra.design.Development;
import demetra.maths.matrices.LowerTriangularMatrix;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.maths.polynomials.Polynomial;
import demetra.maths.polynomials.RationalFunction;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class MaLjungBoxFilter {

    private int m_n, m_q;

    private Polynomial m_ma;
    private double[] m_u;

    private Matrix m_G, m_X, m_V1;

    private double m_t;

    // / <summary>
    // / MA(a0) = AR(w) or a0 = M w
    // / </summary>
    // / <param name="w"></param>
    // / <returns></returns>
    private double[] calca0(Doubles w) {
	double[] a0 = new double[w.length()];
	w.copyTo(a0, 0);
	rma(a0);

	return a0;
    }

    // / <summary>
    // / MA'(g)= a0 or g = L2^-1 * a0
    // / </summary>
    // / <param name="a0"></param>
    // / <returns></returns>
    private double[] calcg(double[] a0) {
	double[] g = a0.clone();
	if (m_q > 0)
	    for (int i = m_n - 2; i >= 0; --i) {
		double s = 0;

		for (int j = 1, k = i + 1; j <= m_q && k < m_n; ++j, ++k)
		    s += m_ma.get(j) * g[k];
		g[i] -= s;
	    }
	return g;
    }

    // / <summary>
    // / G = PI' * PI
    // / </summary>
    // / <param name="m"></param>
    private void calcg(int m) {
	RationalFunction rf = RationalFunction.of(Polynomial.ONE, m_ma);
	double[] pi = rf.coefficients(m_n);
	Matrix gg = Matrix.square(m);

	// compute first column
	for (int i = 0; i < m; ++i) {
	    double s = 0;
	    for (int j = i; j < m_n; ++j)
		s += pi[j] * pi[j - i];
	    gg.set(i, 0, s);
	}

	for (int c = 1; c < m; ++c) {
	    DataBlock col = gg.column(c), prevcol = gg.column(c - 1);
	    for (int r = c; r < m; ++r)
		col.set(r, prevcol.get(r - 1) - pi[m_n - r] * pi[m_n - c]);
	}

	SymmetricMatrix.fromLower(gg);
	m_G = gg;
    }

    // / <summary>
    // / V1' * g
    // / </summary>
    // / <param name="g"></param>
    // / <returns></returns>
    private double[] calch(double[] g) {
	double[] h = new double[m_q];
	for (int i = 0; i < m_q; ++i)
	    for (int j = 0; j <= i; ++j)
		h[i] += m_ma.get(m_q - i + j) * g[j];
	return h;
    }

    private void calcv(double[] v) {
	for (int i = 0; i < m_q; ++i)
	    for (int j = i; j < m_q; ++j)
		v[i] += m_ma.get(m_q + i - j) * m_u[j];
	rma(v);
    }

    public void filter(Doubles w, DataBlock wl) {
	// compute a0=Mw
	double[] a0 = calca0(w);
	double[] g = calcg(a0);
	m_u = calch(g);
        DataBlock U=DataBlock.ofInternal(m_u);
	LowerTriangularMatrix.rsolve(m_X, U);
	LowerTriangularMatrix.lsolve(m_X, U);
	double[] v = new double[w.length()];
	calcv(v);
	wl.range(0, m_q).copyFrom(m_u, 0);
	wl.drop(m_q, 0).set(i->a0[i]-v[i]);
    }

    /**
     * 
     * @return
     */
    public Doubles getInitialResiduals()
    {
	return Doubles.ofInternal(m_u);
    }

    // / <summary>
    // / v = V1 * m_u
    // / </summary>
    // / <param name="v"></param>
    public double getLogDeterminant() {
	return m_t;
    }

    public int initialize(IArimaModel arima, int n) {
	m_ma = arima.getMA().asPolynomial();
	m_n = n;
	m_q = m_ma.getDegree();

	// compute V1' * G * V1 = X' X and V (covar model)

	m_V1 = Matrix.square(m_q);

	if (m_q > 0) {
	    m_V1.diagonal().set(m_ma.get(m_q));
	    for (int i = 1; i < m_q; ++i)
		m_V1.subDiagonal(i).set(m_ma.get(m_q - i));
	}

	// compute G
	calcg(m_q);
	m_X = SymmetricMatrix.XtSX(m_G, m_V1);

	m_X.diagonal().add(1);
	SymmetricMatrix.lcholesky(m_X);
	m_t = 2 * LogSign.of(m_X.diagonal()).value;
	return n + m_q;
    }

    void rma(double[] a) {
	// MA(a) by induction
	if (m_q > 0) {
	    // first q steps
	    for (int i = 1; i < m_q; ++i) {
		double s = 0;
		for (int j = 1; j <= i; ++j)
		    s += m_ma.get(j) * a[i - j];
		a[i] -= s;
	    }
	    // next steps
	    for (int i = m_q; i < a.length; ++i) {
		double s = 0;
		for (int j = 1; j <= m_q; ++j)
		    s += m_ma.get(j) * a[i - j];
		a[i] -= s;
	    }
	}
    }
}
