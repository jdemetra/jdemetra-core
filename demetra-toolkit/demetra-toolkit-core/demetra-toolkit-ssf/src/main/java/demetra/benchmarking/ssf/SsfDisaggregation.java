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
import demetra.data.DataBlockIterator;
import demetra.design.Development;
import demetra.maths.matrices.FastMatrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.ISsfInitialization;
import demetra.ssf.ISsfLoading;
import demetra.ssf.univariate.Ssf;
import demetra.ssf.SsfComponent;
import demetra.data.DoubleVectorCursor;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.experimental.UtilityClass
public class SsfDisaggregation {

    public Ssf of(SsfComponent s, int conversion) {
        return Ssf.of(new Initialization(s.initialization()), new Dynamics(s, conversion), new Loading(s, conversion));
    }

    public Ssf of(SsfComponent s, int conversion, int start) {
        return Ssf.of(new Initialization(s.initialization()), new Dynamics(s, conversion, start), new Loading(s, conversion, start));
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
        public void diffuseConstraints(FastMatrix b) {
            initialization.diffuseConstraints(b.dropTopLeft(1, 0));
        }

        @Override
        public void a0(DataBlock a0) {
            initialization.a0(a0.drop(1, 0));
        }

        @Override
        public void Pf0(FastMatrix pf0) {
            initialization.Pf0(pf0.dropTopLeft(1, 1));
        }

        @Override
        public void Pi0(FastMatrix pi0) {
            initialization.Pi0(pi0.dropTopLeft(1, 1));
        }

    }

    static class Dynamics implements ISsfDynamics {

        private final ISsfDynamics dynamics;
        private final ISsfLoading loading;
        private final int conversion;
        private final int start;

        Dynamics(SsfComponent ssf, int conversion) {
            this.dynamics = ssf.dynamics();
            this.loading = ssf.loading();
            this.conversion = conversion;
            this.start = 0;
        }

        Dynamics(SsfComponent ssf, int conversion, int start) {
            this.dynamics = ssf.dynamics();
            this.loading = ssf.loading();
            this.conversion = conversion;
            this.start = start;
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
        public void V(int pos, FastMatrix qm) {
            dynamics.V(pos, qm.dropTopLeft(1, 1));
        }

        @Override
        public void S(int pos, FastMatrix s) {
            dynamics.S(pos, s.dropTopLeft(1, 0));
        }

        @Override
        public boolean hasInnovations(int pos) {
            return dynamics.hasInnovations(pos);
        }

        @Override
        public void T(int pos, FastMatrix tr) {
            dynamics.T(pos, tr.dropTopLeft(1, 1));
            if ((start + pos + 1) % conversion != 0) {
                loading.Z(pos, tr.row(0).drop(1, 0));
                if ((start + pos) % conversion != 0) {
                    tr.set(0, 0, 1);
                }
            }
        }

        @Override
        public void TX(int pos, DataBlock x) {
            DataBlock xc = x.drop(1, 0);

            if ((start + pos + 1) % conversion != 0) {
                double s = loading.ZX(pos, xc);
                if ((start + pos) % conversion == 0) {
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
        public void TVT(int pos, FastMatrix vm) {
            FastMatrix v = vm.dropTopLeft(1, 1);
            if ((start + pos) % conversion == 0) {
                DataBlock v0 = vm.row(0).drop(1, 0);
                loading.ZM(pos, v, v0);
                vm.set(0, 0, loading.ZX(pos, v0));
                dynamics.TX(pos, v0);
                vm.column(0).drop(1, 0).copy(v0);
            } else if ((start + pos + 1) % conversion != 0) {
                DataBlock r0 = vm.row(0).drop(1, 0);
                double zv0 = loading.ZX(pos, r0);
                loading.ZM(pos, v, r0);
                vm.add(0, 0, 2 * zv0 + loading.ZX(pos, r0));
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
        public void addV(int pos, FastMatrix p) {
            dynamics.addV(pos, p.dropTopLeft(1, 1));
        }

        @Override
        public void XT(int pos, DataBlock x) {
            DataBlock xc = x.drop(1, 0);
            dynamics.XT(pos, xc);
            if ((start + pos + 1) % conversion != 0) {
                loading.XpZd(pos, xc, x.get(0));
                if ((start + pos) % conversion == 0) {
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

    static class Loading implements ISsfLoading {

        private final ISsfLoading measurement;
        private final int conversion;
        private final int start;

        Loading(SsfComponent s, int conversion) {
            this.measurement = s.loading();
            this.conversion = conversion;
            this.start = 0;
        }

        Loading(SsfComponent s, int conversion, int start) {
            this.measurement = s.loading();
            this.conversion = conversion;
            this.start = start;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            if ((start + pos) % conversion != 0) {
                z.set(0, 1);
            }
            measurement.Z(pos, z.drop(1, 0));
        }

        @Override
        public double ZX(int pos, DataBlock x) {
            double r = ((start + pos) % conversion == 0) ? 0 : x.get(0);
            return r + measurement.ZX(pos, x.drop(1, 0));
        }

        @Override
        public void ZM(int pos, FastMatrix m, DataBlock zm) {
            if ((start + pos) % conversion == 0) {
                zm.set(0);
            } else {
                zm.copy(m.row(0));
            }
            FastMatrix q = m.dropTopLeft(1, 0);
            DataBlockIterator cols = q.columnsIterator();
            DoubleVectorCursor cur = zm.cursor();
            while (cols.hasNext()) {
                cur.applyAndNext(x -> x + measurement.ZX(pos, cols.next()));
            }
        }

        @Override
        public double ZVZ(int pos, FastMatrix vm) {
            FastMatrix v = vm.dropTopLeft(1, 1);
            if ((start + pos) % conversion == 0) {
                return measurement.ZVZ(pos, v);
            } else {
                double r = vm.get(0, 0);
                r += 2 * measurement.ZX(pos, vm.row(0).drop(1, 0));
                r += measurement.ZVZ(pos, v);
                return r;
            }
        }

        @Override
        public void VpZdZ(int pos, FastMatrix vm, double d) {
            FastMatrix v = vm.dropTopLeft(1, 1);
            measurement.VpZdZ(pos, v, d);
            if ((start + pos) % conversion != 0) {
                vm.add(0, 0, d);
                measurement.XpZd(pos, vm.column(0).drop(1, 0), d);
                measurement.XpZd(pos, vm.row(0).drop(1, 0), d);
            }
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            measurement.XpZd(pos, x.drop(1, 0), d);
            if ((start + pos) % conversion != 0) {
                x.add(0, d);
            }
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }

    }

}
