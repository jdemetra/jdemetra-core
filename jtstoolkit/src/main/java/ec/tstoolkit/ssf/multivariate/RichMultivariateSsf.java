/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 will be approved by the European Commission - subsequent
 versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 writing, software distributed under the Licence is
 distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 * See the Licence for the specific language governing
 permissions and limitations under the Licence.
 */
package ec.tstoolkit.ssf.multivariate;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.data.DataBlockStorage;
import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.MatrixStorage;
import ec.tstoolkit.maths.matrices.SubMatrix;

/**
 * This class represents the following model: 
 * Y(t) = Z(t) a(t) + X(t)b + e(t)
 * a(t+1) = T(t)a(t) + k(t+1) + W(t+1)c + u(t+1) 
 * a(-1) = Ad + k(-1) + W(-1)c + u(-1)
 *
 * That model is considered as an extension of the model: 
 * Y(t) = Z(t) a(t)
 * a(t+1) = T(t) a(t) + u(t+1) 
 * a(-1) = Ad + u(-1) 
 * which can be represented by an
 * IMultivariateSsf instance
 *
 * The extension consists in the noise in the measurement equation and in the
 * regression effects in the transition equation and in the measurement
 * equation. Some of the extensions could be empty.
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class RichMultivariateSsf {

    private final IMultivariateSsf ssf_;
    private final MatrixStorage X_, W_;
    private final DataBlockStorage k_;
    private final INoiseProvider e_;
    private final int nv_, ndim_, nx_, nw_, ne_;
    // l(t+1) = T(t) l(t) + k(t), l(-1) = k(-1)
    // yc(t) = Z(t)l(t)
    private DataBlockStorage l_, yc_;
    // V(t+1) = T(t) V(t) + W(t), V(-1) = W(-1)
    // Xc(t) = Z(t)V(t)
    private MatrixStorage V_, Xc_;

    public RichMultivariateSsf(final IMultivariateSsf ssf, final INoiseProvider e,
            final MatrixStorage X, final MatrixStorage W, final DataBlockStorage k) {
        ssf_ = ssf;
        e_ = e;
        X_ = X;
        W_ = W;
        k_ = k;
        ndim_ = ssf_.getStateDim();
        nv_ = ssf.getVarsCount();
        nx_ = X_ == null ? 0 : X_.getMatrixColumnsCount();
        nw_ = W_ == null ? 0 : W_.getMatrixColumnsCount();
        ne_ = e_ == null ? 0 : nv_;
        cumul();
    }

    private void cumul() {
        if (W_ != null) {
            int n = W_.getCapacity();
            V_ = new MatrixStorage(ndim_, nw_, n);
            Xc_ = new MatrixStorage(nv_, nw_, n);
            // initialization
            SubMatrix v0 = V_.matrix(0);
            v0.copy(W_.matrix(0));
            ssf_.ZM(0, v0, Xc_.matrix(0));
            // iteration
            for (int i = 1; i < n; ++i) {
                SubMatrix vi = V_.matrix(i);
                vi.copy(V_.matrix(i - 1));
                ssf_.TM(i, vi);
                vi.add(W_.matrix(i));
                ssf_.ZM(i, vi, Xc_.matrix(i));
            }
        }
        if (k_ != null) {
            int n = k_.getCapacity();
            yc_ = new DataBlockStorage(nv_, n);
            l_ = new DataBlockStorage(ndim_, n);
            // initialization
            DataBlock l0 = l_.block(0);
            l0.copy(k_.block(0));
            ssf_.ZX(0, l0, yc_.block(0));
            // iteration
            for (int i = 1; i < n; ++i) {
                DataBlock li = l_.block(i);
                li.copy(k_.block(i - 1));
                ssf_.TX(i, li);
                li.add(k_.block(i));
                ssf_.ZX(i, li, yc_.block(i));
            }
        }
    }
    
    public IMultivariateSsf toSsf(){
        return new SsfRepresentation();
    }
    
    public IMSsfData adapt(IMSsfData data){
        double[] initialState = data.getInitialState();
        int nr=data.getVarsCount();
        int nc=data.count(0);
        for (int i=1; i<nr; ++i){
            int c=data.count(i);
            if (c != nc)
                return null;
        }
        Matrix m = new Matrix(nr, nc);
        for (int c = 0; c < nc; ++c) {
            for (int r = 0; r < nr; ++r) {
                m.set(r, c, data.get(r, c));
            }
            m.column(c).sub(yc_.block(c));
        }
        return new MultivariateSsfData(m, initialState);
    }

    /**
     * The state vector will contain: [a(ssf),e,b(x),c(xc)]
     *
     * and the matrices will be
     *
     * Z = [ Z(t, ssf), I, x(t), w(t) ]
     *
     * |T 0 0 0| T = |0 0 0 0| |0 0 I 0| |0 0 0 I|
     *
     * |Q 0 0 0| Q = |0 E 0 0| |0 0 0 0| |0 0 0 0|
     *
     * |Pf 0 0 0| Pf = |0 0 0 0| |0 0 0 0| |0 0 0 0|
     *
     * |Pi 0 0 0| Pi = |0 0 0 0| |0 0 I 0| |0 0 0 I|
     */
    private class SsfRepresentation extends AbstractMultivariateSsf {

        private SsfRepresentation() {
            int dim = ndim_ + nx_ + nw_ + ne_;
            tmp0_ = new DataBlock(dim);
            tmp1_ = new DataBlock(dim);
        }
        private final DataBlock tmp0_, tmp1_;

        @Override
        public int getVarsCount() {
            return ssf_.getVarsCount();
        }

        @Override
        public boolean hasZ(int pos, int v) {
            return true;
        }

        @Override
        public void VpZdZ(int pos, int v, int w, SubMatrix vm, double d) {
            tmp0_.set(0);
            Z(pos, v, tmp0_);
            if (v != w) {
                tmp1_.set(0);
                Z(pos, w, tmp1_);
            } else {
                tmp1_.copy(tmp0_);
            }
            DataBlockIterator cols = vm.columns();
            DataBlock col = cols.getData();
            do {
                double k = d * tmp0_.get(cols.getPosition());
                col.addAY(k, tmp1_);
            } while (cols.next());
        }

        @Override
        public void XpZd(int pos, int v, DataBlock x, double d) {
            int n0 = 0, n1 = ndim_;
            ssf_.XpZd(pos, v, x.range(n0, n1), d);
            n0 = n1;
            if (ne_ > 0) {
                n1 += ne_;
                x.range(n0, n1).add(d);
                n0 = n1;
            }
            if (nx_ > 0) {
                n1 += nx_;
                x.range(n0, n1).addAY(d, X_.matrix(pos).row(v));
                n0 = n1;
            }
            if (nw_ > 0) {
                n1 += nw_;
                x.range(n0, n1).addAY(d, Xc_.matrix(pos).row(v));
            }
        }

        @Override
        public void Z(int pos, int v, DataBlock z) {
            int n0 = 0, n1 = ndim_;
            ssf_.Z(pos, v, z.range(n0, n1));
            n0 = n1;
            if (ne_ > 0) {
                n1 += ne_;
                z.range(n0, n1).set(1);
                n0 = n1;
            }
            if (nx_ > 0) {
                n1 += nx_;
                z.range(n0, n1).copy(X_.matrix(pos).row(v));
                n0 = n1;
            }
            if (nw_ > 0) {
                n1 += nw_;
                z.range(n0, n1).copy(Xc_.matrix(pos).row(v));
            }
        }

        @Override
        public double ZVZ(int pos, int v, int w, SubMatrix vm) {
            ZM(pos, v, vm, tmp0_);
            return ZX(pos, w, tmp0_);
        }

        @Override
        public double ZX(int pos, int v, DataBlock x) {
            int n0 = 0, n1 = ndim_;
            double zx = ssf_.ZX(pos, v, x.range(n0, n1));
            n0 = n1;
            if (ne_ > 0) {
                n1 += ne_;
                zx += x.range(n0, n1).sum();
                n0 = n1;
            }
            if (nx_ > 0) {
                n1 += nx_;
                zx += x.range(n0, n1).dot(X_.matrix(pos).row(v));
                n0 = n1;
            }
            if (nw_ > 0) {
                n1 += nw_;
                zx += x.range(n0, n1).dot(Xc_.matrix(pos).row(v));
            }
            return zx;
        }

        @Override
        public void diffuseConstraints(SubMatrix b) {
            int nd = ssf_.getNonStationaryDim();
            if (nd > 0) {
                ssf_.diffuseConstraints(b.extract(0, ndim_, 0, nd));
            }
            for (int i = 0; i < nx_ + nw_; ++i) {
                b.set(ndim_ + i, nd + i, 1);
            }
        }

        @Override
        public void fullQ(int pos, SubMatrix qm) {
            ssf_.fullQ(pos, qm.extract(0, ndim_, 0, ndim_));
            if (e_ != null) {
                int e0 = ndim_, e1 = e0 + ne_;
                e_.e(pos, qm.extract(e0, e1, e0, e1));
            }
        }

        @Override
        public int getNonStationaryDim() {
            return ssf_.getNonStationaryDim() + nx_ + nw_;
        }

        @Override
        public int getStateDim() {
            return ndim_ + nx_ + nw_ + ne_;
        }

        @Override
        public int getTransitionResCount() {
            return ssf_.getTransitionResCount() + ne_;
        }

        @Override
        public int getTransitionResDim() {
            return ssf_.getTransitionResDim() + ne_;
        }

        @Override
        public boolean hasR() {
            return nx_ > 0 || nw_ > 0 || ssf_.hasR();
        }

        @Override
        public boolean hasTransitionRes(int pos) {
            return ne_ > 0 || ssf_.hasTransitionRes(pos);
        }

        @Override
        public boolean hasW() {
            return ssf_.hasW();
        }

        @Override
        public boolean isDiffuse() {
            return nx_ > 0 || nw_ > 0 || ssf_.isDiffuse();
        }

        @Override
        public boolean isMeasurementEquationTimeInvariant() {
            return ssf_.isMeasurementEquationTimeInvariant() && nx_ == 0 && nw_ == 0;
        }

        @Override
        public boolean isTimeInvariant() {
            return ssf_.isTimeInvariant() && nx_ == 0 && nw_ == 0 && (e_ == null || e_.isTimeInvariant());
        }

        @Override
        public boolean isTransitionEquationTimeInvariant() {
            return ssf_.isTransitionEquationTimeInvariant() && (e_ == null || e_.isTimeInvariant());
        }

        @Override
        public boolean isTransitionResidualTimeInvariant() {
            return ssf_.isTransitionResidualTimeInvariant() && (e_ == null || e_.isTimeInvariant());
        }

        @Override
        public boolean isValid() {
            return ssf_.isValid();
        }

        @Override
        public void Pf0(SubMatrix pf0) {
            ssf_.Pf0(pf0.extract(0, ndim_, 0, ndim_));
        }

        @Override
        public void Pi0(SubMatrix pi0) {
            ssf_.Pf0(pi0.extract(0, ndim_, 0, ndim_));
            if (nx_ > 0 || nw_ > 0) {
                pi0.diagonal().drop(ndim_ + ne_, 0).set(1);
            }
        }

        @Override
        public void Q(int pos, SubMatrix qm) {
            int n = ssf_.getTransitionResDim();
            ssf_.Q(pos, qm.extract(0, n, 0, n));
            if (ne_ > 0) {
                e_.e(pos, qm.extract(n, n + ne_, n, n + ne_));
            }
        }

        @Override
        public void R(int pos, SubArrayOfInt rv) {
            int n;
            if (ssf_.hasR()) {
                n = ssf_.getTransitionResCount();
                ssf_.R(pos, rv.range(0, n));
            } else {
                n = ndim_;
                for (int i = 0; i < n; ++i) {
                    rv.set(i, i);
                }
            }
            for (int i = 0; i < ne_; ++i) {
                rv.set(n + i, ndim_ + i);
            }
        }

        @Override
        public void T(int pos, SubMatrix tr) {
            ssf_.T(pos, tr.extract(0, ndim_, 0, ndim_));
            tr.diagonal().drop(ndim_ + ne_, 0).set(1);
        }

        @Override
        public void TX(int pos, DataBlock x) {
            ssf_.TX(pos, x.range(0, ndim_));
            if (ne_ > 0) {
                x.range(ndim_, ndim_ + ne_).set(0);
            }
        }

        @Override
        public void W(int pos, SubMatrix wv) {
            if (ssf_.hasW()) {
                int r = ssf_.getTransitionResCount(), c = ssf_.getTransitionResDim();
                ssf_.W(pos, wv.extract(0, r, 0, c));
                for (int i = 0; i < ne_; ++i) {
                    wv.set(r + i, c + i, 1);
                }
            }
        }

        @Override
        public void XT(int pos, DataBlock x) {
            ssf_.XT(pos, x.range(0, ndim_));
            if (ne_ > 0) {
                x.range(ndim_, ndim_ + ne_).set(0);
            }
        }
    }
}
