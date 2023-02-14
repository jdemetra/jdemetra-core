/*
 * Copyright 2022 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.ssf.sts;

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import internal.jdplus.math.functions.gsl.interpolation.CubicSplines;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.ISsfInitialization;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.StateComponent;

/**
 * Integer period, regular knots on integer "periods"
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class RegularSplineComponent {

    @lombok.Value
    @lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    public static class Data {

        public static Data of(int[] xi) {
            DoubleSeq X = DoubleSeq.onMapping(xi.length, k -> xi[k]);
            int dim = xi.length - 1;
            CubicSplines.Spline[] nodes = new CubicSplines.Spline[dim];
            for (int i = 0; i < dim; ++i) {
                double[] f = new double[dim + 1];
                if (i == 0) {
                    f[0] = 1;
                    f[nodes.length] = 1;
                } else {
                    f[i] = 1;
                }
                nodes[i] = CubicSplines.periodic(X, DoubleSeq.of(f));
            }

            int period = xi[dim];
            double[] wstar = new double[dim];
            FastMatrix Z = FastMatrix.make(period, dim);
            for (int i = 0; i < dim; ++i) {
                DoubleSeqCursor.OnMutable cursor = Z.column(i).cursor();
                double s = 0;
                for (int j = 0; j < period; ++j) {
                    double w = nodes[i].applyAsDouble(j);
                    cursor.setAndNext(w);
                    s += w;
                }
                wstar[i] = s;
            }
            DataBlock zh = Z.column(dim - 1);
            double wh = wstar[dim - 1];
            for (int i = 0; i < dim - 1; ++i) {
                Z.column(i).addAY(-wstar[i] / wh, zh);
            }

            DataBlock W = DataBlock.of(wstar, 0, dim);
            FastMatrix Q = FastMatrix.identity(dim - 1);
            Q.addXaXt(-1 / W.ssq(), W.drop(0, 1));
            return new Data(Q, Z.dropBottomRight(0, 1), period, dim - 1);
        }

        private FastMatrix Q, Z;
        private int period, dim;
    }

    public ISsfLoading loading(Data data, int startPos) {

        return new Loading(data.getZ(), data.getPeriod(), startPos);
    }

    public class Loading implements ISsfLoading {

        private final int startpos, period;
        private final FastMatrix Z;

        public Loading(FastMatrix Z, int period, int startpos) {
            this.Z = Z;
            this.period = period;
            this.startpos = startpos;
        }

        private DataBlock z(int pos) {
            return Z.row((pos + startpos) % period);
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            z.copy(z(pos));
        }

        @Override
        public double ZX(int pos, DataBlock m) {
            return z(pos).dot(m);
        }

        @Override
        public void ZM(int pos, FastMatrix m, DataBlock zm) {
            DataBlock row = z(pos);
            zm.set(m.columnsIterator(), x -> row.dot(x));
        }

        /**
         * Computes M*Z' (or ZM')
         *
         * @param pos
         * @param m
         * @param zm
         */
        @Override
        public void MZt(int pos, FastMatrix m, DataBlock zm) {
            DataBlock row = z(pos);
            zm.set(m.rowsIterator(), x -> row.dot(x));
        }

        @Override
        public double ZVZ(int pos, FastMatrix V) {
            DataBlock row = z(pos);
            DataBlock zv = DataBlock.make(V.getColumnsCount());
            zv.product(row, V.columnsIterator());
            return zv.dot(row);
        }

        @Override
        public void VpZdZ(int pos, FastMatrix V, double d) {
            if (d == 0) {
                return;
            }
            DataBlockIterator cols = V.columnsIterator();
            DataBlock row = z(pos);
            DoubleSeqCursor z = row.cursor();
            while (cols.hasNext()) {
                cols.next().addAY(d * z.getAndNext(), row);
            }
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            x.addAY(d, z(pos));
        }
    }

    public StateComponent stateComponent(int[] xi, double var) {
        Data data = Data.of(xi);

        Dynamics dynamics = new Dynamics(data.getQ(), var);
        Initialization initialization = new Initialization(data.getDim());

        return new StateComponent(initialization, dynamics);
    }

    public StateComponent stateComponent(Data data, double var) {

        Dynamics dynamics = new Dynamics(data.getQ(), var);
        Initialization initialization = new Initialization(data.getDim());

        return new StateComponent(initialization, dynamics);

    }

    static class Initialization implements ISsfInitialization {

        private final int dim;

        Initialization(int dim) {
            this.dim = dim;
        }

        @Override
        public int getStateDim() {
            return dim;
        }

        @Override
        public boolean isDiffuse() {
            return true;
        }

        @Override
        public int getDiffuseDim() {
            return dim;
        }

        @Override
        public void diffuseConstraints(FastMatrix b) {
            b.diagonal().set(1);
        }

        @Override
        public void a0(DataBlock a0) {
        }

        @Override
        public void Pf0(FastMatrix pf0) {
        }

        @Override
        public void Pi0(FastMatrix pi0) {
            pi0.diagonal().set(1);
        }
    }

    static class Dynamics implements ISsfDynamics {

        private final FastMatrix var, s;

        Dynamics(final FastMatrix Q, double var) {
            this.var = Q.times(var);
            s = this.var.deepClone();
            SymmetricMatrix.lcholesky(s, 1e-9);
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
            return var.getColumnsCount();
        }

        @Override
        public void V(int pos, FastMatrix qm) {
            qm.copy(var);
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public void S(int pos, FastMatrix sm) {
            sm.copy(s);
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            x.addProduct(s.rowsIterator(), u);
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            xs.product(x, s.columnsIterator());
        }

        @Override
        public void T(int pos, FastMatrix tr) {
            tr.diagonal().set(1);
        }

        @Override
        public void TX(int pos, DataBlock x) {
        }

        @Override
        public void XT(int pos, DataBlock x) {
        }

        @Override
        public void TVT(int pos, FastMatrix v) {
        }

        @Override
        public void addV(int pos, FastMatrix p) {
            p.add(var);
        }
    }

}
