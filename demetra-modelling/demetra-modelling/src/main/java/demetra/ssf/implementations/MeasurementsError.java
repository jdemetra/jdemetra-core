/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.ssf.implementations;

import demetra.data.DoubleSequence;
import demetra.data.Doubles;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.ssf.State;
import demetra.ssf.multivariate.ISsfErrors;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class MeasurementsError {

    public ISsfErrors of(Matrix H, Matrix R) {
        return new Errors(H, R);
    }

    public ISsfErrors of(DoubleSequence var) {
        return new IndependentErrors(var);
    }

    public ISsfErrors of(double var) {
        return new IndependentErrors2(var);
    }

    private static class Errors implements ISsfErrors {

        private final Matrix H, R;

        private Errors(final Matrix H, Matrix R) {
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
        public void H(int pos, Matrix h) {
            h.copy(H);
        }

        @Override
        public void R(int pos, Matrix r) {
            r.copy(R);
        }

        @Override
        public void addH(int pos, Matrix V) {
            V.add(H);
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

    }

    private static class IndependentErrors implements ISsfErrors {

        private final DoubleSequence v, e;

        private IndependentErrors(DoubleSequence v) {
            this.v = v;
            this.e = Doubles.fn(v, z -> z <= 0 ? 0 : Math.sqrt(z));
        }

        @Override
        public boolean areIndependent() {
            return true;
        }

        @Override
        public void H(int pos, Matrix h) {
            h.diagonal().copy(v);
        }

        @Override
        public void R(int pos, Matrix r) {
            r.diagonal().copy(e);
        }

        @Override
        public void addH(int pos, Matrix V) {
            V.diagonal().apply(v, (a, b) -> a + b);
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
        public void H(int pos, Matrix h) {
            h.diagonal().set(v);
        }

        @Override
        public void R(int pos, Matrix r) {
            r.diagonal().set(e);
        }

        @Override
        public void addH(int pos, Matrix V) {
            V.diagonal().add(v);
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

    }
}
