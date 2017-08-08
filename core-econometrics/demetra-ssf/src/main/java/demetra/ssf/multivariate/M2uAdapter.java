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
/*
 */
package demetra.ssf.multivariate;

import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfData;
import demetra.ssf.univariate.ISsfMeasurement;
import demetra.ssf.univariate.Ssf;

/**
 *
 * @author Jean Palate
 */
public class M2uAdapter {

    public static ISsfData of(IMultivariateSsfData data) {
        if (data.isHomogeneous()) {
            return new HomogeneousData(data);
        } else {
            return null;
        }
    }
    
    

    public static ISsf of(IMultivariateSsf mssf) {
        ISsfMeasurements measurements = mssf.getMeasurements();
        if (!measurements.hasIndependentErrors()) {
            return null;
        }
        if (!measurements.isHomogeneous()) {
            return null;
        } else {
            ISsfDynamics mdynamics = mssf.getDynamics();
            HomogeneousDynamics ndyn = new HomogeneousDynamics(mdynamics, measurements.getMaxCount());
            HomogeneousMeasurement nm = new HomogeneousMeasurement(measurements);
            return new Ssf(mssf.getInitialization(), ndyn, nm);
        }
    }

    static class HomogeneousData implements ISsfData {

        private final int nvars;
        private final IMultivariateSsfData data;

        HomogeneousData(IMultivariateSsfData data) {
            this.data = data;
            this.nvars = data.getMaxVarsCount();
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
            return data.getCount() * nvars;
        }
    }

    static class HomogeneousMeasurement implements ISsfMeasurement {

        private final ISsfMeasurements measurements;
        private final int nvars;
        private final Matrix H;
        private int hpos = -1;

        HomogeneousMeasurement(ISsfMeasurements measurements) {
            this.measurements = measurements;
            this.nvars = measurements.getMaxCount();
            if (measurements.hasErrors()) {
                H = Matrix.square(nvars);
                if (measurements.isTimeInvariant()) {
                    measurements.H(0, H);
                }
            } else {
                H = null;
            }
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            measurements.Z(pos / nvars, pos % nvars, z);
        }

        @Override
        public boolean hasErrors() {
            return H != null;
        }

        @Override
        public boolean hasError(int pos) {
            return errorVariance(pos) != 0;
        }

        @Override
        public double errorVariance(int pos) {
            if (H == null) {
                return 0;
            }
            int i = pos % nvars;
            if (measurements.isTimeInvariant()) {
                return H.get(i, i);
            }
            int j = pos / nvars;
            if (j != hpos) {
                measurements.H(j, H);
                hpos = j;
            }
            return H.get(i, i);
        }

        @Override
        public double ZX(int pos, DataBlock m) {
            return measurements.ZX(pos / nvars, pos % nvars, m);
        }

        @Override
        public double ZVZ(int pos, Matrix V) {
            int i = pos % nvars;
            return measurements.ZVZ(pos / nvars, i, i, V);
        }

        @Override
        public void VpZdZ(int pos, Matrix V, double d) {
            int i = pos % nvars;
            measurements.VpZdZ(pos / nvars, i, i, V, d);
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            int i = pos % nvars;
            measurements.XpZd(pos / nvars, i, x, d);
        }

    }

    static class HomogeneousDynamics implements ISsfDynamics {

        private final ISsfDynamics mdynamics;
        private final int nstep;

        public HomogeneousDynamics(final ISsfDynamics mdynamics, final int nstep) {
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
            }else
                xs.set(0);
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
