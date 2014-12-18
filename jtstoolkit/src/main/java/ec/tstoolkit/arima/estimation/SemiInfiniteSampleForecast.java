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
package ec.tstoolkit.arima.estimation;

import ec.tstoolkit.arima.*;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.polynomials.Polynomial;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class SemiInfiniteSampleForecast {

    private double m_mean, m_stdev = 1;

    private double[] m_z, m_res;

    private IArimaModel m_model;

    private int m_nf;

    private double[] m_f, m_ef;

    /**
	 *
	 */
    public SemiInfiniteSampleForecast() {
    }

    private boolean calc() {
	if (m_f != null || m_model == null || m_nf == 0)
	    return false;
	Polynomial phi = m_model.getAR().getPolynomial();
	int p = phi.getDegree();
	Polynomial theta = m_model.getMA().getPolynomial();
	m_f = new double[m_nf];
	// 
	for (int i = 0; i < m_nf; ++i) {
	    double z = m_mean;
	    for (int j = i + 1, k = m_res.length - 1; j <= theta.getDegree(); ++j, --k)
		z += theta.get(j) * m_res[k];
	    // first use previous forecast
	    int jmax = i > p ? p : i;
	    for (int j = 1; j <= jmax; ++j)
		z -= m_f[i - j] * phi.get(j);
	    // and then last values of z
	    for (int j = jmax + 1, k = m_z.length - 1; j <= p; ++j, --k)
		z -= m_z[k] * phi.get(j);
	    m_f[i] = z;
	}

	double[] psi = m_model.getPsiWeights().getWeights(m_nf);
	m_ef = new double[m_nf];
	m_ef[0] = m_stdev;
	double var = 1;
	for (int i = 1; i < m_nf; ++i) {
	    var += psi[i] * psi[i];
	    m_ef[i] = Math.sqrt(var) * m_stdev;
	}

	return true;
    }

    private void clearresults() {
	m_f = null;
	m_ef = null;
    }

    /**
     * 
     * @return
     */
    public int getForecastHorizon() {
	return m_nf;
    }

    /**
     * 
     * @return
     */
    public double[] getForecasts() {
	return m_f;
    }

    /**
     * 
     * @return
     */
    public double[] getForecastsStdev() {
	return m_ef;
    }

    /**
     * 
     * @return
     */
    public double getMeanCorrection() {
	return m_mean;
    }

    /**
     * 
     * @return
     */
    public IArimaModel getModel() {
	return m_model;
    }

    /**
     * 
     * @param z
     * @param res
     * @param mean
     * @param stdev
     * @return
     */
    public boolean process(final double[] z, final double[] res,
	    final double mean, final double stdev) {
	// takes only the needed values. The model must be initialized...
	int n = res.length;
	if (z.length < n)
	    n = z.length;

	m_z = new double[n];
	m_res = new double[n];
	System.arraycopy(z, z.length - n, m_z, 0, n);
	System.arraycopy(res, res.length - n, m_res, 0, n);
	m_mean = mean;
	m_stdev = stdev;

	clearresults();
	return calc();
    }

    /**
     * 
     * @param value
     */
    public void setForecastHorizon(final int value) {
	m_nf = value;
	clearresults();
    }

    /**
     * 
     * @param value
     */
    public void setMeanCorrection(final double value)
    {
	m_mean = value;
	clearresults();
    }

    /**
     * 
     * @param value
     */
    public void setModel(final IArimaModel value) {
	m_model = value;
	clearresults();
    }

}
