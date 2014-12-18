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
package ec.tstoolkit.random;

/**
 * Random number generator (RNG).
 *
 * @author Philippe Charles
 */
public interface IRandomNumberGenerator {

    /**
     * Returns the next pseudorandom, uniformly distributed {@code double} value
     * between {@code 0.0} and {@code 1.0} from this random number generator's
     * sequence.
     *
     * <p>The general contract of {@code nextDouble} is that one {@code double}
     * value, chosen (approximately) uniformly from the range {@code 0.0d}
     * (inclusive) to {@code 1.0d} (exclusive), is pseudorandomly generated and
     * returned.
     *
     * @return the next pseudorandom, uniformly distributed {@code double} value
     * between {@code 0.0} and {@code 1.0} from this random number generator's
     * sequence
     */
    double nextDouble();

    /**
     * Returns the next pseudorandom, uniformly distributed {@code int} value
     * from this random number generator's sequence. The general contract of
     * {@code nextInt} is that one {@code int} value is pseudorandomly generated
     * and returned.
     *
     * @return the next pseudorandom, uniformly distributed {@code int} value
     * from this random number generator's sequence
     */
    int nextInt();

    /**
     * Returns the next pseudorandom, uniformly distributed {@code long} value
     * from this random number generator's sequence. The general contract of
     * {@code nextLong} is that one {@code long} value is pseudorandomly
     * generated and returned.
     *
     * @return the next pseudorandom, uniformly distributed {@code long} value
     * from this random number generator's sequence
     */
    long nextLong();

    /**
     * Returns the next pseudorandom, uniformly distributed {@code boolean}
     * value from this random number generator's sequence. The general contract
     * of {@code nextBoolean} is that one {@code boolean} value is
     * pseudorandomly generated and returned. The values {@code true} and
     * {@code false} are produced with (approximately) equal probability.
     *
     * @return the next pseudorandom, uniformly distributed {@code boolean}
     * value from this random number generator's sequence
     */
    boolean nextBoolean();

    /**
     * Returns the next pseudorandom, uniformly distributed {@code float} value
     * between {@code 0.0} and {@code 1.0} from this random number generator's
     * sequence.
     *
     * @return the next pseudorandom, uniformly distributed {@code float} value
     * between {@code 0.0} and {@code 1.0} from this random number generator's
     * sequence
     */
    float nextFloat();

    /**
     * Returns a pseudorandom, uniformly distributed {@code int} value between 0
     * (inclusive) and the specified value (exclusive), drawn from this random
     * number generator's sequence.
     *
     * @param n the bound on the random number to be returned. Must be positive.
     * @return a pseudorandom, uniformly distributed {@code int} value between 0
     * (inclusive) and n (exclusive).
     * @throws IllegalArgumentException if {@code n <= 0}.
     */
    int nextInt(int n) throws IllegalArgumentException;

    /**
     * Return a thread-safe random number generator. <br>Note that this method
     * does nothing if the current generator is already thread-safe.
     *
     * @param rng
     * @return
     */
    IRandomNumberGenerator synchronize();
}
