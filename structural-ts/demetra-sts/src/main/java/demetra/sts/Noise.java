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
package demetra.sts;

import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.ISsfInitialization;
import demetra.ssf.univariate.ISsfMeasurement;
import demetra.ssf.univariate.Ssf;


/**
 *
 * @author Jean Palate
 */
public class Noise extends Ssf {

    public Noise(final double var) {
        super(new Initialization(var), new Dynamics(var), new Measurement());
    }

    static class Initialization implements ISsfInitialization {

        private final double var;

        Initialization(final double var) {
            this.var = var;
        }
        
        @Override
        public int getStateDim() {
            return 1;
        }

        @Override
        public boolean isDiffuse() {
            return false;
        }

        @Override
        public int getDiffuseDim() {
            return 0;
        }

        @Override
        public void diffuseConstraints(Matrix b) {
        }

        @Override
        public void a0(DataBlock a0) {
        }

        @Override
        public void Pf0(Matrix pf0) {
            pf0.set(0, 0, var);
        }

    }

    static class Dynamics implements ISsfDynamics {

        private final double var, e;

        Dynamics(final double var) {
            this.var = var;
            this.e = Math.sqrt(var);
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
        public void S(int pos, Matrix cm) {
            cm.set(0, 0, e);
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
        }

        @Override
        public void TX(int pos, DataBlock x) {
            x.set(0);
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            x.add(0, e * u.get(0));
        }

        @Override
        public void addV(int pos, Matrix p) {
            p.add(0, 0, var);
        }

        @Override
        public void XT(int pos, DataBlock x) {
            x.set(0, 0);
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            xs.set(0, x.get(0) * e);
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

    }

    private static class Measurement implements ISsfMeasurement {

        @Override
        public void Z(int pos, DataBlock z) {
            z.set(0, 0);
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
        public double ZX(int pos, DataBlock m) {
            return m.get(0);
        }

        @Override
        public double ZVZ(int pos, Matrix V) {
            return V.get(0, 0);
        }

        @Override
        public void VpZdZ(int pos, Matrix V, double d) {
            V.add(0, 0, d);
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            x.add(0, d);
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }


    }
}
