/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.arima.ssf;

import jdplus.arima.AutoCovarianceFunction;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import demetra.data.DoubleSeqCursor;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.math.polynomials.Polynomial;
import jdplus.math.polynomials.RationalFunction;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.ISsfInitialization;
import jdplus.ssf.implementations.Loading;
import org.checkerframework.checker.nullness.qual.NonNull;
import jdplus.math.matrices.Matrix;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.StateComponent;

/**
 * State array: y(t-nlags)...y(t)y(t+1|t)...y(t+fcasts|t)
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class SsfAr2 {

    public StateComponent of(@NonNull double[] ar, double var, int nlags, int fcasts) {
        if (ar.length == 0) {
            throw new IllegalArgumentException();
        }
        if (fcasts < ar.length - 1) {
            fcasts = ar.length - 1;
        }
        Polynomial p = polynomial(ar);
        AutoCovarianceFunction acf = acf(p, var);
        double[] psi = psi(p).coefficients(fcasts + 1);
        Data data = new Data(ar, psi, var, acf, nlags);
        return new StateComponent(new Initialization(data), new Dynamics(data));
    }

    public static ISsfLoading defaultoading(int nlags) {
        return Loading.fromPosition(nlags);
    }

    private static Polynomial polynomial(double[] phi) {
        double[] c = new double[1 + phi.length];
        c[0] = 1;
        for (int i = 0; i < phi.length; ++i) {
            c[i + 1] = -phi[i];
        }
        return Polynomial.ofInternal(c);
    }

    private static AutoCovarianceFunction acf(Polynomial ar, double var) {
        return new AutoCovarianceFunction(Polynomial.ONE, ar, var);
    }

    private static RationalFunction psi(Polynomial ar) {
        return RationalFunction.of(Polynomial.ONE, ar);
    }

    @lombok.Value
    private static class Data {

        double[] phi;
        double[] psi;
        double var;
        AutoCovarianceFunction acf;
        int nlags;

        int dim() {
            return psi.length + nlags;
        }

        int last() {
            return psi.length + nlags - 1;
        }

        double se() {
            return Math.sqrt(var);
        }
    }

    private static class Initialization implements ISsfInitialization {

        private final Data info;

        Initialization(final Data info) {
            this.info = info;
        }

        @Override
        public int getStateDim() {
            return info.dim();
        }

        @Override
        public boolean isDiffuse() {
            return false;
        }

        @Override
        public int getDiffuseDim() {
            return 0;
        }

        @Override
        public void diffuseConstraints(Matrix b) {
        }

        @Override
        public void a0(DataBlock a0) {
        }

        @Override
        public void Pi0(Matrix pf0) {
        }

        @Override
        public void Pf0(Matrix pf0) {
            // initialization of the "forecast part"
            int dim = info.psi.length, nl = info.nlags;
            info.acf.prepare(dim + nl);
            pf0.diagonal().set(info.acf.get(0));
            for (int i = 1; i < dim + nl; ++i) {
                pf0.subDiagonal(i).set(info.acf.get(i));
                pf0.subDiagonal(-i).set(info.acf.get(i));
            }
            for (int j = 0, J = nl; j < dim - 1; ++j, ++J) {
                double psij = info.psi[j];
                pf0.set(J + 1, J + 1, pf0.get(J, J) - psij * psij * info.var);
                for (int k = 0, K = nl; k < j; ++k, ++K) {
                    double q = pf0.get(J, K) - psij * info.psi[k] * info.var;
                    pf0.set(J + 1, K + 1, q);
                    pf0.set(K + 1, J + 1, q);
                }
            }
        }

    }

    static class Dynamics implements ISsfDynamics {

        private final Data data;
        private final DataBlock z;
        private final Matrix V;

        static Matrix v(double var, double[] psi) {
            Matrix v = SymmetricMatrix.xxt(DataBlock.of(psi));
            v.mul(var);
            return v;
        }

        public Dynamics(Data data) {
            this.data = data;
            z = DataBlock.make(data.dim());
            V = v(data.var, data.psi);
        }

        /**
         *
         * @param pos
         * @param tr
         */
        @Override
        public void T(final int pos, final Matrix tr) {
            T(tr);
        }

        /**
         *
         * @param tr
         */
        public void T(final Matrix tr) {
            tr.subDiagonal(1).set(1);
            int l = data.last();
            for (int i = 0; i < data.phi.length; ++i) {
                tr.set(l, l - i, data.phi[i]);
            }
        }

        /**
         *
         * @param pos
         * @param vm
         */
        @Override
        public void TVT(final int pos, final Matrix vm) {
            int l = data.last();
            z.set(0);
            DataBlockIterator cols = vm.reverseColumnsIterator();
            for (int i = 0; i < data.phi.length; ++i) {
                z.addAY(data.phi[i], cols.next());
            }
            TX(pos, z);
            vm.upLeftShift(1);
            vm.column(l).copy(z);
            vm.row(l).copy(z);
        }

        /**
         *
         * @param pos
         * @param x
         */
        @Override
        public void TX(final int pos, final DataBlock x) {
            double z = 0;
            DoubleSeqCursor reader = x.reverseReader();
            for (int i = 0; i < data.phi.length; ++i) {
                z += data.phi[i] * reader.getAndNext();
            }
            x.bshift(1);
            x.set(data.last(), z);
        }

        /**
         *
         * @param pos
         * @param x
         */
        @Override
        public void XT(final int pos, final DataBlock x) {
            int l = data.last();
            double last = x.get(l);
            x.fshift(1);
            x.set(0, 0);
            if (last != 0) {
                for (int i = 0, j = l; i < data.phi.length; ++i, --j) {
                    if (data.phi[i] != 0) {
                        x.add(j, last * data.phi[i]);
                    }
                }
            }
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public boolean areInnovationsTimeInvariant() {
            return true;
        }

        @Override
        public int getInnovationsDim() {
            return 1;
        }

        @Override
        public void V(int pos, Matrix qm) {
            qm.extract(data.nlags, data.psi.length, data.nlags, data.psi.length).copy(V);
        }

        @Override
        public void S(int pos, Matrix sm) {
            DataBlock s = sm.column(0).drop(data.nlags, 0);
            s.copyFrom(data.psi, 0);
            s.mul(data.se());
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public void addV(int pos, Matrix p) {
            p.extract(data.nlags, data.psi.length, data.nlags, data.psi.length).add(V);
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock sx) {
            double a = x.drop(data.nlags, 0).dot(DataBlock.of(data.psi)) * data.se();
            sx.set(0, a);
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            double a = u.get(data.nlags) * data.se();
            x.addAY(a, DataBlock.of(data.psi));
        }
    }

}
