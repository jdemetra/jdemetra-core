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
package demetra.random;

import demetra.design.Development;
import java.util.Date;

/**
 * A pseudorandom number generator that uses the Mersenne twister algorithm.
 *
 * @see http://en.wikipedia.org/wiki/Mersenne_twister
 * @author Jean Palate, Jeremy Demortier
 */
@Development(status = Development.Status.Alpha)
public class MersenneTwister extends AbstractRNG {

    // STATIC FACTORY METHODS >
    /**
     * Creates a new MersenneTwister using {@link System#nanoTime()} as seed.
     *
     * @return a new MersenneTwister instance
     */
    public static MersenneTwister fromSystemNanoTime() {
        return new MersenneTwister((int) System.nanoTime());
    }

    /**
     * Creates a new MersenneTwister using {@link Date#getTime()} as seed.
     *
     * @return a new MersenneTwister instance
     */
    public static MersenneTwister fromDate(Date date) {
        return new MersenneTwister((int) date.getTime());
    }
    // < STATIC FACTORY METHODS
    //
    /* Period parameters */
    private static final int N = 624;
    private static final int M = 397;
    private static final int MATRIX_A = 0x9908b0df; /* constant vector a */

    /*
     * most significant w-r bits
     */
    private static final int UPPER_MASK = 0x80000000;
    /*
     * least significant r bits
     */
    private static final int LOWER_MASK = 0x7fffffff;

    /* for tempering */
    private static final int TEMPERING_MASK_B = 0x9d2c5680;
    private static final int TEMPERING_MASK_C = 0xefc60000;
    private static final int mag0 = 0x0;
    private static final int mag1 = MATRIX_A;
    private int mti;
    private final int[] mt = new int[N]; /* set initial seeds: N = 624 words */

    // private final int DEFAULT_SEED = 4357;

    /**
     * Constructs and returns a random number generator with the given seed.
     *
     * @param seed should not be 0, in such a case
     * <tt>MersenneTwister.DEFAULT_SEED</tt> is silently substituted.
     */
    public MersenneTwister(final int seed) {
        setSeed(seed);
    }

    /**
     *
     */
    protected void nextBlock() {
        // ******************** UNOPTIMIZED **********************
        int y;
        int kk;

        for (kk = 0; kk < N - M; kk++) {
            y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
            mt[kk] = mt[kk + M] ^ (y >>> 1) ^ ((y & 0x1) == 0 ? mag0 : mag1);
        }
        for (; kk < N - 1; kk++) {
            y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
            mt[kk] = mt[kk + M - N] ^ (y >>> 1)
                    ^ ((y & 0x1) == 0 ? mag0 : mag1);
        }
        y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
        mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ ((y & 0x1) == 0 ? mag0 : mag1);

        mti = 0;
    }

    /**
     * Returns a 32 bit uniformly distributed random number in the closed
     * interval <tt>[Integer.MIN_VALUE,Integer.MAX_VALUE]</tt> (including
     * <tt>Integer.MIN_VALUE</tt> and <tt>Integer.MAX_VALUE</tt>).
     *
     * @return
     */
    @Override
    public int nextInt() {
        /* Each single bit including the sign bit will be random */
        if (mti == N) {
            nextBlock(); // generate N ints at one time
        }
        int y = mt[mti++];
        y ^= (y >>> 11); // y ^= TEMPERING_SHIFT_U(y );
        y ^= (y << 7) & TEMPERING_MASK_B; // y ^= TEMPERING_SHIFT_S(y) &
        // TEMPERING_MASK_B;
        y ^= (y << 15) & TEMPERING_MASK_C; // y ^= TEMPERING_SHIFT_T(y) &
        // TEMPERING_MASK_C;
        // y &= 0xffffffff; //you may delete this line if word size = 32
        y ^= (y >>> 18); // y ^= TEMPERING_SHIFT_L(y);
        return y;
    }

    /**
     * Sets the receiver's seed. This method resets the receiver's entire
     * internal state.
     *
     * @param seed should not be 0, in such a case
     * <tt>MersenneTwister.DEFAULT_SEED</tt> is substituted.
     */
    protected final void setSeed(int seed) {
        /*
         * setting initial seeds to mt[N] using the generator Line 25 of Table 1
         * in [KNUTH 1981, The Art of Computer Programming Vol. 2 (2nd Ed.),
         * pp102]
         */
        if (seed == 0) {
            seed = 123456;
        }

        mt[0] = seed & 0xffffffff;
        for (mti = 1; mti < N; mti++) {
            mt[mti] = (69069 * mt[mti - 1]) & 0xffffffff;
        }

    }
}
