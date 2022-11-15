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
import static org.junit.Assert.assertTrue;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class XorshiftRNGTest {

    static final int SEED = 1234;
    static final int N = 1000;

    static XorshiftRNG createRNG() {
        return new XorshiftRNG(SEED);
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
    
    @Test
    public void testRange() {
        double s=0;
        XorshiftRNG rng = createRNG();
        int N=100000;
        for (int i=0; i<N; ++i){
            s+=rng.nextDouble();
        }
        s/=N;
        assertTrue(Math.abs(s-.5)<.01);
    }
    
}
