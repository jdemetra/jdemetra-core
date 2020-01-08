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
import jdplus.data.DataBlockIterator;
import demetra.data.DoubleSeqCursor;
import demetra.design.Development;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.ISsfInitialization;
import jdplus.ssf.ISsfLoading;
import jdplus.math.matrices.Matrix;
import jdplus.ssf.StateComponent;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.experimental.UtilityClass
public class SsfCumulator {

    public StateComponent of(StateComponent s, ISsfLoading loading, int conversion, int start) {
        if (conversion == 0) {
            return new StateComponent(new Initialization(s.initialization()), new CDynamics(s.dynamics(), loading));
        } else {
            return new StateComponent(new Initialization(s.initialization()), new Dynamics(s.dynamics(), loading, conversion, start));
        }
    }

    public ISsfLoading defaultLoading(ISsfLoading l, int conversion, int start) {
        if (conversion == 0) {
            return new CLoading(l);
        } else {
            return new Loading(l, conversion, start);
        }
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
        private final ISsfLoading loading;
        private final int conversion;
        private final int start;

        Dynamics(ISsfDynamics dynamics, ISsfLoading loading, int conversion) {
            this.dynamics = dynamics;
            this.loading = loading;
            this.conversion = conversion;
            this.start = 0;
        }

        Dynamics(ISsfDynamics dynamics, ISsfLoading loading, int conversion, int start) {
            this.dynamics = dynamics;
            this.loading = loading;
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
        public void TVT(int pos, Matrix vm) {
            Matrix v = vm.dropTopLeft(1, 1);
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
        public void addV(int pos, Matrix p) {
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

        private final ISsfLoading loading;
        private final int conversion;
        private final int start;

        Loading(ISsfLoading loading, int conversion) {
            this.loading = loading;
            this.conversion = conversion;
            this.start = 0;
        }

        Loading(ISsfLoading loading, int conversion, int start) {
            this.loading = loading;
            this.conversion = conversion;
            this.start = start;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            if ((start + pos) % conversion != 0) {
                z.set(0, 1);
            }
            loading.Z(pos, z.drop(1, 0));
        }

        @Override
        public double ZX(int pos, DataBlock x) {
            double r = ((start + pos) % conversion == 0) ? 0 : x.get(0);
            return r + loading.ZX(pos, x.drop(1, 0));
        }

        @Override
        public void ZM(int pos, Matrix m, DataBlock zm) {
            if ((start + pos) % conversion == 0) {
                zm.set(0);
            } else {
                zm.copy(m.row(0));
            }
            Matrix q = m.dropTopLeft(1, 0);
            DataBlockIterator cols = q.columnsIterator();
            DoubleSeqCursor.OnMutable cur = zm.cursor();
            while (cols.hasNext()) {
                cur.applyAndNext(x -> x + loading.ZX(pos, cols.next()));
            }
        }

        @Override
        public double ZVZ(int pos, Matrix vm) {
            Matrix v = vm.dropTopLeft(1, 1);
            if ((start + pos) % conversion == 0) {
                return loading.ZVZ(pos, v);
            } else {
                double r = vm.get(0, 0);
                r += 2 * loading.ZX(pos, vm.row(0).drop(1, 0));
                r += loading.ZVZ(pos, v);
                return r;
            }
        }

        @Override
        public void VpZdZ(int pos, Matrix vm, double d) {
            if (d == 0) {
                return;
            }
            Matrix v = vm.dropTopLeft(1, 1);
            loading.VpZdZ(pos, v, d);
            if ((start + pos) % conversion != 0) {
                vm.add(0, 0, d);
                loading.XpZd(pos, vm.column(0).drop(1, 0), d);
                loading.XpZd(pos, vm.row(0).drop(1, 0), d);
            }
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            loading.XpZd(pos, x.drop(1, 0), d);
            if ((start + pos) % conversion != 0) {
                x.add(0, d);
            }
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }

    }

    static class CDynamics implements ISsfDynamics {

        private final ISsfDynamics dynamics;
        private final ISsfLoading loading;

        CDynamics(ISsfDynamics dynamics, ISsfLoading loading) {
            this.dynamics = dynamics;
            this.loading = loading;
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
            loading.Z(pos, tr.row(0).drop(1, 0));
            tr.set(0, 0, 1);
        }

        @Override
        public void TX(int pos, DataBlock x) {
            DataBlock xc = x.drop(1, 0);
            double s = loading.ZX(pos, xc);
            x.add(0, s);
            dynamics.TX(pos, xc);
        }

        @Override
        public void TVT(int pos, Matrix vm) {
            Matrix v = vm.dropTopLeft(1, 1);
            DataBlock r0 = vm.row(0).drop(1, 0);
            double zv0 = loading.ZX(pos, r0);
            loading.ZM(pos, v, r0);
            vm.add(0, 0, 2 * zv0 + loading.ZX(pos, r0));
            dynamics.TX(pos, r0);
            DataBlock c0 = vm.column(0).drop(1, 0);
            dynamics.TX(pos, c0);
            c0.add(r0);
            r0.copy(c0);
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
            loading.XpZd(pos, xc, x.get(0));
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

    static class CLoading implements ISsfLoading {

        private final ISsfLoading loading;

        CLoading(ISsfLoading loading) {
            this.loading = loading;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            z.set(0, 1);
            loading.Z(pos, z.drop(1, 0));
        }

        @Override
        public double ZX(int pos, DataBlock x) {
            return x.get(0) + loading.ZX(pos, x.drop(1, 0));
        }

        @Override
        public void ZM(int pos, Matrix m, DataBlock zm) {
            zm.copy(m.row(0));
            Matrix q = m.dropTopLeft(1, 0);
            DataBlockIterator cols = q.columnsIterator();
            DoubleSeqCursor.OnMutable cur = zm.cursor();
            while (cols.hasNext()) {
                cur.applyAndNext(x -> x + loading.ZX(pos, cols.next()));
            }
        }

        @Override
        public double ZVZ(int pos, Matrix vm) {
            Matrix v = vm.dropTopLeft(1, 1);
            double r = vm.get(0, 0);
            r += 2 * loading.ZX(pos, vm.row(0).drop(1, 0));
            r += loading.ZVZ(pos, v);
            return r;
        }

        @Override
        public void VpZdZ(int pos, Matrix vm, double d) {
            if (d == 0) {
                return;
            }
            Matrix v = vm.dropTopLeft(1, 1);
            loading.VpZdZ(pos, v, d);
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
            return false;
        }

    }
}
