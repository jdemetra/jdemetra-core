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

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.ssf.ISsf;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public abstract class DefaultMultivariateSsf extends AbstractMultivariateSsf {

    int[] m_R;
    Matrix m_Z;
    DataBlock m_tmp;
    Matrix m_T, m_W;
    Matrix m_Pf0, m_B0, m_Q;

    /**
     *
     */
    public DefaultMultivariateSsf() {
    }

    /**
     *
     * @param b
     */
    @Override
    public void diffuseConstraints(final SubMatrix b) {
        if (m_B0 != null) {
            b.copy(m_B0.subMatrix());
        }
    }

    /**
     *
     * @param pos
     * @param qm
     */
    @Override
    public void fullQ(final int pos, final SubMatrix qm) {
        if (!loadQ(pos, m_Q)) {
            return;
        }
        Matrix WQW;
        if (hasW()) {
            if (!loadW(pos, m_W)) {
                return;
            } else {
                WQW = SymmetricMatrix.quadraticFormT(m_Q, m_W);
            }
        } else {
            WQW = m_Q;
        }
        if (hasR()) {
            if (loadR(pos, m_R)) {
                int nr = m_R.length;
                for (int i = 0; i < nr; ++i) {
                    for (int j = 0; j <= i; ++j) {
                        qm.set(m_R[i], m_R[j], WQW.get(i, j));
                    }
                }
            }
        } else {
            qm.copy(WQW.subMatrix());
        }
    }

    @Override
    public int getVarsCount() {
        return m_Z == null ? 0 : m_Z.getRowsCount();
    }

    /**
     *
     * @return
     */
    @Override
    public int getNonStationaryDim() {
        return m_B0 == null ? 0 : m_B0.getColumnsCount();
    }

    /**
     *
     * @return
     */
    @Override
    public int getStateDim() {
        return m_Z == null ? 0 : m_Z.getColumnsCount();
    }

    /**
     *
     * @return
     */
    @Override
    public int getTransitionResCount() {
        return m_R == null ? getStateDim() : m_R.length;
    }

    /**
     *
     * @return
     */
    @Override
    public int getTransitionResDim() {
        return m_Q == null ? 0 : m_Q.getRowsCount();
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasR() {
        return m_R != null;
    }

    /**
     *
     * @param pos
     * @return
     */
    @Override
    public boolean hasTransitionRes(final int pos) {
        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public abstract boolean hasW();

    /**
     *
     * @param dim
     * @param rescount
     * @param resdim
     */
    public void initialize(final int dim, final int vardim, final int rescount, final int resdim) {
        m_T = new Matrix(dim, dim);
        m_Z = new Matrix(vardim, dim);
        m_tmp = new DataBlock(dim);
        m_Q = new Matrix(resdim, resdim);
        if (rescount != dim) {
            m_R = new int[rescount];
        }
        if (rescount != resdim) {
            m_W = new Matrix(rescount, resdim);
        }
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isDiffuse() {
        return m_B0 != null;
    }

    /**
     *
     * @return
     */
    @Override
    public abstract boolean isMeasurementEquationTimeInvariant();

    /**
     *
     * @return
     */
    @Override
    public abstract boolean isTimeInvariant();

    /**
     *
     * @return
     */
    @Override
    public abstract boolean isTransitionEquationTimeInvariant();

    /**
     *
     * @return
     */
    @Override
    public abstract boolean isTransitionResidualTimeInvariant();

    /**
     *
     * @return
     */
    @Override
    public boolean isValid() {

        if (m_Z == null || m_T == null || m_Q == null) {
            return false;
        }
        int r = m_Z.getColumnsCount();
        if (r != m_T.getColumnsCount() || r != m_T.getRowsCount()) {
            return false;
        }
        if (m_R != null) {
            if (m_W == null || m_R.length != m_W.getRowsCount()) {
                return false;
            }
        }
        if (m_W != null && m_W.getColumnsCount() != m_Q.getRowsCount()) {
            return false;
        }
        if (m_Pf0 != null && m_Pf0.getRowsCount() != r) {
            return false;
        }
        if (m_B0 != null
                && (m_B0.getRowsCount() != r || m_B0.getColumnsCount() > m_B0
                .getRowsCount())) {
            return false;
        }
        return true;

    }

    /**
     * L = T - K * Z
     *
     * @param pos
     * @param k
     * @param lm
     */
    @Override
    public void L(final int pos, final SubMatrix k, final SubMatrix lm) {
        if (!loadT(pos, m_T) || !loadZ(pos, m_Z)) {
            return;
        }
        T(pos, lm);
        DataBlockIterator cols = lm.columns();
        DataBlock col = cols.getData();
        DataBlockIterator kcols = k.columns();
        DataBlock kcol = kcols.getData();
        do {
            kcols.begin();
            do {
                double z = -m_Z.get(kcols.getPosition(), cols.getPosition());
                col.addAY(z, kcol);

            } while (kcols.next());
        } while (cols.next());
    }

    /**
     *
     * @param pos
     * @param q
     * @return
     */
    protected abstract boolean loadQ(int pos, Matrix q);

    /**
     *
     * @param pos
     * @param r
     * @return
     */
    protected abstract boolean loadR(int pos, int[] r);

    /**
     *
     * @param pos
     * @param t
     * @return
     */
    protected abstract boolean loadT(int pos, Matrix t);

    /**
     *
     * @param pos
     * @param w
     * @return
     */
    protected abstract boolean loadW(int pos, Matrix w);

    /**
     *
     * @param pos
     * @param z
     * @return
     */
    protected abstract boolean loadZ(int pos, Matrix z);

    /**
     *
     * @param pf0
     */
    @Override
    public void Pf0(final SubMatrix pf0) {
        if (m_Pf0 != null) {
            pf0.copy(m_Pf0.subMatrix());
        }
    }

    /**
     *
     * @param pf0
     */
    @Override
    public void Pi0(final SubMatrix pf0) {
        if (m_B0 != null) {
            pf0.copy(SymmetricMatrix.XXt(m_B0).subMatrix());
        }
    }

    /**
     *
     * @param pos
     * @param qm
     */
    @Override
    public void Q(final int pos, final SubMatrix qm) {
        if (!loadQ(pos, m_Q)) {
            return;
        }
        qm.copy(m_Q.subMatrix());
    }

    /**
     *
     * @param pos
     * @param rv
     */
    @Override
    public void R(final int pos, final SubArrayOfInt rv) {
        if (!loadR(pos, m_R)) {
            return;
        }
        rv.copy(SubArrayOfInt.create(m_R));
    }

    /**
     *
     * @param B0
     */
    public void setB0(final Matrix B0) {
        m_B0 = B0;
    }

    // Initialisation
    /**
     *
     * @param pf0
     */
    public void setPf0(final Matrix pf0) {
        m_Pf0 = pf0;
    }

    /**
     *
     * @param pos
     * @param tr
     */
    @Override
    public void T(final int pos, final SubMatrix tr) {
        if (!loadT(pos, m_T)) {
            return;
        }
        tr.copy(m_T.subMatrix());
    }

    /**
     *
     * @param pos
     * @param vm
     */
    @Override
    public void TVT(final int pos, final SubMatrix vm) {
        if (!loadT(pos, m_T)) {
            return;
        }
        vm.copy(SymmetricMatrix.quadraticFormT(vm, m_T.subMatrix())
                .subMatrix());
    }

    /**
     *
     * @param pos
     * @param x
     */
    @Override
    public void TX(final int pos, final DataBlock x) {
        if (!loadT(pos, m_T)) {
            return;
        }
        m_tmp.product(m_T.subMatrix().rows(), x);
        x.copy(m_tmp);
    }

    /**
     *
     * @param pos
     * @param vm
     * @param d
     */
    @Override
    public void VpZdZ(final int pos, int v, int w, final SubMatrix vm, final double d) {
        if (!loadZ(pos, m_Z)) {
            return;
        }
        int n = m_Z.getColumnsCount();
        for (int r = 0; r < n; ++r) {
            double zr = m_Z.get(v, r);
            if (zr != 0) {
                zr *= d;
                for (int c = 0; c <= r; ++c) {
                    double zc = m_Z.get(w, c);
                    if (zc != 0) {
                        double z = zr * zc;
                        vm.add(r, c, z);
                        if (r != c) {
                            vm.add(c, r, z);
                        }
                    }

                }
            }
        }
    }

    /**
     *
     * @param pos
     * @param wv
     */
    @Override
    public void W(final int pos, final SubMatrix wv) {
        if (!loadW(pos, m_W)) {
            return;
        }
        wv.copy(m_W.subMatrix());
    }

    /**
     *
     * @param pos
     * @param x
     * @param d
     */
    @Override
    public void XpZd(final int pos, final int v, final DataBlock x, final double d) {
        if (!loadZ(pos, m_Z)) {
            return;
        }
        x.addAY(d, m_Z.row(v));
    }

    /**
     *
     * @param pos
     * @param x
     */
    @Override
    public void XT(final int pos, final DataBlock x) {
        if (!loadT(pos, m_T)) {
            return;
        }
        m_tmp.product(x, m_T.subMatrix().columns());
        x.copy(m_tmp);
    }

    /**
     *
     * @param pos
     * @param x
     */
    @Override
    public void Z(final int pos, final SubMatrix x) {
        if (!loadZ(pos, m_Z)) {
            return;
        }
        x.copy(m_Z.subMatrix());
    }

    /**
     *
     * @param pos
     * @param m
     * @param x
     */
    @Override
    public void ZM(final int pos, final SubMatrix m, final SubMatrix zm) {
        if (!loadZ(pos, m_Z)) {
            return;
        }
        zm.product(m_Z.subMatrix(), m);
    }

    /**
     *
     * @param pos
     * @param m
     * @param x
     */
    @Override
    public void ZM(final int pos, final int v, final SubMatrix m, final DataBlock zm) {
        if (!loadZ(pos, m_Z)) {
            return;
        }
        zm.product(m_Z.row(v), m.columns());
    }
    /**
     *
     * @param pos
     * @param vm
     * @return
     */
    @Override
    public void ZVZ(final int pos, final SubMatrix vm, final SubMatrix zvz) {
        if (!loadZ(pos, m_Z)) {
            return;
        }
        SymmetricMatrix.quadraticFormT(vm, m_Z.subMatrix(), zvz);
    }

    /**
     *
     * @param pos
     * @param x
     * @return
     */
    @Override
    public void ZX(final int pos, final DataBlock x, final DataBlock zx) {
        if (!loadZ(pos, m_Z)) {
            return;
        }
        zx.product(m_Z.rows(), x);
    }

    @Override
    public double ZX(final int pos, final int v, final DataBlock x) {
        if (!loadZ(pos, m_Z)) {
            return 0;
        }
        return m_Z.row(v).dot(x);
    }

    @Override
    public void Z(int pos, int v, DataBlock z) {
        if (!loadZ(pos, m_Z)) {
            return;
        }
        z.copy(m_Z.row(v));
    }

    @Override
    public double ZVZ(int pos, int v, int w, SubMatrix vm) {
        if (!loadZ(pos, m_Z)) {
            return 0;
        }
        this.ZM(pos, v, vm, m_tmp);
        return m_tmp.dot(m_Z.row(w));
    }
    
}
