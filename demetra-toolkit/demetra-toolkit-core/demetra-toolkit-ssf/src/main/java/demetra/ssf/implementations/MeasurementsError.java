/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.ssf.implementations;

import demetra.maths.matrices.FastMatrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.ssf.State;
import demetra.ssf.multivariate.ISsfErrors;
import demetra.data.DoubleSeq;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class MeasurementsError {

    public ISsfErrors of(FastMatrix H, FastMatrix R) {
        return new Errors(H, R);
    }

    public ISsfErrors of(DoubleSeq var) {
        double[] v = var.toArray();
        for (int i = 0; i < v.length; ++i) {
            if (v[i] > 0) {
                return new IndependentErrors(v);
            }
        }
        return null;
    }

    public ISsfErrors of(double var) {
        return new IndependentErrors2(var);
    }

    private static class Errors implements ISsfErrors {

        private final FastMatrix H, R;

        private Errors(final FastMatrix H, FastMatrix R) {
            if (H != null) {
                this.H = H;
            } else {
                this.H = SymmetricMatrix.LLt(R);
            }
            if (R != null) {
                this.R = R;
            } else {
                this.R = H.deepClone();
                SymmetricMatrix.lcholesky(this.R, State.ZERO);
            }

        }

        @Override
        public boolean areIndependent() {
            return false;
        }

        @Override
        public void H(int pos, FastMatrix h) {
            h.copy(H);
        }

        @Override
        public void R(int pos, FastMatrix r) {
            r.copy(R);
        }

        @Override
        public void addH(int pos, FastMatrix V) {
            V.add(H);
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

    }

    private static class IndependentErrors implements ISsfErrors {

        private final double[] v, e;

        private IndependentErrors(double[] v) {
            this.v = v;
            e = this.v.clone();
            for (int i = 0; i < e.length; ++i) {
                e[i] = v[i] <= 0 ? 0 : Math.sqrt(v[i]);
            }
        }

        @Override
        public boolean areIndependent() {
            return true;
        }

        @Override
        public void H(int pos, FastMatrix h) {
            h.diagonal().copyFrom(v, 0);
        }

        @Override
        public void R(int pos, FastMatrix r) {
            r.diagonal().copyFrom(e, 0);
        }

        @Override
        public void addH(int pos, FastMatrix V) {
            V.diagonal().apply(pos, x -> x + v[pos]);
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

    }

    private static class IndependentErrors2 implements ISsfErrors {

        private final double v, e;

        private IndependentErrors2(double v) {
            this.v = v;
            this.e = Math.sqrt(v);
        }

        @Override
        public boolean areIndependent() {
            return true;
        }

        @Override
        public void H(int pos, FastMatrix h) {
            h.diagonal().set(v);
        }

        @Override
        public void R(int pos, FastMatrix r) {
            r.diagonal().set(e);
        }

        @Override
        public void addH(int pos, FastMatrix V) {
            V.diagonal().add(v);
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

    }
}
