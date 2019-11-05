/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.maths;

import jdplus.math.Arithmetics;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class ArithmeticsTest {

    public ArithmeticsTest() {
    }

    @Test
    public void testSumPowers() {
        for (int k = 2; k <= 10; ++k) {
            assertTrue(Arithmetics.sumOfPowers(k, 25) == sp(k, 25));
        }
    }

    @Test
    public void testgcd() {
        int c = 2 * 3 * 4 * 5, a = c * 12, b = c * 35;
        assertTrue(Arithmetics.gcd(a, b) == c);
        b = c * 30;
        assertFalse(Arithmetics.gcd(a, b) == c);
        b = -c * 35;
        assertTrue(Arithmetics.gcd(a, b) == c);
    }

    @Test
    public void testlonggcd() {
        long c = 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9, a = c * 12, b = c * 35;
        assertTrue(Arithmetics.gcd(a, b) == c);
        b = c * 30;
        assertFalse(Arithmetics.gcd(a, b) == c);
    }

    private long sp(int k, int n) {
        long s = 1;
        for (int i = 2; i <= n; ++i) {
            long c = i;
            for (int j = 2; j <= k; ++j) {
                c *= i;
            }
            s += c;
        }
        return s;
    }

}
