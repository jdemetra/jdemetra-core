/*
 * Copyright 2016-2017 National Bank of Belgium
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

import demetra.ssf.ISsfDynamics;
import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.ssf.ISsfInitialization;
import demetra.ssf.implementations.Measurement;
import demetra.ssf.univariate.Ssf;

/**
 *
 * @author Jean Palate
 */
public class CyclicalComponent {
    
   public static Ssf of(final double dumpingFactor, final double period, final double cvar) {
        Data data = new Data(dumpingFactor, period, cvar);
        return new Ssf(new Initialization(data), new Dynamics(data), Measurement.create(0));
    }

    static class Data{
        private final double var;
        private final double cdump, cperiod;
        
        public Data(double cyclicaldumpingfactor, double cyclicalperiod, double var) {
            this.var = var;
            cperiod = cyclicalperiod;
            cdump = cyclicaldumpingfactor;
       }
    }

    static class Initialization implements ISsfInitialization {

        final Data data;        

        Initialization(Data data){
            this.data=data;
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
        public int getStateDim() {
            return 2;
        }

        @Override
        public void diffuseConstraints(Matrix b) {
        }

        @Override
        public void a0(DataBlock a0) {
        }

        @Override
        public void Pf0(Matrix p) {
            double q = data.var / (1 - data.cdump * data.cdump);
            p.diagonal().set(q);
        }

    }

    static class Dynamics implements ISsfDynamics {

        final Data data;        
        private final double ccos, csin, e;

        Dynamics(Data data){
            this.data=data;
            e = Math.sqrt(data.var);
            double q = Math.PI * 2 / data.cperiod;
            ccos = data.cdump * Math.cos(q);
            csin = data.cdump * Math.sin(q);
         }

        @Override
        public int getInnovationsDim() {
            return data.var == 0 ? 0 : 2;
        }

        @Override
        public void V(int pos, Matrix v) {
            v.diagonal().set(data.var);
        }

        @Override
        public void S(int pos, Matrix s) {
            s.diagonal().set(e);
        }

        @Override
        public boolean hasInnovations(int pos) {
            return data.var != 0;
        }

        @Override
        public boolean areInnovationsTimeInvariant() {
            return true;
        }

        @Override
        public void T(int pos, Matrix tr) {
            tr.set(0, 0, ccos);
            tr.set(1, 1, ccos);
            tr.set(0, 1, csin);
            tr.set(1, 0, -csin);
        }

        @Override
        public void TX(int pos, DataBlock x) {
            double a = x.get(0), b = x.get(1);
            x.set(0, a * ccos + b * csin);
            x.set(1, -a * csin + b * ccos);
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            x.addAY(e, u);
        }

        @Override
        public void addV(int pos, Matrix p) {
            p.diagonal().add(data.var);
        }

        @Override
        public void XT(int pos, DataBlock x) {
            double a = x.get(0), b = x.get(1);
            x.set(0, a * ccos - b * csin);
            x.set(1, a * csin + b * ccos);
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            xs.setAY(e, x);
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

    }
}
