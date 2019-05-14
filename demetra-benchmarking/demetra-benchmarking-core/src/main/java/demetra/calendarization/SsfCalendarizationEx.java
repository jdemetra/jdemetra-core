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

import demetra.data.DataBlock;
import demetra.design.Development;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.ISsfInitialization;
import demetra.ssf.ISsfLoading;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.Ssf;
import java.util.HashSet;
import javax.annotation.Nonnull;
import demetra.maths.matrices.Matrix;

/**
 * See "Calendarization with splines and state space models" B. Quenneville, F.
 * Picard and S.Fortier Appl. Statistics (2013) 62, part 3, pp 371-399
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.experimental.UtilityClass
public class SsfCalendarizationEx {

    /**
     * State vector: Cumulative sum, relative to the observation constraints
     * Cumulative sum, relative to the aggregation constraints, (Unweighted)
     * component *
     */
    private final int LAST = 1, FIRST = 2, DEF = 0;

    /**
     * Creates a state space model for calendarization
     *
     * @param starts The starting positions of the aggregation periods
     * @param astarts
     * @param weights The weights of each observation
     * @return
     */
    public ISsf of(@Nonnull final int[] starts, final int[] astarts, final double[] weights) {
        Data data = new Data(starts, astarts, weights);
        return Ssf.of(new Initialization(), new Dynamics(data), new Loading(data));
    }

    static class Data {

        final double[] weights;
        final HashSet<Integer> starts = new HashSet<>(), ends = new HashSet<>();
        final HashSet<Integer> astarts = new HashSet<>(), aends = new HashSet<>();
        int curpos = -1, curtype = -1, apos = -1, aggtype = -1;

        Data(int[] starts, int[] astarts, double[] weights) {
            this.weights = weights;
            for (int i = 0; i < starts.length; ++i) {
                int cur = starts[i];
                this.starts.add(cur);
                if (cur > 0) {
                    this.ends.add(cur - 1);
                }
            }
            for (int i = 0; i < astarts.length; ++i) {
                int cur = astarts[i];
                this.astarts.add(cur);
                if (cur > 0) {
                    this.aends.add(cur - 1);
                }
            }
        }

        private int posType(int pos) {
            if (curpos == pos) {
                return curtype;
            }
            curpos = pos;
            // 1 = last
            // 2 = first
            // 0 others
            if (starts.contains(pos)) {
                curtype = FIRST;
            } else if (ends.contains(pos)) {
                curtype = LAST;
            } else {
                curtype = DEF;
            }
            return curtype;
        }

        private int aggType(int pos) {
            if (apos == pos) {
                return aggtype;
            }
            apos = pos;
            // 1 = last
            // 2 = first
            // 0 others
            if (astarts.contains(pos)) {
                aggtype = FIRST;
            } else if (aends.contains(pos)) {
                aggtype = LAST;
            } else {
                aggtype = DEF;
            }
            return aggtype;
        }

        double weight(int pos) {
            return weights == null ? 1 : weights[pos];
        }

        double mweight(int pos, double m) {
            return weights == null ? m : weights[pos] * m;
        }

        double mweight2(int pos, double m) {
            return weights == null ? m : weights[pos] * weights[pos] * m;
        }

    }

    static class Initialization implements ISsfInitialization {

        @Override
        public int getStateDim() {
            return 3;
        }

        @Override
        public int getDiffuseDim() {
            return 1;
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
        public void Pf0(Matrix pf0) {
            pf0.set(2, 2, 1);
        }

        /**
         *
         * @param pi0
         */
        @Override
        public void Pi0(Matrix pi0) {
            pi0.set(2, 2, 1);
        }

        @Override
        public void diffuseConstraints(Matrix b) {
            b.set(2, 0, 1);
        }

        @Override
        public void a0(DataBlock a0) {
        }
    }

    static class Dynamics implements ISsfDynamics {

        private final Data info;

        Dynamics(Data info) {
            this.info = info;
        }

        /**
         *
         * @return
         */
        @Override
        public int getInnovationsDim() {
            return 1;
        }

        /**
         *
         * @param pos
         * @param qm
         */
        @Override
        public void V(int pos, Matrix qm) {
            qm.set(2, 2, 1);
        }

        /**
         * case I: pos+1 % c = 0. Last pos T=| 0 0 | | 0 1 | case II: pos % c =
         * 0. First pos T=| 0 w | | 0 1 | case III: others. Inside T=| 1 w | | 0
         * 1 |
         *
         * @param pos
         * @param tr
         */
        @Override
        public void T(int pos, Matrix tr) {
            tr.set(2, 2, 1);
            int postype = info.posType(pos);
            if (postype != LAST) {
                tr.set(0, 2, info.weight(pos));
                if (postype != FIRST) {
                    tr.set(0, 0, 1);
                }
            }
            int atype = info.aggType(pos);
            if (atype != LAST) {
                tr.set(1, 2, info.weight(pos));
                if (atype != FIRST) {
                    tr.set(1, 1, 1);
                }
            }
        }

        /**
         *
         * @param pos
         * @param x
         */
        @Override
        public void TX(int pos, DataBlock x) {
            // case I
            int postype = info.posType(pos);
            double s = info.mweight(pos, x.get(2));
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
            // case I
            int atype = info.aggType(pos);
            switch (atype) {
                case LAST:
                    x.set(1, 0);
                    break;
                case FIRST:
                    // case II.
                    x.set(1, s);
                    break;
                default:
                    // case III
                    x.add(1, s);
                    break;
            }
        }

        /**
         * |a0 0 a1| |0 b0 b1| |0 0 1|
         *
         * @param pos
         * @param x
         */
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
                    x.add(2, info.mweight(pos, x.get(0)));
                    x.set(0, 0);
                    break;
                default:
                    x.add(2, info.mweight(pos, x.get(0)));
                    break;
            }
            int atype = info.aggType(pos);
            if (atype == LAST) {
                x.set(1, 0);
            } // case II: 0, w x0 + x1
            else if (postype == FIRST) {
                x.add(2, info.mweight(pos, x.get(1)));
                x.set(1, 0);
            } // case III: x0, w x0 + x1
            else {
                x.add(2, info.mweight(pos, x.get(1)));
            }
        }

        @Override
        public void S(int pos, Matrix cm) {
            cm.set(2, 0, 1);
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
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
        public void addSU(int pos, DataBlock x, DataBlock u) {
            x.add(2, u.get(0));
        }

        @Override
        public void addV(int pos, Matrix p) {
            p.add(2, 2, 1);
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            xs.set(0, x.get(2));
        }

    }

    static class Loading implements ISsfLoading {

        private final Data info;

        Loading(Data info) {
            this.info = info;
        }

        /**
         *
         * @param pos
         * @param vm
         * @param d
         */
        @Override
        public void VpZdZ(int pos, Matrix vm, double d) {

            vm.add(2, 2, info.mweight2(pos, d));
            int postype = info.posType(pos);
            if (postype != FIRST) {
                double w = info.mweight(pos, d);
                vm.add(0, 0, d);
                vm.add(0, 2, w);
                vm.add(2, 0, w);
            }
        }

        /**
         *
         * @param pos
         * @param x
         * @param d
         */
        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            x.add(2, info.mweight(pos, d));
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
            z.set(2, info.weight(pos));
        }

        /**
         *
         * @param pos
         * @param m
         * @param x
         */
        @Override
        public void ZM(int pos, Matrix m, DataBlock x) {
            x.setAY(info.weight(pos), m.row(2));
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
        public double ZVZ(int pos, Matrix vm) {
            int postype = info.posType(pos);
            if (postype == FIRST) {
                return info.mweight2(pos, vm.get(2, 2));
            } else {
                double r = vm.get(0, 0);
                r += info.mweight(pos, 2 * vm.get(2, 0));
                r += info.mweight2(pos, vm.get(2, 2));
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
            return r + info.mweight(pos, x.get(2));
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }
    }

}
