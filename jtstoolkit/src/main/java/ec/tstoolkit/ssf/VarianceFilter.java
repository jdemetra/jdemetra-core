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
public class VarianceFilter implements IFilteringResults, IFastFilteringResults {

    ISsf m_ssf;

    MatrixStorage m_P;

    DataBlockStorage m_C, m_L;

    double[] m_f;

    boolean[] m_nd;

    int m_n, m_dim;

    boolean m_bC = false, m_bP = false, m_bL = false;

    boolean m_open;

    /**
     * 
     */
    public VarianceFilter()
    {
	m_bP = false;
	m_bC = false;
	m_bL = false;
    }

    /**
     * 
     * @param hasC
     */
    public VarianceFilter(final boolean hasC)
    {
	m_bP = false;
	m_bC = hasC;
	m_bL = false;
    }

    /**
     * 
     * @param t
     * @return
     */
    public DataBlock C(final int t)
    {
	return (m_C == null) ? null : m_C.block(t);
    }

    /**
     * 
     * @param n
     */
    protected void checkSize(final int n)
    {
	if (m_n >= n)
	    return;
	m_n = n;
	if (m_f.length >= n)
	    return;
	int sz = DataBlockStorage.calcSize(n);
	double[] tmp = new double[sz];
	Arrays2.copy(m_f, tmp, m_f.length);
	m_f = tmp;

	boolean[] btmp = new boolean[sz];
	System.arraycopy(m_nd, 0, btmp, 0, m_nd.length);
	m_nd = btmp;
	if (m_bC)
	    m_C.resize(sz);

	if (m_bL)
	    m_L.resize(sz);
	else if (m_bP)
	    m_P.resize(sz);
    }

    /**
     * 
     */
    public void clear()
    {
	m_n = 0;
	m_dim = 0;
	m_P = null;
	m_C = null;
	m_L = null;
	m_f = null;
	m_nd = null;
    }

    /**
     * 
     */
    public void close()
    {
	m_open = false;
    }

    /**
     * 
     * @param t
     * @return
     */
    public double F(final int t)
    {
	return m_f[t];
    }

    /**
     * 
     * @return
     */
    public int getSize()
    {
	return m_n;
    }

    /**
     * 
     * @return
     */
    public int getStateDim()
    {
	return m_dim;
    }

    /**
     * 
     * @param ssf
     * @param n
     */
    protected void init(final ISsf ssf, final int n)
    {
	clear();
	m_open = true;
	m_ssf = ssf;

	m_dim = ssf.getStateDim();
	m_n = n;

	m_f = new double[DataBlockStorage.calcSize(n)];
	m_nd = new boolean[DataBlockStorage.calcSize(n)];
	if (m_bC)
	    m_C = new DataBlockStorage(m_dim, m_n);
	if (m_bL)
	    m_L = new DataBlockStorage(m_dim, m_n);
	else if (m_bP)
	    m_P = new MatrixStorage(m_dim, m_n);
    }

    /**
     * 
     * @param t
     * @return
     */
    public boolean isMissing(final int t)
    {
	return m_nd[t];
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
     * @return
     */
    public boolean isSavingC()
    {
	return m_bC;
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
    public boolean isSavingP()
    {
	return m_bP;
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
     * @param t
     * @return
     */
    public SubMatrix P(final int t)
    {
	if (m_P == null)
	    return null;
	else
	    return m_P.matrix(t);
    }

    /**
     * 
     * @param ssf
     * @param data
     */
    public void prepare(final ISsf ssf, final ISsfData data)
    {
	if (!m_open)
	    init(ssf, data.getCount());
	else
	    checkSize(data.getCount());
    }

    /**
     *
     * @param fdata
     * @param startpos
     * @param data
     * @param initialstate
     */
    public void process(final FilteredData fdata, final int startpos,
	    final double[] data, final double[] initialstate) {
	DataBlock a = new DataBlock(m_dim);
	if (initialstate != null)
	    a.copyFrom(initialstate, 0);

	int imax = m_n - 1;
	double e = 0;
	for (int i = startpos; i < imax; ++i) {
	    // compute e = y(i) - Za
	    e = data[i] - m_ssf.ZX(i, a);
	    fdata.m_e[i] = e;
	    if (fdata.m_bA)
		fdata.A(i).copy(a);

	    m_ssf.TX(i, a);

	    if (!m_nd[i]) {
		double c = e / m_f[i];
		a.addAY(c, C(i));
	    }
	}

	e = data[imax] - m_ssf.ZX(imax, a);
	fdata.m_e[imax] = e;
	if (fdata.m_bA)
	    fdata.A(imax).copy(a);
    }

    /**
     * 
     * @param t
     * @param state
     */
    public void save(final int t, final FastState state)
    {
	m_f[t] = state.f;
	if (m_bC)
	    m_C.save(t, state.C);
	if (m_bL)
	    m_L.save(t, state.L);
    }

    /**
     * 
     * @param t
     * @param state
     */
    @Override
    public void save(final int t, final State state)
    {
	m_f[t] = state.f;
	m_nd[t] = state.isMissing();
	if (m_bC)
	    m_C.save(t, state.C);
	if (m_bP)
	    m_P.save(t, state.P);
    }

    /**
     * 
     * @param value
     */
    public void setSavingC(final boolean value)
    {
	m_bC = value;
    }

    /**
     * 
     * @param value
     */
    public void setSavingL(final boolean value)
    {
	m_bL = value;
	if (m_bL)
	    m_bP = false;
    }

    /**
     * 
     * @param value
     */
    public void setSavingP(final boolean value)
    {
	m_bP = value;
	if (m_bP)
	    m_bL = false;
    }
}
