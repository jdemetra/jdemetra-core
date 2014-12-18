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

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Assert;
import static org.junit.Assert.*;

/**
 *
 * @author Philippe Charles
 */
final class RandomNumberGeneratorAssert {

    private RandomNumberGeneratorAssert() {
        // static class
    }

    static void assertDoubleRange(IRandomNumberGenerator rng, int n) {
        Matcher<Double> m = doubleBetween(0, 1);
        for (int i = 0; i < n; i++) {
            assertThat(rng.nextDouble(), m);
        }
    }

    static void assertPseudoRandomDoubleGenerator(IRandomNumberGenerator l, IRandomNumberGenerator r, int n) {
        for (int i = 0; i < n; i++) {
            assertEquals(l.nextDouble(), r.nextDouble(), 0);
        }
    }

    static void assertIntRange(IRandomNumberGenerator rng, int n, int to) {
        Matcher<Integer> m = intBetween(0, to);
        for (int i = 0; i < n; i++) {
            assertThat(rng.nextInt(to), m);
        }
    }

    static void assertSameSynchronized(IRandomNumberGenerator rng) {
        IRandomNumberGenerator tmp = rng.synchronize();
        Assert.assertSame(tmp, tmp.synchronize());
    }

    static Matcher<Double> doubleBetween(final double from, final double to) {
        return new TypeSafeMatcher<Double>() {
            @Override
            public boolean matchesSafely(Double item) {
                return from <= item && item < to;
            }

            @Override
            public void describeTo(Description d) {
                d.appendText("value between " + from + " (inclusive) and " + to + " (exclusive)");
            }
        };
    }

    static Matcher<Integer> intBetween(final double from, final double to) {
        return new TypeSafeMatcher<Integer>() {
            @Override
            public boolean matchesSafely(Integer item) {
                return from <= item && item < to;
            }

            @Override
            public void describeTo(Description d) {
                d.appendText("value between " + from + " (inclusive) and " + to + " (exclusive)");
            }
        };
    }
}
