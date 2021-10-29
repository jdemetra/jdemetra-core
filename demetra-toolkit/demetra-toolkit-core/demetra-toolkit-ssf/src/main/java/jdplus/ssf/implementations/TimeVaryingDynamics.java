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
package jdplus.ssf.implementations;

import jdplus.data.DataBlock;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.ssf.ISsfDynamics;
import demetra.data.DoubleSeq;
import jdplus.math.matrices.FastMatrix;

/**
 * Dynamics for time varying coefficients
 * @author Jean Palate
 */
public class TimeVaryingDynamics {

    public static ISsfDynamics of(int n, double var) {
        return new TimeVaryingCDiag(n, var);
    }

    public static ISsfDynamics of(DoubleSeq dvar) {
        return new TimeVaryingDiag(dvar);
    }

    public static ISsfDynamics of(FastMatrix var) {
        return new TimeVaryingFull(var);
    }

        static class TimeVaryingCDiag implements ISsfDynamics {

        private final int n;
        private final double var, std;

        TimeVaryingCDiag(final int n, final double var) {
            this.n=n;
            this.var = var;
            this.std =Math.sqrt(var);
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
            return n;
        }

        @Override
        public void V(int pos, FastMatrix qm) {
            qm.diagonal().set(var);
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public void S(int pos, FastMatrix sm) {
            sm.diagonal().set(std);
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            int n = x.length();
            for (int i = 0; i < n; ++i) {
                x.add(i, u.get(i) * std);
            }
        }

        @Override

        public void XS(int pos, DataBlock x, DataBlock xs) {
            int n = x.length();
            for (int i = 0; i < n; ++i) {
                xs.set(i, x.get(i) * std);
            }
        }

        @Override
        public void T(int pos, FastMatrix tr) {
            tr.diagonal().set(1);
        }

        @Override
        public void TX(int pos, DataBlock x) {
        }

        @Override
        public void XT(int pos, DataBlock x) {
        }

        @Override
        public void TVT(int pos, FastMatrix v) {
        }

        @Override
        public void addV(int pos, FastMatrix p) {
            p.diagonal().add(var);
        }
    }

    
    static class TimeVaryingDiag implements ISsfDynamics {

        private final DataBlock var, std;

        TimeVaryingDiag(final double[] var) {
            this.var = DataBlock.copyOf(var);
            this.std = DataBlock.copyOf(var);
            std.apply(x -> Math.sqrt(x));
        }

        TimeVaryingDiag(final DoubleSeq var) {
            this.var = DataBlock.of(var);
            this.std = DataBlock.of(var);
            std.apply(x -> Math.sqrt(x));
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
            return var.length();
        }

        @Override
        public void V(int pos, FastMatrix qm) {
            qm.diagonal().copy(var);
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public void S(int pos, FastMatrix sm) {
            sm.diagonal().copy(std);
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            int n = x.length();
            for (int i = 0; i < n; ++i) {
                x.add(i, u.get(i) * std.get(i));
            }
        }

        @Override

        public void XS(int pos, DataBlock x, DataBlock xs) {
            int n = x.length();
            for (int i = 0; i < n; ++i) {
                xs.set(i, x.get(i) * std.get(i));
            }
        }

        @Override
        public void T(int pos, FastMatrix tr) {
            tr.diagonal().set(1);
        }

        @Override
        public void TX(int pos, DataBlock x) {
        }

        @Override
        public void XT(int pos, DataBlock x) {
        }

        @Override
        public void TVT(int pos, FastMatrix v) {
        }

        @Override
        public void addV(int pos, FastMatrix p) {
            p.diagonal().add(var);
        }
    }

    static class TimeVaryingFull implements ISsfDynamics {

        private final FastMatrix var, s;

        TimeVaryingFull(final FastMatrix var) {
            this.var = var;
            s = var.deepClone();
            SymmetricMatrix.lcholesky(s, 1e-9);
        }

        TimeVaryingFull(final FastMatrix var, final FastMatrix s) {
            this.var = var;
            this.s = s;
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
            return var.getColumnsCount();
        }

        @Override
        public void V(int pos, FastMatrix qm) {
            qm.copy(var);
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public void S(int pos, FastMatrix sm) {
            sm.copy(s);
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            x.addProduct(s.rowsIterator(), u);
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            xs.product(x, s.columnsIterator());
        }

        @Override
        public void T(int pos, FastMatrix tr) {
            tr.diagonal().set(1);
        }

        @Override
        public void TX(int pos, DataBlock x) {
        }

        @Override
        public void XT(int pos, DataBlock x) {
        }

        @Override
        public void TVT(int pos, FastMatrix v) {
        }

        @Override
        public void addV(int pos, FastMatrix p) {
            p.add(var);
        }
    }
}
