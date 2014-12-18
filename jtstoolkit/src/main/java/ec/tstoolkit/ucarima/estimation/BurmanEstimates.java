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
package ec.tstoolkit.ucarima.estimation;

import ec.tstoolkit.ucarima.WienerKolmogorovEstimators;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.arima.estimation.FastArimaForecasts;
import ec.tstoolkit.arima.estimation.FastArimaML;
import ec.tstoolkit.arima.estimation.SemiInfiniteSampleForecast;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;
import ec.tstoolkit.maths.matrices.Householder;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.ucarima.UcarimaModel;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Temporary)
public class BurmanEstimates
{

    private double[] m_data;

    private int m_nf;

    private WienerKolmogorovEstimators m_wk;

    private Polynomial m_ar, m_ma;

    private Polynomial[] m_g;

    private double m_ser;

    private int m_nparams;

    // private int m_p, m_q;, m_r;
    private double[][] m_e, m_f;

    private double[] m_xb, m_xf, m_res, m_bres;

    private double m_fwm, m_bwm;

    private boolean m_bmean;

    private double m_meancorrection;

    /** Creates a new instance of WKEstimators */
    public BurmanEstimates() {
    }

    /**
     * 
     * @param cmp
     */
    protected void calc(final int cmp)
    {
	if (m_e[cmp] != null || m_data == null) {
	    return;
	}
	if (m_g[cmp] == null) {
	    if (cmp == 0 && m_bmean) {
		m_e[cmp] = new double[m_data.length];
		for (int i = 0; i < m_data.length; ++i) {
		    m_e[cmp][i] = m_meancorrection;
		}
		m_f[cmp] = new double[m_nf];
		for (int i = 0; i < m_nf; ++i) {
		    m_f[cmp][i] = m_meancorrection;
		}
	    }
	    return;
	}
	extendSeries();
	int n = m_data.length, nf = m_xf.length;
	int nr = m_g[cmp].getDegree();
	Polynomial ma = m_ma;
	Polynomial ar = m_ar;
	int qstar = ma.getDegree();
	int pstar = ar.getDegree();

	double[] z = new double[n + 2 * nf];
	for (int i = 0; i < n; ++i) {
	    z[i + nf] = m_data[i];
	}
	int ntmp = nf + n;
	for (int i = 0; i < nf; ++i) {
	    z[ntmp + i] = m_xf[i];
	}
	ntmp = nf - 1;
	for (int i = 0; i < nf; ++i) {
	    z[ntmp - i] = m_xb[i];
	}
	if (m_meancorrection != 0) {
	    for (int i = 0; i < z.length; ++i) {
		z[i] -= m_meancorrection;

		//
		// z goes from -nf to n+nf with nf=q+ max(p, q) !! z[0] == z at
		// time
		// -nf;

		// calculation of x1

		// calculation of w=g*data, for 0<=t<n+q , elements nf to nf+n+q
		// of data
	    }
	}
	double[] w = new double[n + qstar];
	Polynomial g = m_g[cmp];
	for (int i = 0; i < n + qstar; ++i) {
	    double s = g.get(0) * z[nf + i];
	    for (int j = 1; j <= nr; ++j) {
		s += g.get(j) * z[nf + i + j];
	    }
	    w[i] = s;
	}

	Matrix m = new Matrix(pstar + qstar, pstar + qstar);
	double[] ww = new double[pstar + qstar];
	ntmp = n + qstar - pstar;
	for (int i = 0; i < pstar; ++i) {
	    ww[i] = w[ntmp + i];
	    for (int j = 0; j <= qstar; ++j) {
		m.set(i, i + j, ma.get(j));
	    }
	}
	for (int i = 0; i < qstar; ++i) {
	    if (m_bmean && cmp == 0 && m_meancorrection == 0) {
		ww[i + pstar] = m_fwm / 2;
	    }
	    for (int j = 0; j <= pstar; ++j) {
		m.set(i + pstar, i + j, ar.get(pstar - j));
	    }
	}

	Householder qr = new Householder(false);
	qr.setEpsilon(1e-30);
	qr.decompose(m);
	double[] mx = qr.solve(ww);

	int nx1 = n + Math.max(2 * qstar, nf);
	double[] x1 = new double[nx1];
	ntmp = n + qstar - pstar;
	for (int i = 0; i < pstar + qstar; ++i) {
	    x1[ntmp + i] = mx[i];
	    // backward iteration
	}
	for (int i = ntmp - 1; i >= 0; --i) {
	    double s = w[i];
	    for (int j = 1; j <= qstar; ++j) {
		s -= x1[i + j] * ma.get(j);
	    }
	    x1[i] = s;
	}

	// forward iteration
	for (int i = ntmp + pstar + qstar; i < nx1; ++i) {
	    double s = 0;
	    for (int j = 1; j <= pstar; ++j) {
		s -= ar.get(j) * x1[i - j];
	    }
	    x1[i] = s;
	}

	// calculation of x2

	// calculation of w2=g*data, for -q<=t<n+nf , elements -q to n+nf of
	// data w: elt t=0 at place q
	double[] w2 = new double[n + nf + qstar];
	for (int i = 0; i < n + nf + qstar; ++i) {
	    double s = g.get(0) * z[nf - qstar + i];
	    for (int j = 1; j <= nr; ++j) {
		s += g.get(j) * z[nf - qstar + i - j];
	    }
	    w2[i] = s;
	}

	for (int i = 0; i < pstar; ++i) {
	    ww[i] = w2[pstar - i - 1];
	}
	if (m_bmean && cmp == 0 && m_meancorrection == 0) {
	    for (int i = pstar; i < ww.length; ++i) {
		ww[i] = m_bwm / 2;
	    }
	}
	mx = qr.solve(ww);

	int nx2 = n + 2 * qstar + Math.max(nf, 2 * qstar);
	double[] x2 = new double[nx2];
	for (int i = 0; i < pstar + qstar; ++i) {
	    x2[pstar + qstar - 1 - i] = mx[i];

	    // iteration w2 start in -q, x2 in -2*q delta q
	}
	for (int i = pstar + qstar; i < nx2; ++i) {
	    double s = w2[i - qstar];
	    for (int j = 1; j <= qstar; ++j) {
		s -= x2[i - j] * ma.get(j);
	    }
	    x2[i] = s;
	}

	double[] rslt = new double[n];
	for (int i = 0; i < n; ++i) {
	    rslt[i] = (x1[i] + x2[i + 2 * qstar]);
	}
	m_e[cmp] = rslt;

	if (m_nf > 0) {
	    double[] fcast = new double[m_nf];

	    for (int i = 0; i < m_nf; ++i) {
		fcast[i] = (x1[n + i] + x2[n + i + 2 * qstar]);
	    }
	    m_f[cmp] = fcast;
	}

	if (m_meancorrection != 0 && cmp == 0 && m_bmean) {
	    for (int i = 0; i < m_e[cmp].length; ++i) {
		m_e[cmp][i] += m_meancorrection;
	    }
	    if (m_nf > 0) {
		for (int i = 0; i < m_f[cmp].length; ++i) {
		    m_f[cmp][i] += m_meancorrection;
		}
	    }
	}
    }

    /**
     * 
     */
    protected void clearForecasts()
    {
	m_xb = null;
	m_xf = null;
	if (m_f != null) {
	    for (int i = 0; i < m_f.length; ++i) {
		m_f[i] = null;
	    }
	}
    }

    /**
     * 
     */
    protected void clearResults()
    {
	m_bres = null;
	m_res = null;
	m_fwm = 0;
	m_bwm = 0;
	m_meancorrection = 0;
	m_ser = 0;
	if (m_e != null) {
	    for (int i = 0; i < m_e.length; ++i) {
		m_e[i] = null;
	    }
	}
    }

    /**
     * 
     * @param cmp
     * @param signal
     * @return
     */
    public double[] estimates(final int cmp, final boolean signal)
    {
	calc(cmp);
	if (signal) {
	    return m_e[cmp];
	} else {
	    double[] e = new double[m_data.length];
	    for (int i = 0; i < e.length; ++i) {
		e[i] = m_data[i] - m_e[cmp][i];
	    }
	    return e;
	}
    }

    private void extendSeries() {
        if (m_xb != null) {
            return;
        }
        int nf = 0;
        for (int i = 0; i < m_g.length; ++i) {
            int nr = 0;
            if (m_g[i] != null) {
                nr = m_g[i].getDegree();
            }
            if (m_ma != null) {
                nr += m_ma.getDegree();
            }
            if (m_bmean) {
                nr += 2;
            }
            if (nr > nf) {
                nf = nr;
            }
        }

        if (m_nf > nf) {
            nf = m_nf;
        }

        FastArimaForecasts fcast = new FastArimaForecasts(m_wk.getUcarimaModel().getModel(), m_bmean);
        m_xf = fcast.forecasts(new DataBlock(m_data), nf);
        m_fwm=fcast.getMean();
        m_xb = fcast.forecasts(new DataBlock(m_data).reverse(), nf);
        m_bwm=fcast.getMean();
        	// correction for the "augmented" AR filter (cfr HP-Arima)
	m_meancorrection = 0;
	if (m_bmean) {
	    BackFilter ur = m_wk.getUcarimaModel().getComponent(0).getNonStationaryAR();
	    BackFilter ar = m_wk.getUcarimaModel().getComponent(0).getStationaryAR();
	    BackFilter mar = m_wk.getUcarimaModel().getModel().getStationaryAR();
	    // correction when the meancorrectedcmp doesn't contain unit
	    // roots...
	    if (Math.abs(ur.getPolynomial().evaluateAt(1)) > Polynomial.getEpsilon()) {
		// no unit root
		for (int i = 0; i < m_data.length; ++i) {
		    m_meancorrection += m_data[i];
		}
		m_meancorrection /= m_data.length;
		m_bwm = 0;
		m_fwm = 0;
	    } else {
		BackFilter.SimplifyingTool smp = new BackFilter.SimplifyingTool(true);
		if (smp.simplify(ar, mar) && smp.getLeft().getLength() > 1) {
		    double z = smp.getLeft().getPolynomial().evaluateAt(1);
		    m_bwm *= z;
		    m_fwm *= z;
		}
	    }

	}

    }      
        
    
    private void extendSeriesOld() {
	if (m_xb != null) {
	    return;
	}
	int nf = 0;
	for (int i = 0; i < m_g.length; ++i) {
	    int nr = m_ma.getDegree();
	    if (m_g[i] != null) {
		nr += m_g[i].getDegree();
	    }
	    if (nr > nf) {
		nf = nr;
	    }
	}

	if (m_nf > nf) {
	    nf = m_nf;

	    // initialization and extension of z...
	}
	int n = m_data.length;
	double[] xb = new double[n];

	FastArimaML fml = new FastArimaML();
	SemiInfiniteSampleForecast fcast = new SemiInfiniteSampleForecast();

	IArimaModel model = m_wk.getUcarimaModel().getModel();

	fml.setModel(model);
	fml.setMeanCorrection(m_bmean);
	fcast.setModel(model);
	fcast.setForecastHorizon(nf);

	fml.process(new DataBlock(m_data));
	m_ser = fml.ser(m_nparams);
	m_res = fml.getResiduals();
	m_fwm = fml.getDMean();
	fcast.process(m_data, m_res, fml.getDMean(), fml.ser(m_nparams));

	m_xf = fcast.getForecasts();

	int ntmp = n - 1;
	for (int i = 0; i <= ntmp; ++i) {
	    xb[ntmp - i] = m_data[i];
	}
	fml.process(new DataBlock(xb));
	m_bres = fml.getResiduals();
	m_bwm = fml.getDMean();
	fcast.process(xb, m_bres, fml.getDMean(), fml.ser(m_nparams));
	m_xb = fcast.getForecasts();

	// correction for the "augmented" AR filter (cfr HP-Arima)
	m_meancorrection = 0;
	if (m_bmean) {
	    BackFilter ar = m_wk.getUcarimaModel().getComponent(0).getAR();
	    BackFilter mar = model.getAR();
	    // correction when the meancorrectedcmp doesn't contain unit
	    // roots...
	    if (Math.abs(ar.getPolynomial().evaluateAt(1)) > Polynomial.getEpsilon()) {
		// no unit root
		for (int i = 0; i < m_data.length; ++i) {
		    m_meancorrection += m_data[i];
		}
		m_meancorrection /= m_data.length;
		m_bwm = 0;
		m_fwm = 0;
	    } else {

		BackFilter.SimplifyingTool smp = new BackFilter.SimplifyingTool(true);
		if (smp.simplify(ar, mar) && smp.getLeft().getLength() > 1) {
		    // URBFilter urb;
		    // ar=ar.DoStationaryTransformation(out urb);
		    double z = smp.getLeft().getPolynomial().evaluateAt(1);
		    m_bwm *= z;
		    m_fwm *= z;

		    // Polynomial par=urb.UnitRoots.ToPolynomial();
		    // z=par.EvaluateAt(.99);
		    // m_bwm*=z;
		    // m_fwm*=z;
		}
	    }

	}

    }

    /**
     * 
     * @param cmp
     * @param signal
     * @return
     */
    public double[] forecasts(final int cmp, final boolean signal)
    {
	calc(cmp);
	if (signal) {
	    return m_f[cmp];
	} else {
	    double[] f = new double[m_nf];
	    for (int i = 0; i < f.length; ++i) {
		f[i] = m_xf[i] - m_f[cmp][i];
	    }
	    return f;
	}
    }

    /**
     * 
     * @return
     */
    public double[] getBackResiduals()
    {
	return m_bres;
    }

    /**
     * 
     * @return
     */
    public double[] getData()
    {
	return m_data;
    }

    /**
     * 
     * @return
     */
    public WienerKolmogorovEstimators getEstimators()
    {
	return m_wk;
    }

    /**
     * 
     * @return
     */
    public int getForecastsCount()
    {
	return m_nf;
    }

    /**
     * 
     * @return
     */
    public int getHyperParametersCount()
    {
	return m_nparams;
    }

    /**
     * 
     * @return
     */
    public double getMean()
    {
	return m_meancorrection;
    }

    /**
     * 
     * @return
     */
    public double[] getResiduals()
    {
	return m_res;
    }

    /**
     * 
     * @return
     */
    public double getSer()
    {
	return m_ser;
    }

    /**
     * 
     * @return
     */
    public double[] getBackcasts()
    {
	extendSeries();
	return m_xb;
    }

    /**
     * 
     * @return
     */
    public double[] getForecasts()
    {
	extendSeries();
	return m_xf;
    }

    /**
     * 
     * @return
     */
    public UcarimaModel getUcarimaModel()
    {
	return m_wk.getUcarimaModel();
    }

    private void initModel() {
	UcarimaModel ucm = m_wk.getUcarimaModel();
	// cfr burman-wilson algorithm
	IArimaModel model = ucm.getModel();
	int ncmps = ucm.getComponentsCount();
	m_e = new double[ncmps][];
	m_f = new double[ncmps][];
	m_g = new Polynomial[ncmps];

	m_ma = model.getMA().getPolynomial();
	m_ar = model.getAR().getPolynomial();

	for (int i = 0; i < ncmps; ++i) {
	    ArimaModel cmp = ucm.getComponent(i);
	    if (!cmp.isNull()) {
		BackFilter umar = model.getNonStationaryAR(), ucar = cmp
			.getNonStationaryAR();
		BackFilter nar = umar.divide(ucar);
		BackFilter smar = model.getStationaryAR(), scar = cmp
			.getStationaryAR();
		BackFilter.SimplifyingTool smp = new BackFilter.SimplifyingTool(false);
		if (smp.simplify(smar, scar)) {
		    smar = smp.getLeft();
		    scar = smp.getRight();
		}

		BackFilter dar = scar;
		nar = nar.times(smar);

		BackFilter denom = new BackFilter(m_ma).times(dar);
		SymmetricFilter c = cmp.sma().times(
			SymmetricFilter.createFromFilter(nar));
		double mvar = model.getInnovationVariance();
		if (mvar != 1)
		    c = c.times(1 / mvar);

		BackFilter gf = c.decompose(denom);
		m_g[i] = gf.getPolynomial();

	    }
	}
	/*
	 * if (m_bmean) {
	 * m_ma=Polynomial.promote(m_ma).times(UnitRoots.D(1)).getCoefficients
	 * ();
	 * m_ar=Polynomial.promote(m_ar).times(UnitRoots.D(1)).getCoefficients
	 * (); }
	 */
    }

    /**
     * 
     * @return
     */
    public boolean isMeanCorrection()
    {
	return m_bmean;
    }

    /**
     * 
     * @param value
     */
    public void setData(final double[] value)
    {
	m_data = value.clone();
	clearResults();
	clearForecasts();
    }

    /**
     * 
     * @param data
     */
    public void setData(final IReadDataBlock data)
    {
	m_data = new double[data.getLength()];
	data.copyTo(m_data, 0);
	clearResults();
	clearForecasts();
    }

    /**
     * 
     * @param value
     */
    public void setEstimators(final WienerKolmogorovEstimators value)
    {
	m_wk = value;
	initModel();
	clearResults();
	clearForecasts();
    }

    /**
     * 
     * @param value
     */
    public void setForecastsCount(final int value)
    {
	m_nf = value;
	clearForecasts();
    }

    /**
     * 
     * @param value
     */
    public void setHyperParametersCount(int value)
    {
	m_nparams = value;
    }

    /**
     * 
     * @param value
     */
    public void setMeanCorrection(final boolean value)
    {
	if (m_bmean != value) {
	    m_bmean = value;
	    clearResults();
	}
    }

    /**
     * 
     * @param value
     */
    public void setUcarimaModel(final UcarimaModel value)
    {
	m_wk=new WienerKolmogorovEstimators(value);
	initModel();
	clearResults();
	clearForecasts();
    }

    /**
     * 
     * @param cmp
     * @return
     */
    public double[] stdevEstimates(final int cmp)
    {
	calc(cmp);
	if (m_wk.getUcarimaModel().getComponent(cmp).isNull()) {
	    return new double[m_data.length];
	} else {
	    int n = (m_data.length + 1) / 2;
	    double[] err = m_wk.totalErrorVariance(cmp, true, 0, n);
	    double[] e = new double[m_data.length];
	    for (int i = 0; i < err.length; ++i) {
		double x = m_ser * Math.sqrt(err[i]);
		e[i] = x;
		e[e.length - i - 1] = x;
	    }
	    return e;
	}
    }

    /**
     * 
     * @param cmp
     * @param signal
     * @return
     */
    public double[] stdevForecasts(final int cmp, final boolean signal)
    {
	calc(cmp);
	if (m_wk.getUcarimaModel().getComponent(cmp).isNull()) {
	    if (signal) {
		return new double[m_nf];
	    } else {
		return null;
	    }
	}

	double[] e = m_wk.totalErrorVariance(cmp, signal, -m_nf, m_nf);
	double[] err = new double[m_nf];
	for (int i = 0; i < m_nf; ++i) {
	    err[i] = m_ser * Math.sqrt(e[m_nf - 1 - i]);
	}
	return err;
    }

    public void setSer(double ser) {
        m_ser=ser;
    }
}