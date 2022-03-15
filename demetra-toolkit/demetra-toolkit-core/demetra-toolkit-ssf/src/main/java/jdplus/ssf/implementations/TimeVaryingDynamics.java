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
 *
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

    /**
     * Time varying coefficient with time varying standard error
     * @param stde Standard deviations of the coefficient (1 outside the range defined by the array)
     * @param scale scaling factor
     * @return 
     */
    public static ISsfDynamics of(DoubleSeq stde, double scale) {
        return new TimeVaryingInnovations(stde, scale);
    }

    static class TimeVaryingCDiag implements ISsfDynamics {

        private final int n;
        private final double var, std;

        TimeVaryingCDiag(final int n, final double var) {
            this.n = n;
            this.var = var;
            this.std = Math.sqrt(var);
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

    static class TimeVaryingInnovations implements ISsfDynamics {

        private final double[] std;
        private final double scale, scale2;

        TimeVaryingInnovations(final DoubleSeq stde, double scale) {
            this.std = stde.toArray();
            this.scale = scale;
            this.scale2 = scale * scale;
        }

        double stderr(int pos) {
            return pos >= std.length ? scale : scale * std[pos];
        }

        double var(int pos) {
            return pos >= std.length ? scale2 : scale2 * std[pos] * std[pos];
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }

        @Override
        public boolean areInnovationsTimeInvariant() {
            return false;
        }

        @Override
        public int getInnovationsDim() {
            return 1;
        }

        @Override
        public void V(int pos, FastMatrix qm) {
            qm.set(0, 0, var(pos));
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public void S(int pos, FastMatrix sm) {
            sm.set(0, 0, stderr(pos));
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            x.add(0, stderr(pos) * u.get(0));
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            xs.set(0, x.get(0) * stderr(pos));
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
            p.add(var(pos));
        }
    }
}
