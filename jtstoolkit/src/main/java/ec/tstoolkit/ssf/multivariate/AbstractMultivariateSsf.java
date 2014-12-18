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
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public abstract class AbstractMultivariateSsf implements IMultivariateSsf {

    @Override
    public void L(int pos, SubMatrix k, SubMatrix lm) {
        T(pos, lm);
        DataBlockIterator rows = lm.rows();
        DataBlock row = rows.getData();
        DataBlockIterator krows = k.rows();
        DataBlock krow = krows.getData();
        do {
            for (int v = 0; v < getVarsCount(); ++v) {
                double c = krow.get(v);
                if (c != 0) {
                    XpZd(pos, v, row, -c);
                }
            }
        } while (rows.next());
    }

    /**
     *
     * @param pos
     * @param M
     */
    @Override
    public void MT(final int pos, final SubMatrix M) {
        DataBlockIterator rm = M.rows();
        DataBlock r = rm.getData();
        do {
            XT(pos, r);
        } while (rm.next());
    }

    /**
     *
     * @param pos
     * @param M
     */
    @Override
    public void TM(final int pos, final SubMatrix M) {
        DataBlockIterator cm = M.columns();
        DataBlock c = cm.getData();
        do {
            TX(pos, c);
        } while (cm.next());
    }

    /**
     *
     * @param pos
     * @param vm
     */
    @Override
    public void TVT(final int pos, final SubMatrix vm) {
        DataBlockIterator cols = vm.columns();
        DataBlock col = cols.getData();
        do {
            TX(pos, col);
        } while (cols.next());

        DataBlockIterator rows = vm.rows();
        DataBlock row = rows.getData();
        do {
            TX(pos, row);
        } while (rows.next());
    }

    /**
     *
     * @param pos
     * @param zm
     */
    @Override
    public void Z(final int pos, final SubMatrix zm) {
        DataBlockIterator rows = zm.rows();
        DataBlock row = rows.getData();
        do {
            Z(pos, rows.getPosition(), row);
        } while (rows.next());
    }

    /**
     *
     * @param pos
     * @param v
     * @param m
     * @param x
     */
    @Override
    public void ZM(final int pos, final int v, final SubMatrix m,
            final DataBlock x) {
        DataBlockIterator cols = m.columns();
        DataBlock col = cols.getData();
        do {
            x.set(cols.getPosition(), ZX(pos, v, col));
        } while (cols.next());
    }

    /**
     *
     * @param pos
     * @param m
     * @param zm
     */
    @Override
    public void ZM(final int pos, final SubMatrix m, final SubMatrix zm) {
        DataBlockIterator rzm = zm.rows();
        DataBlock r = rzm.getData();
        do {
            ZM(pos, rzm.getPosition(), m, r);
        } while (rzm.next());
    }

    /**
     *
     * @param pos
     * @param v
     * @param zvz
     */
    @Override
    public void ZVZ(final int pos, final SubMatrix v, final SubMatrix zvz) {
        int n = getVarsCount();
        for (int i = 0; i < n; ++i) {
            zvz.set(i, i, ZVZ(pos, i, i, v));
            for (int j = 0; j < i; ++j) {
                zvz.set(i, j, ZVZ(pos, i, j, v));
            }
        }
        SymmetricMatrix.fromLower(zvz);
    }

    @Override
    public void ZX(final int pos, DataBlock x, DataBlock zx) {
        int n = getVarsCount();
        for (int i = 0; i < n; ++i) {
            zx.set(i, ZX(pos, i, x));
        }
    }

    @Override
    public boolean hasZ(int pos, int v) {
        return true;
    }

    @Override
    public int getTransitionResCount() {
        return getStateDim();
    }

    @Override
    public int getTransitionResDim() {
        return getStateDim();
    }

    @Override
    public boolean hasR() {
        return false;
    }

    @Override
    public boolean hasTransitionRes(int pos) {
        return true;
    }

    @Override
    public boolean hasW() {
        return false;
    }

    @Override
    public void Q(int pos, SubMatrix qm) {
        fullQ(pos, qm);
    }

    @Override
    public void R(int pos, SubArrayOfInt rv) {
    }

    @Override
    public void W(int pos, SubMatrix wv) {
    }
}
