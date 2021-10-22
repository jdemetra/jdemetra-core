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
package jdplus.ssf.implementations;

import jdplus.data.DataBlock;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.ISsfInitialization;
import jdplus.math.matrices.FastMatrix;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.StateComponent;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class VarNoise {

    public StateComponent of(final @NonNull double[] std, double scale) {
        return new StateComponent(new Initialization(scale), new Dynamics(std, scale));
    }

    public ISsfLoading defaultLoading() {
        return Loading.fromPosition(0);
    }

    static class Initialization implements ISsfInitialization {

        private final double std;

        Initialization(final double std) {
            this.std = std;
        }

        @Override
        public int getStateDim() {
            return 1;
        }

        @Override
        public boolean isDiffuse() {
            return false;
        }

        @Override
        public int getDiffuseDim() {
            return 0;
        }

        @Override
        public void diffuseConstraints(FastMatrix b) {
        }

        @Override
        public void a0(DataBlock a0) {
        }

        @Override
        public void Pf0(FastMatrix pf0) {
            pf0.set(0, 0, std * std);
        }
    }

    static class Dynamics implements ISsfDynamics {

        private final double scale, scale2;
        private final double[] std;

        Dynamics(double[] std, final double scale) {
            this.scale = scale;
            this.scale2 = scale * scale;
            this.std = std;
        }

        private double v(int pos) {
            if (pos < std.length) {
                double q = scale * std[pos];
                return q * q;
            } else {
                return scale2;
            }
        }

        private double e(int pos) {
            return pos < std.length ? std[pos] * scale : scale;
        }

        @Override
        public int getInnovationsDim() {
            return 1;
        }

        @Override
        public void V(int pos, FastMatrix qm) {
            qm.set(0, 0, v(pos));
        }

        @Override
        public void S(int pos, FastMatrix cm) {
            cm.set(0, 0, e(pos));
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
        }

        @Override
        public void TX(int pos, DataBlock x) {
            x.set(0);
        }

        @Override
        public void TVT(int pos, FastMatrix v) {
            v.set(0, 0, 0);
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            x.add(0, e(pos) * u.get(0));
        }

        @Override
        public void addV(int pos, FastMatrix p) {
            p.add(0, 0, v(pos));
        }

        @Override
        public void XT(int pos, DataBlock x) {
            x.set(0, 0);
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            xs.set(0, x.get(0) * e(pos));
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }

    }
}
