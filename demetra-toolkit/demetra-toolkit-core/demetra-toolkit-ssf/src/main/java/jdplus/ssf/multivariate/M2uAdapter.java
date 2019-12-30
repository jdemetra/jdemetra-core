/*
 * Copyright 2015 National Bank of Belgium
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
package jdplus.ssf.multivariate;

import jdplus.data.DataBlock;
import jdplus.math.matrices.Matrix;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.ISsfInitialization;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.implementations.MeasurementError;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.ISsfData;
import jdplus.ssf.univariate.ISsfError;
import jdplus.ssf.univariate.Ssf;

/**
 *
 * @author Jean Palate
 */
public class M2uAdapter {

    public static ISsfData of(IMultivariateSsfData data) {
        return new Data(data);
    }

    public static ISsf of(IMultivariateSsf mssf) {
        ISsfMeasurements measurements = mssf.measurements();
        int m = measurements.getCount();
        if (m > 1) {
            ISsfErrors errors = mssf.errors();
            if (errors != null && !errors.areIndependent()) {
                return null;
            }
            ISsfDynamics mdynamics = mssf.dynamics();
            Dynamics ndyn = new Dynamics(mdynamics, measurements.getCount());
            Loading nload = new Loading(measurements);
            Error ne = errors == null ? null : new Error(errors, measurements.getCount());
            return Ssf.of(mssf.initialization(), ndyn, nload, ne);
        } else {
            ISsfInitialization initialization = mssf.initialization();
            ISsfDynamics dynamics = mssf.dynamics();
            ISsfErrors errors = mssf.errors();
            ISsfError error = null;
            if (errors != null) {
                if (errors.isTimeInvariant()) {
                    Matrix h = Matrix.square(1);
                    errors.H(0, h);
                    error = MeasurementError.of(h.get(0, 0));
                } else {
                    error = new Error(errors, 1);
                }
            }
            return Ssf.of(initialization, dynamics, measurements.loading(0), error);
        }
    }

    static class Data implements ISsfData {

        private final int nvars;
        private final IMultivariateSsfData data;

        Data(IMultivariateSsfData data) {
            this.data = data;
            this.nvars = data.getVarsCount();
        }

        @Override
        public double get(int pos) {
            return data.get(pos / nvars, pos % nvars);
        }

        @Override
        public boolean isMissing(int pos) {
            return data.isMissing(pos / nvars, pos % nvars);
        }

        @Override
        public boolean hasData() {
            return data.hasData();
        }

        @Override
        public int length() {
            return data.getObsCount() * nvars;
        }
    }

    static class Loading implements ISsfLoading {

        private final ISsfMeasurements measurements;
        private final int nvars;

        Loading(ISsfMeasurements measurements) {
            this.measurements = measurements;
            this.nvars = measurements.getCount();
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            measurements.loading(pos % nvars).Z(pos / nvars, z);
        }

        @Override
        public double ZX(int pos, DataBlock m) {
            return measurements.loading(pos % nvars).ZX(pos / nvars, m);
        }

        @Override
        public double ZVZ(int pos, Matrix V) {
            return measurements.loading(pos % nvars).ZVZ(pos / nvars, V);
        }

        @Override
        public void VpZdZ(int pos, Matrix V, double d) {
            measurements.loading(pos % nvars).VpZdZ(pos / nvars, V, d);
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            measurements.loading(pos % nvars).XpZd(pos / nvars, x, d);
        }

    }

    static class Error implements ISsfError {

        private final ISsfErrors errors;
        private final int nvars;
        private final Matrix H;
        private int hpos = -1;

        Error(ISsfErrors errors, int nvars) {
            this.errors = errors.isTimeInvariant() ? null : errors;
            this.nvars = nvars;
            H = Matrix.square(nvars);
            if (errors.isTimeInvariant()) {
                errors.H(0, H);
            }
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }

        @Override
        public double at(int pos) {
            int i = pos % nvars;
            if (errors == null) {
                return H.get(i, i);
            }
            int j = pos / nvars;
            if (j != hpos) {
                errors.H(j, H);
                hpos = j;
            }
            return H.get(i, i);
        }
    }

    static class Dynamics implements ISsfDynamics {

        private final ISsfDynamics mdynamics;
        private final int nstep;

        public Dynamics(final ISsfDynamics mdynamics, final int nstep) {
            this.mdynamics = mdynamics;
            this.nstep = nstep;
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }

        @Override
        public int getInnovationsDim() {
            return mdynamics.getInnovationsDim();
        }

        @Override
        public void V(int pos, Matrix qm) {
            if (pos % nstep == nstep - 1) {
                mdynamics.V(pos / nstep, qm);
            }
        }

        @Override
        public boolean hasInnovations(int pos) {
            if (pos % nstep == nstep - 1) {
                return mdynamics.hasInnovations(pos / nstep);
            } else {
                return false;
            }
        }

        @Override
        public boolean areInnovationsTimeInvariant() {
            return false;
        }

        @Override
        public void S(int pos, Matrix sm) {
            if (pos % nstep == nstep - 1) {
                mdynamics.S(pos / nstep, sm);
            }
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            if (pos % nstep == nstep - 1) {
                mdynamics.addSU(pos / nstep, x, u);
            }
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            if (pos % nstep == nstep - 1) {
                mdynamics.XS(pos / nstep, x, xs);
            } else {
                xs.set(0);
            }
        }

        @Override
        public void T(int pos, Matrix tr) {
            if (pos % nstep == nstep - 1) {
                mdynamics.T(pos / nstep, tr);
            } else {
                tr.diagonal().set(1);
            }
        }

        @Override
        public void TX(int pos, DataBlock x) {
            if (pos % nstep == nstep - 1) {
                mdynamics.TX(pos / nstep, x);
            }
        }

        @Override
        public void TM(int pos, Matrix m) {
            if (pos % nstep == nstep - 1) {
                mdynamics.TM(pos / nstep, m);
            }
        }

        @Override
        public void TVT(int pos, Matrix m) {
            if (pos % nstep == nstep - 1) {
                mdynamics.TVT(pos / nstep, m);
            }
        }

        @Override
        public void XT(int pos, DataBlock x) {
            if (pos % nstep == nstep - 1) {
                mdynamics.XT(pos / nstep, x);
            }
        }

        @Override
        public void MT(int pos, Matrix m) {
            if (pos % nstep == nstep - 1) {
                mdynamics.MT(pos / nstep, m);
            }
        }

        @Override
        public void addV(int pos, Matrix p) {
            if (pos % nstep == nstep - 1) {
                mdynamics.addV(pos / nstep, p);
            }
        }
    }
}
