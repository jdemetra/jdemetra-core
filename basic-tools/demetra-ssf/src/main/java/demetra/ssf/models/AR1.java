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

/**
 *
 * @author Jean Palate
 */
public class AR1 extends Ssf {

    public static AR1 of(final double rho) {
        Data data = new Data(rho, 1, false);
        return new AR1(data);
    }

    public static AR1 of(final double rho, final double var, final boolean zeroinit) {
        Data data = new Data(rho, var, zeroinit);
        return new AR1(data);
    }

    private AR1(Data data) {
        super(new Initialization(data), new Dynamics(data), Measurement.create(0));
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

    static class Data {

        final boolean zeroinit;
        final double rho;
        final double var;

        Data(double rho, double var, boolean zeroinit) {
            this.rho = rho;
            this.var = var;
            this.zeroinit = zeroinit;
        }

        double std() {
            return var == 1 ? 1 : Math.sqrt(var);
        }
    }

    static class Initialization implements ISsfInitialization {

        private final Data data;

        Initialization(Data data) {
            this.data = data;
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
            if (data.zeroinit) {
                pf0.set(0, 0, data.var);
            } else {
                pf0.set(0, 0, data.var / (1 - data.rho * data.rho));
            }
        }

    }

    static class Dynamics implements ISsfDynamics {

        private final Data data;

        public Dynamics(Data data) {
            this.data = data;
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public int getInnovationsDim() {
            return 1;
        }

        @Override
        public void V(int pos, Matrix qm) {
            qm.set(0, 0, data.var);
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
        public void S(int pos, Matrix sm) {
            sm.set(0, 0, data.std());
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            x.add(0, data.std() * u.get(0));
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            xs.set(0, data.std() * x.get(0));
        }

        @Override
        public void T(int pos, Matrix tr) {
            tr.set(0, 0, data.rho);
        }

        @Override
        public void TX(int pos, DataBlock x) {
            x.mul(0, data.rho);
        }

        @Override
        public void TVT(int pos, Matrix v) {
            v.mul(0, 0, data.rho * data.rho);
        }

        @Override
        public void XT(int pos, DataBlock x) {
            x.mul(0, data.rho);
        }

        @Override
        public void addV(int pos, Matrix p) {
            p.add(0, 0, data.var);
        }
    }
}
