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

package demetra.arima.estimation;

import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.LowerTriangularMatrix;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.polynomials.Polynomial;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class ModifiedLjungBoxFilter implements IArmaFilter {

    private int m_n, m_p, m_q;

    private Polynomial m_ar, m_ma;

    private double m_s;
    private MaLjungBoxFilter m_malb;
    private Matrix m_L, m_C;

    @Override
    public ModifiedLjungBoxFilter exemplar(){
        return new ModifiedLjungBoxFilter();
    }

    @Override
    public void filter(IReadDataBlock rw, DataBlock wl) {
	DataBlock w = new DataBlock(rw);
	// step 1. AR filter w, if necessary
	DataBlock z = w;
	if (m_p > 0) {
	    z = new DataBlock(w.getLength() - m_p);
	    DataBlock x = w.drop(m_p, 0);
	    z.copy(x);
	    for (int i = 1; i <= m_p; ++i) {
		x.move(-1);
		z.addAY(m_ar.get(i), x);
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
	    LowerTriangularMatrix.rsolve(m_L, wl.range(0, m_p));
    }

    @Override
    public double getLogDeterminant() {
	double s = m_s;
	if (m_malb != null)
	    s += m_malb.getLogDeterminant();
	return s;
    }

    @Override
    public int initialize(IArimaModel arima, int n) {
        clear();
	m_ar = arima.getAR().getPolynomial().adjustDegree();
	m_ma = arima.getMA().getPolynomial().adjustDegree();
	m_n = n;
	m_p = m_ar.getDegree();
	m_q = m_ma.getDegree();

	if (m_q > 0) {
	    m_malb = new MaLjungBoxFilter();
	    m_malb.initialize(arima, n - m_p);
	}
	// Compute the covariance matrix V

	if (m_p > 0) {
	    m_L = new Matrix(m_p, m_p);

	    // W = var(y)
	    SubMatrix W = m_L.subMatrix();
	    double[] cov = arima.getAutoCovarianceFunction().values(m_p);
	    W.diagonal().set(cov[0]);

	    for (int i = 1; i < m_p; ++i)
		W.subDiagonal(i).set(cov[i]);
	    if (m_q > 0) {
		double[] psi = arima.getPsiWeights().getRationalFunction()
			.coefficients(m_q);
		Matrix C = new Matrix(m_n - m_p, m_p);
		m_C = new Matrix(m_n + m_q - m_p, m_p);
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
	    m_s = 2 * m_L.diagonal().sumLog().value;
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
