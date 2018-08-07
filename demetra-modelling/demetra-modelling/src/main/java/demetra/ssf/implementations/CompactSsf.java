/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.ssf.implementations;

import demetra.data.Cell;
import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.maths.matrices.Matrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.ISsfInitialization;
import demetra.ssf.multivariate.IMultivariateSsf;
import demetra.ssf.multivariate.MultivariateSsf;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfError;
import demetra.ssf.ISsfLoading;
import demetra.ssf.univariate.Ssf;
import demetra.ssf.univariate.ISsfMeasurement;

/**
 * This class provides algorithms that integrate the measurement errors into the
 * state vector
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class CompactSsf {

    public ISsf compact(ISsf ssf) {
        
        if (!ssf.measurement().hasError()) {
            return ssf;
        } else {
            return Ssf.builder()
                    .initialization(new Initialization(ssf))
                    .dynamics(new Dynamics(ssf))
                    .loading(new Loading(ssf))
                    .build();
        }
    }

    static class Initialization implements ISsfInitialization {

        private final ISsfInitialization initialization;

        Initialization(ISsf ssf) {
            this.initialization = ssf.initialization();
        }

        @Override
        public int getStateDim() {
            return initialization.getStateDim() + 1;
        }

        @Override
        public boolean isDiffuse() {
            return initialization.isDiffuse();
        }

        @Override
        public int getDiffuseDim() {
            return initialization.getStateDim();
        }

        @Override
        public void diffuseConstraints(Matrix b) {
            initialization.diffuseConstraints(b.dropTopLeft(1, 0));
        }

        @Override
        public void a0(DataBlock a0) {
            initialization.a0(a0.drop(1, 0));
        }

        @Override
        public void Pf0(Matrix pf0) {
            initialization.Pf0(pf0.dropTopLeft(1, 1));
        }

        @Override
        public void Pi0(Matrix pi0) {
            initialization.Pi0(pi0.dropTopLeft(1, 1));
        }

    }

    static class Dynamics implements ISsfDynamics {

        private final ISsfDynamics dynamics;
        private final ISsfError error;
        private final double e;

        Dynamics(ISsf ssf) {
            this.dynamics = ssf.dynamics();
            this.error = ssf.measurementError();
            if (error.isTimeInvariant()) {
                e = Math.sqrt(error.at(0));
            } else {
                e = 0;
            }
        }
        
        private double err(int pos){
            return e != 0 ? e : Math.sqrt(error.at(pos));
        }

        @Override
        public int getInnovationsDim() {
            return dynamics.getInnovationsDim() + 1;
        }

        @Override
        public boolean areInnovationsTimeInvariant() {
            return dynamics.areInnovationsTimeInvariant() && e != 0;
        }

        @Override
        public void V(int pos, Matrix qm) {
            dynamics.V(pos, qm.dropTopLeft(1, 1));
            qm.set(0, 0, error.at(pos));
        }

        @Override
        public void S(int pos, Matrix s) {
            dynamics.S(pos, s.dropTopLeft(1, 1));
            s.set(0, 0, err(pos));
        }

        @Override
        public boolean hasInnovations(int pos) {
            return e != 0 || (dynamics.hasInnovations(pos) || error.at(pos) > 0);
        }

        @Override
        public void T(int pos, Matrix tr) {
            dynamics.T(pos, tr.dropTopLeft(1, 1));
        }

        @Override
        public void TX(int pos, DataBlock x) {
            dynamics.TX(pos, x.drop(1, 0));
            x.set(0, 0);
        }

        @Override
        public void TVT(int pos, Matrix vm) {
            dynamics.TVT(pos, vm.dropTopLeft(1, 1));
            vm.row(0).set(0);
            vm.column(0).set(0);
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            dynamics.addSU(pos, x.drop(1, 0), u.drop(1, 0));
            x.add(0, u.get(0) * err(pos));
        }

        @Override
        public void addV(int pos, Matrix p) {
            dynamics.addV(pos, p.dropTopLeft(1, 1));
            p.add(0, 0, error.at(pos));
        }

        @Override
        public void XT(int pos, DataBlock x) {
            dynamics.XT(pos, x.drop(1, 0));
            x.set(0, 0);
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            dynamics.XS(pos, x.drop(1, 0), xs.drop(1, 0));
            xs.set(0, x.get(0) * err(pos));
        }

        @Override
        public boolean isTimeInvariant() {
            return dynamics.isTimeInvariant() && error.isTimeInvariant();
        }
    }

    static class Loading implements ISsfLoading {

        private final ISsfLoading loading;

        Loading(ISsf s) {
            this.loading = s.loading();
        }

        @Override
        public void Z(int pos, DataBlock z) {
            z.set(0, 1);
            loading.Z(pos, z.drop(1, 0));
        }

        @Override
        public double ZX(int pos, DataBlock x) {
            double r = x.get(0);
            return r + loading.ZX(pos, x.drop(1, 0));
        }

        @Override
        public void ZM(int pos, Matrix m, DataBlock zm) {
            zm.copy(m.row(0));
            Matrix q = m.dropTopLeft(1, 0);
            DataBlockIterator cols = q.columnsIterator();
            Cell cur = zm.cells();
            while (cols.hasNext()) {
                cur.applyAndNext(x -> x + loading.ZX(pos, cols.next()));
            }
        }

        @Override
        public double ZVZ(int pos, Matrix vm) {
            double r = vm.get(0, 0);
            r += 2 * loading.ZX(pos, vm.row(0).drop(1, 0));
            r += loading.ZVZ(pos, vm.dropTopLeft(1, 1));
            return r;
        }

        @Override
        public void VpZdZ(int pos, Matrix vm, double d) {
            loading.VpZdZ(pos, vm.dropTopLeft(1, 1), d);
            vm.add(0, 0, d);
            loading.XpZd(pos, vm.column(0).drop(1, 0), d);
            loading.XpZd(pos, vm.row(0).drop(1, 0), d);
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            loading.XpZd(pos, x.drop(1, 0), d);
            x.add(0, d);
        }

        @Override
        public boolean isTimeInvariant() {
            return loading.isTimeInvariant();
        }

    }
    

}
