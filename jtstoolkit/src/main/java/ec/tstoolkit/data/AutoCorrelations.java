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
package ec.tstoolkit.data;

import java.util.Arrays;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.NewObject;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class AutoCorrelations
{

    private double[] m_auto;
    private double[] m_pauto;
    private double[] m_pc;
    private boolean m_bmean;
    private int m_kmax;
    private static int g_atom = 4;
    DescriptiveStatistics stats;

    /**
     * 
     * @param stats
     */
    public AutoCorrelations(DescriptiveStatistics stats)
    {
	this.stats = stats;
    }

    /**
     * 
     * @param data
     */
    public AutoCorrelations(IReadDataBlock data)
    {
	stats = new DescriptiveStatistics(data);
    }

    /**
     * 
     * @param order
     * @return
     */
    public double autoCorrelation(final int order)
    {
	if (order == 0)
	    return 1;
	checkAC(order);
	return m_auto[order - 1];
    }

    /**
     * 
     */
    protected void calcAutoCorrelations()
    {
	m_auto = new double[m_kmax];
	if (m_bmean) {
	    double[] tmp = stats.internalStorage().clone();
	    int n = tmp.length;
	    double m = stats.getAverage();
	    for (int i = 0; i < n; ++i)
		if (Double.isFinite(tmp[i]))
		    tmp[i] -= m;
	    m_auto = DescriptiveStatistics.ac(m_kmax, tmp);
	} else
	    m_auto = DescriptiveStatistics.ac(m_kmax, stats.internalStorage());

    }

    /**
     * 
     */
    protected void calcPartialAutoCorrelations()
    {
	m_pc = new double[m_auto.length];
	m_pauto = DescriptiveStatistics.pac(m_auto, m_pc);
    }

    private void checkAC(final int order) {
	if (order == 0)
	    return;
	if (m_auto == null || m_auto.length < order) {
	    if (m_kmax < order)
		m_kmax = nextK(order);

	    calcAutoCorrelations();
	}

    }

    private void checkPAC(final int order) {
	if (order == 0)
	    return;
	if (m_pauto == null || m_pauto.length < order) {
	    checkAC(order);
	    calcPartialAutoCorrelations();
	}
    }

    /**
     * Computes the auto-correlations of the data, with lags in [1, kmax]
     * @return An array with the kmax auto-correlations is returned.
     * ac[i] corresponds to the auto-correlation of lag i+1
     */
    @NewObject
    public double[] getAC()
    {
	checkAC(m_kmax);
	return Arrays.copyOf(m_auto, m_kmax);
    }

    /**
     * 
     * @return
     */
    public double getDurbinWatson()
    {
	double nom = 0.0;
	double[] data = stats.internalStorage();
	int n = data.length;
	for (int i = 1; i < n; i++) {
	    double cur = data[i];
	    double prev = data[i - 1];
	    if (Double.isFinite(cur))
		nom += (cur - prev) * (cur - prev);
	}
	return nom / stats.getSumSquare();
    }

    /**
     * 
     * @return
     */
    public int getKMax()
    {
	return m_kmax;
    }

    /**
     * 
     * @return
     */
    public double[] getPAC()
    {
	checkPAC(m_kmax);
	return Arrays.copyOf(m_pauto, m_kmax);
    }

    /**
     * 
     * @return
     */
    public double[] getPACCoefficients()
    {
	checkPAC(m_kmax);
	return m_pc;
    }

    /**
     * Retrieves the data used to compute the auto-correlations.
     * 
     * @return
     */
    public DescriptiveStatistics getUnderlyingData() {
	return stats;
    }

    /**
     * 
     * @return
     */
    public boolean isCorrectedForMean()
    {
	return m_bmean;
    }

    /**
     * 
     * @param k
     * @return
     */
    public int nextK(int k)
    {
	if (k <= 1)
	    k = g_atom;
	int n = (k - 1) / g_atom;
	n = (n + 1) * g_atom;
	int nmax = stats.getDataCount() * 3 / 4;
	if (n > nmax)
	    n = nmax;
	return n;
    }

    /**
     * 
     * @param order
     * @return
     */
    public double partialAutoCorrelation(final int order)
    {
	if (order == 0)
	    return 1;
	checkPAC(order);
	return m_pauto[order - 1];
    }

    /**
     * Recalculates the autocorrelations and partial autocorrelations
     */
    protected void recalc() {
	calcAutoCorrelations();
	calcPartialAutoCorrelations();
    }

    /**
     * 
     * @param bool
     */
    public void setCorrectedForMean(final boolean bool)
    {
	m_bmean = bool;
	m_auto = null;
	m_pauto = null;
	m_pc = null;
    }

    /**
     * Sets the maximum order for the (partial- auto-correlations
     * @param value
     */
    public void setKMax(final int value)
    {
	m_kmax = nextK(value);
    }
}
