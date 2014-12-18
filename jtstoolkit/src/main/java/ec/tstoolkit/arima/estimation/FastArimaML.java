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

import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.arima.StationaryTransformation;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.matrices.Householder;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.MatrixException;
import ec.tstoolkit.maths.polynomials.Polynomial;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class FastArimaML{

    private IArimaModel m_model, m_stmodel;

    private BackFilter m_ur;

    private int m_ndata;

    private double[] m_res;

    private boolean m_bmean;

    private double m_dmean, m_detn, m_s2;

    /**
     *
     */
    public FastArimaML() {
    }

    private void clearresults() {
	m_res = null;
	m_detn = 0;
	m_dmean = 0;
	m_ndata = 0;
    }

    private void correctmean() {
	if (m_dmean == 0)
	    return;
	Polynomial p = m_stmodel.getAR().getPolynomial();
	double c = 1;
	for (int i = 1; i <= p.getDegree(); ++i)
	    c += p.get(i);
	m_dmean *= c;
    }

    /**
     * 
     * @param np
     * @return
     */
    public int degreesofFreedom(int np)
    {
	int df = m_ndata - m_model.getARCount();
	if (m_bmean)
	    --df;
	return df - np;
    }

    /**
     * 
     * @return
     */
    public double getDeterminantalFactor() {
	return m_detn;
    }

    /**
     * 
     * @return
     */
    public double getDMean() {
	return m_dmean;
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
     * @return
     */
    public double getObjective() {
	return m_s2 * m_detn;
    }

    /**
     * 
     * @return
     */
    public double[] getResiduals() {
	return m_res;
    }

    /**
     * 
     * @return
     */
    public double getSsqErr()
    {
	return m_s2;
    }

    /**
     * 
     * @return
     */
    public boolean isMeanCorrection() {
	return m_bmean;
    }

    /**
     * 
     * @param data
     * @return
     */
    public boolean process(DataBlock data) {
	if (m_model == null)
	    return false;
	try {
	    clearresults();
	    m_ndata = data.getLength();
	    DataBlock dy = new DataBlock(m_ndata - m_ur.getDegree());
	    m_ur.filter(data, dy);
	    m_dmean = 0;
	    if (m_bmean)
		m_dmean = dy.sum() / dy.getLength();
	    correctmean();
	    // fcast.Process(ts.Values.Data, tramo.Estimation.Residuals, dmean,
	    // tramo.Estimation.SER);
	    // double[] f=fcast.Forecasts, ef=fcast.ForecastsStdev;
	    BackFilter phi = m_model.getAR();

	    // Seats : Conditional ML

	    int p = phi.getDegree();
	    double[] u = new double[m_ndata - p];
	    DataBlock ru = new DataBlock(u);
	    phi.filter(data, ru);
	    if (m_dmean != 0)
		ru.sub(m_dmean);

	    // compute residuals
	    Polynomial theta = m_model.getMA().getPolynomial();
	    int q = theta.getDegree();

	    int n = m_ndata - p + q;
	    double[] a = new double[n];

	    for (int i = q; i < n; ++i) {
		double s = u[i - q];
		for (int j = 1; j <= q; ++j)
		    if (theta.get(j) != 0)
			s -= theta.get(j) * a[i - j];
		a[i] = s;
	    }

	    double[] v = new double[n - q];
	    System.arraycopy(a, q, v, 0, v.length);

	    Matrix k = new Matrix(n, q);

	    // construct the K matrix. Unoptimized code
	    for (int i = 0; i < q; ++i) {
		k.set(i, i, 1);

		for (int j = q; j < n; ++j) {
		    double s = 0;
		    for (int l = 1; l <= theta.getDegree(); ++l)
			s -= k.get(j - l, i) * theta.get(l);
		    k.set(j, i, s);
		}
	    }

	    // k.Clean(qr.Epsilon);
	    Householder qr = new Householder(false);
	    qr.decompose(k);
	    double[] q0 = qr.solve(a);

	    for (int i = 0; i < q; ++i)
		a[i] = -q0[i];

	    for (int i = q; i < n; ++i) {
		double sum = u[i - q];
		for (int j = 1; j <= q; ++j)
		    if (theta.get(j) != 0)
			sum -= theta.get(j) * a[i - j];
		a[i] = sum;
	    }

	    double det = 1;
	    DataBlock rdiag = qr.getRDiagonal();
	    for (int i = 0; i < rdiag.getLength(); ++i)
		det *= rdiag.get(i) * rdiag.get(i);

	    m_s2 = 0;
	    for (int i = 0; i < a.length; ++i)
		m_s2 += a[i] * a[i];

	    m_detn = Math.pow(det, .5 / n);
	    m_res = a;
	    return true;
	} catch (MatrixException ex) {
	    clearresults();
	    return false;
	}
    }

    /**
     * 
     * @param np
     * @return
     */
    public double ser(int np)
    {
	return Math.sqrt(m_s2 / degreesofFreedom(np));
    }

    /**
     * 
     * @param value
     */
    public void setMeanCorrection(boolean value) {
	if (m_bmean != value) {
	    clearresults();
	    m_bmean = value;
	}
    }

    /**
     * 
     * @param value
     */
    public void setModel(IArimaModel value) {
	if (value != m_model) {
	    m_model = value;
	    StationaryTransformation st = m_model.stationaryTransformation();
	    m_ur = st.unitRoots;
	    m_stmodel = (IArimaModel) st.stationaryModel;
	    clearresults();
	}
    }
}
