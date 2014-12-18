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

import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class RegSsf implements ISsf {

    private final int m_mr, m_r;

    private final SubMatrix m_X;

    private ISsf m_ssf;

    private DataBlock m_tmp;

    /**
     * 
     * @param ssf
     * @param X
     */
    public RegSsf(final ISsf ssf, final SubMatrix X)
    {
	m_ssf = ssf;
	m_X = X;
	m_mr = ssf.getStateDim();
	m_r = m_mr + X.getColumnsCount();
	m_tmp = new DataBlock(m_r);
    }

    /**
     * 
     * @param essf
     */
    public RegSsf(final RegSsf essf)
    {
	m_ssf = essf.m_ssf;
	m_X = essf.m_X;
	m_r = essf.m_r;
	m_mr = essf.m_mr;
	m_tmp = new DataBlock(m_r);
    }

    /**
     *
     * @param b
     */
    @Override
    public void diffuseConstraints(final SubMatrix b) {
	int nd = m_ssf.getNonStationaryDim();
	if (nd > 0)
	    m_ssf.diffuseConstraints(b.extract(0, m_mr, 0, nd));
	b.extract(m_mr, m_r, nd, nd + m_X.getColumnsCount()).diagonal().set(1);
    }

    /**
     *
     * @param pos
     * @param qm
     */
    @Override
    public void fullQ(final int pos, final SubMatrix qm) {
	m_ssf.fullQ(pos, qm.extract(0, m_mr, 0, m_mr));
    }

    /**
     * 
     * @return
     */
    public int getFinalPosition()
    {
	return m_X.getRowsCount();
    }

    /**
     *
     * @return
     */
    @Override
    public int getNonStationaryDim() {
	return m_ssf.getNonStationaryDim() + m_X.getColumnsCount();
    }

    /**
     *
     * @return
     */
    @Override
    public int getStateDim() {
	return m_r;
    }

    /**
     *
     * @return
     */
    @Override
    public int getTransitionResCount() {
	return m_ssf.getTransitionResCount();
    }

    /**
     *
     * @return
     */
    @Override
    public int getTransitionResDim() {
	return m_ssf.getTransitionResDim();
    }

    /**
     * 
     * @return
     */
    public SubMatrix getX()
    {
	return m_X;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasR() {
	return true;
    }

    /**
     *
     * @param pos
     * @return
     */
    @Override
    public boolean hasTransitionRes(final int pos) {
	return m_ssf.hasTransitionRes(pos);
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasW() {
	return m_ssf.hasW();
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isDiffuse() {
	return true;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isMeasurementEquationTimeInvariant() {
	return false;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isTimeInvariant() {
	return false;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isTransitionEquationTimeInvariant() {
	return m_ssf.isTransitionEquationTimeInvariant();
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isTransitionResidualTimeInvariant() {
	return m_ssf.isTransitionResidualTimeInvariant();
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isValid() {
	return m_ssf.isValid();
    }

    /**
     *
     * @param pos
     * @param k
     * @param lm
     */
    @Override
    public void L(final int pos, final DataBlock k, final SubMatrix lm) {
	T(pos, lm);
        m_tmp.set(0);
	Z(pos, m_tmp);
	DataBlockIterator cols = lm.columns();
	DataBlock col = cols.getData();
	do {
	    double w = m_tmp.get(cols.getPosition());
	    if (w != 0)
		col.addAY(-w, k);
	} while (cols.next());
    }

    /**
     *
     * @param pf0
     */
    @Override
    public void Pf0(final SubMatrix pf0) {
	m_ssf.Pf0(pf0.extract(0, m_mr, 0, m_mr));
    }

    /**
     *
     * @param pi0
     */
    @Override
    public void Pi0(final SubMatrix pi0) {
	m_ssf.Pi0(pi0.extract(0, m_mr, 0, m_mr));
	pi0.extract(m_mr, m_r, m_mr, m_r).diagonal().set(1);
    }

    /**
     *
     * @param pos
     * @param qm
     */
    @Override
    public void Q(final int pos, final SubMatrix qm) {
	m_ssf.Q(pos, qm);
    }

    /**
     *
     * @param pos
     * @param rv
     */
    @Override
    public void R(final int pos, final SubArrayOfInt rv) {
	if (m_ssf.hasR())
	    m_ssf.R(pos, rv);
	else
	    for (int i = 0; i < m_mr; ++i)
		rv.set(i, i);
    }

    /**
     *
     * @param pos
     * @param tr
     */
    @Override
    public void T(final int pos, final SubMatrix tr) {
	m_ssf.T(pos, tr.extract(0, m_mr, 0, m_mr));
	tr.extract(m_mr, m_r, m_mr, m_r).diagonal().set(1);
    }

    /**
     *
     * @param pos
     * @param vm
     */
    @Override
    public void TVT(final int pos, final SubMatrix vm) {
	m_ssf.TVT(pos, vm.extract(0, m_mr, 0, m_mr));
	SubMatrix v01 = vm.extract(0, m_mr, m_mr, m_r);
	SubMatrix v10 = vm.extract(m_mr, m_r, 0, m_mr);
	DataBlockIterator cols = v01.columns(), rows = v10.rows();
	DataBlock col = cols.getData(), row = rows.getData();
	do {
	    m_ssf.TX(pos, col);
	    row.copy(col);
	} while (cols.next() && rows.next());
    }

    /**
     *
     * @param pos
     * @param x
     */
    @Override
    public void TX(final int pos, final DataBlock x) {
	m_ssf.TX(pos, x.range(0, m_mr));
    }

    /**
     *
     * @param pos
     * @param vm
     * @param d
     */
    @Override
    public void VpZdZ(final int pos, final SubMatrix vm, final double d) {
        m_tmp.set(0);
	Z(pos, m_tmp);
	DataBlockIterator cols = vm.columns();
	DataBlock col = cols.getData();
	do {
	    double w = d * m_tmp.get(cols.getPosition());
	    if (w != 0)
		col.addAY(w, m_tmp);
	} while (cols.next());
    }

    /**
     *
     * @param pos
     * @param wv
     */
    @Override
    public void W(final int pos, final SubMatrix wv) {
	m_ssf.W(pos, wv);
    }

    /**
     *
     * @param pos
     * @param x
     * @param d
     */
    @Override
    public void XpZd(final int pos, final DataBlock x, final double d) {
	DataBlock xm = x.range(0, m_mr);
	m_ssf.XpZd(pos, xm, d);
	DataBlock xx = x.range(m_mr, m_r);
	DataBlock X = m_X.row(pos);
	xx.addAY(d, X);
    }

    /**
     *
     * @param pos
     * @param x
     */
    @Override
    public void XT(final int pos, final DataBlock x) {
	m_ssf.XT(pos, x.range(0, m_mr));
    }

    /**
     *
     * @param pos
     * @param x
     */
    @Override
    public void Z(final int pos, final DataBlock x) {
	m_ssf.Z(pos, x.range(0, m_mr));
	x.range(m_mr, m_r).copy(m_X.row(pos));
    }

    /**
     *
     * @param pos
     * @param m
     * @param x
     */
    @Override
    public void ZM(final int pos, final SubMatrix m, final DataBlock x) {
	DataBlockIterator cols = m.columns();
	DataBlock col = cols.getData();
	for (int i = 0; i < m.getColumnsCount(); ++i) {
	    x.set(i, ZX(pos, col));
	    cols.next();
	}
    }

    /**
     *
     * @param pos
     * @param vm
     * @return
     */
    @Override
    public double ZVZ(final int pos, final SubMatrix vm) {
	double v00 = m_ssf.ZVZ(pos, vm.extract(0, m_mr, 0, m_mr));
	double v11 = SymmetricMatrix.quadraticForm(vm.extract(m_mr, m_r, m_mr,
		m_r), m_X.row(pos));
	DataBlock tmp = m_tmp.range(m_mr, m_r);
	m_ssf.ZM(pos, vm.extract(0, m_mr, m_mr, m_r), tmp);
	double v01 = tmp.dot(m_X.row(pos));
	return v00 + 2 * v01 + v11;
    }

    /**
     *
     * @param pos
     * @param x
     * @return
     */
    @Override
    public double ZX(final int pos, final DataBlock x) {
	return m_ssf.ZX(pos, x.range(0, m_mr))
		+ x.range(m_mr, m_r).dot(m_X.row(pos));
    }
}
