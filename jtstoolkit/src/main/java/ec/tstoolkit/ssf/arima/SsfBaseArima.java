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
package ec.tstoolkit.ssf.arima;

import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.ssf.ISsf;
import ec.tstoolkit.ssf.SsfException;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public abstract class SsfBaseArima implements ISsf {

    IArimaModel m_model;

    int m_dim;

    double[] m_phi;

    double[] m_psi;

    double[] m_tmp;

    DataBlock m_Phi;

    /**
     * 
     */
    public SsfBaseArima()
    {
    }

    /**
     * 
     * @param arima
     */
    public SsfBaseArima(final IArimaModel arima)
    {
	m_model = arima;
    }

    /**
     *
     * @param pm
     */
    public abstract void diffuseConstraints(SubMatrix pm);

    /**
     * 
     * @param pos
     * @param qm
     */
    public void fullQ(final int pos, final SubMatrix qm)
    {
	fullQ(qm);
    }

    /**
     * 
     * @param qm
     */
    public void fullQ(final SubMatrix qm)
    {
	double v = m_model.getInnovationVariance();
	for (int i = 0; i < m_dim; ++i)
	    for (int j = 0; j <= i; ++j)
		qm.set(i, j, m_psi[i] * m_psi[j] * v);
	SymmetricMatrix.fromLower(qm);
    }

    /**
     * 
     * @return
     */
    public IArimaModel getModel()
    {
	return m_model;
    }

    /**
     *
     * @return
     */
    public abstract int getNonStationaryDim();

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
     * @return
     */
    public int getTransitionResCount()
    {
	return m_dim;
    }

    /**
     * 
     * @return
     */
    public int getTransitionResDim()
    {
	return 1;
    }

    /**
     * 
     * @param pos
     * @return
     */
    public double H(final int pos)
    {
	return 0;
    }

    /**
     * 
     * @return
     */
    public boolean hasH()
    {
	return false;
    }

    /**
     * 
     * @return
     */
    public boolean hasR()
    {
	return false;
    }

    /**
     * 
     * @param pos
     * @return
     */
    public boolean hasTransitionRes(final int pos)
    {
	return true;
    }

    /**
     * 
     * @return
     */
    public boolean hasW()
    {
	return true;
    }

    /**
     *
     */
    protected abstract void initModel();

    /**
     *
     * @return
     */
    public abstract boolean isDiffuse();

    /**
     * 
     * @return
     */
    public boolean isMeasurementEquationTimeInvariant()
    {
	return true;
    }

    /**
     * 
     * @return
     */
    public boolean isTimeInvariant()
    {
	return true;
    }

    /**
     * 
     * @return
     */
    public boolean isTransitionEquationTimeInvariant()
    {
	return true;
    }

    /**
     * 
     * @return
     */
    public boolean isTransitionResidualTimeInvariant()
    {
	return true;
    }

    /**
     * 
     * @return
     */
    public boolean isValid()
    {
	return m_model != null;
    }

    // / <summary>
    // / L = T - K*Z
    // / </summary>
    // / <param name="pos"></param>
    // / <param name="k"></param>
    // / <param name="lm"></param>
    /**
     * 
     * @param pos
     * @param k
     * @param lm
     */
    public void L(final int pos, final DataBlock k, final SubMatrix lm)
    {
	T(lm);
	DataBlock col = lm.column(0);
	col.sub(k);
    }

    /**
     *
     * @param pm
     */
    public abstract void Pf0(SubMatrix pm);

    /**
     *
     * @param pm
     */
    public abstract void Pi0(SubMatrix pm);

    /**
     * 
     * @param pos
     * @param qm
     */
    public void Q(final int pos, final SubMatrix qm)
    {
	Q(qm);
    }

    /**
     * 
     * @param qm
     */
    public void Q(final SubMatrix qm)
    {
	qm.set(0, 0, m_model.getInnovationVariance());
    }

    /**
     * 
     * @param pos
     * @param rv
     */
    public void R(final int pos, final SubArrayOfInt rv)
    {
    }

    /**
     * 
     * @param value
     */
    public void setModel(final IArimaModel value)
    {
	if (!value.isStationary())
	    throw new SsfException(SsfException.STATIONARY, "Arima ssf", 0);
	m_model = value;
	initModel();
    }

    /**
     * 
     * @param pos
     * @param tr
     */
    public void T(final int pos, final SubMatrix tr)
    {
	T(tr);
    }

    /**
     * 
     * @param tr
     */
    public void T(final SubMatrix tr)
    {
	tr.set(0);
	for (int i = 1; i < m_dim; ++i)
	    tr.set(i - 1, i, 1);
	for (int i = 1; i < m_phi.length; ++i)
	    tr.set(m_dim - 1, m_dim - i, -m_phi[i]);
    }

    /**
     * 
     * @param pos
     * @param vm
     */
    public void TVT(final int pos, final SubMatrix vm)
    {
	DataBlock tmp = new DataBlock(m_tmp);
	tmp.set(0);
	DataBlockIterator cols = vm.columns();
	DataBlock col = cols.getData();
	cols.end();
	for (int p = 1; p < m_phi.length; ++p) {
	    tmp.addAY(-m_phi[p], col);
	    cols.previous();
	}

	double tlast = -m_Phi.dotReverse(tmp);

	vm.shift(-1);
	tmp.bshift(DataBlock.ShiftOption.None);
	m_tmp[m_dim - 1] = tlast;
	vm.column(m_dim - 1).copy(tmp);
	vm.row(m_dim - 1).copy(tmp);
    }

    /**
     * 
     * @param x
     */
    public void TX(final DataBlock x)
    {
	double last = m_Phi.dot(x.reverse());
	x.bshift(DataBlock.ShiftOption.None);
	x.set(m_dim - 1, -last);
    }

    /**
     * 
     * @param pos
     * @param x
     */
    public void TX(final int pos, final DataBlock x)
    {
	double last = m_Phi.dotReverse(x);
	x.bshift(DataBlock.ShiftOption.None);
	x.set(m_dim - 1, -last);
    }

    /**
     * 
     * @param pos
     * @param vm
     * @param d
     */
    public void VpZdZ(final int pos, final SubMatrix vm, final double d)
    {
	vm.add(0, 0, d);
    }

    /**
     * 
     * @param pos
     * @param wv
     */
    public void W(final int pos, final SubMatrix wv)
    {
	DataBlock tmp = new DataBlock(m_psi);
	wv.column(0).copy(tmp);
    }

    /**
     * 
     * @param pos
     * @param x
     * @param d
     */
    public void XpZd(final int pos, final DataBlock x, final double d)
    {
	x.add(0, d);
    }

    /**
     * 
     * @param pos
     * @param x
     */
    public void XT(final int pos, final DataBlock x)
    {
	double last = -x.get(m_dim - 1);
	x.fshift(DataBlock.ShiftOption.None);
	x.set(0, 0);
	if (last != 0)
	    for (int i = 1; i < m_phi.length; ++i)
		if (m_phi[i] != 0)
		    x.add(m_dim - i, last * m_phi[i]);
    }

    /**
     * 
     * @param pos
     * @param x
     */
    public void Z(final int pos, final DataBlock x)
    {
	x.set(0, 1);
    }

    /**
     * 
     * @param pos
     * @param m
     * @param x
     */
    public void ZM(final int pos, final SubMatrix m, final DataBlock x)
    {
	x.copy(m.row(0));
    }

    /**
     * 
     * @param pos
     * @param vm
     * @return
     */
    public double ZVZ(final int pos, final SubMatrix vm)
    {
	return vm.get(0, 0);
    }

    /**
     * 
     * @param x
     * @return
     */
    public double ZX(final DataBlock x)
    {
	return x.get(0);
    }

    /**
     * 
     * @param pos
     * @param x
     * @return
     */
    public double ZX(final int pos, final DataBlock x)
    {
	return x.get(0);
    }

    /**
     * 
     * @param pos
     * @param vX
     * @return
     */
    public double ZX(final int pos, final double[] vX)
    {
	return vX[0];
    }
}
