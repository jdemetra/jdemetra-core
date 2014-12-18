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

package ec.tstoolkit.modelling.arima;

import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.arima.estimation.ConcentratedLikelihoodEstimation;
import ec.tstoolkit.arima.estimation.KalmanFilter;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.regression.AbstractOutlierVariable;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;

/**
 * 
 * @param <T>
 * @author Jean Palate
 */
@Development(status = Development.Status.Exploratory)
public class ApproximateSingleOutlierDetector<T extends IArimaModel> extends
	AbstractSingleOutlierDetector<T> {

    private double[] m_el;

    private double m_ss;

    private double m_c, m_tmax;

    private boolean m_bmad = true;

    /**
     * 
     */
    public ApproximateSingleOutlierDetector()
    {
    }

    /**
     *
     * @return
     */
    @Override
    protected boolean calc() {
	if (!initmodel())
	    return false;
	m_c = 0;
	m_tmax = 0;
	for (int i = 0; i < getOutlierFactoriesCount(); ++i)
	    processOutlier(i);

	return true;
    }

    /**
     *
     */
    @Override
    protected void clear() {
	super.clear();
	m_el = null;
	m_c = 0;
	m_tmax = 0;
    }

    /**
     * 
     * @return
     */
    public double getMaxCoefficient()
    {
	calc();
	return m_c;
    }

    private boolean initmodel() {
	ConcentratedLikelihoodEstimation estimation = new ConcentratedLikelihoodEstimation(
		new KalmanFilter(true));
	if (!estimation.estimate(getModel()))
	    return false;
	m_el = estimation.getResiduals();
	DataBlock EL = new DataBlock(m_el);
	setMAD(AbstractOutlierVariable.mad(EL, true));
	m_ss = EL.ssq();
	// alternative implementation
	/*
	 * ConcentratedLikelihoodEstimation estimation = new
	 * ConcentratedLikelihoodEstimation(new KalmanFilter(true)); if
	 * (!estimation.estimate(getModel())) return false; DataBlock res =
	 * null; if (getModel().getVarsCount() > 0) res =
	 * getModel().getDModel().calcRes(new
	 * DataBlock(estimation.getLikelihood().getB())); else res =
	 * getModel().getDModel().getY(); int n = res.getLength(); double[] tmp
	 * = new double[n]; DataBlock T = new DataBlock(tmp); // OL = PHI(B)*O
	 * getModel().getArima().getStationaryAR().filter(res, T); // TH(B) O =
	 * OL; getModel().getArima().getMA().solve(T.getData(), res.getData());
	 * m_el = res.getData(); setMAD(AbstractOutlierVariable.mad(res, true));
	 * m_ss = res.ssq();
	 */
	return true;
    }

    /**
     * 
     * @return
     */
    public boolean isUsingMAD()
    {
	return m_bmad;
    }

    private void processOutlier(int idx) {
	int nl = m_el.length;
	int n = getModel().getY().getLength();
	int d = getModel().getDifferencingFilter().getDegree();
	int lb = this.getLBound(), ub = this.getUBound() - 1;
	double[] o = new double[n];
	double[] ol = new double[o.length];
	DataBlock O = new DataBlock(o);
	DataBlock OL = new DataBlock(ol);
	IOutlierVariable outlier = getOutlierFactory(idx).create(
		getDomain().getStart());
	outlier.data(getDomain().getStart(), O);
	// OL = PHI(B)*O
	getModel().getArima().getAR().filter(O, OL);
	// TH(B) O = OL;
	getModel().getArima().getMA().solve(ol, o);

	// o contains the filtered outlier
	// we start at the end

	double sxx = 0;

	for (int ix = lb; ix <= ub; ++ix) {
	    double rmse = rmse(n - ix - 1 - d);
	    sxx += o[ix] * o[ix];
	    int kmax = ix + 1;
	    if (kmax > nl) {
		kmax = nl;
		sxx -= o[ix - nl] * o[ix - nl];
	    }
	    if (!this.isDefined(ub - ix, idx))
		continue;
	    double sxy = 0;
	    for (int k = 0, ek = nl - 1; k < kmax; ++k, --ek)
		sxy += m_el[ek] * o[ix - k];
	    double c = sxy / sxx;
	    double val = c * Math.sqrt(sxx) / rmse;
	    double aval = Math.abs(val);
	    if (aval > m_tmax) {
		m_tmax = aval;
		m_c = c;
	    }
	    this.setT(ub - ix, idx, val);
	}
    }

    /**
     * 
     * @param i
     * @return
     */
    protected double rmse(int i)
    {
	if (m_bmad)
	    return getMAD();
	else if (i >= 0) {
	    double ss = (m_ss - m_el[i] * m_el[i]) / (m_el.length - 1);
	    return Math.sqrt(ss);
	} else
	    return Math.sqrt(m_ss / (m_el.length - 1));

    }

    /**
     * 
     * @param value
     */
    public void useMAD(boolean value)
    {
	m_bmad = value;
    }
}
