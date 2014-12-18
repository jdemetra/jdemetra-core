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
package ec.tstoolkit.ssf.arima;

import ec.tstoolkit.arima.AutoCovarianceFunction;
import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.polynomials.RationalFunction;
import ec.tstoolkit.ssf.IFilteringResults;
import ec.tstoolkit.ssf.ISsfData;
import ec.tstoolkit.ssf.ISsfInitializer;
import ec.tstoolkit.ssf.SsfException;
import ec.tstoolkit.ssf.State;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public final class SsfArima extends SsfBaseArima {

    /**
     * 
     */
    public static class Initializer implements ISsfInitializer<SsfArima>
    {

        /**
         * 
         */
        public Initializer()
        {
	}

        /**
         *
         * @param ssf
         * @param a
         * @param data
         */
        public void calcInitialState(final SsfArima ssf, final DataBlock a,
		final ISsfData data) {
	    // the first d variables are the original ones...
	    // computing the remaining original variable

	    // should be modified if the first values contain missing values
	    int nr = ssf.m_dim, nd = ssf.m_dif.length - 1;
	    Matrix A = new Matrix(nr + nd, nd);
	    for (int j = 0; j < nd; ++j) {
		A.set(j, j, 1);
		for (int i = nd; i < nd + nr; ++i) {
		    double c = 0;
		    for (int k = 1; k <= nd; ++k)
			c -= ssf.m_dif[k] * A.get(i - k, j);
		    A.set(i, j, c);
		}
	    }

	    for (int i = 0; i < nr; ++i) {
		double c = 0;
		for (int j = 0; j < nd; ++j)
		    c += A.get(i + nd, j) * data.get(j);
		a.set(i, c);
	    }
	}

        /**
         *
         * @param ssf
         * @param data
         * @param state
         * @param rslts
         * @return
         */
        @Override
        public int initialize(final SsfArima ssf, final ISsfData data,
		final State state, final IFilteringResults rslts) {
	    int d = ssf.m_dif.length - 1;
	    if (d == 0)
		ssf.Pf0(state.P.subMatrix());
	    else {
		calcInitialState(ssf, state.A, data);
		int dim = ssf.m_dim;
		Matrix stV = new Matrix(dim, dim);
		stVar(stV.subMatrix(), ssf.m_stpsi, ssf.m_stacgf, ssf.m_model
			.getInnovationVariance());
		Matrix K = new Matrix(dim, dim);
		Ksi(K.subMatrix(), ssf.m_dif);
		SymmetricMatrix.quadraticFormT(stV.subMatrix(), K.subMatrix(),
			state.P.subMatrix());
	    }
	    return d;
	}
    }

    /**
     * 
     * @param b
     * @param d
     */
    public static void B0(final SubMatrix b, final double[] d)
    {
	int nd = d.length - 1;
	if (nd == 0)
	    return;
	int nr = b.getRowsCount();
	for (int i = 0; i < nd; ++i) {
	    b.set(i, i, 1);
	    for (int k = nd; k < nr; ++k) {
		double w = 0;
		for (int l = 1; l <= nd; ++l)
		    w -= d[l] * b.get(k - l, i);
		b.set(k, i, w);
	    }
	}
    }

    /**
     * 
     * @param X
     * @param dif
     */
    public static void Ksi(final SubMatrix X, final double[] dif)
    {
	int n = X.getRowsCount();
	double[] ksi = new RationalFunction(Polynomial.ONE, Polynomial.of(dif)).coefficients(n);

	for (int j = 0; j < n; ++j)
	    for (int k = 0; k <= j; ++k)
		X.set(j, k, ksi[j - k]);
    }

    /**
     *
     * @param stV
     * @param stpsi
     * @param stacgf
     * @param var
     */
    public static void stVar(final SubMatrix stV, final double[] stpsi,
	    final double[] stacgf, final double var) {
	int n = stV.getRowsCount();

	for (int j = 0; j < n; ++j)
	    stV.set(j, 0, stacgf[j]);

	for (int j = 0; j < n - 1; ++j) {
	    stV.set(j + 1, j + 1, stV.get(j, j) - stpsi[j] * stpsi[j] * var);
	    for (int k = 0; k < j; ++k)
		stV
			.set(j + 1, k + 1, stV.get(j, k) - stpsi[j] * stpsi[k]
				* var);
	}

	SymmetricMatrix.fromLower(stV);
    }

    double[] m_dif;

    double[] m_stacgf, m_stpsi;

    /**
     * 
     * @param arima
     */
    public SsfArima(final IArimaModel arima)
    {
	super(arima);
	initModel();
    }

    /**
     *
     * @param pm
     */
    @Override
    public void diffuseConstraints(final SubMatrix pm) {
	int d = m_dif.length - 1;
	if (d == 0)
	    return;
	B0(pm, m_dif);
    }

    /**
     *
     * @return
     */
    @Override
    public int getNonStationaryDim() {
	return m_dif.length - 1;
    }

    /**
     *
     */
    @Override
    protected void initModel() {
	double var = m_model.getInnovationVariance();
	if (var == 0)
	    throw new SsfException(SsfException.STOCH);
	// BFilter ur = new BFilter(0);
	// IArimaModel stmodel = m_model.DoStationary(ur);
	BackFilter ur = m_model.getNonStationaryAR();
	m_dif = ur.getCoefficients();
        Polynomial phi = m_model.getAR().getPolynomial();
	m_phi = phi.getCoefficients();
	m_Phi = new DataBlock(m_phi, 1, m_phi.length, 1);
	Polynomial theta = m_model.getMA().getPolynomial();
	m_dim = Math.max(phi.getDegree(), theta.getDegree() + 1);
	m_psi = new RationalFunction(theta, phi).coefficients(m_dim);

	Polynomial stphi = m_model.getStationaryAR().getPolynomial();
	m_stacgf = new AutoCovarianceFunction(theta, stphi, var).values(m_dim);
	m_stpsi = new RationalFunction(theta, stphi).coefficients(m_dim);

	m_tmp = new double[m_dim];
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isDiffuse() {
	return m_dif.length > 1;
    }

    /**
     *
     * @param pm
     */
    @Override
    public void Pf0(final SubMatrix pm) {
	Matrix stV = new Matrix(m_dim, m_dim);
	stVar(stV.subMatrix(), m_stpsi, m_stacgf, m_model
		.getInnovationVariance());
	Matrix K = new Matrix(m_dim, m_dim);
	Ksi(K.subMatrix(), m_dif);
	SymmetricMatrix.quadraticFormT(stV.subMatrix(), K.subMatrix(), pm);
    }

    /**
     *
     * @param pm
     */
    @Override
    public void Pi0(final SubMatrix pm) {
	Matrix B = new Matrix(m_dim, m_dif.length - 1);
	B0(B.subMatrix(), m_dif);
	SymmetricMatrix.XXt(B.subMatrix(), pm);
    }
}
