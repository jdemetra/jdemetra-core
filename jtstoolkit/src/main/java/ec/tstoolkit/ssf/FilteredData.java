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
package ec.tstoolkit.ssf;

import ec.tstoolkit.utilities.Arrays2;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockStorage;
import ec.tstoolkit.design.Development;
import java.util.Arrays;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class FilteredData {

    DataBlockStorage m_A;

    double[] m_std, m_e;

    boolean m_bA = false;

    private int m_n;

    /**
     * 
     */
    public FilteredData()
    {
	m_bA = false;
    }

    /**
     * 
     * @param hasA
     */
    public FilteredData(final boolean hasA)
    {
	m_bA = hasA;
    }

    /*
     * public bool IsMissing(int t) { return Double.IsNaN(m_e[t]); }
     */
    /**
     * 
     * @param t
     * @return
     */
    public DataBlock A(final int t)
    {
	return (m_A == null) ? null : m_A.block(t);
    }

    /**
     * 
     * @param n
     */
    public void checkSize(int n)
    {
	if (n <= m_e.length)
	    return;
	n = DataBlockStorage.calcSize(n);
	double[] tmp = new double[n];
        Arrays.fill(tmp, m_e.length, n, Double.NaN);
	Arrays2.copy(m_e, tmp, m_e.length);
	m_e = tmp;
	tmp = new double[n];
	Arrays2.copy(m_std, tmp, m_std.length);
	m_std = tmp;

	if (m_bA)
	    m_A.resize(n);
    }

    /**
     * 
     */
    public void clear()
    {
	m_e = null;
	m_A = null;
	m_std = null;
	m_n = 0;
    }

    /**
     * 
     */
    public void close()
    {
    }

    /**
     * 
     * @param studentized
     * @param clean
     * @return
     */
    public double[] data(boolean studentized, boolean clean)
    {
	if (m_e == null)
	    return null;

	int n = getCount(), nobs = obsCount();
	if (n == nobs || !clean)
	    if (!studentized)
		return m_e.clone();
	    else {
		double[] rslt = new double[n];
		for (int i = 0; i < n; ++i)
		    if (!Double.isNaN(m_e[i]) && !Double.isInfinite(m_std[i])
			    && !Double.isNaN(m_std[i]))
			rslt[i] = m_e[i] / m_std[i];
		    else
			rslt[i] = Double.NaN;
		return rslt;
	    }
	else {
	    double[] rslt = new double[nobs];
	    if (!studentized) {
		for (int i = 0, j = 0; j < nobs; ++i)
		    if (!Double.isNaN(m_e[i]) && !Double.isInfinite(m_std[i])
			    && !Double.isNaN(m_std[i]))
			rslt[j++] = m_e[i];
	    } else
		for (int i = 0, j = 0; j < nobs; ++i)
		    if (!Double.isNaN(m_e[i]) && !Double.isInfinite(m_std[i])
			    && !Double.isNaN(m_std[i]))
			rslt[j++] = m_e[i] / m_std[i];
	    return rslt;
	}
    }

    /**
     * 
     * @param t
     * @return
     */
    public double E(final int t)
    {
	return m_e[t];
    }

    /**
     * 
     * @return
     */
    public int getCount()
    {
	return m_n;
    }

    /**
     * 
     * @param dim
     * @param n
     */
    public void init(final int dim, int n)
    {
	n = DataBlockStorage.calcSize(n);
	clear();
	m_e = new double[n];
        Arrays.fill(m_e, Double.NaN);
	m_std = new double[n];

	if (m_bA)
	    m_A = new DataBlockStorage(dim, n);
    }

    /**
     * 
     * @return
     */
    public boolean isSavingA()
    {
	return m_bA;
    }

    /**
     * 
     * @return
     */
    public int obsCount()
    {
	if (m_e == null)
	    return 0;
	int nobs = 0;
	for (int i = 0; i < m_n; ++i)
	    if (!Double.isNaN(m_e[i]) && !Double.isInfinite(m_std[i])
		    && !Double.isNaN(m_std[i]))
		++nobs;
	return nobs;
    }

    /**
     * 
     * @param t
     * @param state
     */
    public void save(final int t, final BaseOrdinaryState state)
    {
	if (state.isMissing()) {
	    m_e[t] = Double.NaN;
	    m_std[t] = Double.NaN;
	} else {
	    m_e[t] = state.e;
	    if (state.f != 0)
		m_std[t] = Math.sqrt(state.f);
	    else
		m_std[t] = 0;
	}
	if (m_bA)
	    m_A.save(t, state.A);
	if (m_n <= t)
	    m_n = t + 1;
    }

    /**
     * 
     * @param t
     * @param state
     */
    public void save(final int t, final DiffuseState state)
    {
	if (state.isMissing()) {
	    m_e[t] = Double.NaN;
	    m_std[t] = Double.NaN;
	} else {
	    m_e[t] = state.e;
	    if (state.fi == 0)
		m_std[t] = Math.sqrt(state.f);
	    else
		m_std[t] = Double.POSITIVE_INFINITY;
	}
	if (m_bA)
	    m_A.save(t, state.A);
	if (m_n <= t)
	    m_n = t + 1;
    }

    /**
     * 
     * @param value
     */
    public void setSavingA(final boolean value)
    {
	m_bA = value;
    }

    /**
     * 
     * @param t
     * @return
     */
    public double std(final int t)
    {
	return m_std[t];
    }
}
