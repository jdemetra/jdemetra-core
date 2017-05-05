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
import demetra.ssf.ISsfDynamics;
import demetra.ssf.multivariate.IMultivariateSsf;
import demetra.ssf.multivariate.ISsfMeasurements;
import demetra.ssf.multivariate.MultivariateSsf;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class MultivariateSsfWithIntercept extends MultivariateSsf {

    public static MultivariateSsfWithIntercept addIntercept(IMultivariateSsf ssf) {
        ISsfMeasurements measurements = ssf.getMeasurements();
        if (!measurements.isHomogeneous()) {
            return null;
        }
        ISsfDynamics dynamics = ssf.getDynamics();
        int n = measurements.getMaxCount();
        return new MultivariateSsfWithIntercept(new RegSsf.Xdynamics(dynamics, n),
                new MeasurementsWithIntercept(measurements));
    }

    MultivariateSsfWithIntercept(ISsfDynamics dynamics, ISsfMeasurements measurements) {
        super(dynamics, measurements);
    }

    static class MeasurementsWithIntercept implements ISsfMeasurements {

        private final int nvars, dim;
        private final ISsfMeasurements measurements;

        MeasurementsWithIntercept(ISsfMeasurements measurements) {
            this.measurements = measurements;
            this.nvars = measurements.getMaxCount();
            this.dim = measurements.getStateDim();
        }

        @Override
        public boolean isTimeInvariant() {
            return measurements.isTimeInvariant();
        }

        @Override
        public int getCount(int pos) {
            return measurements.getMaxCount();
        }

        @Override
        public int getMaxCount() {
            return measurements.getMaxCount();
        }

        @Override
        public boolean isHomogeneous() {
            return true;
        }

        @Override
        public void Z(int pos, int var, DataBlock z) {
            measurements.Z(pos, var, z.range(0, dim));
            z.set(dim + var, 1);
        }

        @Override
        public boolean hasErrors() {
            return measurements.hasErrors();
        }

        @Override
        public boolean hasIndependentErrors() {
            return measurements.hasIndependentErrors();
        }

        @Override
        public boolean hasError(int pos) {
            return measurements.hasError(pos);
        }

        @Override
        public void H(int pos, Matrix h) {
            measurements.H(pos, h);
        }

        @Override
        public void R(int pos, Matrix r) {
            measurements.R(pos, r);
        }

        @Override
        public double ZX(int pos, int var, DataBlock m) {
            return measurements.ZX(pos, var, m.range(0, dim)) + m.get(dim + var);
        }

        @Override
        public double ZVZ(int pos, int ivar, int jvar, Matrix V) {
            Matrix v = V.topLeft(dim, dim);
            double v00=measurements.ZVZ(pos, ivar, jvar, v);
            double v11=V.get(dim+ivar, dim+jvar);
            return v00+v11;
        }

        @Override
        public void addH(int pos, Matrix V) {
            measurements.addH(pos, V);
        }

        @Override
        public void VpZdZ(int pos, int ivar, int jvar, Matrix V, double d) {
             Matrix v = V.topLeft(dim, dim);
             measurements.VpZdZ(pos, ivar, jvar, v, d);
             V.add(dim+ivar, dim+jvar, d);
        }

        @Override
        public void XpZd(int pos, int ivar, DataBlock x, double d) {
            measurements.XpZd(pos, ivar, x.range(0, dim), d);
            x.add(dim + ivar, d);
        }

        @Override
        public int getStateDim() {
            return nvars + dim;
        }

        @Override
        public boolean isValid() {
            return measurements.isValid();
        }

    }
}
