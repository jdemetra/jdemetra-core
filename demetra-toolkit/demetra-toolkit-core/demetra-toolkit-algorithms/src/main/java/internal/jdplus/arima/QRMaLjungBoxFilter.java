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

package internal.jdplus.arima;

import jdplus.arima.IArimaModel;
import jdplus.data.DataBlock;
import jdplus.data.LogSign;
import nbbrd.design.Development;
import jdplus.math.matrices.LowerTriangularMatrix;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.math.polynomials.Polynomial;
import jdplus.math.polynomials.RationalFunction;
import demetra.data.DoubleSeq;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class QRMaLjungBoxFilter {

    private int n, q;

    private Polynomial ma;
    private double[] u;

    private Matrix G, X, V1;

    private double m_t;

    // / <summary>
    // / MA(a0) = AR(w) or a0 = M w
    // / </summary>
    // / <param name="w"></param>
    // / <returns></returns>
    private double[] calca0(DoubleSeq w) {
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
	if (q > 0)
	    for (int i = n - 2; i >= 0; --i) {
		double s = 0;

		for (int j = 1, k = i + 1; j <= q && k < n; ++j, ++k)
		    s += ma.get(j) * g[k];
		g[i] -= s;
	    }
	return g;
    }

    // / <summary>
    // / G = PI' * PI
    // / </summary>
    // / <param name="m"></param>
    private void calcg(int m) {
	RationalFunction rf = RationalFunction.of(Polynomial.ONE, ma);
	double[] pi = rf.coefficients(n);
	Matrix gg = Matrix.square(m);

	// compute first column
	for (int i = 0; i < m; ++i) {
	    double s = 0;
	    for (int j = i; j < n; ++j)
		s += pi[j] * pi[j - i];
	    gg.set(i, 0, s);
	}

	for (int c = 1; c < m; ++c) {
	    DataBlock col = gg.column(c), prevcol = gg.column(c - 1);
	    for (int r = c; r < m; ++r)
		col.set(r, prevcol.get(r - 1) - pi[n - r] * pi[n - c]);
	}

	SymmetricMatrix.fromLower(gg);
	G = gg;
    }

    // / <summary>
    // / V1' * g
    // / </summary>
    // / <param name="g"></param>
    // / <returns></returns>
    private double[] calch(double[] g) {
	double[] h = new double[q];
	for (int i = 0; i < q; ++i)
	    for (int j = 0; j <= i; ++j)
		h[i] += ma.get(q - i + j) * g[j];
	return h;
    }

    private void calcv(double[] v) {
	for (int i = 0; i < q; ++i)
	    for (int j = i; j < q; ++j)
		v[i] += ma.get(q + i - j) * u[j];
	rma(v);
    }

    public void filter(DoubleSeq w, DataBlock wl) {
	// compute a0=Mw
	double[] a0 = calca0(w);
	double[] g = calcg(a0);
	u = calch(g);
        DataBlock U=DataBlock.of(u);
	LowerTriangularMatrix.solveLx(X, U);
	LowerTriangularMatrix.solvexL(X, U);
	double[] v = new double[w.length()];
	calcv(v);
	wl.range(0, q).copyFrom(u, 0);
	wl.drop(q, 0).set(i->a0[i]-v[i]);
    }

    /**
     * 
     * @return
     */
    public DoubleSeq getInitialResiduals()
    {
	return DoubleSeq.of(u);
    }

    // / <summary>
    // / v = V1 * m_u
    // / </summary>
    // / <param name="v"></param>
    public double getLogDeterminant() {
	return m_t;
    }

    public int initialize(IArimaModel arima, int n) {
	ma = arima.getMa().asPolynomial();
	this.n = n;
	q = ma.degree();

	// compute V1' * G * V1 = X' X and V (covar model)

	V1 = Matrix.square(q);

	if (q > 0) {
	    V1.diagonal().set(ma.get(q));
	    for (int i = 1; i < q; ++i)
		V1.subDiagonal(i).set(ma.get(q - i));
	}

	// compute G
	calcg(q);
	X = SymmetricMatrix.XtSX(G, V1);

	X.diagonal().add(1);
	SymmetricMatrix.lcholesky(X);
	m_t = 2 * LogSign.of(X.diagonal()).getValue();
	return n + q;
    }

    void rma(double[] a) {
	// MA(a) by induction
	if (q > 0) {
	    // first q steps
	    for (int i = 1; i < q; ++i) {
		double s = 0;
		for (int j = 1; j <= i; ++j)
		    s += ma.get(j) * a[i - j];
		a[i] -= s;
	    }
	    // next steps
	    for (int i = q; i < a.length; ++i) {
		double s = 0;
		for (int j = 1; j <= q; ++j)
		    s += ma.get(j) * a[i - j];
		a[i] -= s;
	    }
	}
    }
}
