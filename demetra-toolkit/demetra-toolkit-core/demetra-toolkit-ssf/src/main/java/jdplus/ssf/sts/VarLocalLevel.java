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
 /*
 */
package jdplus.ssf.sts;

import jdplus.data.DataBlock;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.ISsfInitialization;
import jdplus.ssf.basic.Loading;
import jdplus.math.matrices.FastMatrix;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.StateComponent;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class VarLocalLevel {

    public ISsfLoading defaultLoading() {
        return Loading.fromPosition(0);
    }

    public StateComponent of(final double[] std, double scale) {
        return new StateComponent(new Initialization(std, scale, Double.NaN), new Dynamics(std, scale));
    }

    public StateComponent of(final double[] std, double scale, final double initialValue) {
        return new StateComponent(new Initialization(std, scale, initialValue), new Dynamics(std, scale));
    }

    static class Initialization implements ISsfInitialization {

        final double scale;
        final double std[];
        final double initialValue;

        Initialization(final double[] std, double scale, final double initialValue) {
            this.std = std;
            this.scale = scale;
            this.initialValue = initialValue;
        }

        @Override
        public int getStateDim() {
            return 1;
        }

        @Override
        public boolean isDiffuse() {
            return Double.isNaN(initialValue);
        }

        @Override
        public int getDiffuseDim() {
            return Double.isNaN(initialValue) ? 1 : 0;
        }

        @Override
        public void diffuseConstraints(FastMatrix b) {
            if (Double.isNaN(initialValue)) {
                b.set(0, 0, 1);
            }
        }

        @Override
        public void a0(DataBlock a0) {
            if (Double.isFinite(initialValue)) {
                a0.set(0, initialValue);
            }
        }

        @Override
        public void Pf0(FastMatrix pf0) {
            if (Double.isFinite(initialValue)) {
                pf0.set(0, 0, scale * scale);
            }
        }

        @Override
        public void Pi0(FastMatrix pi0) {
            if (Double.isNaN(initialValue)) {
                pi0.set(0, 0, 1);
            }
        }
    }

    static class Dynamics implements ISsfDynamics {

        private final double scale, scale2;
        private final double std[];

        Dynamics(final double[] std, double scale) {
            this.std = std;
            this.scale = scale;
            this.scale2 = scale * scale;
        }

        double var(int pos) {
            if (pos < std.length) {
                double q = std[pos] * scale;
                return q * q;
            } else {
                return scale2;
            }
        }

        double std(int pos) {
            return pos < std.length ? std[pos] * scale : scale;
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }

        @Override
        public boolean areInnovationsTimeInvariant() {
            return false;
        }

        @Override
        public int getInnovationsDim() {
            return 1;
        }

        @Override
        public void V(int pos, FastMatrix qm) {
            qm.set(0, 0, var(pos));
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public void S(int pos, FastMatrix sm) {
            sm.set(0, 0, std(pos));
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            x.add(0, std(pos) * u.get(0));
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            xs.set(0, std(pos) * x.get(0));
        }

        @Override
        public void T(int pos, FastMatrix tr) {
            tr.set(0, 0, 1);
        }

        @Override
        public void TX(int pos, DataBlock x) {
        }

        @Override
        public void TVT(int pos, FastMatrix v) {
        }

        @Override
        public void XT(int pos, DataBlock x) {
        }

        @Override
        public void addV(int pos, FastMatrix p) {
            p.add(0, 0, var(pos));
        }

    }
}
