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
package jdplus.benchmarking.ssf;

import jdplus.data.DataBlock;
import nbbrd.design.Development;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.ssf.DiffuseInitialization;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.StateComponent;
import jdplus.ssf.implementations.Loading;

/**
 * State space representation of a non parametric spline model
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.experimental.UtilityClass
public class SsfSpline {
    
    public ISsfLoading defaultLoading(){
        return Loading.fromPosition(0);
    }

    public StateComponent of(double d) {
        return new StateComponent(new DiffuseInitialization(2), new SimpleDynamics(d));
    }

    public StateComponent of(double[] d) {
        return new StateComponent(new DiffuseInitialization(2), new Dynamics(d));
    }

    static class SimpleDynamics implements ISsfDynamics {

        private final double d;
        private final FastMatrix vm, sm;

        SimpleDynamics(double d) {
            this.d = d;
            double d2 = d * d, d3 = d2 * d;
            vm = FastMatrix.square(2);
            vm.set(0, 0, d3 / 3);
            vm.set(0, 1, d2 / 2);
            vm.set(1, 0, d2 / 2);
            vm.set(1, 1, d);
            sm = FastMatrix.square(2);
            sm.copy(vm);
            SymmetricMatrix.lcholesky(sm);
        }

        @Override
        public int getInnovationsDim() {
            return 2;
        }

        @Override
        public void V(int pos, FastMatrix qm) {
            qm.copy(vm);
        }

        @Override
        public void S(int pos, FastMatrix m) {
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
        public void T(int pos, FastMatrix tr) {
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
        public void addV(int pos, FastMatrix p) {
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
        public void V(int pos, FastMatrix vm) {
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
        public void T(int pos, FastMatrix tr) {
            tr.diagonal().set(1);
            tr.set(0, 1, d[pos]);
        }

        @Override
        public void TX(int pos, DataBlock x) {
            x.add(0, d[pos] * x.get(1));
        }

        @Override
        public void addV(int pos, FastMatrix p) {
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
        public void S(int pos, FastMatrix cm) {
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
