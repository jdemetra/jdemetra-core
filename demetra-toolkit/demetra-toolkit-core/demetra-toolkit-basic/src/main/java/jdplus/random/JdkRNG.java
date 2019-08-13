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

import demetra.design.AlgorithmImplementation;
import demetra.random.RandomNumberGenerator;
import static demetra.design.AlgorithmImplementation.Feature.Balanced;
import java.security.SecureRandom;
import java.util.Random;
import nbbrd.service.ServiceProvider;

/**
 * A random number generator (RNG) that uses JDK's {@link Random} class.<br>
 * Note that this implementation is thread-safe thanks to synchronizes in the
 * {@link Random} class.<br> {@link Random} generates pseudo random numbers. In
 * order to get a cryptographically strong random number generator, you must use
 * {@link SecureRandom}.
 *
 * @see Random
 * @see SecureRandom
 *
 * @author Philippe Charles
 */
@AlgorithmImplementation(algorithm=RandomNumberGenerator.class)
public class JdkRNG implements RandomNumberGenerator {

    // STATIC FACTORY METHODS >
    public static JdkRNG newRandom() {
        return new JdkRNG(new Random());
    }

    public static JdkRNG newRandom(long seed) {
        return new JdkRNG(new Random(seed));
    }
    // < STATIC FACTORY METHODS
    //
    final Random random;

    public JdkRNG(Random random) {
        this.random = random;
    }

    public Random getRandom() {
        return random;
    }

    @Override
    public double nextDouble() {
        return random.nextDouble();
    }

    @Override
    public int nextInt() {
        return random.nextInt();
    }

    @Override
    public long nextLong() {
        return random.nextLong();
    }

    @Override
    public boolean nextBoolean() {
        return random.nextBoolean();
    }

    @Override
    public float nextFloat() {
        return random.nextFloat();
    }

    @Override
    public int nextInt(int n) {
        return random.nextInt(n);
    }

    @Override
    public JdkRNG synchronize() {
        return this;
    }
}
