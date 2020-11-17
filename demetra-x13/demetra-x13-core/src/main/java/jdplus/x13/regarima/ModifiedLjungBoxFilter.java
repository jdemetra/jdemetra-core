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

package jdplus.x13.regarima;

import jdplus.arima.IArimaModel;
import jdplus.data.DataBlock;
import jdplus.data.DataWindow;
import jdplus.data.LogSign;
import demetra.design.AlgorithmImplementation;
import static demetra.design.AlgorithmImplementation.Feature.Legacy;
import nbbrd.design.Development;
import jdplus.math.matrices.LowerTriangularMatrix;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.math.polynomials.Polynomial;
import jdplus.arima.estimation.ArmaFilter;
import demetra.data.DoubleSeq;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@AlgorithmImplementation(algorithm=ArmaFilter.class, feature=Legacy)
public class ModifiedLjungBoxFilter implements ArmaFilter {

    private int m_n, m_p, m_q;

    private Polynomial m_ar, m_ma;

    private double m_s;
    private MaLjungBoxFilter m_malb;
    private Matrix m_L, m_C;

    @Override
    public void apply(DoubleSeq rw, DataBlock wl) {
	DataBlock w = DataBlock.of(rw);
	// step 1. AR filter w, if necessary
	DataBlock z = w;
	if (m_p > 0) {
	    z = DataBlock.make(w.length() - m_p);
	    DataWindow x = w.drop(m_p, 0).window();
	    z.copy(x.get());
	    for (int i = 1; i <= m_p; ++i) {
		z.addAY(m_ar.get(i), x.move(-1));
	    }
	}
	// filter z (pure ma part)
	if (m_malb != null)
	    m_malb.filter(z, wl.drop(m_p, 0));
	else
	    wl.drop(m_p, 0).copy(z);
	if (m_C != null)
	    // computes the first residuals
	    // y-LC * wl
	    for (int i = 0; i < m_p; ++i)
		wl.set(i, w.get(i) - m_C.column(i).dot(wl.drop(m_p, 0)));
	else
	    wl.range(0, m_p).copy(w.range(0, m_p));
	if (m_L != null)
	    LowerTriangularMatrix.solveLx(m_L, wl.range(0, m_p));
    }

    @Override
    public double getLogDeterminant() {
	double s = m_s;
	if (m_malb != null)
	    s += m_malb.getLogDeterminant();
	return s;
    }

    @Override
    public int prepare(IArimaModel arima, int n) {
        clear();
	m_ar = arima.getAr().asPolynomial();
	m_ma = arima.getMa().asPolynomial();
	m_n = n;
	m_p = m_ar.degree();
	m_q = m_ma.degree();

	if (m_q > 0) {
	    m_malb = new MaLjungBoxFilter();
	    m_malb.prepare(arima, n - m_p);
	}
	// Compute the covariance matrix V

	if (m_p > 0) {
	    m_L = Matrix.square(m_p);

	    // W = var(y)
	    double[] cov = arima.getAutoCovarianceFunction().values(m_p);
	    m_L.diagonal().set(cov[0]);

	    for (int i = 1; i < m_p; ++i)
		m_L.subDiagonal(i).set(cov[i]);
	    if (m_q > 0) {
		double[] psi = arima.getPsiWeights().getRationalFunction()
			.coefficients(m_q);
		Matrix C = Matrix.make(m_n - m_p, m_p);
		m_C = Matrix.make(m_n + m_q - m_p, m_p);
		// fill in the columns of m_C and filter them
		for (int c = 0; c < m_p; ++c) {
		    DataBlock col = C.column(c);
		    for (int r = 0; r < m_q; ++r) {
			int imin = m_p + r - c;
			if (imin > m_q)
			    break;
			double s = 0;
			for (int i = imin; i <= m_q; ++i)
			    s += m_ma.get(i) * psi[i - imin];
			col.set(r, s);
		    }
		    if (m_malb != null)
			m_malb.filter(col, m_C.column(c));
		}

		// Compute var(y|z)
		for (int i = 0; i < m_p; ++i)
		    for (int j = 0; j <= i; ++j) {
			double z = m_C.column(i).dot(m_C.column(j));
			m_L.add(j, i, -z);

		    }
	    }
	    SymmetricMatrix.fromUpper(m_L);
	    SymmetricMatrix.lcholesky(m_L);
	    m_s = 2 * LogSign.of(m_L.diagonal()).getValue();
	}
	return n + m_q;
    }

    private void clear() {
        m_malb=null;
        m_s=0;
        m_L=null;
        m_C=null;
    }
}
