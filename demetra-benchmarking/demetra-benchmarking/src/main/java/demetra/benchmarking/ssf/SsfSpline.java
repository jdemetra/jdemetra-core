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
package demetra.benchmarking.ssf;

import demetra.data.DataBlock;
import demetra.design.Development;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.ssf.DiffuseInitialization;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.ISsfInitialization;
import demetra.ssf.implementations.Loading;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.Ssf;

/**
 * State space representation of a non parametric spline model
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.experimental.UtilityClass
public class SsfSpline {

    public ISsf of(double measurementError) {
        return Ssf.builder()
                .initialization(new DiffuseInitialization(2))
                .dynamics(new SimpleDynamics(1))
                .loading(Loading.create(0))
                .measurementError(measurementError)
                .build();
    }

    public ISsf of(double measurementError, double d) {
        return Ssf.builder()
                .initialization(new DiffuseInitialization(2))
                .dynamics(new SimpleDynamics(d))
                .loading(Loading.create(0))
                .measurementError(measurementError)
                .build();
    }

    public ISsf of(double measurementError, double[] d) {
        return Ssf.builder()
                .initialization(new DiffuseInitialization(2))
                .dynamics(new Dynamics(d))
                .loading(Loading.create(0))
                .measurementError(measurementError)
                .build();
    }

    static class SimpleDynamics implements ISsfDynamics {

        private final double d;
        private final Matrix vm, sm;

        SimpleDynamics(double d) {
            this.d = d;
            double d2 = d * d, d3 = d2 * d;
            vm = Matrix.square(2);
            vm.set(0, 0, d3 / 3);
            vm.set(0, 1, d2 / 2);
            vm.set(1, 0, d2 / 2);
            vm.set(1, 1, d);
            sm = Matrix.square(2);
            sm.copy(vm);
            SymmetricMatrix.lcholesky(sm);
        }

        @Override
        public int getInnovationsDim() {
            return 2;
        }

        @Override
        public void V(int pos, Matrix qm) {
            qm.copy(vm);
        }

        @Override
        public void S(int pos, Matrix m) {
            m.copy(sm);
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
            tr.diagonal().set(1);
            tr.set(0, 1, d);
        }

        @Override
        public void TX(int pos, DataBlock x) {
            x.add(0, d * x.get(1));
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            x.addProduct(sm.rowsIterator(), u);
        }

        @Override
        public void addV(int pos, Matrix p) {
            p.add(vm);
        }

        @Override
        public void XT(int pos, DataBlock x) {
            x.add(1, d * x.get(0));
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            xs.set(0, x.dot(sm.column(0)));
            xs.set(1, x.dot(sm.column(1)));
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

    }

    static class Dynamics implements ISsfDynamics {

        private final double[] d;

        Dynamics(double[] d) {
            this.d = d;
        }

        @Override
        public int getInnovationsDim() {
            return 2;
        }

        @Override
        public void V(int pos, Matrix vm) {
            double d1 = d[pos];
            double d2 = d1 * d1, d3 = d2 * d1;
            vm.set(0, 0, d3 / 3);
            vm.set(0, 1, d2 / 2);
            vm.set(1, 0, d2 / 2);
            vm.set(1, 1, d1);
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public boolean areInnovationsTimeInvariant() {
            return false;
        }

        @Override
        public void T(int pos, Matrix tr) {
            tr.diagonal().set(1);
            tr.set(0, 1, d[pos]);
        }

        @Override
        public void TX(int pos, DataBlock x) {
            x.add(0, d[pos] * x.get(1));
        }

 
        @Override
        public void addV(int pos, Matrix p) {
            double d1 = d[pos];
            double d2 = d1 * d1, d3 = d2 * d1;
            p.add(0, 0, d3 / 3);
            p.add(0, 1, d2 / 2);
            p.add(1, 0, d2 / 2);
            p.add(1, 1, d1);
        }

        @Override
        public void XT(int pos, DataBlock x) {
            x.add(1, d[pos] * x.get(0));
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }

        @Override
        public void S(int pos, Matrix cm) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }
}
