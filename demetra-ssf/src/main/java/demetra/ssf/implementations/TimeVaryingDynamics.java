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

package demetra.ssf.implementations;

import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.ssf.ISsfDynamics;
import demetra.data.Doubles;

/**
 *
 * @author Jean Palate
 */
public class TimeVaryingDynamics {

    public static ISsfDynamics create(Doubles dvar) {
        return new TimeVaryingDiag(dvar);
    }

    public static ISsfDynamics create(Matrix var) {
        return new TimeVaryingFull(var);
    }

    static class TimeVaryingDiag implements ISsfDynamics {

        private final DataBlock var, std;

        TimeVaryingDiag(final double[] var) {
            this.var = DataBlock.copyOf(var);
            this.std = DataBlock.copyOf(var);
            std.apply(x->Math.sqrt(x));
        }

        TimeVaryingDiag(final Doubles var) {
           this.var = DataBlock.copyOf(var);
            this.std = DataBlock.copyOf(var);
            std.apply(x->Math.sqrt(x));
        }

        @Override
        public int getStateDim() {
            return var.length();
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public int getInnovationsDim() {
            return var.length();
        }

        @Override
        public void V(int pos, Matrix qm) {
            qm.diagonal().copy(var);
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public void S(int pos, Matrix sm) {
            sm.diagonal().copy(std);
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            int n=x.length();
            for (int i=0; i<n; ++i){
                x.add(i, u.get(i)*std.get(i));
            }
        }

        @Override

        public void XS(int pos, DataBlock x, DataBlock xs) {
            int n=x.length();
            for (int i=0; i<n; ++i){
                xs.set(i, x.get(i)*std.get(i));
            }
        }
        @Override
        public void T(int pos, Matrix tr) {
            tr.diagonal().set(1);
        }

        @Override
        public boolean isDiffuse() {
            return true;
        }

        @Override
        public int getNonStationaryDim() {
            return var.length();
        }

        @Override
        public void diffuseConstraints(Matrix b) {
            b.diagonal().set(1);
        }

        @Override
        public boolean a0(DataBlock a0) {
            return true;
        }

        @Override
        public boolean Pf0(Matrix pf0) {
            return true;
        }

        @Override
        public void Pi0(Matrix p) {
            p.diagonal().set(1);
        }

        @Override
        public void TX(int pos, DataBlock x) {
        }

        @Override
        public void XT(int pos, DataBlock x) {
        }

        @Override
        public void TVT(int pos, Matrix v) {
        }

        @Override
        public void addV(int pos, Matrix p) {
            p.diagonal().add(var);
        }
    }

    static class TimeVaryingFull implements ISsfDynamics {

        private final Matrix var, s;

        TimeVaryingFull(final Matrix var) {
            this.var = var;
            s=var.deepClone();
            SymmetricMatrix.lcholesky(s, 1e-9);
        }

        TimeVaryingFull(final Matrix var, final Matrix s) {
            this.var = var;
            this.s=s;
        }

        @Override
        public int getStateDim() {
            return var.getColumnsCount();
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public int getInnovationsDim() {
            return var.getColumnsCount();
        }

        @Override
        public void V(int pos, Matrix qm) {
            qm.copy(var);
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public void S(int pos, Matrix sm) {
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
        public void T(int pos, Matrix tr) {
            tr.diagonal().set(1);
        }

        @Override
        public boolean isDiffuse() {
            return true;
        }

        @Override
        public int getNonStationaryDim() {
            return var.getColumnsCount();
        }

        @Override
        public void diffuseConstraints(Matrix b) {
            b.diagonal().set(1);
        }

        @Override
        public boolean a0(DataBlock a0) {
            return true;
        }

        @Override
        public boolean Pf0(Matrix pf0) {
            return true;
        }

        @Override
        public void Pi0(Matrix p) {
            p.diagonal().set(1);
        }

        @Override
        public void TX(int pos, DataBlock x) {
        }

        @Override
        public void XT(int pos, DataBlock x) {
        }

        @Override
        public void TVT(int pos, Matrix v) {
        }

        @Override
        public void addV(int pos, Matrix p) {
            p.add(var);
        }
    }
}
