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

import static ec.tstoolkit.random.RandomNumberGeneratorAssert.*;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class MersenneTwisterTest {

    static final int SEED = 1234;
    static final int N = 1000;

    static MersenneTwister createRNG() {
        return new MersenneTwister(SEED);
    }

    @Test
    public void testNextDouble() {
        assertDoubleRange(createRNG(), N);
        assertPseudoRandomDoubleGenerator(createRNG(), createRNG(), N);
    }

    @Test
    public void testNextIntNegativeBound() {
        Assertions.assertThatIllegalArgumentException().isThrownBy(() -> createRNG().nextInt(-1));
    }

    @Test
    public void testNextIntPositiveBound() {
        assertIntRange(createRNG(), N, 10);
    }

    @Test
    public void testSynchronize() {
        assertSameSynchronized(createRNG());
    }
}
