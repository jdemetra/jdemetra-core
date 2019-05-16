/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.calendarization;

import jdplus.data.DataBlock;
import demetra.design.Development;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.ISsfInitialization;
import demetra.ssf.ISsfLoading;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.Ssf;
import java.util.HashSet;
import javax.annotation.Nonnull;
import jdplus.maths.matrices.FastMatrix;

/**
 * State space form for calendarization. State vector: 0: Cumulative (weighted) sum (from
 * the start of a "cumulation period" to the previous position) 1: (Unweighted)
 * component
 *
 * The matrices/vectors of the state space form can take three different forms,
 * following its position (identified by (FIRST, LAST, DEF)
 *
 * The transition matrix and the measurement vector will be: case FIRST: T= | 0
 * w |, Z= | 0 w | | 0 1 |
 *
 * case LAST: T= | 0 0 |, Z= | 1 w | | 0 1 |
 *
 * case DEF:
 *
 * T= | 1 w |, Z= | 1 w | | 0 1 |
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.experimental.UtilityClass
public class SsfCalendarization {
    
    /**
     * Creates a state space model for calendarization
     * @param starts The starting positions of the aggregation periods
     * @param weights The weights of each observation
     * @return 
     */
    public ISsf of(@Nonnull final int[] starts, final double[] weights){
        Data data=new Data(starts, weights);
        return Ssf.of(new Initialization(), new Dynamics(data), new Loading(data));
     }

    static class Data {

        Data(final int[] starts, final double[] weights) {
            this.weights = weights;
            for (int i = 0; i < starts.length; ++i) {
                int cur = starts[i];
                this.starts.add(cur);
                if (cur > 0) {
                    this.ends.add(cur - 1);
                }
            }
        }

        private final double[] weights;
        private final HashSet<Integer> starts = new HashSet<>();
        private final HashSet<Integer> ends = new HashSet<>();
        private int curpos = -1, curtype = -1;

        /**
         * Gets the type of the model (its vectors/matrices) at a given
         * position.
         *
         * @param pos
         * @return FIRST if pos corresponds to the beginning of a "cumulation
         * period", LAST if pos corresponds to the end of a "cumulation period",
         * DEF otherwise.
         */
        int posType(int pos) {
            if (curpos == pos) {
                return curtype;
            }
            curpos = pos;
            if (starts.contains(pos)) {
                curtype = FIRST;
            } else if (ends.contains(pos)) {
                curtype = LAST;
            } else {
                curtype = DEF;
            }
            return curtype;
        }

        private double weight(int pos) {
            return weights == null ? 1 : weights[pos];
        }

        private double mweight(int pos, double m) {
            return weights == null ? m : weights[pos] * m;
        }

        private double mweight2(int pos, double m) {
            return weights == null ? m : weights[pos] * weights[pos] * m;
        }

    }

    final int LAST = 1, FIRST = 2, DEF = 0;

    static class Initialization implements ISsfInitialization {

        Initialization() {
        }

        @Override
        public void diffuseConstraints(FastMatrix b) {
            b.set(1, 0, 1);
        }

        /**
         *
         * @return
         */
        @Override
        public int getDiffuseDim() {
            return 1;
        }

        /**
         *
         * @return
         */
        @Override
        public int getStateDim() {
            return 2;
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
         * @param pf0
         */
        @Override
        public void Pf0(FastMatrix pf0) {
            // pf0.set(1, 1, 1);
        }

        @Override
        public void a0(DataBlock a0) {

        }

        /**
         *
         * @param pi0
         */
        @Override
        public void Pi0(FastMatrix pi0) {
            pi0.set(1, 1, 1);
        }

    }

    static class Dynamics implements ISsfDynamics {

        private final Data info;

        Dynamics(Data info) {
            this.info = info;
        }

        /**
         *
         * @param pos
         * @param qm
         */
        @Override
        public void V(int pos, FastMatrix qm) {
            qm.set(1, 1, 1);
        }

        @Override
        public int getInnovationsDim() {
            return 1;
        }

        @Override
        public void S(int pos, FastMatrix cm) {
            cm.set(1, 0, 1);
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public boolean areInnovationsTimeInvariant() {
            return true;
        }

        @Override
        public void T(int pos, FastMatrix tr) {
            tr.set(1, 1, 1);
            int postype = info.posType(pos);
            if (postype != info.weight(pos));
            if (postype != FIRST) {
                tr.set(0, 0, 1);
            }
        }

        @Override
        public void TX(int pos, DataBlock x) {
            int postype = info.posType(pos);
            double s = info.mweight(pos, x.get(1));
            switch (postype) {
                case LAST:
                    x.set(0, 0);
                    break;
                case FIRST:
                    // case II.
                    x.set(0, s);
                    break;
                default:
                    // case III
                    x.add(0, s);
                    break;
            }
        }

        @Override
        public void TVT(int pos, FastMatrix vm) {
            int postype = info.posType(pos);
            switch (postype) {
                case LAST:
                    vm.set(0, 0, 0);
                    vm.set(1, 0, 0);
                    vm.set(0, 1, 0);
                    break;
                case FIRST: {
                    double w = info.weight(pos);
                    double v = w * vm.get(1, 1);
                    vm.set(0, 0, w * v);
                    vm.set(1, 0, v);
                    vm.set(0, 1, v);
                    break;
                }
                default: {
                    double w = info.weight(pos);
                    double wV = w * vm.get(1, 1);
                    double wv = w * vm.get(0, 1);
                    vm.add(0, 1, wV);
                    vm.add(1, 0, wV);
                    vm.add(0, 0, 2 * wv + w * wV);
                    break;
                }
            }
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            x.add(1, u.get(0));
        }

        @Override
        public void addV(int pos, FastMatrix p) {
            p.add(1, 1, 1);
        }

        @Override
        public void XT(int pos, DataBlock x) {
            // case I: 0, x1
            int postype = info.posType(pos);
            switch (postype) {
                // case II: 0, w x0 + x1
                case LAST:
                    x.set(0, 0);
                    break;
                // case III: x0, w x0 + x1
                case FIRST:
                    x.add(1, info.mweight(pos, x.get(0)));
                    x.set(0, 0);
                    break;
                default:
                    x.add(1, info.mweight(pos, x.get(0)));
                    break;
            }
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            xs.set(0, x.get(1));
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }

    }

    static class Loading implements ISsfLoading {

        private final Data info;

        Loading(Data info) {
            this.info = info;
        }

        @Override
        public void VpZdZ(int pos, FastMatrix vm, double d) {

            vm.add(1, 1, info.mweight2(pos, d));
            int postype = info.posType(pos);
            if (postype != FIRST) {
                double w = info.mweight(pos, d);
                vm.add(0, 0, d);
                vm.add(0, 1, w);
                vm.add(1, 0, w);
            }
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            x.add(1, info.mweight(pos, d));
            int postype = info.posType(pos);
            if (postype != FIRST) {
                x.add(0, d);
            }
        }

        /**
         * Z(t) = [ 0, w(t)] for t%c == 0. [ 1, w(t)] for t%c != 0.
         *
         * @param pos
         * @param z
         */
        @Override
        public void Z(int pos, DataBlock z) {
            int postype = info.posType(pos);
            if (postype == FIRST) {
                z.set(0, 0);
            } else {
                z.set(0, 1);
            }
            z.set(1, info.weight(pos));
        }

        @Override
        public void ZM(int pos, FastMatrix m, DataBlock x) {
            x.setAY(info.weight(pos), m.row(1));
            int postype = info.posType(pos);
            if (postype != FIRST) {
                x.add(m.row(0));
            }
        }

        /**
         *
         * @param pos
         * @param vm
         * @return
         */
        @Override
        public double ZVZ(int pos, FastMatrix vm) {
            int postype = info.posType(pos);
            if (postype == FIRST) {
                return info.mweight2(pos, vm.get(1, 1));
            } else {
                double r = vm.get(0, 0);
                r += info.mweight(pos, 2 * vm.get(1, 0));
                r += info.mweight2(pos, vm.get(1, 1));
                return r;
            }
        }

        /**
         *
         * @param pos
         * @param x
         * @return
         */
        @Override
        public double ZX(int pos, DataBlock x) {
            int postype = info.posType(pos);
            double r = (postype == FIRST) ? 0 : x.get(0);
            return r + info.mweight(pos, x.get(1));
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }
    }

}
