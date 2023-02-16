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
package jdplus.ssf.sts.splines;

import demetra.data.DoubleSeqCursor;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.math.matrices.FastMatrix;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.ISsfInitialization;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.StateComponent;

/**
 * Integer period, regular knots on integer "periods"
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class SplineComponent {


    public ISsfLoading loading(SplineData data, int startPos) {

        return new Loading(data, startPos);
    }

    public class Loading implements ISsfLoading {

        private final SplineData data;
        private final int[] cdim;

        public Loading(SplineData data, int startpos) {
            this.data = data;
            cdim = new int[data.getInfos().size()];
            int i = 0, j = -startpos;
            for (SplineData.PeriodInformation info : data.getInfos()) {
                j += info.getZ().getRowsCount();
                cdim[i++] = j;
            }
        }

        private DataBlock z(int pos) {
            int cur = 0;
            for (; cur < cdim.length; ++cur) {
                if (pos < cdim[cur]) {
                    break;
                }
            }
            FastMatrix z = data.getInfos().get(cur).getZ();
            int row = z.getRowsCount() - cdim[cur] + pos;

            return z.row(row);
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

    public StateComponent stateComponent(SplineData data, double var, int startpos) {

        Dynamics dynamics = new Dynamics(data, var, startpos);
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

        private final SplineData data;
        private final double var, svar;
        private final int[] cdim;

        Dynamics(final SplineData data, double var, int startpos) {
            this.var = var;
            this.svar = Math.sqrt(var);
            this.data = data;
            cdim = new int[data.getInfos().size()];
            int i = 0, j = -startpos;
            for (SplineData.PeriodInformation info : data.getInfos()) {
                j += info.getZ().getRowsCount();
                cdim[i++] = j;
            }
        }

        private FastMatrix q(int pos) {
            int cur = 0;
            for (; cur < cdim.length; ++cur) {
                if (pos < cdim[cur]) {
                    break;
                }
            }
            return data.getInfos().get(cur).getQ().times(var);
        }

        private FastMatrix s(int pos) {
            int cur = 0;
            for (; cur < cdim.length; ++cur) {
                if (pos < cdim[cur]) {
                    break;
                }
            }
            return data.getInfos().get(cur).getS().times(svar);
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
            return data.getDim();
        }

        @Override
        public void V(int pos, FastMatrix qm) {
            qm.copy(q(pos));
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public void S(int pos, FastMatrix sm) {
            sm.copy(s(pos));
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            x.addProduct(s(pos).rowsIterator(), u);
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            xs.product(x, s(pos).columnsIterator());
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
            p.add(q(pos));
        }
    }

}
