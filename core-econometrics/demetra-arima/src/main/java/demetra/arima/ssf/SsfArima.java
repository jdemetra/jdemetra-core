/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.arima.ssf;

import demetra.arima.AutoCovarianceFunction;
import demetra.arima.IArimaModel;
import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.data.DataWindow;
import demetra.design.Development;
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.maths.polynomials.Polynomial;
import demetra.maths.polynomials.RationalFunction;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.SsfException;
import demetra.ssf.State;
import demetra.ssf.ckms.CkmsDiffuseInitializer;
import demetra.ssf.ckms.CkmsFilter;
import demetra.ssf.ckms.CkmsState;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfData;
import demetra.ssf.univariate.ISsfMeasurement;
import demetra.ssf.univariate.OrdinaryFilter;
import demetra.ssf.univariate.Ssf;
import demetra.ssf.implementations.Measurement;
import demetra.ssf.UpdateInformation;
import demetra.data.DoubleReader;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SsfArima extends Ssf {

//    public static IParametricMapping<SsfArima> mapping(final SarimaSpecification spec) {
//        return new Mapping(spec);
//    }
    public static CkmsFilter.IFastInitializer<SsfArima> fastInitializer() {
        return (CkmsState state, UpdateInformation upd, SsfArima ssf, ISsfData data) -> {
            if (ssf.model.isStationary()) {
                return stInitialize(state, upd, ssf, data);
            } else {
                return dInitialize(state, upd, ssf, data);
            }
        };
    }

    private static int stInitialize(CkmsState state, UpdateInformation upd, SsfArima ssf1, ISsfData data) {
        int n = ssf1.getStateDim();
        double[] values = ssf1.model.getAutoCovarianceFunction().values(n);
        DataBlock M = upd.M(), L = state.l();
        upd.M().copyFrom(values, 0);
        L.copy(M);
        ssf1.dynamics.TX(0, L);
        upd.setVariance(values[0]);
        return 0;

    }

    private static int dInitialize(CkmsState state, UpdateInformation upd, SsfArima ssf1, ISsfData data) {
        return new CkmsDiffuseInitializer<SsfArima>(diffuseInitializer()).initialize(state, upd, ssf1, data);
    }

    public static OrdinaryFilter.Initializer diffuseInitializer() {
        return (State state, ISsf ssf, ISsfData data) -> {
            SsfArima ssfArima = (SsfArima) ssf;
            SsfArimaDynamics dyn = (SsfArimaDynamics) ssfArima.getDynamics();
            ISsfMeasurement m = ssf.getMeasurement();
            int nr = ssf.getStateDim(), nd = dyn.getNonStationaryDim();
            Matrix A = Matrix.make(nr + nd, nd);
            double[] dif = ssfArima.model.getNonStationaryAR().asPolynomial().toArray();
            for (int j = 0; j < nd; ++j) {
                A.set(j, j, 1);
                for (int i = nd; i < nd + nr; ++i) {
                    double c = 0;
                    for (int k = 1; k <= nd; ++k) {
                        c -= dif[k] * A.get(i - k, j);
                    }
                    A.set(i, j, c);
                }
            }

            for (int i = 0; i < nr; ++i) {
                double c = 0;
                for (int j = 0; j < nd; ++j) {
                    c += A.get(i + nd, j) * data.get(j);
                }
                state.a().set(i, c);
            }
            Matrix stV = Matrix.square(nr);
            SsfArimaDynamics.stVar(stV, dyn.stpsi, dyn.stacgf, dyn.var);
            Matrix K = Matrix.square(nr);
            SsfArimaDynamics.Ksi(K, dyn.dif);
            SymmetricMatrix.XSXt(stV, K, state.P());
            return nd;
        };
    }

    private final IArimaModel model;

    /**
     *
     * @param arima
     */
    private SsfArima(final IArimaModel arima, final ISsfDynamics dynamics, ISsfMeasurement measurement) {
        super(dynamics, measurement);
        model = arima;
    }

    /**
     *
     * @return
     */
    public IArimaModel getModel() {
        return model;
    }

    public static SsfArima of(IArimaModel arima) {
        if (arima.isStationary()) {
            return ofStationary(arima);
        } else {
            return ofNonStationary(arima);
        }
    }

    private static SsfArima ofStationary(IArimaModel arima) {
        double var = arima.getInnovationVariance();
        if (var == 0) {
            throw new SsfException(SsfException.STOCH);
        }
        ISsfDynamics dynamics = new StDynamics(arima);
        ISsfMeasurement measurement = Measurement.create(dynamics.getStateDim(), 0);
        return new SsfArima(arima, dynamics, measurement);
    }

    private static SsfArima ofNonStationary(IArimaModel arima) {
        double var = arima.getInnovationVariance();
        if (var == 0) {
            throw new SsfException(SsfException.STOCH);
        }
        ISsfDynamics dynamics = new SsfArimaDynamics(arima);
        ISsfMeasurement measurement = Measurement.create(dynamics.getStateDim(), 0);
        return new SsfArima(arima, dynamics, measurement);
    }
    
    public static class StDynamics implements ISsfDynamics {

        private final int dim;
        private final double var;
        private final double[] phi;
        private final DataBlock psi, z, acgf;
        private Matrix V;
        private Matrix P0;

        public StDynamics(IArimaModel arima) {
            var = arima.getInnovationVariance();
            Polynomial ar = arima.getAR().asPolynomial();
            Polynomial ma = arima.getMA().asPolynomial();
            phi = ar.toArray();
            dim = Math.max(ar.getDegree(), ma.getDegree() + 1);
            psi = DataBlock.ofInternal(RationalFunction.of(ma, ar).coefficients(dim));
            acgf = DataBlock.ofInternal(arima.getAutoCovarianceFunction().values(dim));
            z = DataBlock.make(dim);
        }

        private void init() {
            P0 = p0(var, acgf, psi);
            V = v(var, psi);
        }

        private static Matrix v(double var, DataBlock psi) {
            Matrix v = SymmetricMatrix.xxt(psi);
            v.mul(var);
            return v;
        }

        private static Matrix p0(double var, final DataBlock acgf, final DataBlock psi) {
            int dim = acgf.length();
            Matrix P = Matrix.square(dim);
            P.column(0).copy(acgf);
            for (int j = 0; j < dim - 1; ++j) {
                double psij = psi.get(j);
                P.set(j + 1, j + 1, P.get(j, j) - psij * psij * var);
                for (int k = 0; k < j; ++k) {
                    P.set(j + 1, k + 1, P.get(j, k) - psij * psi.get(k) * var);
                }
            }
            SymmetricMatrix.fromLower(P);
            return P;
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
            for (int i = 1; i < phi.length; ++i) {
                tr.set(dim - 1, dim - i, -phi[i]);
            }
        }

        /**
         *
         * @param pos
         * @param vm
         */
        @Override
        public void TVT(final int pos, final Matrix vm) {
            if (phi.length == 1) {
                vm.upLeftShift(1);
                vm.column(dim - 1).set(0);
                vm.row(dim - 1).set(0);
            } else {
                z.set(0);
                DataBlockIterator cols = vm.reverseColumnsIterator();
                for (int i = 1; i < phi.length; ++i) {
                    z.addAY(-phi[i], cols.next());
                }
                TX(pos, z);
                vm.upLeftShift(1);
                vm.column(dim - 1).copy(z);
                vm.row(dim - 1).copy(z);
            }

        }

        /**
         *
         * @param pos
         * @param x
         */
        @Override
        public void TX(final int pos, final DataBlock x) {
            double z = 0;
            if (phi.length > 1) {
                DoubleReader reader = x.reverseReader();
                for (int i = 1; i < phi.length; ++i) {
                    z -= phi[i] * reader.next();
                }
            }
            x.bshift(1);
            x.set(dim - 1, z);
        }

        /**
         *
         * @param pos
         * @param x
         */
        @Override
        public void XT(final int pos, final DataBlock x) {
            double last = -x.get(dim - 1);
            x.fshift(1);
            x.set(0, 0);
            if (last != 0) {
                for (int i = 0, j = dim - phi.length; i < phi.length; ++i, ++j) {
                    if (phi[i] != 0) {
                        x.add(j, last * phi[i]);
                    }
                }
            }
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public boolean isDiffuse() {
            return false;
        }

        @Override
        public int getNonStationaryDim() {
            return 0;
        }

        @Override
        public void diffuseConstraints(Matrix b) {
        }

        @Override
        public boolean a0(DataBlock a0) {
            return true;
        }

        @Override
        public boolean Pf0(Matrix pf0) {
            if (P0 == null) {
                init();
            }
            pf0.copy(P0);
            return true;
        }

        @Override
        public void Pi0(Matrix pi0) {
        }

        @Override
        public int getStateDim() {
            return dim;
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public int getInnovationsDim() {
            return 1;
        }

        @Override
        public void V(int pos, Matrix qm) {
            if (V == null) {
                init();
            }
            qm.copy(V);
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public void S(int pos, Matrix sm) {
            if (psi == null) {
                init();
            }
            sm.column(0).copy(psi);
            if (var != 1) {
                sm.mul(Math.sqrt(var));
            }
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            if (psi == null) {
                init();
            }
            double a = x.dot(psi);
            if (var != 1) {
                a *= Math.sqrt(var);
            }
            xs.set(0, a);
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            if (psi == null) {
                init();
            }
            double a = u.get(0);
            if (var != 1) {
                a *= Math.sqrt(var);
            }
            x.addAY(a, psi);
        }

        @Override
        public void addV(int pos, Matrix p) {
            if (V == null) {
                init();
            }
            p.add(V);
        }
    }

    public static class SsfArimaDynamics implements ISsfDynamics {

        private final int dim;
        private final double var, se;
        private final double[] phi;
        private final DataBlock z, psi, stpsi, stacgf;
        private final Matrix V;
        private final Matrix P0;
        private final double[] dif;

        /**
         * Computes B =
         *
         * @param b B
         * @param d The coefficients of the differencing polynomial
         */
        static void B0(final Matrix b, final double[] d) {
            int nd = d.length - 1;
            if (nd == 0) {
                return;
            }
            int nr = b.getRowsCount();
            b.diagonal().set(1);
            if (nd == nr) {
                return;
            }

            DataBlock D = DataBlock.ofInternal(d, d.length - 1, 0, -1);
            for (int i = 0; i < nd; ++i) {
                DataBlock C = b.column(i);
                DataWindow R = C.window(0, nd);
                C.set(nd, -R.get().dot(D));
                for (int k = nd + 1; k < nr; ++k) {
                    C.set(k, -R.move(1).dot(D));
                }
            }
        }

        /**
         *
         * @param X
         * @param dif
         */
        static void Ksi(final Matrix X, final double[] dif) {
            int n = X.getRowsCount();
            double[] ksi = RationalFunction.of(Polynomial.ONE, Polynomial.of(dif)).coefficients(n);

            for (int j = 0; j < n; ++j) {
                for (int k = 0; k <= j; ++k) {
                    X.set(j, k, ksi[j - k]);
                }
            }
        }

        /**
         *
         * @param stV
         * @param stpsi
         * @param stacgf
         * @param var
         */
        static void stVar(final Matrix stV, final DataBlock stpsi,
                final DataBlock stacgf, final double var) {
            int n = stV.getRowsCount();

            stV.column(0).copy(stacgf);

            for (int j = 0; j < n - 1; ++j) {
                double stpsij = stpsi.get(j);
                stV.set(j + 1, j + 1, stV.get(j, j) - stpsij * stpsij * var);
                for (int k = 0; k < j; ++k) {
                    stV.set(j + 1, k + 1, stV.get(j, k) - stpsij * stpsi.get(k) * var);
                }
            }

            SymmetricMatrix.fromLower(stV);
        }

        public SsfArimaDynamics(IArimaModel arima) {
            var = arima.getInnovationVariance();
            Polynomial ar = arima.getAR().asPolynomial();
            Polynomial ma = arima.getMA().asPolynomial();
            phi = ar.toArray();
            BackFilter ur = arima.getNonStationaryAR();
            dif = ur.asPolynomial().toArray();
            dim = Math.max(ar.getDegree(), ma.getDegree() + 1);
            psi = DataBlock.ofInternal(RationalFunction.of(ma, ar).coefficients(dim));

            Polynomial stphi = arima.getStationaryAR().asPolynomial();
            stacgf = DataBlock.ofInternal(new AutoCovarianceFunction(ma, stphi, var).values(dim));
            stpsi = DataBlock.ofInternal(RationalFunction.of(ma, stphi).coefficients(dim));
            z = DataBlock.make(dim);
            Matrix stvar = StDynamics.p0(var, stacgf, stpsi);
            Matrix K = Matrix.square(dim);
            Ksi(K, dif);
            P0 = SymmetricMatrix.XSXt(stvar, K);
            V = StDynamics.v(var, psi);
            se = Math.sqrt(var);
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
            tr.set(0);
            for (int i = 1; i < dim; ++i) {
                tr.set(i - 1, i, 1);
            }
            for (int i = 1; i < phi.length; ++i) {
                tr.set(dim - 1, dim - i, -phi[i]);
            }
        }

        /**
         *
         * @param pos
         * @param vm
         */
        @Override
        public void TVT(final int pos, final Matrix vm) {
            if (phi.length == 1) {
                vm.upLeftShift(1);
                vm.column(dim - 1).set(0);
                vm.row(dim - 1).set(0);
            } else {
                z.set(0);
                DataBlockIterator cols = vm.reverseColumnsIterator();
                for (int i = 1; i < phi.length; ++i) {
                    z.addAY(-phi[i], cols.next());
                }
                TX(pos, z);
                vm.upLeftShift(1);
                vm.column(dim - 1).copy(z);
                vm.row(dim - 1).copy(z);
            }

        }

        /**
         *
         * @param pos
         * @param x
         */
        @Override
        public void TX(final int pos, final DataBlock x) {
            double z = 0;
            if (phi.length > 1) {
                DoubleReader reader = x.reverseReader();
                for (int i = 1; i < phi.length; ++i) {
                    z -= phi[i] * reader.next();
                }
            }
            x.bshift(1);
            x.set(dim - 1, z);
        }

        /**
         *
         * @param pos
         * @param x
         */
        @Override
        public void XT(final int pos, final DataBlock x) {
            double last = -x.get(dim - 1);
            x.fshift(1);
            x.set(0, 0);
            if (last != 0) {
                for (int i = 0, j = dim - phi.length; i < phi.length; ++i, ++j) {
                    if (phi[i] != 0) {
                        x.add(j, last * phi[i]);
                    }
                }
            }
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public boolean isDiffuse() {
            return dif.length > 1;
        }

        @Override
        public int getNonStationaryDim() {
            return dif.length - 1;
        }

        @Override
        public void diffuseConstraints(Matrix b) {
            int d = dif.length - 1;
            if (d == 0) {
                return;
            }
            B0(b, dif);
        }

        @Override
        public boolean a0(DataBlock a0) {
            return true;
        }

        @Override
        public boolean Pf0(Matrix pf0) {
            pf0.copy(P0);
            return true;
        }

        @Override
        public void Pi0(Matrix pi0) {
            Matrix B = Matrix.make(dim, dif.length - 1);
            B0(B, dif);
            SymmetricMatrix.XXt(B, pi0);
        }

        @Override
        public int getStateDim() {
            return dim;
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public int getInnovationsDim() {
            return 1;
        }

        @Override
        public void V(int pos, Matrix qm) {
            qm.copy(V);
        }

        @Override
        public void S(int pos, Matrix sm) {
            sm.column(0).copy(psi);
            if (se != 1) {
                sm.mul(se);
            }
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public void addV(int pos, Matrix p) {
            p.add(V);
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock sx) {
            double a = x.dot(psi) * se;
            sx.set(0, a);
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            double a = u.get(0) * se;
            x.addAY(a, psi);
        }

    }

}
