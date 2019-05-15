/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.var;

import jd.data.DataBlock;
import demetra.ssf.ISsfLoading;
import demetra.ssf.multivariate.ISsfErrors;
import demetra.ssf.multivariate.ISsfMeasurements;
import jd.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
class VarMeasurements implements ISsfMeasurements {

    private final int nv, nl;

    VarMeasurements(int nv, int nl) {
        this.nv = nv;
        this.nl = nl;
    }

    @Override
    public boolean isTimeInvariant() {
        return true;
    }

    @Override
    public int getCount() {
        return nv;
    }

    @Override
    public ISsfLoading loading(int equation) {
        return new Loading(equation);
    }

    @Override
    public ISsfErrors errors() {
        return null;
    }

    class Loading implements ISsfLoading {

        private final int var;

        Loading(int var) {
            this.var = var;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            z.set(var, 1);
        }

        @Override
        public double ZX(int pos, DataBlock m) {
            return m.get(var);
        }

        @Override
        public double ZVZ(int pos, FastMatrix V) {
            return V.get(var, var);
        }

        @Override
        public void VpZdZ(int pos, FastMatrix V, double d) {
            V.add(var, var, d);
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            x.add(var, d);
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }
    }
}
