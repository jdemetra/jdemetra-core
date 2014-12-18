/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 will be approved by the European Commission - subsequent
 versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 writing, software distributed under the Licence is
 distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 * See the Licence for the specific language governing
 permissions and limitations under the Licence.
 */
package ec.tstoolkit.ssf.multivariate;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.MatrixStorage;
import ec.tstoolkit.maths.matrices.SubMatrix;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class NoiseProviders {

    public static INoiseProvider timeInvariant(final Matrix E) {
        return new TimeInvariantNoise(E);
    }

    public static INoiseProvider timeVariant(final MatrixStorage E) {
        return new TimeVariantNoise(E);
    }

    private static class TimeInvariantNoise implements INoiseProvider {

        private final Matrix E;

        private TimeInvariantNoise(final Matrix E) {
            this.E = E;
        }

        @Override
        public boolean hasNoise(int pos) {
            return true;
        }

        @Override
        public void e(int pos, SubMatrix n) {
            n.copy(E.subMatrix());
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }
    }

    private static class TimeVariantNoise implements INoiseProvider {

        private final MatrixStorage E;

        private TimeVariantNoise(final MatrixStorage E) {
            this.E = E;
        }

        @Override
        public boolean hasNoise(int pos) {
            return true;
        }

        @Override
        public void e(int pos, SubMatrix n) {
            n.copy(E.matrix(pos));
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }
    }
}
