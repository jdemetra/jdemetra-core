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
import demetra.design.BuilderPattern;
import demetra.design.Development;
import demetra.maths.matrices.Matrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.ISsfInitialization;
import demetra.ssf.ISsfLoading;
import demetra.ssf.multivariate.IMultivariateSsf;
import demetra.ssf.multivariate.ISsfErrors;
import demetra.ssf.multivariate.ISsfMeasurements;
import demetra.ssf.multivariate.MultivariateSsf;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.experimental.UtilityClass
public class MultivariateSsfCholette {

    public Builder builder(int nvars) {
        return new Builder(nvars);
    }

    @BuilderPattern(IMultivariateSsf.class)
    public static class Builder {

        private final int nvars;
        private int conversion = 4;
        double rho = 1;
        double[][] w = null;
        Constraint[] constraints = null;

        private Builder(int nvars) {
            this.nvars = nvars;
        }

        public Builder conversion(int c) {
            this.conversion = c;
            return this;
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

        public IMultivariateSsf build() {
            Data data = new Data(nvars, conversion, rho, w, constraints);
            return new MultivariateSsf(new Initialization(data), new Dynamics(data), new Measurements(data));
        }

    }

    static class Data {

        final int c;
        final double rho;
        final int nvars;
        final double[][] w;
        final Constraint[] constraints;

        Data(int nvars, int c, double rho, double[][] weights, Constraint[] constraints) {
            this.nvars = nvars;
            this.c = c;
            this.rho = rho;
            this.w = weights;
            this.constraints = constraints;
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
            return 2 * info.nvars;
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
                for (int j = 1, k = 0; j < 2 * info.nvars; j += 2, ++k) {
                    b.set(j, k, 1);
                }
            }
        }

        @Override
        public void a0(DataBlock a0) {
        }

        @Override
        public void Pf0(Matrix pf0) {
            if (info.rho != 1) {
                double v = 1 / (1 - info.rho * info.rho);
                pf0.diagonal().extract(1, -1, 2).set(v);
            }
        }

        @Override
        public void Pi0(Matrix pi0) {
            if (info.rho == 1) {
                pi0.diagonal().extract(1, -1, 2).set(1);
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
            qm.diagonal().extract(1, -1, 2).set(1);
        }

        @Override
        public void S(int pos, Matrix cm) {
            for (int i = 0; i < info.nvars; ++i) {
                cm.set(2 * i + 1, i, 1);
            }
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
            // TO DO : optimization
            for (int i = 0; i < 2 * info.nvars; i += 2) {
                tr.set(i + 1, i + 1, info.rho);
                if ((pos + 1) % info.c != 0) {
                    tr.set(i, i + 1, info.weight(pos, i));
                    if (pos % info.c != 0) {
                        tr.set(i, i, 1);
                    }
                }
            }
        }

        @Override
        public void TX(int pos, DataBlock x) {
            // TO DO : optimization
            for (int i = 0, j = 0; i < info.nvars; ++i, j += 2) {
                // case I
                if ((pos + 1) % info.c == 0) {
                    x.set(j, 0);
                } else if (pos % info.c == 0) {
                    // case II.
                    double s = x.get(j + 1);
                    x.set(j, info.mweight(pos, i, s));
                } else {
                    // case III
                    double s = x.get(j + 1);
                    x.add(j, info.mweight(pos, i, s));
                }
                x.mul(j + 1, info.rho);
            }
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            x.extract(1, -1, 2).add(u);
        }

        @Override
        public void addV(int pos, Matrix p) {
            p.diagonal().extract(1, -1, 2).add(1);
        }

        @Override
        public void XT(int pos, DataBlock x) {
            // TO DO : optimization
            for (int i = 0, j = 0; i < info.nvars; ++i, j += 2) {
                // case I: 0, x1
                if ((pos + 1) % info.c == 0) {
                    x.set(j, 0);
                    x.mul(j + 1, info.rho);
                } // case II: 0, w x0 + x1
                else if (pos % info.c == 0) {
                    double x0 = x.get(j), x1 = x.get(j + 1);
                    x.set(j + 1, info.rho * x1 + info.mweight(pos, i, x0));
                    x.set(j, 0);
                } // case III: x0, w x0 + x1
                else {
                    double x0 = x.get(j), x1 = x.get(j + 1);
                    x.set(j + 1, info.rho * x1 + info.mweight(pos, i, x0));
                }
            }
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            xs.copy(x.extract(1, -1, 2));
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }
    }

    static class Measurements implements ISsfMeasurements {

        final Data info;

        Measurements(Data info) {
            this.info = info;
        }

        @Override
        public int getCount() {
            if (info.constraints == null) {
                return info.nvars;
            } else {
                return info.constraints.length + info.nvars;
            }
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

    static class Loading implements ISsfLoading {

        final Data info;
        final int v;

        Loading(Data info, int v) {
            this.info = info;
            this.v = v;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            if (v < info.nvars) {
                int iv = 2 * v;
                if ((pos + 1) % info.c == 0) {
                    z.set(iv, 1);
                }
                z.set(iv + 1, info.weight(pos, v));
            } else {
                int k = v - info.nvars;
                Constraint cnt = info.constraints[k];
                for (int i = 0; i < cnt.index.length; ++i) {
                    int l = cnt.index[i];
                    int il = 2 * l;
                    z.set(il + 1, info.mweight(pos, l, cnt.weights[i]));
                }
            }
        }

        @Override
        public double ZX(int pos, DataBlock x) {
            if (v < info.nvars) {
                int iv = 2 * v;
                double r = ((pos + 1) % info.c != 0) ? 0 : x.get(iv);
                return r + info.mweight(pos, v, x.get(iv + 1));
            } else {
                int k = v - info.nvars;
                Constraint cnt = info.constraints[k];
                double sum = 0;
                for (int i = 0; i < cnt.index.length; ++i) {
                    int l = cnt.index[i];
                    int il = 2 * l;
                    sum += info.mweight(pos, l, x.get(il + 1) * cnt.weights[i]);
                }
                return sum;
            }
        }

        @Override
        public void ZM(int pos, Matrix m, DataBlock x) {
            if (v < info.nvars) {
                int iv = 2 * v;
                if ((pos + 1) % info.c == 0) {
                    x.copy(m.row(iv));
                }
                x.addAY(info.weight(pos, v), m.row(iv + 1));
            } else {
                x.set(0);
                int k = v - info.nvars;
                Constraint cnt = info.constraints[k];
                for (int i = 0; i < cnt.index.length; ++i) {
                    int l = cnt.index[i];
                    int il = 2 * l;
                    x.addAY(info.mweight(pos, l, cnt.weights[i]), m.row(il + 1));
                }
            }
        }

        @Override
        public double ZVZ(int pos, Matrix vm) {
            int iv = 2 * v;
            if (v < info.nvars) {
                double dv = info.weight(pos, v), dw = info.weight(pos, v);
                double s = dv * vm.get(iv + 1, iv + 1) * dw;
                if ((pos + 1) % info.c == 0) {
                    s += vm.get(iv, iv);
                    s += dv * vm.get(iv + 1, iv);
                    s += dw * vm.get(iv, iv + 1);
                }
                return s;
            } else {
                int w = v-info.nvars;
                Constraint cnt = info.constraints[w];
                double s = 0;
                for (int i = 0; i < cnt.index.length; ++i) {
                    int k = cnt.index[i];
                    int ik = 2 * k;
                    double dk = info.mweight(pos, k, cnt.weights[i]);
                    for (int j = 0; j < cnt.index.length; ++j) {
                        int l = cnt.index[j];
                        int il = 2 * l;
                        double dl = info.mweight(pos, l, cnt.weights[j]);
                        s += dk * vm.get(ik + 1, il + 1) * dl;
                    }
                }
                return s;
            }
        }


        @Override
        public void VpZdZ(int pos, Matrix vm, double d) {
            int iv = 2 * v;
            if (v < info.nvars) {
                double dv = info.weight(pos, v), dw = info.weight(pos, v);
                vm.add(iv + 1, iv + 1, dv * d * dw);
                if ((pos + 1) % info.c == 0) {
                    vm.add(iv, iv, d);
                    vm.add(iv, iv + 1, d * dw);
                    vm.add(iv + 1, iv, d * dv);
                }
            }  else {
                int w = v-info.nvars;
                Constraint cnt = info.constraints[w];
                for (int i = 0; i < cnt.index.length; ++i) {
                    int k = cnt.index[i];
                    int ik = 2 * k;
                    double dk = info.mweight(pos, k, cnt.weights[i]);
                    for (int j = 0; j < cnt.index.length; ++j) {
                        int l = cnt.index[j];
                        int il = 2 * l;
                        double dl = info.mweight(pos, l, cnt.weights[j]);
                        vm.add(ik + 1, il + 1, d * dk * dl);
                    }
                }
            }
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            if (v < info.nvars) {
                int iv = 2 * v;
                x.add(iv + 1, info.mweight(pos, v, d));
                if ((pos + 1) % info.c == 0) {
                    x.add(iv, d);
                }
            } else {
                int w= v- info.nvars;
                Constraint cnt = info.constraints[w];
                for (int i = 0; i < cnt.index.length; ++i) {
                    int k = cnt.index[i];
                    int ik = 2 * k;
                    x.add(ik + 1, info.mweight(pos, k, cnt.weights[i] * d));
                }
            }
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }
    }

}
