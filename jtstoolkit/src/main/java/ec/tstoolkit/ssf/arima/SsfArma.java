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
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.polynomials.RationalFunction;
import ec.tstoolkit.ssf.FastArrayState;
import ec.tstoolkit.ssf.FastState;
import ec.tstoolkit.ssf.IFastArrayFilteringResults;
import ec.tstoolkit.ssf.IFastArrayInitializer;
import ec.tstoolkit.ssf.IFastFilteringResults;
import ec.tstoolkit.ssf.IFastInitializer;
import ec.tstoolkit.ssf.ISsfData;
import ec.tstoolkit.ssf.SsfException;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public final class SsfArma extends SsfBaseArima {

    /**
     *
     */
    public static class FastInitializer implements IFastInitializer<SsfArma>,
	    IFastArrayInitializer<SsfArma> {

        /**
         * 
         */
        public FastInitializer()
        {
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
        public int initialize(final SsfArma ssf, final ISsfData data,
		final FastArrayState state,
		final IFastArrayFilteringResults rslts) {
	    double r0 = Math.sqrt(ssf.m_acgf[0]);
	    DataBlock k0 = new DataBlock(ssf.m_acgf.clone());
	    ssf.TX(k0);
	    double[] a = data.getInitialState();
	    if (a != null)
		state.A.copyFrom(a, 0);
	    state.r = r0;
	    k0.mul(1 / r0);
	    state.K.copy(k0);
	    state.L.copy(k0);
	    return 0;
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
        public int initialize(final SsfArma ssf, final ISsfData data,
		final FastState state, final IFastFilteringResults rslts) {
	    double v0 = ssf.m_acgf[0];
	    DataBlock k0 = new DataBlock(ssf.m_acgf.clone());
	    ssf.TX(k0);
	    double[] a = data.getInitialState();
	    if (a != null)
		state.A.copyFrom(a, 0);
	    state.f = v0;
	    state.C.copy(k0);
	    state.L.copy(k0);
	    return 0;
	}
    }

    double[] m_acgf;

    /**
     * 
     */
    public SsfArma()
    {
    }

    /**
     * 
     * @param arima
     */
    public SsfArma(final IArimaModel arima)
    {
	m_model = arima;
	initModel();
    }

    /**
     *
     * @param pm
     */
    @Override
    public void diffuseConstraints(final SubMatrix pm) {
    }

    /**
     *
     * @return
     */
    @Override
    public int getNonStationaryDim() {
	return 0;
    }

    /**
     *
     */
    @Override
    protected void initModel() {
	if (!m_model.isStationary())
	    throw new SsfException(SsfException.STATIONARY);
	double var = m_model.getInnovationVariance();
	if (var == 0)
	    throw new SsfException(SsfException.STOCH);
        Polynomial phi = m_model.getAR().getPolynomial();
	m_phi = phi.getCoefficients();
	m_Phi = new DataBlock(m_phi, 1, m_phi.length, 1);
	Polynomial theta = m_model.getMA().getPolynomial();
	m_dim = Math.max(phi.getDegree(), theta.getDegree() + 1);
	m_psi = new RationalFunction(theta, phi).coefficients(m_dim);
	m_acgf = new AutoCovarianceFunction(theta, phi, var).values(m_dim);
	m_tmp = new double[m_dim];
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isDiffuse() {
	return false;
    }

    /**
     *
     * @param pm
     */
    @Override
    public void Pf0(final SubMatrix pm) {
	for (int j = 0; j < m_dim; ++j)
	    pm.set(j, 0, m_acgf[j]);
	double v = m_model.getInnovationVariance();
	for (int j = 0; j < m_dim - 1; ++j) {
	    pm.set(j + 1, j + 1, pm.get(j, j) - m_psi[j] * m_psi[j] * v);
	    for (int k = 0; k < j; ++k)
		pm.set(j + 1, k + 1, pm.get(j, k) - m_psi[j] * m_psi[k] * v);
	}
	SymmetricMatrix.fromLower(pm);
    }

    /**
     *
     * @param pm
     */
    @Override
    public void Pi0(final SubMatrix pm) {
    }
}
