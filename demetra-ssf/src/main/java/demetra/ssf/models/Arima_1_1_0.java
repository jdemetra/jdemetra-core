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

    public Arima_1_1_0(final double rho, final double var, final boolean zeroinit) {
        super(new Dynamics(rho, var, zeroinit), Measurement.createSum(2));
    }

    private Dynamics dynamics() {
        return (Dynamics) this.dynamics;
    }

    public double getRho() {
        return dynamics().rho;
    }

    public double getInnovationVariance() {
        return dynamics().var;
    }

    public boolean isZeroInitialization() {
        return dynamics().zeroinit;
    }

    static class Dynamics implements ISsfDynamics {

        private final boolean zeroinit;
        private final double rho;
        private final double var;

        Dynamics(double rho, double var, boolean zeroinit) {
            this.rho = rho;
            this.var = var;
            this.zeroinit = zeroinit;
        }

        private double std() {
            return var == 1 ? 1 : Math.sqrt(var);
        }

        boolean isZeroInit() {
            return zeroinit;
        }

        double getRho() {
            return rho;
        }

        double getVar() {
            return var;
        }

        @Override
        public int getStateDim() {
            return 2;
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public boolean isValid() {
            return var > 0;
        }

        @Override
        public int getInnovationsDim() {
            return 1;
        }

        @Override
        public void V(int pos, Matrix qm) {
            qm.set(1, 1, var);
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public void S(int pos, Matrix sm) {
            sm.set(1, 0, std());
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            x.add(1, std() * u.get(0));
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            xs.set(0, std() * x.get(1));
        }

//        @Override
//        public void addSX(int pos, DataBlock x, DataBlock y) {
//             y.add(1, x.get(0));
//        }
//        
        @Override
        public void T(int pos, Matrix tr) {
            tr.set(0, 0, 1);
            tr.set(0, 1, 1);
            tr.set(1, 1, rho);
        }

        @Override
        public boolean isDiffuse() {
            return !zeroinit;
        }

        @Override
        public int getNonStationaryDim() {
            return zeroinit ? 0 : 1;
        }

        @Override
        public void diffuseConstraints(Matrix b) {
            if (!zeroinit) {
                b.set(0, 0, 1);
            }
        }

        @Override
        public boolean a0(DataBlock a0) {
            return true;
        }

        @Override
        public boolean Pf0(Matrix pf0) {
            if (zeroinit) {
                pf0.set(0, 0, var);
            } else {
                pf0.set(0, 0, var / (1 - rho * rho));
            }
            return true;
        }

        @Override
        public void Pi0(Matrix pi0) {
            if (!zeroinit) {
                pi0.set(0, 0, 1);
            }
        }

        @Override
        public void TX(int pos, DataBlock x) {
            x.set(0, x.sum());
            x.mul(1, rho);
        }

        @Override
        public void TVT(int pos, Matrix vm) {
            double v00 = vm.get(0, 0);
            double v01 = vm.get(0, 1);
            double v10 = vm.get(1, 0);
            double v11 = vm.get(1, 1);
            vm.set(0, 0, v00 + v01 + v10 + v11);
            vm.set(0, 1, rho * (v01 + v11));
            vm.set(1, 0, rho * (v10 + v11));
            vm.set(1, 1, rho * rho * v11);
        }

        @Override
        public void XT(int pos, DataBlock x) {
            x.set(1, x.get(0) + rho * x.get(1));
        }

        @Override
        public void addV(int pos, Matrix p) {
            p.add(1, 1, var);
        }
    }
}
