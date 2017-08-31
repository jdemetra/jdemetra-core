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
import demetra.ssf.univariate.ISsfMeasurement;
import demetra.ssf.univariate.Ssf;

/**
 * This class provides algorithms that integrate the measurement errors into the
 * state vector
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class CompactSsf {

    public ISsf compact(ISsf ssf) {
        if (!ssf.getMeasurement().hasErrors()) {
            return ssf;
        } else {
            return new Ssf(
                    new Initialization(ssf),
                    new Dynamics(ssf),
                    new Measurement(ssf));
        }
    }

    static class Initialization implements ISsfInitialization {

        private final ISsfInitialization initialization;

        Initialization(ISsf ssf) {
            this.initialization = ssf.getInitialization();
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
        private final ISsfMeasurement measurement;
        private final double e;

        Dynamics(ISsf ssf) {
            this.dynamics = ssf.getDynamics();
            this.measurement = ssf.getMeasurement();
            if (measurement.areErrorsTimeInvariant()) {
                e = Math.sqrt(measurement.errorVariance(0));
            } else {
                e = 0;
            }
        }

        @Override
        public int getInnovationsDim() {
            return dynamics.getInnovationsDim() + 1;
        }

        public boolean areInnovationsTimeInvariant() {
            return dynamics.areInnovationsTimeInvariant() && measurement.areErrorsTimeInvariant();
        }

        @Override
        public void V(int pos, Matrix qm) {
            dynamics.V(pos, qm.dropTopLeft(1, 1));
            qm.set(0, 0, measurement.errorVariance(pos));
        }

        @Override
        public void S(int pos, Matrix s) {
            dynamics.S(pos, s.dropTopLeft(1, 1));
            s.set(0, 0, e == 0 ? Math.sqrt(measurement.errorVariance(pos)) : e);
        }

        @Override
        public boolean hasInnovations(int pos) {
            return e != 0 || (dynamics.hasInnovations(pos) || measurement.hasError(pos));
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
            x.add(0, u.get(0) * (e == 0 ? Math.sqrt(measurement.errorVariance(pos)) : e));
        }

        @Override
        public void addV(int pos, Matrix p) {
            dynamics.addV(pos, p.dropTopLeft(1, 1));
            p.add(0, 0, measurement.errorVariance(pos));
        }

        @Override
        public void XT(int pos, DataBlock x) {
            dynamics.XT(pos, x.drop(1, 0));
            x.set(0, 0);
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            dynamics.XS(pos, x.drop(1, 0), xs.drop(1, 0));
            xs.set(0, x.get(0) * (e == 0 ? Math.sqrt(measurement.errorVariance(pos)) : e));
        }

        @Override
        public boolean isTimeInvariant() {
            return dynamics.isTimeInvariant() && measurement.areErrorsTimeInvariant();
        }
    }

    static class Measurement implements ISsfMeasurement {

        private final ISsfMeasurement measurement;

        Measurement(ISsf s) {
            this.measurement = s.getMeasurement();
        }

        @Override
        public void Z(int pos, DataBlock z) {
            z.set(0, 1);
            measurement.Z(pos, z.drop(1, 0));
        }

        @Override
        public boolean hasErrors() {
            return false;
        }

        public boolean areErrorsTimeInvariant() {
            return true;
        }

        @Override
        public boolean hasError(int pos) {
            return false;
        }

        @Override
        public double errorVariance(int pos) {
            return 0;
        }

        @Override
        public double ZX(int pos, DataBlock x) {
            double r = x.get(0);
            return r + measurement.ZX(pos, x.drop(1, 0));
        }

        @Override
        public void ZM(int pos, Matrix m, DataBlock zm) {
            zm.copy(m.row(0));
            Matrix q = m.dropTopLeft(1, 0);
            DataBlockIterator cols = q.columnsIterator();
            Cell cur = zm.cells();
            while (cols.hasNext()) {
                cur.applyAndNext(x -> x + measurement.ZX(pos, cols.next()));
            }
        }

        @Override
        public double ZVZ(int pos, Matrix vm) {
            double r = vm.get(0, 0);
            r += 2 * measurement.ZX(pos, vm.row(0).drop(1, 0));
            r += measurement.ZVZ(pos, vm.dropTopLeft(1, 1));
            return r;
        }

        @Override
        public void VpZdZ(int pos, Matrix vm, double d) {
            measurement.VpZdZ(pos, vm.dropTopLeft(1, 1), d);
            vm.add(0, 0, d);
            measurement.XpZd(pos, vm.column(0).drop(1, 0), d);
            measurement.XpZd(pos, vm.row(0).drop(1, 0), d);
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            measurement.XpZd(pos, x.drop(1, 0), d);
            x.add(0, d);
        }

        @Override
        public boolean isTimeInvariant() {
            return measurement.isTimeInvariant();
        }

    }
    
//        public IMultivariateSsf compact(IMultivariateSsf ssf) {
//        if (!ssf.getMeasurements().hasErrors()) {
//            return ssf;
//        } else {
//            return new MultivariateSsf(
//                    new MInitialization(ssf),
//                    new MDynamics(ssf),
//                    new MMeasurement(ssf));
//        }
//    }
//
//    static class MInitialization implements ISsfInitialization {
//
//        private final ISsfInitialization initialization;
//        private final int nequations;
//
//        MInitialization(IMultivariateSsf ssf) {
//            this.initialization = ssf.getInitialization();
//            this.nequations=ssf.getMeasurements().getMaxCount();
//        }
//
//        @Override
//        public int getStateDim() {
//            return initialization.getStateDim() + nequations;
//        }
//
//        @Override
//        public boolean isDiffuse() {
//            return initialization.isDiffuse();
//        }
//
//        @Override
//        public int getDiffuseDim() {
//            return initialization.getStateDim();
//        }
//
//        @Override
//        public void diffuseConstraints(Matrix b) {
//            initialization.diffuseConstraints(b.dropTopLeft(nequations, 0));
//        }
//
//        @Override
//        public void a0(DataBlock a0) {
//            initialization.a0(a0.drop(nequations, 0));
//        }
//
//        @Override
//        public void Pf0(Matrix pf0) {
//            initialization.Pf0(pf0.dropTopLeft(nequations, nequations));
//        }
//
//        @Override
//        public void Pi0(Matrix pi0) {
//            initialization.Pi0(pi0.dropTopLeft(1, 1));
//        }
//
//        @Override
//        public boolean isValid() {
//            return initialization.isValid();
//        }
//    }
//
//    static class MDynamics implements ISsfDynamics {
//
//        private final ISsfDynamics dynamics;
//        private final ISsfMeasurements measurement;
//        private final double e;
//
//        Dynamics(ISsf ssf) {
//            this.dynamics = ssf.getDynamics();
//            this.measurement = ssf.getMeasurement();
//            if (measurement.areErrorsTimeInvariant()) {
//                e = Math.sqrt(measurement.errorVariance(0));
//            } else {
//                e = 0;
//            }
//        }
//
//        @Override
//        public int getInnovationsDim() {
//            return dynamics.getInnovationsDim() + 1;
//        }
//
//        public boolean areInnovationsTimeInvariant() {
//            return dynamics.areInnovationsTimeInvariant() && measurement.areErrorsTimeInvariant();
//        }
//
//        @Override
//        public void V(int pos, Matrix qm) {
//            dynamics.V(pos, qm.dropTopLeft(1, 1));
//            qm.set(0, 0, measurement.errorVariance(pos));
//        }
//
//        @Override
//        public void S(int pos, Matrix s) {
//            dynamics.S(pos, s.dropTopLeft(1, 1));
//            s.set(0, 0, e == 0 ? Math.sqrt(measurement.errorVariance(pos)) : e);
//        }
//
//        @Override
//        public boolean hasInnovations(int pos) {
//            return e != 0 || (dynamics.hasInnovations(pos) || measurement.hasError(pos));
//        }
//
//        @Override
//        public void T(int pos, Matrix tr) {
//            dynamics.T(pos, tr.dropTopLeft(1, 1));
//        }
//
//        @Override
//        public void TX(int pos, DataBlock x) {
//            dynamics.TX(pos, x.drop(1, 0));
//            x.set(0, 0);
//        }
//
//        @Override
//        public void TVT(int pos, Matrix vm) {
//            dynamics.TVT(pos, vm.dropTopLeft(1, 1));
//            vm.row(0).set(0);
//            vm.column(0).set(0);
//        }
//
//        @Override
//        public void addSU(int pos, DataBlock x, DataBlock u) {
//            dynamics.addSU(pos, x.drop(1, 0), u.drop(1, 0));
//            x.add(0, u.get(0) * (e == 0 ? Math.sqrt(measurement.errorVariance(pos)) : e));
//        }
//
//        @Override
//        public void addV(int pos, Matrix p) {
//            dynamics.addV(pos, p.dropTopLeft(1, 1));
//            p.add(0, 0, measurement.errorVariance(pos));
//        }
//
//        @Override
//        public void XT(int pos, DataBlock x) {
//            dynamics.XT(pos, x.drop(1, 0));
//            x.set(0, 0);
//        }
//
//        @Override
//        public void XS(int pos, DataBlock x, DataBlock xs) {
//            dynamics.XS(pos, x.drop(1, 0), xs.drop(1, 0));
//            xs.set(0, x.get(0) * (e == 0 ? Math.sqrt(measurement.errorVariance(pos)) : e));
//        }
//
//        @Override
//        public boolean isTimeInvariant() {
//            return dynamics.isTimeInvariant() && measurement.areErrorsTimeInvariant();
//        }
//    }
//
//    static class Measurement implements ISsfMeasurement {
//
//        private final ISsfMeasurement measurement;
//
//        Measurement(ISsf s) {
//            this.measurement = s.getMeasurement();
//        }
//
//        @Override
//        public void Z(int pos, DataBlock z) {
//            z.set(0, 1);
//            measurement.Z(pos, z.drop(1, 0));
//        }
//
//        @Override
//        public boolean hasErrors() {
//            return false;
//        }
//
//        public boolean areErrorsTimeInvariant() {
//            return true;
//        }
//
//        @Override
//        public boolean hasError(int pos) {
//            return false;
//        }
//
//        @Override
//        public double errorVariance(int pos) {
//            return 0;
//        }
//
//        @Override
//        public double ZX(int pos, DataBlock x) {
//            double r = x.get(0);
//            return r + measurement.ZX(pos, x.drop(1, 0));
//        }
//
//        @Override
//        public void ZM(int pos, Matrix m, DataBlock zm) {
//            zm.copy(m.row(0));
//            Matrix q = m.dropTopLeft(1, 0);
//            DataBlockIterator cols = q.columnsIterator();
//            Cell cur = zm.cells();
//            while (cols.hasNext()) {
//                cur.applyAndNext(x -> x + measurement.ZX(pos, cols.next()));
//            }
//        }
//
//        @Override
//        public double ZVZ(int pos, Matrix vm) {
//            double r = vm.get(0, 0);
//            r += 2 * measurement.ZX(pos, vm.row(0).drop(1, 0));
//            r += measurement.ZVZ(pos, vm.dropTopLeft(1, 1));
//            return r;
//        }
//
//        @Override
//        public void VpZdZ(int pos, Matrix vm, double d) {
//            measurement.VpZdZ(pos, vm.dropTopLeft(1, 1), d);
//            vm.add(0, 0, d);
//            measurement.XpZd(pos, vm.column(0).drop(1, 0), d);
//            measurement.XpZd(pos, vm.row(0).drop(1, 0), d);
//        }
//
//        @Override
//        public void XpZd(int pos, DataBlock x, double d) {
//            measurement.XpZd(pos, x.drop(1, 0), d);
//            x.add(0, d);
//        }
//
//        @Override
//        public boolean isTimeInvariant() {
//            return measurement.isTimeInvariant();
//        }
//
//    }

}
