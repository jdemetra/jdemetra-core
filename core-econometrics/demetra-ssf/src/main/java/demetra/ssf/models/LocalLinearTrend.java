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
package demetra.ssf.models;

import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.ISsfInitialization;
import demetra.ssf.implementations.Measurement;
import demetra.ssf.univariate.Ssf;

/**
 * Usual local linear trend y(t)=l(t)+n(t) l(t+1)=s(t)+l(t)+u(t)
 * s(t+1)=s(t)+v(t)
 *
 * @author Jean Palate
 */
public class LocalLinearTrend extends Ssf {

    public static LocalLinearTrend of(double lvar, double svar, double nvar) {
        Data data = new Data(lvar, svar, nvar);
        return new LocalLinearTrend(data);
    }

    private LocalLinearTrend(Data data) {
        super(new Initialization(data), new Dynamics(data), Measurement.create(0, data.nv));
        this.data = data;
    }

    private final Data data;

    static class Data {

        final double lv, sv, nv;

        Data(final double lv, final double sv, final double nv) {
            this.lv = lv;
            this.sv = sv;
            this.nv = nv;
        }
    }

    public double getVariance() {
        return data.lv;
    }

    public double getSlopeVariance() {
        return data.sv;
    }

    public double getNoiseVariance() {
        return data.nv;
    }

    public static class Initialization implements ISsfInitialization {

        private final Data data;

        Initialization(Data data) {
            this.data = data;
        }

        @Override
        public boolean isValid() {
            return data.lv >= 0 && data.sv >= 0;
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
            return 2;
        }

        @Override
        public void diffuseConstraints(Matrix b) {
            b.diagonal().set(1);
        }

        @Override
        public boolean a0(DataBlock a0) {
            return true;
        }

        @Override
        public boolean Pf0(Matrix pf0) {
            if (data.lv > 0) {
                pf0.set(0, 0, data.lv);
            }
            if (data.sv > 0) {
                pf0.set(1, 1, data.sv);
            }
            return true;
        }

        @Override
        public void Pi0(Matrix pi0) {
            pi0.diagonal().set(1);
        }

    }

    public static class Dynamics implements ISsfDynamics {

        private final Data data;

        Dynamics(Data data) {
            this.data = data;
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public int getInnovationsDim() {
            int n = 0;
            if (data.lv > 0) {
                ++n;
            }
            if (data.sv > 0) {
                ++n;
            }
            return n;
        }

        @Override
        public void V(int pos, Matrix qm) {
            if (data.lv > 0) {
                qm.set(0, 0, data.lv);
            }
            if (data.sv > 0) {
                qm.set(1, 1, data.sv);
            }
        }

        @Override
        public boolean hasInnovations(int pos) {
            return data.lv != 0 || data.sv != 0;
        }

        @Override
        public void S(int pos, Matrix s) {
            if (data.lv != 0 && data.sv != 0) {
                s.set(0, 0, Math.sqrt(data.lv));
                s.set(1, 1, Math.sqrt(data.sv));
            } else if (data.lv != 0) {
                s.set(1, 0, Math.sqrt(data.lv));
            } else if (data.sv != 0) {
                s.set(0, 1, Math.sqrt(data.sv));
            }
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            if (data.lv != 0 && data.sv != 0) {
                x.add(0, Math.sqrt(data.lv) * u.get(0));
                x.add(1, Math.sqrt(data.sv) * u.get(1));
            } else if (data.lv != 0) {
                x.add(0, Math.sqrt(data.lv) * u.get(0));
            } else if (data.sv != 0) {
                x.add(1, Math.sqrt(data.sv) * u.get(0));
            }
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            if (data.sv != 0 && data.lv != 0) {
                xs.set(0, Math.sqrt(data.lv) * x.get(0));
                xs.set(1, Math.sqrt(data.sv) * x.get(1));
            } else if (data.lv != 0) {
                xs.set(0, Math.sqrt(data.lv) * x.get(0));
            } else if (data.sv != 0) {
                xs.set(0, Math.sqrt(data.sv) * x.get(1));
            }
        }

        @Override
        public void T(int pos, Matrix tr) {
            tr.set(0, 0, 1);
            tr.set(0, 1, 1);
            tr.set(1, 1, 1);
        }

        @Override
        public void TX(int pos, DataBlock x) {
            x.add(0, x.get(1));
        }

        @Override
        public void TVT(int pos, Matrix vm) {
            double v01 = vm.get(0, 1), v11 = vm.get(1, 1);
            vm.add(0, 0, 2 * v01 + v11);
            vm.add(0, 1, v11);
            vm.add(1, 0, v11);
        }

        @Override
        public void XT(int pos, DataBlock x) {
            x.add(1, x.get(0));
        }

        @Override
        public void addV(int pos, Matrix p) {
            if (data.lv > 0) {
                p.add(0, 0, data.lv);
            }
            if (data.sv > 0) {
                p.add(1, 1, data.sv);
            }
        }

    }
}
