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


package ec.tstoolkit.ssf.extended;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.ssf.ISsf;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Exploratory)
public class LinearizedLogSsf<S extends ISsf> implements ISsf
{
    private S m_ssf;

    private double[] m_e;

    /**
     * 
     * @param ssf
     * @param e0
     */
    public LinearizedLogSsf(S ssf, double[] e0)
    {
	m_ssf = ssf;
	m_e = e0.clone();
    }

    /**
     * 
     * @param b
     */
    public void diffuseConstraints(SubMatrix b)
    {
	m_ssf.diffuseConstraints(b);
    }

    /**
     * 
     * @param pos
     * @param qm
     */
    public void fullQ(int pos, SubMatrix qm)
    {
	m_ssf.fullQ(pos, qm);
    }

    /**
     * 
     * @return
     */
    public DataBlock getE()
    {
	return new DataBlock(m_e);
    }

    /**
     * 
     * @return
     */
    public int getNonStationaryDim()
    {
	return m_ssf.getNonStationaryDim();
    }

    /**
     * 
     * @return
     */
    public S getSsf()
    {
	return m_ssf;
    }

    /**
     * 
     * @return
     */
    public int getStateDim()
    {
	return m_ssf.getStateDim();
    }

    /**
     * 
     * @return
     */
    public int getTransitionResCount()
    {
	return m_ssf.getTransitionResCount();
    }

    /**
     * 
     * @return
     */
    public int getTransitionResDim()
    {
	return m_ssf.getTransitionResDim();
    }

    /**
     * 
     * @return
     */
    public boolean hasR()
    {
	return m_ssf.hasR();
    }

    /**
     * 
     * @param pos
     * @return
     */
    public boolean hasTransitionRes(int pos)
    {
	return m_ssf.hasTransitionRes(pos);
    }

    /**
     * 
     * @return
     */
    public boolean hasW()
    {
	return m_ssf.hasW();
    }

    /**
     * 
     * @return
     */
    public boolean isDiffuse()
    {
	return m_ssf.isDiffuse();
    }

    /**
     * 
     * @return
     */
    public boolean isMeasurementEquationTimeInvariant()
    {
	return false;
    }

    /**
     * 
     * @return
     */
    public boolean isTimeInvariant()
    {
	return false;
    }

    /**
     * 
     * @return
     */
    public boolean isTransitionEquationTimeInvariant()
    {
	return m_ssf.isTransitionEquationTimeInvariant();
    }

    /**
     * 
     * @return
     */
    public boolean isTransitionResidualTimeInvariant()
    {
	return m_ssf.isTransitionResidualTimeInvariant();
    }

    /**
     * 
     * @return
     */
    public boolean isValid()
    {
	return m_e != null && m_ssf.isValid();
    }

    /**
     * 
     * @param pos
     * @param k
     * @param lm
     */
    public void L(int pos, DataBlock k, SubMatrix lm)
    {
	m_ssf.T(pos, lm);
	double e = m_e[pos];
	DataBlockIterator rows = lm.rows();
	DataBlock row = rows.getData();
	do
	    m_ssf.XpZd(pos, row, k.get(rows.getPosition()) * e);
	while (rows.next());
    }

    /**
     * 
     * @param pf0
     */
    public void Pf0(SubMatrix pf0)
    {
	m_ssf.Pf0(pf0);
    }

    /**
     * 
     * @param pf0
     */
    public void Pi0(SubMatrix pf0)
    {
	m_ssf.Pi0(pf0);
    }

    /**
     * 
     * @param pos
     * @param qm
     */
    public void Q(int pos, SubMatrix qm)
    {
	m_ssf.Q(pos, qm);
    }

    /**
     * 
     * @param pos
     * @param rv
     */
    public void R(int pos, SubArrayOfInt rv)
    {
	m_ssf.R(pos, rv);
    }

    /**
     * 
     * @param pos
     * @param tr
     */
    public void T(int pos, SubMatrix tr)
    {
	m_ssf.T(pos, tr);
    }

    /**
     * 
     * @param pos
     * @param vm
     */
    public void TVT(int pos, SubMatrix vm)
    {
	m_ssf.TVT(pos, vm);
    }

    /**
     * 
     * @param pos
     * @param x
     */
    public void TX(int pos, DataBlock x)
    {
	m_ssf.TX(pos, x);
    }

    /**
     * 
     * @param pos
     * @param vm
     * @param d
     */
    public void VpZdZ(int pos, SubMatrix vm, double d)
    {
	m_ssf.VpZdZ(pos, vm, d * m_e[pos] * m_e[pos]);
    }

    /**
     * 
     * @param pos
     * @param wv
     */
    public void W(int pos, SubMatrix wv)
    {
	m_ssf.W(pos, wv);
    }

    /**
     * 
     * @param pos
     * @param x
     * @param d
     */
    public void XpZd(int pos, DataBlock x, double d)
    {
	m_ssf.XpZd(pos, x, d * m_e[pos]);
    }

    /**
     * 
     * @param pos
     * @param x
     */
    public void XT(int pos, DataBlock x)
    {
	m_ssf.XT(pos, x);
    }

    /**
     * 
     * @param pos
     * @param x
     */
    public void Z(int pos, DataBlock x)
    {
	m_ssf.Z(pos, x);
	x.mul(m_e[pos]);
    }

    /**
     * 
     * @param pos
     * @param m
     * @param x
     */
    public void ZM(int pos, SubMatrix m, DataBlock x)
    {
	m_ssf.ZM(pos, m, x);
	x.mul(m_e[pos]);
    }

    /**
     * 
     * @param pos
     * @param vm
     * @return
     */
    public double ZVZ(int pos, SubMatrix vm)
    {
	return m_ssf.ZVZ(pos, vm) * m_e[pos] * m_e[pos];
    }

    /**
     * 
     * @param pos
     * @param x
     * @return
     */
    public double ZX(int pos, DataBlock x)
    {
	return m_e[pos] * m_ssf.ZX(pos, x);
    }

}
