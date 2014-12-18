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
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.TableOfBoolean;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.dstats.Normal;
import ec.tstoolkit.dstats.ProbabilityType;
import ec.tstoolkit.maths.matrices.Householder;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.timeseries.regression.IOutlierFactory;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * 
 * @author Jean Palate
 * @param <T>
 */
@Development(status = Development.Status.Preliminary)
public abstract class AbstractSingleOutlierDetector<T extends IArimaModel>
{

    /**
     * 
     * @param nval
     * @return
     */
    public static double calcVA(int nval)
    {
	return calcVA(nval, 0.05);
    }

    /**
     * 
     * @param nvals
     * @param alpha
     * @return
     */
    public static double calcVA(int nvals, double alpha)
    {
	Normal normal = new Normal();
	if (nvals == 1)
	    return normal.getProbabilityInverse(alpha / 2,
		    ProbabilityType.Upper);
	double n = nvals;
	double[] y = new double[3];
	int[] x = new int[] { 2, 100, 200 };
	Matrix X = new Matrix(3, 3);

	for (int i = 0; i < 3; ++i) {
	    X.set(i, 0, 1);
	    X.set(i, 2, Math.sqrt(2 * Math.log(x[i])));
	    X.set(i, 1, (Math.log(Math.log(x[i])) + Math.log(4 * Math.PI))
		    / (2 * X.get(i, 2)));
	}

	y[0] = normal.getProbabilityInverse((1 + Math.sqrt(1 - alpha)) / 2,
		ProbabilityType.Lower);
	for (int i = 1; i < 3; ++i)
	    y[i] = calcVAL(x[i], alpha);
	// solve X b = y
	Householder qr = new Householder(false);
	qr.decompose(X);
	double[] b = qr.solve(y);

	double acv = Math.sqrt(2 * Math.log(n));
	double bcv = (Math.log(Math.log(n)) + Math.log(4 * Math.PI))
		/ (2 * acv);
	return b[0] + b[1] * bcv + b[2] * acv;
    }

    private static double calcVAL(int nvals, double alpha) {
	if (nvals == 1)
	    return 1.96; // normal distribution
	double n = nvals;
	double pmod = 2 - Math.sqrt(1 + alpha);
	double acv = Math.sqrt(2 * Math.log(n));
	double bcv = acv - (Math.log(Math.log(n)) + Math.log(4 * Math.PI))
		/ (2 * acv);
	double xcv = -Math.log(-.5 * Math.log(pmod));
	return xcv / acv + bcv;
    }

    private ArrayList<IOutlierFactory> m_o = new ArrayList<>();

    private RegArimaModel<T> m_model;

    private TsDomain m_domain;

    private double m_mad;

    private int m_lbound, m_ubound;

    private Matrix m_T;

    private TableOfBoolean m_bT;

    private int m_posmax = -1, m_omax = -1;

    private int m_centile = 50;

    /**
     * 
     */
    public AbstractSingleOutlierDetector()
    {
    }

    /**
     * 
     * @param o
     */
    public void addOutlierFactory(IOutlierFactory o)
    {
	m_o.add(o);
	clear();
    }

    /**
     *
     * @return
     */
    protected abstract boolean calc();

    /**
     * 
     * @param e
     */
    protected void calcMAD(IReadDataBlock e)
    {
	int n = e.getLength();
	double[] a = new double[n];
        e.copyTo(a, 0);
	for (int i = 0; i < n; ++i)
	    a[i] = Math.abs(a[i]);
	Arrays.sort(a);
	double m = 0;
	int nm = n * m_centile / 100;
	if (n % 2 == 0) // n even
	    m = (a[nm - 1] + a[nm]) / 2;
	else
	    m = a[nm];
	Normal normal = new Normal();
	double l = normal.getProbabilityInverse(0.5 + .005 * m_centile,
		ProbabilityType.Lower);
	m_mad = m / l;
    }

    /**
     * 
     */
    protected void clear()
    {
        m_model=null;
	m_mad = 0;
	m_omax = -1;
	m_posmax = -1;
	if (m_T != null)
	    m_T.clear();
    }

    /**
     * 
     */
    public void clearOutlierFactories()
    {
	m_o.clear();
	clear();
    }

    /**
     * 
     * @param pos
     * @param outlier
     * @return
     */
    public double coeff(int pos, int outlier)
    {
	return m_T.get(pos, outlier) * m_mad;
    }

    /**
     * 
     * @param pos
     * @param ioutlier
     */
    public void exclude(int pos, int ioutlier)
    {
	m_bT.set(pos, ioutlier, false);
	m_T.set(pos, ioutlier, 0);
    }

    /**
     * 
     * @param pos
     */
    public void exclude(int[] pos)
    {
	if (pos == null)
	    return;
	for (int i = 0; i < pos.length; ++i)
	    for (int j = 0; j < m_o.size(); ++j)
		exclude(pos[i], j);
    }

    /**
     * 
     * @param o
     */
    public void exclude(IOutlierVariable o)
    {
	for (int i = 0; i < m_o.size(); ++i) {
	    IOutlierFactory exemplar = m_o.get(i);
	    if (exemplar.getOutlierType().equals(o.getOutlierType())) {
		int pos = o.getPosition().minus(m_domain.getStart());
		exclude(pos, i);
		break;
	    }
	}
    }

    /**
     * 
     * @param outliers
     */
    public void exclude(IOutlierVariable[] outliers)
    {
	for (IOutlierVariable o : outliers)
	    exclude(o);
    }

    /**
     * 
     * @param outliers
     */
    public void exclude(Iterator<IOutlierVariable> outliers)
    {
	while (outliers.hasNext())
	    exclude(outliers.next());
    }

    /**
     * 
     * @param pos
     * @param ioutlier
     */
    public void exclude(TsPeriod pos, int ioutlier)
    {
	int r = pos.minus(m_domain.getStart());
	if (r >= 0)
	    exclude(r, ioutlier);
    }

    /**
     * 
     * @return
     */
    public TsDomain getDomain()
    {
	return m_domain;
    }

    /**
     * 
     * @return
     */
    public int getLBound()
    {
	return m_lbound;
    }

    /**
     * 
     * @return
     */
    public double getMAD()
    {
	return m_mad;
    }

    /**
     * 
     * @return
     */
    public IOutlierVariable getMaxOutlier()
    {
	if (m_posmax == -1)
	    searchMax();
	if (m_omax == -1)
	    return null;
	return m_o.get(m_omax).create(m_domain.get(m_posmax));
    }

    /**
     * 
     * @return
     */
    public int getMaxOutlierType()
    {
	if (m_omax == -1)
	    searchMax();
	return m_omax;
    }

    /**
     * 
     * @return
     */
    public int getMaxPosition()
    {
	if (m_posmax == -1)
	    searchMax();
	return m_posmax;
    }

    /**
     * 
     * @return
     */
    public double getMaxTStat()
    {
	if (m_omax == -1)
	    searchMax();
	double tmax = T(m_posmax, m_omax);
	return tmax;
    }

    /**
     * 
     * @return
     */
    public RegArimaModel<T> getModel()
    {
	return m_model;
    }

    /**
     * 
     * @return
     */
    public int getOutlierFactoriesCount()
    {
	return m_o.size();
    }

    /**
     * 
     * @param i
     * @return
     */
    public IOutlierFactory getOutlierFactory(int i)
    {
	return m_o.get(i);
    }

    /**
     * 
     * @return
     */
    public int getUBound()
    {
	return m_ubound;
    }

    /**
     * 
     * @param pos
     * @param outlier
     * @return
     */
    public boolean isDefined(int pos, int outlier)
    {
	return m_bT.get(pos, outlier);
    }

    /**
     * 
     * @param estimationdomain
     * @param outliersdomain
     */
    public void prepare(TsDomain estimationdomain, TsDomain outliersdomain)
    {
	m_domain = estimationdomain;
	if (outliersdomain == null) {
	    m_lbound = 0;
	    m_ubound = estimationdomain.getLength();
	} else {
	    TsDomain common = estimationdomain.intersection(outliersdomain);
	    m_lbound = common.getStart().minus(estimationdomain.getStart());
	    m_ubound = m_lbound + common.getLength();
	}

	prepareT(estimationdomain.getLength());
    }

    /**
     * 
     * @param n
     */
    protected void prepareT(int n)
    {
	m_T = new Matrix(n, m_o.size());
	m_bT = new TableOfBoolean(n, m_o.size());
	for (int i = 0; i < m_o.size(); ++i) {
	    IOutlierFactory fac = getOutlierFactory(i);
	    TsDomain dom = fac.definitionDomain(m_domain);
	    int jstart = Math.max(m_lbound, dom.getStart().minus(m_domain.getStart()));
	    int jend = Math.min(m_ubound, dom.getEnd().minus(m_domain.getStart()));
	    for (int j = jstart; j < jend; ++j)
		m_bT.set(j, i, Boolean.TRUE);
	}
    }

    /**
     * 
     * @param model
     * @return
     */
    public boolean process(RegArimaModel<T> model)
    {
	clear();
	m_model = model.clone();
	return calc();
    }

    private void searchMax() {
	if (m_T == null)
	    return;
	double max = 0;
	int imax = -1;
	double[] T = m_T.internalStorage();
	for (int i = 0; i < T.length; ++i) {
	    double cur = Math.abs(T[i]);
	    if (cur > max) {
		imax = i;
		max = cur;
	    }
	}
	if (imax == -1)
	    return;
	m_posmax = imax % m_T.getRowsCount();
	m_omax = imax / m_T.getRowsCount();
    }

    /**
     * 
     * @param value
     */
    public void setMAD(double value)
    {
	m_mad = value;
    }

    /**
     * 
     * @param pos
     * @param outlier
     * @param val
     */
    protected void setT(int pos, int outlier, double val)
    {
	m_T.set(pos, outlier, val);
    }

    /**
     * 
     * @param pos
     * @param outlier
     * @return
     */
    public double T(int pos, int outlier)
    {
	return m_T.get(pos, outlier);
    }

}
