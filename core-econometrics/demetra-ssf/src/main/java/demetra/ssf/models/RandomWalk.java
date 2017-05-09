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
 *
 * @author Jean Palate
 */
public class RandomWalk extends Ssf {

    public RandomWalk(final double var, final boolean zeroinit) {
        super(new Dynamics(var, zeroinit), Measurement.create(1, 0));
    }

    public RandomWalk() {
        super(new Dynamics(1, false), Measurement.create(1, 0));
    }

    private Dynamics dynamics() {
        return (Dynamics) this.dynamics;
    }

    public double getInnovationVariance() {
        return dynamics().var;
    }

    public boolean isZeroInitialization() {
        return dynamics().zeroinit;
    }

    public static class Dynamics implements ISsfDynamics {

        private final boolean zeroinit;
        private final double var;

        public Dynamics(double var) {
            this.var = var;
            this.zeroinit = false;
        }
        
        public Dynamics(double var, boolean zeroinit) {
            this.var = var;
            this.zeroinit = zeroinit;
        }

        private double std() {
            return var == 1 ? 1 : Math.sqrt(var);
        }

        public boolean isZeroInit() {
            return zeroinit;
        }

        public double getVar() {
            return var;
        }

        @Override
        public int getStateDim() {
            return 1;
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
            qm.set(0, 0, var);
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public void S(int pos, Matrix sm) {
            sm.set(0, 0, std());
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            x.add(0, std() * u.get(0));
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            xs.set(0, std() * x.get(0));
        }

        @Override
        public void T(int pos, Matrix tr) {
            tr.set(0, 0, 1);
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
            pf0.set(0, 0, var);
            return true;
        }

        @Override
        public void Pi0(Matrix pi0) {
            if (!zeroinit) {
                pi0.set(0, 0, var);
            }
        }

        @Override
        public void TX(int pos, DataBlock x) {
        }

        @Override
        public void TVT(int pos, Matrix x) {
        }

        @Override
        public void XT(int pos, DataBlock x) {
        }

        @Override
        public void addV(int pos, Matrix p) {
            p.add(0, 0, var);
        }
    }
}
