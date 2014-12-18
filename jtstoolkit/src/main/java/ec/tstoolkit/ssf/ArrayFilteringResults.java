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

import ec.tstoolkit.maths.matrices.MatrixStorage;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockStorage;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.SubMatrix;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class ArrayFilteringResults extends ResidualsCumulator implements
	IArrayFilteringResults, IFastArrayFilteringResults {

    private MatrixStorage m_S;

    private DataBlockStorage m_K, m_A, m_L;

    private double[] m_r, m_e;

    private int m_dim;

    private boolean m_bK, m_bS, m_bA, m_bL;

    /**
     * 
     */
    public ArrayFilteringResults()
    {
	m_bA = false;
	m_bS = false;
	m_bK = false;
	m_bL = false;
    }

    /**
     * 
     * @param hasA
     * @param hasK
     */
    public ArrayFilteringResults(final boolean hasA, final boolean hasK)
    {
	m_bA = hasA;
	m_bS = false;
	m_bK = hasK;
	m_bL = false;
    }

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
     */
    @Override
    public void clear() {
	super.clear();
	m_n = 0;
	m_dim = 0;
	m_S = null;
	m_K = null;
	m_e = null;
	m_A = null;
	m_r = null;
	m_L = null;
    }

    /**
     * 
     */
    public void close()
    {
    }

    /**
     * 
     * @param t
     * @return
     */
    public double E(final int t)
    {
	return m_e == null ? 0 : m_e[t];
    }

    /**
     * 
     * @param t
     * @return
     */
    public double F(final int t)
    {
	return m_r[t];
    }

    /**
     * 
     * @return
     */
    public double[] getResiduals()
    {
	if (m_e == null)
	    return null;
	int n = 0;
	for (int i = 0; i < m_e.length; ++i)
	    if (!Double.isNaN(m_e[i]))
		++n;
	if (n == 0)
	    return m_e;
	else {
	    double[] res = new double[n];
	    n = 0;
	    for (int i = 0; i < m_e.length; ++i)
		if (!Double.isNaN(m_e[i]))
		    res[n++] = m_e[i] / m_r[i];
	    return res;
	}
    }

    /**
     * 
     * @param t
     * @return
     */
    public boolean isMissing(final int t)
    {
	return m_e == null ? false : Double.isNaN(m_e[t]);
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
    public boolean isSavingK()
    {
	return m_bK;
    }

    /**
     * 
     * @return
     */
    public boolean isSavingL()
    {
	return m_bL;
    }

    /**
     * 
     * @return
     */
    public boolean isSavingS()
    {
	return m_bS;
    }

    /**
     * 
     * @param t
     * @return
     */
    public DataBlock K(final int t)
    {
	return (m_K == null) ? null : m_K.block(t);
    }

    /**
     * 
     * @param t
     * @return
     */
    public DataBlock L(final int t)
    {
	return (m_L == null) ? null : m_L.block(t);
    }

    /**
     * 
     * @param ssf
     * @param data
     */
    public void prepare(final ISsf ssf, final ISsfData data)
    {
	clear();
	m_dim = ssf.getStateDim();
	int n = data.getCount();
	boolean hasdata = data.hasData();
	if (hasdata) {
	    if (m_bA)
		m_A = new DataBlockStorage(m_dim, n);
	    m_e = new double[n];
	    for (int i = 0; i < n; ++i)
		m_e[i] = Double.NaN;
	}

	m_r = new double[n];
	if (m_bK)
	    m_K = new DataBlockStorage(m_dim, n);
	if (m_bL)
	    m_L = new DataBlockStorage(m_dim, n);
	else if (m_bS)
	    m_S = new MatrixStorage(m_dim, n);
    }

    /**
     * 
     * @param t
     * @return
     */
    public SubMatrix S(final int t)
    {
	if (m_S == null)
	    return null;
	else
	    return m_S.matrix(t);
    }

    /**
     * 
     * @param t
     * @param state
     */
    public void save(final int t, final ArrayState state)
    {
	m_r[t] = state.r;
	if (m_e != null)
	    if (state.isMissing())
		m_e[t] = Double.NaN;
	    else {
		add(state.e, state.r * state.r);
		m_e[t] = state.e;
	    }
	if (m_bA)
	    m_A.save(t, state.A);
	if (m_bK)
	    m_K.save(t, state.K);
	if (m_bS)
	    m_S.save(t, state.S);
    }

    /**
     * 
     * @param t
     * @param state
     */
    public void save(final int t, final FastArrayState state)
    {
	m_r[t] = state.r;
	add(state.e, state.r * state.r);
	m_e[t] = state.e;

	if (m_bA)
	    m_A.save(t, state.A);
	if (m_bK)
	    m_K.save(t, state.K);
	if (m_bL)
	    m_L.save(t, state.L);
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
     * @param value
     */
    public void setSavingK(final boolean value)
    {
	m_bK = value;
    }

    /**
     * 
     * @param value
     */
    public void setSavingL(final boolean value)
    {
	m_bL = value;
	if (m_bL)
	    m_bS = false;
    }

    /**
     * 
     * @param value
     */
    public void setSavingS(final boolean value)
    {
	m_bS = value;
	if (m_bS)
	    m_bL = false;
    }
}
