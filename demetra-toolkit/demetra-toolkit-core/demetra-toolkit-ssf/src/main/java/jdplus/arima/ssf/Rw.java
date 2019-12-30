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
package jdplus.arima.ssf;

import jdplus.data.DataBlock;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.implementations.Loading;
import jdplus.ssf.ISsfInitialization;
import jdplus.ssf.SsfComponent;
import jdplus.math.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class Rw {
    
    public SsfComponent DEFAULT=of(1, false);

    public static SsfComponent of(final double var, final boolean zeroinit) {
        Data data = new Data(var, zeroinit);
        return new SsfComponent(new Initialization(data), new Dynamics(data), Loading.fromPosition(0));
    }

    static class Data {

        final boolean zeroinit;
        final double var;

        Data(double var, boolean zeroinit) {
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
            return !data.zeroinit;
        }

        @Override
        public int getDiffuseDim() {
            return data.zeroinit ? 0 : 1;
        }

        @Override
        public void diffuseConstraints(Matrix b) {
            if (!data.zeroinit)
                b.set(1);
        }

        @Override
        public void a0(DataBlock a0) {
        }

        @Override
        public void Pf0(Matrix pf0) {
            if (data.zeroinit) {
                pf0.set(0, 0, data.var);
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
            tr.set(0, 0, 1);
        }

        @Override
        public void TX(int pos, DataBlock x) {
        }

        @Override
        public void TVT(int pos, Matrix v) {
        }

        @Override
        public void XT(int pos, DataBlock x) {
        }

        @Override
        public void addV(int pos, Matrix p) {
            p.add(0, 0, data.var);
        }
    }
}
