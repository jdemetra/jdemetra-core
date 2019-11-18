/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.benchmarking.multivariate;

import jdplus.data.DataBlock;
import demetra.design.BuilderPattern;
import demetra.design.Development;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.ISsfInitialization;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.multivariate.IMultivariateSsf;
import jdplus.ssf.multivariate.ISsfErrors;
import jdplus.ssf.multivariate.ISsfMeasurements;
import jdplus.ssf.multivariate.MultivariateSsf;
import jdplus.math.matrices.lapack.FastMatrix;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.experimental.UtilityClass
class ContemporaneousSsfCholette {

    Builder builder(int nvars) {
        return new Builder(nvars);
    }

    @BuilderPattern(IMultivariateSsf.class)
    static class Builder {

        private final int nvars;
        private double rho = 1;
        private double[][] w = null;
        private Constraint[] constraints = null;

        private Builder(int nvars) {
            this.nvars = nvars;
        }

        Builder rho(double rho) {
            this.rho = rho;
            return this;
        }

        Builder weights(double[][] weights) {
            if (weights.length != nvars) {
                throw new IllegalArgumentException();
            }
            this.w = weights;
            return this;
        }

        Builder constraints(Constraint[] constraints) {
            this.constraints = constraints;
            return this;
        }

        IMultivariateSsf build() {
            Data data = new Data(nvars, rho, w, constraints);
            return new MultivariateSsf(new Initialization(data), new Dynamics(data), new Measurements(data));
        }

    }

    static class Data {

        final double rho;
        final int nvars, nm;
        final double[][] w;
        final Constraint[] constraints;

        Data(int nvars, double rho, double[][] weights, Constraint[] constraints) {
            this.nvars = nvars;
            this.rho = rho;
            this.w = weights;
            this.constraints = constraints;
            this.nm = constraints.length;
        }

        double weight(int pos, int v) {
            return w == null ? 1 : w[v][pos];
        }

        double mweight(int pos, int v, double m) {
            return w == null ? m : w[v][pos] * m;
        }
    }

    static class Initialization implements ISsfInitialization {

        final Data info;

        Initialization(Data info) {
            this.info = info;
        }

        @Override
        public int getStateDim() {
            return info.nvars;
        }

        @Override
        public boolean isDiffuse() {
            return info.rho == 1;
        }

        @Override
        public int getDiffuseDim() {
            return info.rho == 1 ? info.nvars : 0;
        }

        @Override
        public void diffuseConstraints(FastMatrix b) {
            if (info.rho == 1) {
                b.diagonal().set(1);
            }
        }

        @Override
        public void a0(DataBlock a0) {
        }

        @Override
        public void Pf0(FastMatrix pf0) {
            if (info.rho != 1) {
                double v = 1 / (1 - info.rho * info.rho);
                pf0.diagonal().set(v);
            }
        }

        @Override
        public void Pi0(FastMatrix pi0) {
            if (info.rho == 1) {
                pi0.diagonal().set(1);
            }
        }
    }

    static class Dynamics implements ISsfDynamics {

        final Data info;

        Dynamics(Data info) {
            this.info = info;
        }

        @Override
        public int getInnovationsDim() {
            return info.nvars;
        }

        @Override
        public void V(int pos, FastMatrix qm) {
            qm.diagonal().set(1);
        }

        @Override
        public void S(int pos, FastMatrix cm) {
            cm.diagonal().set(1);
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
            tr.diagonal().set(info.rho);
        }

        @Override
        public void TX(int pos, DataBlock x) {
            x.mul(info.rho);
        }

        @Override
        public void TM(int pos, FastMatrix m) {
            m.mul(info.rho);
        }

        @Override
        public void TVT(int pos, FastMatrix v) {
            if (info.rho != 1) {
                v.mul(info.rho * info.rho);
            }
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            x.add(u);
        }

        @Override
        public void addV(int pos, FastMatrix p) {
            p.diagonal().add(1);
        }

        @Override
        public void XT(int pos, DataBlock x) {
            x.mul(info.rho);
        }

        @Override
        public void MT(int pos, FastMatrix m) {
            m.mul(info.rho);
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            xs.copy(x);
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }
    }

    static class Measurements implements ISsfMeasurements {

        final Data info;

        Measurements(Data info) {
            this.info = info;
        }

        @Override
        public int getCount() {
            return info.nm;
        }

        @Override
        public ISsfLoading loading(int equation) {
            return new Loading(info, equation);
       }

        @Override
        public ISsfErrors errors() {
            return null;
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

    }
    
    static class Loading implements ISsfLoading{
        
        private final Constraint cnt;
        private final Data info;
        
        Loading(Data info, int eq){
            this.info=info;
            this.cnt=info.constraints[eq];
        }
        
        @Override
        public void Z(int pos, DataBlock z) {
             for (int i = 0; i < cnt.index.length; ++i) {
                int l = cnt.index[i];
                z.set(l, info.mweight(pos, l, cnt.weights[i]));
            }
        }
 
        @Override
        public double ZX(int pos, DataBlock x) {
            double sum = 0;
            for (int i = 0; i < cnt.index.length; ++i) {
                int l = cnt.index[i];
                sum += info.mweight(pos, l, x.get(l) * cnt.weights[i]);
            }
            return sum;
        }

        @Override
        public void ZM(int pos, FastMatrix m, DataBlock x) {
            x.set(0);
            for (int i = 0; i < cnt.index.length; ++i) {
                int l = cnt.index[i];
                x.addAY(info.mweight(pos, l, cnt.weights[i]), m.row(l));
            }
        }

        @Override
        public double ZVZ(int pos, FastMatrix vm) {
            double s = 0;
            for (int i = 0; i < cnt.index.length; ++i) {
                int k = cnt.index[i];
                double dk = info.mweight(pos, k, cnt.weights[i]);
                for (int j = 0; j < cnt.index.length; ++j) {
                    int l = cnt.index[j];
                    double dl = info.mweight(pos, l, cnt.weights[j]);
                    s += dk * vm.get(k, l) * dl;
                }
            }
            return s;
        }

        @Override
        public void VpZdZ(int pos, FastMatrix vm, double d) {
            for (int i = 0; i < cnt.index.length; ++i) {
                int k = cnt.index[i];
                double dk = info.mweight(pos, k, cnt.weights[i]);
                for (int j = 0; j < cnt.index.length; ++j) {
                    int l = cnt.index[j];
                    double dl = info.mweight(pos, l, cnt.weights[j]);
                    vm.add(k, l, d * dk * dl);
                }
            }
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            for (int i = 0; i < cnt.index.length; ++i) {
                int k = cnt.index[i];
                x.add(k, info.mweight(pos, k, cnt.weights[i] * d));
            }
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }
        
    }

}
