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

import ec.tstoolkit.arima.estimation.IRegArimaProcessor;
import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.eco.EcoException;
import ec.tstoolkit.eco.Ols;
import ec.tstoolkit.eco.RegModel;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.estimation.GlsSarimaMonitor;
import ec.tstoolkit.sarima.estimation.HannanRissanen;
import ec.tstoolkit.sarima.estimation.SarimaMapping;
import ec.tstoolkit.timeseries.regression.AdditiveOutlierFactory;
import ec.tstoolkit.timeseries.regression.IOutlierFactory;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.regression.LevelShiftFactory;
import ec.tstoolkit.timeseries.regression.SeasonalOutlierFactory;
import ec.tstoolkit.timeseries.regression.TransitoryChangeFactory;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Exploratory)
public class GeneralOutliersDetector 
{
    // / <summary>Computes the default critical value for the outliers detection
    // procedure</summary>
    // / <returns>The critical value</returns>
    // / <param name="n">The length of the series</param>
    /**
     * 
     * @param n
     * @return
     */
    public static double calcDefaultVA(int n)
    {
	double cv = 0;
	if (n < 50)
	    cv = 3;
	else if (n < 450)
	    cv = 3 + 0.0025 * (n - 50);
	else
	    cv = 4;
	return cv;
    }

    private AbstractSingleOutlierDetector<SarimaModel> m_sod;

    private RegArimaModel<SarimaModel> m_model;

    private double m_cv, m_pc = 0.12;

    private ArrayList<IOutlierVariable> m_o = new ArrayList<>();

    private int m_maxiter = 30;

    private int m_round;

    private IOutlierVariable m_lastremoved;

    private boolean m_mvx = true;

    private int m_n;

    /**
     * 
     */
    public GeneralOutliersDetector()
    {
	m_sod = new ExactSingleOutlierDetector<>(null);// TrenchSingleOutlierDetector();
    }

    /**
     * 
     * @param sod
     */
    public GeneralOutliersDetector(AbstractSingleOutlierDetector<SarimaModel> sod)
    {
	m_sod = sod;
    }

    private void addOutlier(IOutlierVariable o) {
	m_o.add(o);
	double[] xo = new double[m_n];
	DataBlock XO = new DataBlock(xo);
	o.data(m_sod.getDomain().getStart(), XO);
	m_model.addX(XO);
	m_sod.exclude(o);
    }

    /**
     * 
     * @param o
     */
    public void addOutlierFactory(IOutlierFactory o)
    {
	m_sod.addOutlierFactory(o);
    }

    /**
     * 
     */
    public void clear()
    {
	m_o.clear();
	m_lastremoved = null;
	m_model = null;
    }

    /**
     * 
     */
    public void clearOutlierFactories()
    {
	m_sod.clearOutlierFactories();
    }

    /**
     *
     * @return
     */
    public boolean continueProcessing() {
	double cv = Math.max(2.8, m_cv - m_cv * m_pc);
	if (cv == m_cv)
	    return false;
	m_cv = cv;
	return execute();
    }

    /**
     * 
     * @param cv
     * @return
     */
    public boolean continueProcessing(double cv)
    {
	m_cv = cv;
	return execute();
    }

    private IRegArimaProcessor<SarimaModel> estimator() {
	IRegArimaProcessor<SarimaModel> est = new GlsSarimaMonitor();
	est.setPrecision(1e-7);
	return est;
    }

    private boolean execute() {
	boolean changed = false;
	double max = 0;
	m_round = 0;

	initmodel();

	do {
	    max = m_sod.getMaxTStat();
	    if (Math.abs(max) > m_cv) {
		m_round++;
		IOutlierVariable o = m_sod.getMaxOutlier();
		addOutlier(o);
		reestimatemodel();
		changed = true;
		/*
		 * int v = verifymodel(m_cv); if (v == -1) break; else if (v ==
		 * 0) reestimatemodel();
		 */
		// updatesod();
	    } else
		break;// no outliers to remove...
	} while (m_round < m_maxiter);
	// if (m_round == m_maxiter)
	// throw new Nbb.Utilities.NbbException();

	while (verifymodel(m_cv) == 0)
	    reestimatemodel();

	return changed;
    }

    /**
     * 
     * @return
     */
    public double getCriticalValue()
    {
	return m_cv;
    }

    /**
     *
     * @return
     */
     public RegArimaModel<SarimaModel> getModel() {
	return m_model;
    }

    /**
     * 
     * @return
     */
    public int getOutlierFactoriesCount()
    {
	return m_sod.getOutlierFactoriesCount();
    }

    /**
     * 
     * @return
     */
    public int getOutliersCount()
    {
	return m_o.size();
    }

    /**
     * 
     * @return
     */
    public double getPctReduction()
    {
	return m_pc;
    }

    private void initmodel() {
	// first compute the residuals

	RegModel dmodel = m_model.getDModel();
	ConcentratedLikelihood ll = null;

	if (!m_mvx) {
	    DataBlock yc = dmodel.getY();
	    if (m_model.getVarsCount() > 0) {
		Ols ols = new Ols();
		if (ols.process(dmodel))
		    yc = dmodel.calcRes(new ReadDataBlock(ols.getLikelihood()
			    .getB()));
	    }

	    SarimaModel mb = null;
	    HannanRissanen hr = new HannanRissanen();
	    hr.process(yc, m_model.getArma().getSpecification().doStationary());
	    mb = hr.getModel();

	    boolean unstable = SarimaMapping.stabilize(mb);
	    if (!unstable) {
		m_model.getArima().setParameters(mb.getParameters());
		m_sod.process(m_model);
		ll = m_model.computeLikelihood();
	    }
	}
	if (ll == null) {
	    // IRegArimaProcessor<SArimaModel> est = m_model.getMissingsCount()
	    // > 0 ? new GlsSArimaMonitor() : new IGlsSArimaMonitor();
	    IRegArimaProcessor<SarimaModel> est = estimator();
	    RegArimaEstimation<SarimaModel> rslt = est.optimize(m_model);
	    if (rslt == null)
		throw new EcoException();
	    m_model = rslt.model;
	    ll = rslt.likelihood;
	    if (ll == null)
		throw new EcoException();
	    m_sod.process(m_model);
	}
    }

    /**
     * 
     * @return
     */
    public boolean isEML()
    {
	return m_mvx;
    }

    /**
     * 
     * @return
     */
    public boolean isInitialized()
    {
	return m_model != null;
    }

    /**
     *
     * @param i
     * @return
     */
    public IOutlierVariable outlier(int i) {
	return m_o.get(i);
    }

    /**
     *
     * @return
     */
    public List<IOutlierVariable> outliers() {
	return m_o;
    }

    /**
     * 
     * @param domain
     * @param edomain
     */
    public void prepare(TsDomain domain, TsDomain edomain)
    {
	m_sod.prepare(domain, edomain);
    }

    /**
     *
     * @param model
     * @return
     */
    public boolean process(RegArimaModel<SarimaModel> model) {
	clear();
	m_model = model.clone();
	m_sod.exclude(model.getMissings());
	m_n = m_model.getObsCount();
	if (m_cv == 0)
	    m_cv = calcDefaultVA(m_n);
	return execute();
    }

    // step I.1
    // / <summary>
    // / Initialize the model.
    // / 1. If m_irflag, compute the coefficients by OLS. That happens in the
    // first run (no previous estimation)
    // / or when the HR (if used) provides bad estimation (unstable models).
    // / 2. Calc by HR. Used in the first run or when a new outlier is added.
    // The model of the SOD is updated, unless
    // / exact ll estimation is used or HR provides an unstable model.
    // / 3. Calc by exact ll. (option or unstable HR (first round only)).
    // / 4. Gls estimation of the new model
    // / </summary>
    private void reestimatemodel() {
	// first compute the residuals

	if (m_mvx) {
	    // IRegArimaProcessor<SArimaModel> est = m_model.getMissingsCount()
	    // > 0 ? new GlsSArimaMonitor() : new IGlsSArimaMonitor();
	    IRegArimaProcessor<SarimaModel> est = estimator();
	    RegArimaEstimation<SarimaModel> rslt = est.optimize(m_model);
	    if (rslt == null)
		throw new EcoException();
	    m_model = rslt.model;
	    m_sod.process(m_model);
	} else {
	    RegModel dmodel = m_model.getDModel();
	    DataBlock yc = dmodel.getY();
	    if (m_model.getVarsCount() > 0)
		yc = dmodel
			.calcRes(new ReadDataBlock(m_model.computeLikelihood().getB()));

	    HannanRissanen hr = new HannanRissanen();
	    hr.process(yc, m_model.getArma().getSpecification().doStationary());
	    SarimaModel mb = hr.getModel();

	    if (!SarimaMapping.stabilize(mb)) {
		m_model.getArima().setParameters(mb.getParameters());
		m_sod.process(m_model);
	    }
	}
    }

    private void removeOutlier(int idx) {
	//
	int opos = m_model.getXCount() - m_o.size() + idx;
	m_model.removeX(opos);
	m_o.remove(idx);
    }

    /**
     * 
     */
    public void setAll()
    {
	clear();
	clearOutlierFactories();
	addOutlierFactory(new AdditiveOutlierFactory());
	LevelShiftFactory lfac = new LevelShiftFactory();
	lfac.setZeroEnded(true);
	addOutlierFactory(lfac);
	addOutlierFactory(new TransitoryChangeFactory());
	SeasonalOutlierFactory sfac = new SeasonalOutlierFactory();
	sfac.setZeroEnded(true);
	addOutlierFactory(sfac);
    }

    /**
     * 
     * @param value
     */
    public void setCriticalValue(double value)
    {
	m_cv = value;
    }

    /**
     * 
     */
    public void setDefault()
    {
	clear();
	clearOutlierFactories();
	addOutlierFactory(new AdditiveOutlierFactory());
	addOutlierFactory(new LevelShiftFactory());
	addOutlierFactory(new TransitoryChangeFactory());
	m_cv = 0;
    }

    /**
     * 
     * @param value
     */
    public void setPctReduction(double value)
    {
	m_pc = value;
    }

    /**
     * 
     * @param value
     */
    public void useEML(boolean value)
    {
	m_mvx = value;
    }

    private int verifymodel(double cv) {
	if (m_model == null)
	    return 1;
	if (m_o.isEmpty())
	    return 1; // no outlier detected

	int imin = 0;
	double[] t = m_model.computeLikelihood().getTStats(true,
		m_model.getArma().getParametersCount());
	int nx0 = m_model.getVarsCount() - m_o.size();

	for (int i = 1; i < m_o.size(); ++i)
	    if (Math.abs(t[i + nx0]) < Math.abs(t[imin + nx0]))
		imin = i;

	if (Math.abs(t[nx0 + imin]) >= cv)
	    return 1;
	IOutlierVariable toremoved = m_o.get(imin);
	removeOutlier(imin);
	if (m_lastremoved != null)
	    if (toremoved.getPosition().equals(m_lastremoved.getPosition())
		    && toremoved.getOutlierType() == m_lastremoved
			    .getOutlierType())
		return -1;
	m_lastremoved = toremoved;
	return 0;
    }
}
