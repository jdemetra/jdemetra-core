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

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockStorage;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.MatrixStorage;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.utilities.Arrays2;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class DiffuseVarianceFilter extends VarianceFilter implements
	IDiffuseFilteringResults {

    MatrixStorage m_Pi;

    DataBlockStorage m_Ci;

    double[] m_fi;

    int m_enddiffusepos, m_ndiffuse;

    /**
     * 
     */
    public DiffuseVarianceFilter()
    {
    }

    /**
     * 
     * @param hasC
     */
    public DiffuseVarianceFilter(final boolean hasC)
    {
	super(hasC);
    }

    private void checkdiffusepos(final int pos) {
	if (pos < m_enddiffusepos)
	    return;
	m_enddiffusepos = pos + 1;
	super.checkSize(m_enddiffusepos);
	if (m_fi == null || m_enddiffusepos > m_fi.length) {
	    double[] tmp = new double[DataBlockStorage
		    .calcSize(m_enddiffusepos)];
	    if (m_fi != null)
		Arrays2.copy(m_fi, tmp, m_fi.length);
	    m_fi = tmp;
	    if (m_bC)
		m_Ci.resize(m_enddiffusepos);
	    if (m_bP)
		m_Pi.resize(m_enddiffusepos);
	}
    }

    /**
     * 
     * @param t
     * @return
     */
    public DataBlock Ci(final int t)
    {
	return (m_Ci == null) ? null : m_Ci.block(t);
    }

    /**
     *
     */
    @Override
    public void clear() {
	m_Pi = null;
	m_Ci = null;
	m_fi = null;
	m_enddiffusepos = 0;
	m_ndiffuse = 0;
	super.clear();
    }

    /**
     * 
     */
    @Override
    public void closeDiffuse()
    {
    }

    /**
     * 
     * @param t
     * @return
     */
    public double Fi(final int t)
    {
	return m_fi == null || t >= m_fi.length ? 0 : m_fi[t];
    }

    /**
     * 
     * @return
     */
    public double getDiffuseCount()
    {
	return m_ndiffuse;
    }

    /**
     * 
     * @return
     */
    public int getEndDiffusePosition()
    {
	return m_enddiffusepos;
    }

    /**
     * 
     * @param t
     * @return
     */
    public SubMatrix Pi(final int t)
    {
	if (m_Pi == null)
	    return null;
	else
	    return m_Pi.matrix(t);
    }

    /**
     * 
     * @param ssf
     * @param data
     */
    @Override
    public void prepareDiffuse(final ISsf ssf, final ISsfData data)
    {
	clear();
	m_ssf = ssf;
	m_dim = ssf.getStateDim();
	int ndiffuse = m_ssf.getNonStationaryDim();
	super.init(ssf, ndiffuse);
	if (m_bC)
	    m_Ci = new DataBlockStorage(m_dim, ndiffuse);
	if (m_bP)
	    m_Pi = new MatrixStorage(m_dim, ndiffuse);
	m_fi = new double[ndiffuse];
    }

    /**
     *
     * @param fdata
     * @param startpos
     * @param data
     * @param initialstate
     */
    @Override
    public void process(final FilteredData fdata, final int startpos,
	    final double[] data, final double[] initialstate) {
	DataBlock a = new DataBlock(m_dim);
	if (initialstate != null)
	    a.copyFrom(initialstate, 0);

	int imax = m_n - 1;
	double e;
	for (int i = startpos; i < imax; ++i) {
	    // compute e = y(i) - Za
	    e = data[i] - m_ssf.ZX(i, a);
	    fdata.m_e[i] = e;
	    if (fdata.m_bA)
		fdata.A(i).copy(a);

	    m_ssf.TX(i, a);

	    if (!m_nd[i])
		if (i >= m_enddiffusepos || m_fi[i] == 0) {
		    double c = e / m_f[i];
		    a.addAY(c, C(i));
		} else {
		    double c = e / m_fi[i];
		    a.addAY(c, Ci(i));
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
    @Override
    public void save(final int t, final DiffuseState state)
    {
	checkdiffusepos(t);
	super.save(t, state);
	double fi = state.fi;
	// if (fi != 0)
	{
	    m_fi[t] = fi;
	    if (m_bC)
		m_Ci.save(t, state.Ci);
	    if (m_bP)
		m_Pi.save(t, state.Pi);
	    if (fi != 0 && !state.isMissing())
		++m_ndiffuse;
	}
    }
}
