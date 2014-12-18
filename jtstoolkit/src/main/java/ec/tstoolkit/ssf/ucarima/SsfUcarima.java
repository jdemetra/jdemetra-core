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
package ec.tstoolkit.ssf.ucarima;

import ec.tstoolkit.arima.AutoCovarianceFunction;
import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.polynomials.RationalFunction;
import ec.tstoolkit.ssf.ISsf;
import ec.tstoolkit.ssf.arima.SsfArima;
import ec.tstoolkit.ucarima.UcarimaModel;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SsfUcarima implements ISsf
{

    private UcarimaModel m_ucm;

    private int m_ncmps, m_dim;

    private double[][] m_dif, m_phi, m_psi, m_stacgf, m_stpsi;

    private double[] m_var, m_rtmp, m_ctmp;

    private int[] m_r;

    private DataBlock[] m_Phi;

    /**
     * 
     */
    public SsfUcarima()
    {
    }

    /**
     * 
     * @param ucm
     */
    public SsfUcarima(final UcarimaModel ucm)
    {
	m_ucm = ucm;
	initModel();
    }

    private void _tvt(final int r, final int c, final SubMatrix v) {
	int nr = m_r[r], nc = m_r[c];
	double[] rphi = m_phi[r], cphi = m_phi[c];
	DataBlock rtmp = new DataBlock(m_rtmp, 0, nc, 1);
	DataBlock ctmp = new DataBlock(m_ctmp, 0, nr, 1);
	rtmp.set(0);
	ctmp.set(0);
	DataBlockIterator cols = v.columns();
	DataBlock col = cols.getData();
	cols.end();
	for (int p = 1; p < cphi.length; ++p) {
	    ctmp.addAY(-cphi[p], col);
	    cols.previous();
	}

	DataBlockIterator rows = v.rows();
	DataBlock row = rows.getData();
	rows.end();
	for (int p = 1; p < rphi.length; ++p) {
	    rtmp.addAY(-rphi[p], row);
	    rows.previous();
	}

	double tlast = -m_Phi[c].dotReverse(rtmp);

	v.shift(-1);
	rtmp.bshift(DataBlock.ShiftOption.None);
	rtmp.set(nc - 1, tlast);
	ctmp.bshift(DataBlock.ShiftOption.None);
	ctmp.set(nr - 1, tlast);
	v.column(nc - 1).copy(ctmp);
	v.row(nr - 1).copy(rtmp);

    }

    private void _tvt(final int cmp, final SubMatrix v) {
	int n = m_r[cmp];
	double[] phi = m_phi[cmp];
	DataBlock tmp = new DataBlock(m_rtmp, 0, n, 1);
	tmp.set(0);
	DataBlockIterator cols = v.columns();
	DataBlock col = cols.getData();
	cols.end();
	for (int p = 1; p < phi.length; ++p) {
	    tmp.addAY(-phi[p], col);
	    cols.previous();
	}

	double tlast = -m_Phi[cmp].dotReverse(tmp);

	v.shift(-1);
	tmp.bshift(DataBlock.ShiftOption.None);
	tmp.set(n - 1, tlast);
	v.column(n - 1).copy(tmp);
	v.row(n - 1).copy(tmp);
    }

    /**
     * 
     * @param cmp
     * @return
     */
    public int cmpPos(final int cmp)
    {
	if (cmp >= m_ucm.getComponentsCount()
		|| m_ucm.getComponent(cmp).isNull())
	    return -1;
	int pos = 0;
	for (int i = 0, j = 0; i < cmp; ++i)
	    if (!m_ucm.getComponent(i).isNull())
		pos += m_r[j++];
	return pos;
    }

    /**
     * 
     * @param b
     */
    @Override
    public void diffuseConstraints(final SubMatrix b)
    {
	for (int c = 0, s = 0, t = 0; c < m_ncmps; ++c) {
	    int dim = m_r[c], d = m_dif[c].length - 1;
	    if (d > 0) {
		SsfArima.B0(b.extract(s, s + dim, t, t + d), m_dif[c]);
		t += d;
	    }
	    s += dim;
	}
    }

    private int DTot() {
	int nd = 0;
	for (int i = 0; i < m_ncmps; ++i)
	    nd += m_dif[i].length - 1;
	return nd;
    }

    /**
     * 
     * @param pos
     * @param qm
     */
    @Override
    public void fullQ(final int pos, final SubMatrix qm)
    {
	for (int cmp = 0, s = 0; cmp < m_ncmps; ++cmp) {
	    int dim = m_r[cmp];
	    for (int i = 0; i < dim; ++i)
		for (int j = 0; j <= i; ++j)
		    qm.set(s + i, s + j, m_psi[cmp][i] * m_psi[cmp][j]
			    * m_var[cmp]);
	    s += dim;
	}
	SymmetricMatrix.fromLower(qm);
    }

    /**
     * 
     * @return
     */
    @Override
    public int getNonStationaryDim()
    {
	return DTot();
    }

    /**
     * 
     * @return
     */
    @Override
    public int getStateDim()
    {
	return m_dim;
    }

    /**
     * 
     * @return
     */
    @Override
    public int getTransitionResCount()
    {
	return m_dim;
    }

    /**
     * 
     * @return
     */
    @Override
    public int getTransitionResDim()
    {
	return m_ncmps;
    }

    /**
     * 
     * @return
     */
    public UcarimaModel getUCModel()
    {
	return m_ucm;
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
    @Override
    public boolean hasR()
    {
	return false;
    }

    /**
     * 
     * @param pos
     * @return
     */
    @Override
    public boolean hasTransitionRes(final int pos)
    {
	return true;
    }

    /**
     * 
     * @return
     */
    @Override
    public boolean hasW()
    {
	return true;
    }

    private void initModel() {
	m_ncmps = 0;
	m_dim = 0;
	for (int i = 0; i < m_ucm.getComponentsCount(); ++i)
	    if (!m_ucm.getComponent(i).isNull())
		++m_ncmps;
	m_dif = new double[m_ncmps][];
	m_phi = new double[m_ncmps][];
	m_Phi = new DataBlock[m_ncmps];
	m_psi = new double[m_ncmps][];
	m_r = new int[m_ncmps];
	m_stacgf = new double[m_ncmps][];
	m_stpsi = new double[m_ncmps][];
	m_var = new double[m_ncmps];
	for (int i = 0, j = 0; i < m_ucm.getComponentsCount(); ++i)
	    if (!m_ucm.getComponent(i).isNull())
		initModel(j++, i);
	m_rtmp = new double[m_dim];
	m_ctmp = new double[m_dim];
    }

    private void initModel(final int i, final int cmp) {
	IArimaModel model = m_ucm.getComponent(cmp);
	m_var[i] = model.getInnovationVariance();
	m_dif[i] = model.getNonStationaryAR().getCoefficients();
        Polynomial phi = model.getAR().getPolynomial();
	m_phi[i] = phi.getCoefficients();
	m_Phi[i] = new DataBlock(m_phi[i], 1, m_phi[i].length, 1);
	Polynomial theta = model.getMA().getPolynomial();
	m_r[i] = Math.max(phi.getDegree(), theta.getDegree() + 1);
	m_dim += m_r[i];
	m_psi[i] = new RationalFunction(theta, phi).coefficients(m_r[i]);

	Polynomial stphi = model.getStationaryAR().getPolynomial();
	m_stacgf[i] = new AutoCovarianceFunction(theta, stphi, m_var[i]).values(m_r[i]);
	m_stpsi[i] = new RationalFunction(theta, stphi).coefficients(m_r[i]);
    }

    /**
     * 
     * @return
     */
    @Override
    public boolean isDiffuse()
    {
	return DTot() > 0;
    }

    /**
     * 
     * @return
     */
    @Override
    public boolean isMeasurementEquationTimeInvariant()
    {
	return true;
    }

    /**
     * 
     * @return
     */
    @Override
    public boolean isTimeInvariant()
    {
	return true;
    }

    /**
     * 
     * @return
     */
    @Override
    public boolean isTransitionEquationTimeInvariant()
    {
	return true;
    }

    /**
     * 
     * @return
     */
    @Override
    public boolean isTransitionResidualTimeInvariant()
    {
	return true;
    }

    /**
     * 
     * @return
     */
    @Override
    public boolean isValid()
    {
	return m_ucm != null;
    }

    /**
     * 
     * @param pos
     * @param k
     * @param lm
     */
    @Override
    public void L(final int pos, final DataBlock k, final SubMatrix lm)
    {
	T(pos, lm);
	for (int cmp = 0, c = 0; cmp < m_ncmps; ++cmp) {
	    lm.column(c).sub(k);
	    c += m_r[cmp];
	}
    }

    /**
     * 
     * @param pf0
     */
    @Override
    public void Pf0(final SubMatrix pf0)
    {
	for (int c = 0, s = 0; c < m_ncmps; ++c) {
	    int dim = m_r[c];
	    Matrix stV = new Matrix(dim, dim);
	    SsfArima.stVar(stV.subMatrix(), m_stpsi[c], m_stacgf[c], m_var[c]);
	    Matrix K = new Matrix(dim, dim);
	    SsfArima.Ksi(K.subMatrix(), m_dif[c]);
	    SymmetricMatrix.quadraticFormT(stV.subMatrix(), K.subMatrix(), pf0
		    .extract(s, s + dim, s, s + dim));
	    s += dim;
	}
    }

    /**
     * 
     * @param pi0
     */
    @Override
    public void Pi0(final SubMatrix pi0)
    {
	Matrix B = new Matrix(m_dim, DTot());
	for (int c = 0, s = 0, t = 0; c < m_ncmps; ++c) {
	    int dim = m_r[c], d = m_dif[c].length - 1;
	    if (d > 0) {
		SsfArima.B0(B.subMatrix(s, s + dim, t, t + d), m_dif[c]);
		t += d;
	    }
	    s += dim;
	}
	SymmetricMatrix.XXt(B.subMatrix(), pi0);
    }

    /**
     * 
     * @param pos
     * @param qm
     */
    @Override
    public void Q(final int pos, final SubMatrix qm)
    {
	qm.diagonal().copyFrom(m_var, 0);
    }

    /**
     * 
     * @param pos
     * @param rv
     */
    @Override
    public void R(final int pos, final SubArrayOfInt rv)
    {
    }

    /**
     * 
     * @param pos
     * @param tr
     */
    @Override
    public void T(final int pos, final SubMatrix tr)
    {
	for (int cmp = 0, s = 0; cmp < m_ncmps; ++cmp) {
	    int n = m_r[cmp], sn = s + n;
	    for (int i = 1; i < n; ++i)
		tr.set(s + i - 1, s + i, 1);
	    double[] phi = m_phi[cmp];
	    for (int i = 1; i < phi.length; ++i)
		tr.set(sn - 1, sn - i, -phi[i]);
	    s += n;
	}
    }

    /**
     * 
     * @param pos
     * @param vm
     */
    @Override
    public void TVT(final int pos, final SubMatrix vm)
    {
	for (int r = 0, i = 0; r < m_ncmps; ++r) {
	    int rn = m_r[r];
	    _tvt(r, vm.extract(i, i + rn, i, i + rn));
	    for (int c = 0, j = 0; c < r; ++c) {
		int cn = m_r[c];
		SubMatrix u = vm.extract(i, i + rn, j, j + cn);
		_tvt(r, c, u);
		SubMatrix v = vm.extract(j, j + cn, i, i + rn);
		v.copy(u.transpose());
		j += cn;
	    }
	    i += rn;
	}
    }

    /**
     * 
     * @param pos
     * @param x
     */
    @Override
    public void TX(final int pos, final DataBlock x)
    {
	for (int c = 0, s = 0; c < m_ncmps; ++c) {
	    int n = m_r[c];
	    DataBlock xc = x.range(s, s + n);
	    double last = m_Phi[c].dotReverse(xc);
	    xc.bshift(DataBlock.ShiftOption.None);
	    xc.set(n - 1, -last);
	    s += n;
	}
    }

    /**
     * 
     * @param pos
     * @param vm
     * @param d
     */
    @Override
    public void VpZdZ(final int pos, final SubMatrix vm, final double d)
    {
	for (int rcmp = 0, r = 0; rcmp < m_ncmps; ++rcmp) {
	    for (int ccmp = 0, c = 0; ccmp <= rcmp; ++ccmp) {
		vm.add(r, c, d);
		if (r != c)
		    vm.add(c, r, d);
		c += m_r[ccmp];
	    }
	    r += m_r[rcmp];
	}
    }

    /**
     * 
     * @param pos
     * @param wv
     */
    @Override
    public void W(final int pos, final SubMatrix wv)
    {
	for (int i = 0, j = 0; i < m_ncmps; ++i) {
	    int n = m_r[i];
	    wv.column(i).range(j, j + n).copyFrom(m_psi[i], 0);
	    j += n;
	}
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
	for (int cmp = 0, s = 0; cmp < m_ncmps; ++cmp) {
	    x.add(s, d);
	    s += m_r[cmp];
	}

    }

    /**
     * 
     * @param pos
     * @param x
     */
    @Override
    public void XT(final int pos, final DataBlock x)
    {
	for (int c = 0, s = 0; c < m_ncmps; ++c) {
	    int n = m_r[c];
	    DataBlock xc = x.range(s, s + n);
	    double last = -xc.get(n - 1);
	    xc.fshift(DataBlock.ShiftOption.None);
	    xc.set(0, 0);
	    if (last != 0)
		for (int i = 1; i < m_phi[c].length; ++i)
		    if (m_phi[c][i] != 0)
			xc.add(n - i, last * m_phi[c][i]);
	    s += n;
	}
    }

    /**
     * 
     * @param pos
     * @param x
     */
    @Override
    public void Z(final int pos, final DataBlock x)
    {
	for (int cmp = 0, s = 0; cmp < m_ncmps; ++cmp) {
	    x.set(s, 1);
	    s += m_r[cmp];
	}
    }

    /**
     * 
     * @param pos
     * @param m
     * @param x
     */
    @Override
    public void ZM(final int pos, final SubMatrix m, final DataBlock x)
    {
	x.copy(m.row(0));
	for (int i = 1, c = m_r[0]; i < m_ncmps; ++i) {
	    x.add(m.row(c));
	    c += m_r[i];
	}
    }

    /**
     * 
     * @param pos
     * @param vm
     * @return
     */
    @Override
    public double ZVZ(final int pos, final SubMatrix vm)
    {
	double z = 0;
	for (int i = 0, r = 0; i < m_ncmps; ++i) {
	    z += vm.get(r, r);
	    double w = 0;
	    for (int j = 0, c = 0; j < i; ++j) {
		w += vm.get(r, c);
		c += m_r[j];
	    }
	    z += 2 * w;
	    r += m_r[i];
	}
	return z;
    }

    /**
     * 
     * @param pos
     * @param x
     * @return
     */
    @Override
    public double ZX(final int pos, final DataBlock x)
    {
	double r = 0;
	for (int i = 0, c = 0; i < m_ncmps; ++i) {
	    r += x.get(c);
	    c += m_r[i];
	}
	return r;
    }
}
