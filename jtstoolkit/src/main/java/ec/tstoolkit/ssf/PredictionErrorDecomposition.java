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

import ec.tstoolkit.design.Development;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class PredictionErrorDecomposition extends ResidualsCumulator implements
	IFilteringResults, IFastFilteringResults, IFastArrayFilteringResults,
	IArrayFilteringResults {

    private double[] m_res;

    private boolean m_bres;

    private boolean m_open;
    /**
     * 
     * @param bres
     */
    public PredictionErrorDecomposition(final boolean bres)
    {
	m_bres = bres;
    }

    private void checkSize(final int pos) {
        if (m_res != null && pos < m_res.length)
	    return;
	double[] tmp = new double[ec.tstoolkit.data.DataBlockStorage
		.calcSize(pos + 1)];
        int l=0;
	if (m_res != null){
            l=m_res.length;
	    System.arraycopy(m_res, 0, tmp, 0, l);
        }
        for (int i=l; i<tmp.length; ++i)
            tmp[i]=Double.NaN;
	m_res = tmp;
    }

    /**
     * 
     * @return
     */
    public boolean isOpen()
    {
	return m_open;
    }

    /**
     * 
     */
    @Override
    public void close()
    {
        m_open=false;
    }

    /**
     * 
     * @return
     */
    public boolean hasResiduals()
    {
	return m_bres;
    }

    /**
     * 
     * @param ssf
     * @param data
     */
    @Override
    public void prepare(final ISsf ssf, final ISsfData data)
    {
	if (!m_open)
	    init(ssf, data.getCount());
        else if(m_bres)
	    checkSize(data.getCount());
    }
    
    protected void init(final ISsf ssf, final int len){
	clear();
        m_open=true;
	if (m_bres) {
	    m_res = new double[len];
	    for (int i = 0; i < m_res.length; ++i)
		m_res[i] = Double.NaN;
	} else
	    m_res = null;
    }

    /**
     * 
     * @param bClean
     * @return
     */
    public double[] residuals(boolean bClean)
    {
	if (m_res == null)
	    return null;
	if (!bClean)
	    return m_res;
	else {
	    int n = 0;
	    for (int i = 0; i < m_res.length; ++i)
		if (!Double.isNaN(m_res[i]))
		    ++n;
	    if (n == m_res.length)
		return m_res;
	    else {
		double[] res = new double[n];
		for (int i = 0, j = 0; i < m_res.length; ++i)
		    if (!Double.isNaN(m_res[i]))
			res[j++] = m_res[i];
		return res;
	    }
	}
    }

    /**
     * 
     * @param t
     * @param state
     */
    public void save(final int t, final ArrayState state)
    {
	if (!state.isMissing()) {
	    add(state.e, state.r * state.r);
	    if (m_bres) {
		checkSize(t);
		m_res[t] = state.e / state.r;
	    }
	}
    }

    /**
     * 
     * @param t
     * @param state
     */
    public void save(final int t, final FastArrayState state)
    {
	add(state.e, state.r * state.r);
	if (m_bres) {
	    checkSize(t);
	    m_res[t] = state.e / state.r;
	}
    }

    /**
     * 
     * @param t
     * @param state
     */
    public void save(final int t, final FastState state)
    {
	add(state.e, state.f);
	if (m_bres) {
	    checkSize(t);
	    m_res[t] = state.e / Math.sqrt(state.f);
	}
    }

    /**
     * 
     * @param t
     * @param state
     */
    public void save(final int t, final State state)
    {
	if (!state.isMissing()) {
	    add(state.e, state.f);
	    if (m_bres) {
		checkSize(t);
		m_res[t] = state.e / Math.sqrt(state.f);
	    }
	}
    }

    /**
     * 
     * @param value
     */
    public void setResiduals(final boolean value)
    {
	m_bres = value;
    }
}
