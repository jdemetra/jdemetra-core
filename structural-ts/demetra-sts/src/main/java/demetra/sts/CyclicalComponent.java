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

/**
 *
 * @author Jean Palate
 */
public class CyclicalComponent {

    public static class Dynamics implements ISsfDynamics {

        private final double var, e;
        private final double ccos, csin;
        private final double cdump, cperiod;

        public Dynamics(double cyclicaldumpingfactor, double cyclicalperiod, double var) {
            this.var = var;
            e = Math.sqrt(var);
            cperiod = cyclicalperiod;
            cdump = cyclicaldumpingfactor;
            double q = Math.PI * 2 / cyclicalperiod;
            ccos = cyclicaldumpingfactor * Math.cos(q);
            csin = cyclicaldumpingfactor * Math.sin(q);
        }

        @Override
        public int getInnovationsDim() {
            return var == 0 ? 0 : 2;
        }

        @Override
        public void V(int pos, Matrix v) {
            v.diagonal().set(var);
        }

        @Override
        public void S(int pos, Matrix s) {
            s.diagonal().set(e);
        }

        @Override
        public boolean hasInnovations(int pos) {
            return var != 0;
        }

        @Override
        public void T(int pos, Matrix tr) {
            tr.set(0, 0, ccos);
            tr.set(1, 1, ccos);
            tr.set(0, 1, csin);
            tr.set(1, 0, -csin);
        }

        @Override
        public boolean isDiffuse() {
            return false;
        }

        @Override
        public int getNonStationaryDim() {
            return 0;
        }

        @Override
        public void diffuseConstraints(Matrix b) {
        }

        @Override
        public boolean a0(DataBlock a0) {
            return true;
        }

        @Override
        public boolean Pf0(Matrix p) {
            double q = var / (1 - cdump * cdump);
            p.diagonal().set(q);
            return true;
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
            p.diagonal().add(var);
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
        public int getStateDim() {
            return 2;
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public boolean isValid() {
            return cdump < 1 && cdump > -1;
        }

    }
}
