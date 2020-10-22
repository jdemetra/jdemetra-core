/*
* Copyright 2013 National Bank of Belgium
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
package jdplus.random;

import nbbrd.design.Development;


/**
 * An abstract class used to simplify the implementation of a random number
 * generator .
 *
 * @author Jean Palate
 * @author Philippe Charles
 */
@Development(status = Development.Status.Alpha)
public abstract class AbstractRNG implements RandomNumberGenerator {

    private static final double IntToDoubleMultiplier = 1.0 / (Integer.MAX_VALUE + 1.0);

    @Override
    public double nextDouble() {
        return (nextInt() >>> 1) * IntToDoubleMultiplier;
    }

    @Override
    public long nextLong() {
        // concatenate two 32-bit strings into one 64-bit string
        return ((nextInt() & 0xFFFFFFFFL) << 32) | ((nextInt() & 0xFFFFFFFFL));
    }

    @Override
    public boolean nextBoolean() {
        return nextInt() % 2 != 0;
    }

    @Override
    public float nextFloat() {
        return (float) nextDouble();
    }

    @Override
    public int nextInt(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException(String.valueOf(n));
        }
        int result = (int) (nextDouble() * n);
        return result < n ? result : n - 1;
    }

    @Override
    public RandomNumberGenerator synchronize() {
        return new SynchronizedRNG(this);
    }

    private static final class SynchronizedRNG implements RandomNumberGenerator {

        private final RandomNumberGenerator rng;

        private SynchronizedRNG(RandomNumberGenerator rng) {
            this.rng = rng;
        }

        @Override
        public synchronized double nextDouble() {
            return rng.nextDouble();
        }

        @Override
        public synchronized int nextInt() {
            return rng.nextInt();
        }

        @Override
        public synchronized long nextLong() {
            return rng.nextLong();
        }

        @Override
        public synchronized boolean nextBoolean() {
            return rng.nextBoolean();
        }

        @Override
        public synchronized float nextFloat() {
            return rng.nextFloat();
        }

        @Override
        public synchronized int nextInt(int n) throws IllegalArgumentException {
            return rng.nextInt(n);
        }

        @Override
        public RandomNumberGenerator synchronize() {
            return this;
        }
    }
}
