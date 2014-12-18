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
package ec.tstoolkit.ssf.multivariate;

import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.ssf.ISsf;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class M2uSsfAdapter implements ISsf
{

    /**
     * 
     * @param mssf
     * @param tmax
     * @return
     */
    public static IMUMap createMap(final IMultivariateSsf mssf, final int tmax)
    {
	int nvars = mssf.getVarsCount();
	int nentry = 0;
	for (int pos = 0; pos < tmax; ++pos)
	    for (int v = 0; v < nvars; ++v)
		if (mssf.hasZ(pos, v))
		    ++nentry;
	if (nentry == nvars * tmax)
	    return new FullM2uMap(nvars);
	else {
	    M2uMap map = new M2uMap(nentry);
	    for (int pos = 0; pos < tmax; ++pos)
		for (int v = 0; v < nvars; ++v)
		    if (mssf.hasZ(pos, v))
			map.add(pos, v);
	    map.close();
	    return map;
	}
    }

    /**
     *
     */
    protected IMultivariateSsf m_mssf;

    /**
     *
     */
    protected IMUMap m_map;

    /**
     *
     */
    protected int m_cur = -1;

    /**
     *
     */
    protected M2uEntry m_entry;

    /**
     *
     */
    protected DataBlock m_z;

    /** Creates a new instance of MSSFAdapter
     * @param mssf
     * @param map
     */
    public M2uSsfAdapter(final IMultivariateSsf mssf, final IMUMap map) {
	m_mssf = mssf;
	m_map = map;
	m_z = new DataBlock(m_mssf.getStateDim());
    }

    /**
     * 
     * @param b0
     */
    @Override
    public void diffuseConstraints(final SubMatrix b0)
    {
	m_mssf.diffuseConstraints(b0);
    }

    /**
     * 
     * @param pos
     * @param q
     */
    @Override
    public void fullQ(final int pos, final SubMatrix q)
    {
	updateEntry(pos);
	if (m_entry.ivar + 1 == m_mssf.getVarsCount())
            m_mssf.fullQ(m_entry.it, q);

    }

    /**
     * 
     * @return
     */
    public IMultivariateSsf getMSsf()
    {
	return m_mssf;
    }

    /**
     * 
     * @return
     */
    @Override
    public int getNonStationaryDim()
    {
	return m_mssf.getNonStationaryDim();
    }

    /**
     * 
     * @return
     */
    @Override
    public int getStateDim()
    {
	return m_mssf.getStateDim();
    }

    /**
     * 
     * @return
     */
    @Override
    public int getTransitionResCount()
    {
	return m_mssf.getTransitionResCount();
    }

    /**
     * 
     * @return
     */
    @Override
    public int getTransitionResDim()
    {
	return m_mssf.getTransitionResDim();
    }

    /**
     * 
     * @return
     */
    @Override
    public boolean hasR()
    {
	return m_mssf.hasR();
    }

    /**
     * 
     * @param pos
     * @return
     */
    @Override
    public boolean hasTransitionRes(final int pos)
    {
	updateEntry(pos);
	return m_entry.ivar + 1 == m_mssf.getVarsCount();
	// return m_entry.ivar == 0;
    }

    /**
     * 
     * @return
     */
    @Override
    public boolean hasW()
    {
	return m_mssf.hasW();
    }

    /**
     * 
     * @return
     */
    @Override
    public boolean isDiffuse()
    {
	return m_mssf.isDiffuse();
    }

    /**
     * 
     * @return
     */
    @Override
    public boolean isMeasurementEquationTimeInvariant()
    {
	return false;
    }

    // information
    /**
     * 
     * @return
     */
    @Override
    public boolean isTimeInvariant()
    {
	return false;
    }

    /**
     * 
     * @return
     */
    @Override
    public boolean isTransitionEquationTimeInvariant()
    {
	return false;
    }

    /**
     * 
     * @return
     */
    @Override
    public boolean isTransitionResidualTimeInvariant()
    {
	return false;
    }

    /**
     * 
     * @return
     */
    @Override
    public boolean isValid()
    {
	return m_mssf.isValid();
    }

    /**
     * 
     * @param pos
     * @param k
     * @param l
     */
    @Override
    public void L(final int pos, final DataBlock k, final SubMatrix l)
    {
	T(pos, l);
        m_z.set(0);
	Z(pos, m_z);
	DataBlockIterator cols = l.columns();
	DataBlock col = cols.getData();
	do {
	    double z = m_z.get(cols.getPosition());
	    col.addAY(-z, k);
	} while (cols.next());
    }

    /**
     * 
     * @param pf0
     */
    @Override
    public void Pf0(final SubMatrix pf0)
    {
	m_mssf.Pf0(pf0);
    }

    /**
     * 
     * @param pi0
     */
    @Override
    public void Pi0(final SubMatrix pi0)
    {
	m_mssf.Pi0(pi0);
    }

    /**
     * 
     * @param pos
     * @param q
     */
    @Override
    public void Q(final int pos, final SubMatrix q)
    {
	updateEntry(pos);
	if (m_entry.ivar + 1 == m_mssf.getVarsCount())
	    m_mssf.Q(m_entry.it, q);

    }

    /**
     * 
     * @param pos
     * @param r
     */
    @Override
    public void R(final int pos, final SubArrayOfInt r)
    {
	updateEntry(pos);
	if (m_entry.ivar + 1 == m_mssf.getVarsCount())
	    m_mssf.R(m_entry.it, r);
    }

    // transition matrix
    /**
     * 
     * @param pos
     * @param tr
     */
    @Override
    public void T(final int pos, final SubMatrix tr)
    {
	updateEntry(pos);
	if (m_entry.ivar + 1 != m_mssf.getVarsCount()) {
	    // if (m_entry.ivar != 0) {
	    tr.set(0);
	    tr.diagonal().set(1);
	} else
	    m_mssf.T(m_entry.it, tr);
    }

    /**
     * 
     * @param pos
     * @param v
     */
    @Override
    public void TVT(final int pos, final SubMatrix v)
    {
	updateEntry(pos);
	if (m_entry.ivar + 1 == m_mssf.getVarsCount())
	    // if (m_entry.ivar == 0)
	    m_mssf.TVT(m_entry.it, v);
    }

    // forward operations
    /**
     * 
     * @param pos
     * @param x
     */
    @Override
    public void TX(final int pos, final DataBlock x)
    {
	updateEntry(pos);
	if (m_entry.ivar + 1 == m_mssf.getVarsCount())
	    // if (m_entry.ivar == 0)
	    m_mssf.TX(m_entry.it, x);
    }

    /**
     * 
     * @param pos
     */
    protected void updateEntry(final int pos)
    {
	if (pos != m_cur) {
	    m_cur = pos;
	    m_entry = m_map.get(pos);
	}
    }

    // backward operations
    /**
     * 
     * @param pos
     * @param v
     * @param d
     */
    @Override
    public void VpZdZ(final int pos, final SubMatrix v, final double d)
    {
	updateEntry(pos);
	m_mssf.VpZdZ(m_entry.it, m_entry.ivar, m_entry.ivar, v, d);
    }

    /**
     * 
     * @param pos
     * @param w
     */
    @Override
    public void W(final int pos, final SubMatrix w)
    {
	updateEntry(pos);
	if (m_entry.ivar + 1 == m_mssf.getVarsCount())
	    m_mssf.W(m_entry.it, w);
    }

    /**
     * 
     * @param pos
     * @param x
     * @param d
     */
    @Override
    public void XpZd(final int pos, final DataBlock x, final double d)
    {
	updateEntry(pos);
	m_mssf.XpZd(m_entry.it, m_entry.ivar, x, d);
    }

    // backward operations
    /**
     * 
     * @param pos
     * @param x
     */
    @Override
    public void XT(final int pos, final DataBlock x)
    {
	updateEntry(pos);
	if (m_entry.ivar + 1 == m_mssf.getVarsCount())
	    // if (m_entry.ivar == 0)
	    m_mssf.XT(m_entry.it, x);
    }

    /**
     * 
     * @param pos
     * @param z
     */
    @Override
    public void Z(final int pos, final DataBlock z)
    {
	updateEntry(pos);
	m_mssf.Z(m_entry.it, m_entry.ivar, z);
    }

    /**
     * 
     * @param pos
     * @param M
     * @param zm
     */
    @Override
    public void ZM(final int pos, final SubMatrix M, final DataBlock zm)
    {
	updateEntry(pos);
	m_mssf.ZM(m_entry.it, m_entry.ivar, M, zm);
    }

    /**
     * 
     * @param pos
     * @param v
     * @return
     */
    @Override
    public double ZVZ(final int pos, final SubMatrix v)
    {
	updateEntry(pos);
	return m_mssf.ZVZ(m_entry.it, m_entry.ivar, m_entry.ivar, v);
    }

    // forward operations
    /**
     * 
     * @param pos
     * @param x
     * @return
     */
    @Override
    public double ZX(final int pos, final DataBlock x)
    {
	updateEntry(pos);
	return m_mssf.ZX(m_entry.it, m_entry.ivar, x);
    }
}
