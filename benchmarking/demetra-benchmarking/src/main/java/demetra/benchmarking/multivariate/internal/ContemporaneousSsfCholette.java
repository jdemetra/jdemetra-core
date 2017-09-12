/*
* Copyright 2013 National Bank of Belgium
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
package demetra.benchmarking.multivariate.internal;

import demetra.data.DataBlock;
import demetra.design.Development;
import demetra.design.IBuilder;
import demetra.maths.matrices.Matrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.ISsfInitialization;
import demetra.ssf.multivariate.IMultivariateSsf;
import demetra.ssf.multivariate.ISsfMeasurements;
import demetra.ssf.multivariate.MultivariateSsf;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.experimental.UtilityClass
public class ContemporaneousSsfCholette {

    public Builder builder(int nvars) {
        return new Builder(nvars);
    }

    public static class Builder implements IBuilder<IMultivariateSsf> {

        private final int nvars;
        double rho = 1;
        double[][] w = null;
        Constraint[] constraints = null;

        private Builder(int nvars) {
            this.nvars = nvars;
        }

        public Builder rho(double rho) {
            this.rho = rho;
            return this;
        }

        public Builder weights(double[][] weights) {
            if (weights.length != nvars) {
                throw new IllegalArgumentException();
            }
            this.w = weights;
            return this;
        }

        public Builder constraints(Constraint[] constraints) {
            this.constraints = constraints;
            return this;
        }

        @Override
        public IMultivariateSsf build() {
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
        public void diffuseConstraints(Matrix b) {
            if (info.rho == 1) {
                b.diagonal().set(1);
            }
        }

        @Override
        public void a0(DataBlock a0) {
        }

        @Override
        public void Pf0(Matrix pf0) {
            if (info.rho != 1) {
                double v = 1 / (1 - info.rho * info.rho);
                pf0.diagonal().set(v);
            }
        }

        @Override
        public void Pi0(Matrix pi0) {
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
        public void V(int pos, Matrix qm) {
            qm.diagonal().set(1);
        }

        @Override
        public void S(int pos, Matrix cm) {
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
        public void T(int pos, Matrix tr) {
            tr.diagonal().set(info.rho);
        }

        @Override
        public void TX(int pos, DataBlock x) {
            x.mul(info.rho);
        }

        @Override
        public void TM(int pos, Matrix m) {
            m.mul(info.rho);
        }

        @Override
        public void TVT(int pos, Matrix v) {
            if (info.rho != 1) {
                v.mul(info.rho * info.rho);
            }
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            x.add(u);
        }

        @Override
        public void addV(int pos, Matrix p) {
            p.diagonal().add(1);
        }

        @Override
        public void XT(int pos, DataBlock x) {
            x.mul(info.rho);
        }

        @Override
        public void MT(int pos, Matrix m) {
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
        public int getCount(int pos) {
            return info.nm;
        }

        @Override
        public int getMaxCount() {
            return info.nm;
         }

        @Override
        public boolean isHomogeneous() {
            return true;
        }

        @Override
        public void Z(int pos, int v, DataBlock z) {
            Constraint cnt = info.constraints[v];
            for (int i = 0; i < cnt.index.length; ++i) {
                int l = cnt.index[i];
                z.set(l, info.mweight(pos, l, cnt.weights[i]));
            }
        }

        @Override
        public boolean hasErrors() {
            return false;
        }

        @Override
        public boolean hasIndependentErrors() {
            return true;
        }

        @Override
        public boolean hasError(int pos) {
            return false;
        }

        @Override
        public void H(int pos, Matrix h) {
        }

        @Override
        public void R(int pos, Matrix r) {
        }

        @Override
        public double ZX(int pos, int v, DataBlock x) {
            Constraint cnt = info.constraints[v];
            double sum = 0;
            for (int i = 0; i < cnt.index.length; ++i) {
                int l = cnt.index[i];
                sum += info.mweight(pos, l, x.get(l) * cnt.weights[i]);
            }
            return sum;
        }

        @Override
        public void ZM(int pos, int v, Matrix m, DataBlock x) {
            x.set(0);
            Constraint cnt = info.constraints[v];
            for (int i = 0; i < cnt.index.length; ++i) {
                int l = cnt.index[i];
                x.addAY(info.mweight(pos, l, cnt.weights[i]), m.row(l));
            }
        }

        @Override
        public double ZVZ(int pos, int v, int w, Matrix vm) {
            Constraint vcnt = info.constraints[v];
            Constraint wcnt = info.constraints[w];
            double s = 0;
            for (int i = 0; i < vcnt.index.length; ++i) {
                int k = vcnt.index[i];
                double dk = info.mweight(pos, k, vcnt.weights[i]);
                for (int j = 0; j < wcnt.index.length; ++j) {
                    int l = wcnt.index[j];
                    double dl = info.mweight(pos, l, wcnt.weights[j]);
                    s += dk * vm.get(k, l) * dl;
                }
            }
            return s;
        }

        @Override
        public void addH(int pos, Matrix V) {
        }

        @Override
        public void VpZdZ(int pos, int v, int w, Matrix vm, double d) {
            Constraint vcnt = info.constraints[v];
            Constraint wcnt = info.constraints[w];
            for (int i = 0; i < vcnt.index.length; ++i) {
                int k = vcnt.index[i];
                double dk = info.mweight(pos, k, vcnt.weights[i]);
                for (int j = 0; j < wcnt.index.length; ++j) {
                    int l = wcnt.index[j];
                    double dl = info.mweight(pos, l, wcnt.weights[j]);
                    vm.add(k, l, d * dk * dl);
                }
            }
        }

        @Override
        public void XpZd(int pos, int v, DataBlock x, double d) {
            Constraint cnt = info.constraints[v];
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
