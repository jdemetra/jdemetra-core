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

import demetra.data.Cell;
import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.design.Development;
import demetra.maths.matrices.Matrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.ISsfInitialization;
import demetra.ssf.SsfException;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfMeasurement;
import demetra.ssf.univariate.Ssf;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.experimental.UtilityClass
public class SsfDisaggregation {

    public Ssf of(ISsf s, int conversion) {
        if (s.getMeasurement().hasErrors()) {
            throw new SsfException(SsfException.ERRORS);
        }
        return new Ssf(
                new Initialization(s.getInitialization()),
                new Dynamics(s, conversion),
                new Measurement(s, conversion));
    }

    static class Initialization implements ISsfInitialization {

        private final ISsfInitialization initialization;

        Initialization(ISsfInitialization initialization) {
            this.initialization = initialization;
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
            return initialization.getDiffuseDim();
        }

        @Override
        public void diffuseConstraints(Matrix b) {
            initialization.diffuseConstraints(b.dropTopLeft(1,0));
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
        private final int conversion;

        Dynamics(ISsf ssf, int conversion) {
            this.dynamics = ssf.getDynamics();
            this.measurement = ssf.getMeasurement();
            this.conversion = conversion;
        }

        @Override
        public int getInnovationsDim() {
            return dynamics.getInnovationsDim();
        }

        @Override
        public boolean areInnovationsTimeInvariant() {
            return dynamics.areInnovationsTimeInvariant();
        }

        @Override
        public void V(int pos, Matrix qm) {
            dynamics.V(pos, qm.dropTopLeft(1, 1));
        }

        @Override
        public void S(int pos, Matrix s) {
            dynamics.S(pos, s.dropTopLeft(1, 0));
        }

        @Override
        public boolean hasInnovations(int pos) {
            return dynamics.hasInnovations(pos);
        }

        @Override
        public void T(int pos, Matrix tr) {
            dynamics.T(pos, tr.dropTopLeft(1, 1));
            if ((pos + 1) % conversion != 0) {
                measurement.Z(pos, tr.row(0).drop(1, 0));
                if (pos % conversion != 0) {
                    tr.set(0, 0, 1);
                }
            }
        }

        @Override
        public void TX(int pos, DataBlock x) {
            DataBlock xc = x.drop(1, 0);

            if ((pos + 1) % conversion != 0) {
                double s = measurement.ZX(pos, xc);
                if (pos % conversion == 0) {
                    x.set(0, s);
                } else {
                    x.add(0, s);
                }
            } else {
                x.set(0, 0);
            }
            dynamics.TX(pos, xc);
        }

        @Override
        public void TVT(int pos, Matrix vm) {
            Matrix v = vm.dropTopLeft(1, 1);
            if (pos % conversion == 0) {
                DataBlock v0 = vm.row(0).drop(1, 0);
                measurement.ZM(pos, v, v0);
                vm.set(0, 0, measurement.ZX(pos, v0));
                dynamics.TX(pos, v0);
                vm.column(0).drop(1, 0).copy(v0);
            } else if ((pos + 1) % conversion != 0) {
                DataBlock r0 = vm.row(0).drop(1, 0);
                double zv0 = measurement.ZX(pos, r0);
                measurement.ZM(pos, v, r0);
                vm.add(0, 0, 2 * zv0 + measurement.ZX(pos, r0));
                dynamics.TX(pos, r0);
                DataBlock c0 = vm.column(0).drop(1, 0);
                dynamics.TX(pos, c0);
                c0.add(r0);
                r0.copy(c0);
            } else {
                vm.row(0).set(0);
                vm.column(0).set(0);
            }
            dynamics.TVT(pos, v);
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            dynamics.addSU(pos, x.drop(1, 0), u);
        }

        @Override
        public void addV(int pos, Matrix p) {
            dynamics.addV(pos, p.dropTopLeft(1, 1));
        }

        @Override
        public void XT(int pos, DataBlock x) {
            DataBlock xc = x.drop(1, 0);
            dynamics.XT(pos, xc);
            if ((pos + 1) % conversion != 0) {
                measurement.XpZd(pos, xc, x.get(0));
                if (pos % conversion == 0) {
                    x.set(0, 0);
                }
            } else {
                x.set(0, 0);
            }
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            dynamics.XS(pos, x.drop(1, 0), xs);
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }
    }

    static class Measurement implements ISsfMeasurement {

        private final ISsfMeasurement measurement;
        private final int conversion;

        Measurement(ISsf s, int conversion) {
            this.measurement = s.getMeasurement();
            this.conversion = conversion;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            if (pos % conversion != 0) {
                z.set(0, 1);
            }
            measurement.Z(pos, z.drop(1, 0));
        }

        @Override
        public boolean hasErrors() {
            return false;
        }

        @Override
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
            double r = (pos % conversion == 0) ? 0 : x.get(0);
            return r + measurement.ZX(pos, x.drop(1, 0));
        }

        @Override
        public void ZM(int pos, Matrix m, DataBlock zm) {
            if (pos % conversion == 0) {
                zm.set(0);
            } else {
                zm.copy(m.row(0));
            }
            Matrix q = m.dropTopLeft(1, 0);
            DataBlockIterator cols = q.columnsIterator();
            Cell cur = zm.cells();
            while (cols.hasNext()) {
                cur.applyAndNext(x -> x + measurement.ZX(pos, cols.next()));
            }
        }

        @Override
        public double ZVZ(int pos, Matrix vm) {
            Matrix v = vm.dropTopLeft(1, 1);
            if (pos % conversion == 0) {
                return measurement.ZVZ(pos, v);
            } else {
                double r = vm.get(0, 0);
                r += 2 * measurement.ZX(pos, vm.row(0).drop(1, 0));
                r += measurement.ZVZ(pos, v);
                return r;
            }
        }

        @Override
        public void VpZdZ(int pos, Matrix vm, double d) {
            Matrix v = vm.dropTopLeft(1, 1);
            measurement.VpZdZ(pos, v, d);
            if (pos % conversion != 0) {
                vm.add(0, 0, d);
                measurement.XpZd(pos, vm.column(0).drop(1, 0), d);
                measurement.XpZd(pos, vm.row(0).drop(1, 0), d);
            }
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            measurement.XpZd(pos, x.drop(1, 0), d);
            if (pos % conversion != 0) {
                x.add(0, d);
            }
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }

    }

}
