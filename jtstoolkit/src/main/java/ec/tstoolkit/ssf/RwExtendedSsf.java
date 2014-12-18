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
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;

/**
 * The model extends a given state space form by means of regression variables whose
 * coefficients follow a randow walk.
 * The model can be used for instance to test seasonality (Canova-Hansen test)
 * or to compute time varying calendar effects.
 * @author Jean palate
 */
public class RwExtendedSsf implements ISsf {

    private final int mr_, r_;
    private final SubMatrix X_;
    private final double[] cvar_;
    private ISsf ssf_;
    private DataBlock tmp_;

    /**
     *
     * @param ssf
     * @param X
     */
    public RwExtendedSsf(final ISsf ssf, final SubMatrix X, final DataBlock var) {
        ssf_ = ssf;
        X_ = X;
        mr_ = ssf.getStateDim();
        r_ = mr_ + X.getColumnsCount();
        tmp_ = new DataBlock(r_);
        cvar_ = new double[X_.getColumnsCount()];
        var.copyTo(cvar_, 0);
    }

    public RwExtendedSsf(final ISsf ssf, final SubMatrix X, final double var) {
        ssf_ = ssf;
        X_ = X;
        mr_ = ssf.getStateDim();
        r_ = mr_ + X.getColumnsCount();
        tmp_ = new DataBlock(r_);
        cvar_ = new double[X.getColumnsCount()];
        for (int i=0; i<cvar_.length; ++i){
            cvar_[i]=var;
        }
    }

    /**
     *
     * @param b
     */
    @Override
    public void diffuseConstraints(final SubMatrix b) {
        int nd = ssf_.getNonStationaryDim();
        if (nd > 0) {
            ssf_.diffuseConstraints(b.extract(0, mr_, 0, nd));
        }
        b.extract(mr_, r_, nd, nd + X_.getColumnsCount()).diagonal().set(1);
    }

    /**
     *
     * @param pos
     * @param qm
     */
    @Override
    public void fullQ(final int pos, final SubMatrix qm) {
        ssf_.fullQ(pos, qm.extract(0, mr_, 0, mr_));
        if (cvar_.length == 1) {
            qm.diagonal().drop(mr_, 0).set(cvar_[0]);
        }
        else {
            qm.diagonal().drop(mr_, 0).copyFrom(cvar_, 0);
        }
    }

    /**
     *
     * @return
     */
    public int getFinalPosition() {
        return X_.getRowsCount();
    }

    /**
     *
     * @return
     */
    @Override
    public int getNonStationaryDim() {
        return ssf_.getNonStationaryDim() + X_.getColumnsCount();
    }

    /**
     *
     * @return
     */
    @Override
    public int getStateDim() {
        return r_;
    }

    /**
     *
     * @return
     */
    @Override
    public int getTransitionResCount() {
        return ssf_.getTransitionResCount() + X_.getColumnsCount();
    }

    /**
     *
     * @return
     */
    @Override
    public int getTransitionResDim() {
        return ssf_.getTransitionResDim() + cvar_.length;
    }

    /**
     *
     * @return
     */
    public SubMatrix getX() {
        return X_;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasR() {
        return ssf_.hasR();
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
    public boolean hasW() {
        return ssf_.hasW() || cvar_.length != X_.getColumnsCount();
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
        return ssf_.isTransitionEquationTimeInvariant();
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isTransitionResidualTimeInvariant() {
        return ssf_.isTransitionResidualTimeInvariant();
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isValid() {
        return ssf_.isValid();
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
        tmp_.set(0);
        Z(pos, tmp_);
        DataBlockIterator cols = lm.columns();
        DataBlock col = cols.getData();
        do {
            double w = tmp_.get(cols.getPosition());
            if (w != 0) {
                col.addAY(-w, k);
            }
        }
        while (cols.next());
    }

    /**
     *
     * @param pf0
     */
    @Override
    public void Pf0(final SubMatrix pf0) {
        ssf_.Pf0(pf0.extract(0, mr_, 0, mr_));
    }

    /**
     *
     * @param pi0
     */
    @Override
    public void Pi0(final SubMatrix pi0) {
        ssf_.Pi0(pi0.extract(0, mr_, 0, mr_));
        pi0.extract(mr_, r_, mr_, r_).diagonal().set(1);
    }

    /**
     *
     * @param pos
     * @param qm
     */
    @Override
    public void Q(final int pos, final SubMatrix qm) {
        int n = ssf_.getTransitionResDim();
        ssf_.Q(pos, qm.extract(0, n, 0, n));
        qm.diagonal().drop(n, 0).copyFrom(cvar_, 0);
    }

    /**
     *
     * @param pos
     * @param rv
     */
    @Override
    public void R(final int pos, final SubArrayOfInt rv) {
        int n = ssf_.getTransitionResCount();
        ssf_.R(pos, rv);
        ssf_.R(pos, rv.range(0, n));
        // fill the next items...
        for (int i = 0; i < X_.getColumnsCount(); ++i) {
            rv.set(n + i, mr_ + i);
        }
    }

    /**
     *
     * @param pos
     * @param tr
     */
    @Override
    public void T(final int pos, final SubMatrix tr) {
        ssf_.T(pos, tr.extract(0, mr_, 0, mr_));
        tr.extract(mr_, r_, mr_, r_).diagonal().set(1);
    }

    /**
     *
     * @param pos
     * @param vm
     */
    @Override
    public void TVT(final int pos, final SubMatrix vm) {
        ssf_.TVT(pos, vm.extract(0, mr_, 0, mr_));
        SubMatrix v01 = vm.extract(0, mr_, mr_, r_);
        SubMatrix v10 = vm.extract(mr_, r_, 0, mr_);
        DataBlockIterator cols = v01.columns(), rows = v10.rows();
        DataBlock col = cols.getData(), row = rows.getData();
        do {
            ssf_.TX(pos, col);
            row.copy(col);
        }
        while (cols.next() && rows.next());
    }

    /**
     *
     * @param pos
     * @param x
     */
    @Override
    public void TX(final int pos, final DataBlock x) {
        ssf_.TX(pos, x.range(0, mr_));
    }

    /**
     *
     * @param pos
     * @param vm
     * @param d
     */
    @Override
    public void VpZdZ(final int pos, final SubMatrix vm, final double d) {
        tmp_.set(0);
        Z(pos, tmp_);
        DataBlockIterator cols = vm.columns();
        DataBlock col = cols.getData();
        do {
            double w = d * tmp_.get(cols.getPosition());
            if (w != 0) {
                col.addAY(w, tmp_);
            }
        }
        while (cols.next());

    }

    /**
     *
     * @param pos
     * @param wv
     */
    @Override
    public void W(final int pos, final SubMatrix wv) {
        int nr = ssf_.getTransitionResCount(), dr = ssf_.getTransitionResDim();
        ssf_.W(pos, wv.extract(0, nr, 0, dr));
        if (cvar_.length == 1) {
            wv.column(dr).drop(nr, 0).set(1);
        }
        else {
            for (int i = 0; i < X_.getColumnsCount(); ++i) {
                wv.set(nr + i, dr + i, 1);
            }
        }
    }

    /**
     *
     * @param pos
     * @param x
     * @param d
     */
    @Override
    public void XpZd(final int pos, final DataBlock x, final double d) {
        DataBlock xm = x.range(0, mr_);
        ssf_.XpZd(pos, xm, d);
        DataBlock xx = x.range(mr_, r_);
        DataBlock X = X_.row(pos);
        xx.addAY(d, X);
    }

    /**
     *
     * @param pos
     * @param x
     */
    @Override
    public void XT(final int pos, final DataBlock x) {
        ssf_.XT(pos, x.range(0, mr_));
    }

    /**
     *
     * @param pos
     * @param x
     */
    @Override
    public void Z(final int pos, final DataBlock x) {
        ssf_.Z(pos, x.range(0, mr_));
        x.range(mr_, r_).copy(X_.row(pos));
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
        double v00 = ssf_.ZVZ(pos, vm.extract(0, mr_, 0, mr_));
        double v11 = SymmetricMatrix.quadraticForm(vm.extract(mr_, r_, mr_,
                r_), X_.row(pos));
        DataBlock tmp = tmp_.range(mr_, r_);
        ssf_.ZM(pos, vm.extract(0, mr_, mr_, r_), tmp);
        double v01 = tmp.dot(X_.row(pos));
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
        return ssf_.ZX(pos, x.range(0, mr_))
                + x.range(mr_, r_).dot(X_.row(pos));
    }
}
