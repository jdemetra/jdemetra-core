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

package demetra.benchmarking.ssf;

import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.design.IBuilder;
import demetra.maths.matrices.Matrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.ISsfInitialization;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfMeasurement;
import demetra.ssf.univariate.Ssf;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SsfDenton {

    public static class Builder implements IBuilder<ISsf> {

        private double[] weights = null;
        private int start = 0;
        private final int conversion;

        private Builder(int conversion) {
            this.conversion = conversion;
        }

        @Override
        public ISsf build() {
            DentonDefinition def = new DentonDefinition(conversion, start, weights);
            return new Ssf(new Initialization(def), new Dynamics(def), new Measurement(def));
        }

        public Builder weights(DoubleSequence weights) {
            if (weights != null) {
                this.weights = weights.toArray();
            } else {
                weights = null;
            }
            return this;
        }

        public Builder start(int start) {
            this.start = start;
            return this;
        }

    }

    public static Builder builder(int conversion) {
        return new Builder(conversion);
    }

    static class DentonDefinition {

        /**
         *
         */
        final double[] weights;
        final int start, conversion;

        /**
         *
         * @param conv
         * @param rho
         * @param w
         */
        DentonDefinition(int conv, int start, double[] w) {
            this.conversion = conv;
            this.weights = w;
            this.start = start;
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

    static class Measurement implements ISsfMeasurement {

        private final DentonDefinition def;

        Measurement(DentonDefinition def) {
            this.def = def;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            int rpos = pos + def.start;
            if (rpos % def.conversion == 0) {
                z.set(0, 0);
            } else {
                z.set(0, 1);
            }
            z.set(1, def.weight(pos));
        }

        @Override
        public boolean hasErrors() {
            return false;
        }

        @Override
        public boolean areErrorsTimeInvariant() {
            return true;
        }

        @Override
        public boolean hasError(int pos) {
            return false;
        }

        @Override
        public double errorVariance(int pos) {
            return 0;
        }

        @Override
        public double ZX(int pos, DataBlock x) {
            int rpos = pos + def.start;
            double r = (rpos % def.conversion == 0) ? 0 : x.get(0);
            return r + def.mweight(pos, x.get(1));
        }

        @Override
        public double ZVZ(int pos, Matrix V) {
            int rpos = pos + def.start;
            if (rpos % def.conversion == 0) {
                return def.mweight2(pos, V.get(1, 1));
            } else {
                double r = V.get(0, 0);
                r += def.mweight(pos, 2 * V.get(1, 0));
                r += def.mweight2(pos, V.get(1, 1));
                return r;
            }
        }

        @Override
        public void VpZdZ(int pos, Matrix vm, double d) {
            int rpos = pos + def.start;
            vm.add(1, 1, def.mweight2(pos, d));
            if (rpos % def.conversion != 0) {
                double w = def.mweight(pos, d);
                vm.add(0, 0, d);
                vm.add(0, 1, w);
                vm.add(1, 0, w);
            }
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            int rpos = pos + def.start;
            x.add(1, def.mweight(pos, d));
            if (rpos % def.conversion != 0) {
                x.add(0, d);
            }
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }
    }

    static class Initialization implements ISsfInitialization {

        private final DentonDefinition def;

        Initialization(DentonDefinition def) {
            this.def = def;
        }

        @Override
        public int getStateDim() {
            return 2;
        }

        @Override
        public boolean isDiffuse() {
            return true;
        }

        @Override
        public int getDiffuseDim() {
            return 1;
        }

        @Override
        public void diffuseConstraints(Matrix b) {
            b.set(1, 0, 1);
        }

        @Override
        public void a0(DataBlock a0) {
        }

        @Override
        public void Pf0(Matrix pf0) {
            pf0.set(1, 1, 1);
        }

        @Override
        public void Pi0(Matrix pi0) {
            pi0.set(1, 1, 1);
        }
    }

    static class Dynamics implements ISsfDynamics {

        private final DentonDefinition def;

        Dynamics(DentonDefinition def) {
            this.def = def;
        }

        @Override
        public int getInnovationsDim() {
            return 1;
        }

        @Override
        public void V(int pos, Matrix qm) {
            qm.set(1, 1, 1);
        }

        @Override
        public void S(int pos, Matrix cm) {
            cm.set(1, 1, 1);
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        /**
         * case I: pos+1 % c = 0 T=| 0 0 | | 0 1 | case II: pos % c = 0 T=| 0 w
         * | | 0 1 | case III: others T=| 1 w | | 0 1 |
         *
         * @param pos
         * @param tr
         */
        @Override
        public void T(int pos, Matrix tr) {
            tr.set(1, 1, 1);
            int rpos = pos + def.start;
            if ((rpos + 1) % def.conversion != 0) {
                tr.set(0, 1, def.weight(pos));
                if (rpos % def.conversion != 0) {
                    tr.set(0, 0, 1);
                }
            }
        }

        @Override
        public void TX(int pos, DataBlock x) {
            // case I
            int rpos = pos + def.start;
            if ((rpos + 1) % def.conversion == 0) {
                x.set(0, 0);
            } else if (rpos % def.conversion == 0) {
                // case II.
                double s = x.get(1);
                x.set(0, def.mweight(pos, s));
            } else {
                // case III
                double s = x.get(1);
                x.add(0, def.mweight(pos, s));
            }
        }

        @Override
        public void TVT(int pos, Matrix vm) {
            int rpos = pos + def.start;
            if ((rpos + 1) % def.conversion == 0) {
                vm.set(0, 0, 0);
                vm.set(1, 0, 0);
                vm.set(0, 1, 0);
            } else if (rpos % def.conversion == 0) {
                double w = def.weight(pos);
                double v = w * vm.get(1, 1);
                vm.set(0, 0, w * v);
                vm.set(1, 0, v);
                vm.set(0, 1, v);
            } else {
                double w = def.weight(pos);
                double v11 = vm.get(1, 1);
                double v01 = vm.get(0, 1);
                double z = (v01 + w * v11);
                vm.set(0, 1, z);
                vm.set(1, 0, z);
                vm.add(0, 0, w * (2 * v01 + w * v11));
            }
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            x.add(1, u.get(0));
        }

        @Override
        public void addV(int pos, Matrix p) {
            p.add(1, 1, 1);
        }

        @Override
        public void XT(int pos, DataBlock x) {
            int rpos = pos + def.start;
            // case I: 0, x1
            if ((rpos + 1) % def.conversion == 0) {
                x.set(0, 0);
            } // case II: 0, w x0 + x1
            else if (rpos % def.conversion == 0) {
                double x0 = x.get(0), x1 = x.get(1);
                x.set(1, x1 + def.mweight(pos, x0));
                x.set(0, 0);
            } // case III: x0, w x0 + x1
            else {
                double x0 = x.get(0), x1 = x.get(1);
                x.set(1, x1 + def.mweight(pos, x0));
            }
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            x.set(0, 0);
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }

        @Override
        public boolean areInnovationsTimeInvariant() {
            return true;
        }
    }
}
