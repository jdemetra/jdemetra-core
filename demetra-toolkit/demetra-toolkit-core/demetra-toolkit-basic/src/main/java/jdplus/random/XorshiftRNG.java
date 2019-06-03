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
import demetra.design.Development;
import demetra.random.RandomNumberGenerator;


/**
 * A pseudorandom number generator that uses the Xorshift algorithm.
 *
 * @see http://en.wikipedia.org/wiki/Xorshift
 * @author Jean Palate, Jeremy Demortier
 */
@Development(status = Development.Status.Release)
@AlgorithmImplementation(algorithm=RandomNumberGenerator.class)
public class XorshiftRNG extends AbstractRNG {

    // STATIC FACTORY METHODS >
    public static XorshiftRNG fromSystemNanoTime() {
        return new XorshiftRNG((int) System.nanoTime());
    }
    // < STATIC FACTORY METHODS 
    //
    private int m_x, m_y, m_z, m_w;
    private final int m_seed;
    private static final int Seed_Y = 362436069;
    private static final int Seed_Z = 521288629;
    private static final int Seed_W = 88675123;

    /**
     *
     * @param seed
     */
    public XorshiftRNG(final int seed) {
        m_seed = seed;
        resetGenerator();
    }

    /**
     *
     * @return
     */
    @Override
    public int nextInt() {
        int t = (m_x ^ (m_x << 11));
        m_x = m_y;
        m_y = m_z;
        m_z = m_w;
        m_w = (m_w ^ (m_w >>> 19)) ^ (t ^ (t >>> 8));

        return m_w;
    }

    private void resetGenerator() {
        // "The seed set for xor128 is four 32-bit integers x,y,z,w not all 0,
        // ..." (George Marsaglia)
        // To meet that requirement the y, z, w seeds are constant values
        // greater 0.
        m_x = m_seed;
        m_y = Seed_Y;
        m_z = Seed_Z;
        m_w = Seed_W;

        // Reset helper variables used for generation of random bools.
    }
}
