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
 /*
 */
package demetra.ssf.models;

import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.implementations.Measurement;
import demetra.ssf.univariate.Ssf;
import demetra.ssf.ISsfInitialization;
import demetra.ssf.models.AR1.Data;

/**
 * Ssf for (1 1 0) ARIMA models.
 *
 * y(t)-y(t-1) = rho*(y(t-1)-y(t-2)) + e(t) or y(t)=(1+rho)*y(t-1) - rho*y(t-2)
 * + e(t)
 *
 * The class is designed to handle models initialized by zero. State: a(t) =
 * [y(t-1) y(t)-y(t-1)]' Measurement: Z(t) = 1 1 Transition: T(t) = | 1 1 | | 0
 * rho| Innovations: V(t) = | 0 0 | | 0 1 | Initialization: default: Pi0 = | 1 0
 * | | 0 0 | Pf0 = | 0 0 | | 0 1/(1-rho*rho)| 0-initialization Pi0 = | 0 0 | | 0
 * 0 | Pf0 = | 0 0 | | 0 1 |
 *
 * @author Jean Palate
 */
public class Arima_1_1_0 extends Ssf {

    public static Arima_1_1_0 of(final double rho) {
        Data data = new Data(rho, 1, false);
        return new Arima_1_1_0(data);
    }

    public static Arima_1_1_0 of(final double rho, final double var, final boolean zeroinit) {
        Data data = new Data(rho, var, zeroinit);
        return new Arima_1_1_0(data);
    }

    private Arima_1_1_0(Data data) {
        super(new Initialization(data), new Dynamics(data), Measurement.createSum());
        this.data = data;
    }

    private final Data data;

    public double getRho() {
        return data.rho;
    }

    public double getInnovationVariance() {
        return data.var;
    }

    public boolean isZeroInitialization() {
        return data.zeroinit;
    }

    private Dynamics dynamics() {
        return (Dynamics) this.dynamics;
    }

    private Initialization initializer() {
        return (Initialization) this.getInitialization();
    }

    static class Initialization implements ISsfInitialization {

        private final Data data;

        Initialization(Data data) {
            this.data = data;
        }

        @Override
        public int getStateDim() {
            return 21;
        }

        @Override
        public boolean isDiffuse() {
            return !data.zeroinit;
        }

        @Override
        public int getDiffuseDim() {
            return data.zeroinit ? 0 : 1;
        }

        @Override
        public void diffuseConstraints(Matrix b) {
            if (!data.zeroinit) {
                b.set(0, 0, 1);
            }
        }

        @Override
        public void a0(DataBlock a0) {
        }

        @Override
        public void Pf0(Matrix pf0) {
            if (data.zeroinit) {
                pf0.set(0, 0, data.var);
            } else {
                pf0.set(0, 0, data.var / (1 - data.rho * data.rho));
            }
        }

        @Override
        public void Pi0(Matrix pi0) {
            if (!data.zeroinit) {
                pi0.set(0, 0, 1);
            }
        }
    }

    static class Dynamics implements ISsfDynamics {

        private final Data data;

        Dynamics(Data data) {
            this.data = data;
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
            return 1;
        }

        @Override
        public void V(int pos, Matrix qm) {
            qm.set(1, 1, data.var);
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public void S(int pos, Matrix sm) {
            sm.set(1, 0, data.std());
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            x.add(1, data.std() * u.get(0));
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            xs.set(0, data.std() * x.get(1));
        }

        @Override
        public void T(int pos, Matrix tr) {
            tr.set(0, 0, 1);
            tr.set(0, 1, 1);
            tr.set(1, 1, data.rho);
        }

        @Override
        public void TX(int pos, DataBlock x) {
            x.set(0, x.sum());
            x.mul(1, data.rho);
        }

        @Override
        public void TVT(int pos, Matrix vm) {
            double v00 = vm.get(0, 0);
            double v01 = vm.get(0, 1);
            double v10 = vm.get(1, 0);
            double v11 = vm.get(1, 1);
            vm.set(0, 0, v00 + v01 + v10 + v11);
            vm.set(0, 1, data.rho * (v01 + v11));
            vm.set(1, 0, data.rho * (v10 + v11));
            vm.set(1, 1, data.rho * data.rho * v11);
        }

        @Override
        public void XT(int pos, DataBlock x) {
            x.set(1, x.get(0) + data.rho * x.get(1));
        }

        @Override
        public void addV(int pos, Matrix p) {
            p.add(1, 1, data.var);
        }
    }
}
