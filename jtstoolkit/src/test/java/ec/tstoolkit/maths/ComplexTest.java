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
package ec.tstoolkit.maths;

import static ec.tstoolkit.maths.Complex.EPS;
import static ec.tstoolkit.maths.Complex.cart;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class ComplexTest {

    private static void assertEquals(Complex expected, Complex actual, double delta) {
        if (!expected.equals(actual, delta)) {
            Assert.fail("expected: " + expected + " but was: " + actual);
        }
    }

    private final Complex c1 = cart(54.654, 7.321);
    private final Complex c2 = cart(77, -66.12);
    private final Complex c3 = cart(3.14, 0);

    @Test
    public void testCart() {
        Assert.assertEquals(54.654, c1.getRe(), 0);
        Assert.assertEquals(7.321, c1.getIm(), 0);
        Assert.assertSame(Complex.ZERO, cart(0, 0));
        Assert.assertSame(Complex.ONE, cart(1, 0));
        Assert.assertSame(Complex.NEG_ONE, cart(-1, 0));
        Assert.assertSame(Complex.I, cart(0, 1));
        Assert.assertSame(Complex.NEG_I, cart(0, -1));
        Assert.assertNotSame(cart(54.654, 7.321), c1);
    }

    @Test
    public void testPlusComplex() {
        assertEquals(cart(131.654, -58.799), c1.plus(c2), EPS);
        Assert.assertEquals(c2.plus(c1), c1.plus(c2));
    }

    @Test
    public void testPlusDouble() {
        assertEquals(cart(57.794, 7.321), c1.plus(3.14), EPS);
    }

    @Test
    public void testMinusComplex() {
        assertEquals(cart(-22.346, 73.441), c1.minus(c2), EPS);
    }

    @Test
    public void testMinusDouble() {
        assertEquals(cart(51.514, 7.321), c1.minus(3.14), EPS);
    }

    @Test
    public void testTimesComplex() {
        assertEquals(cart(4692.42252, -3050.0054800000002), c1.times(c2), EPS);
        assertEquals(c2.times(c1), c1.times(c2), EPS);
        assertEquals(cart(171.61356, 22.987940000000002), c1.times(c3), EPS);
        assertEquals(c3.times(c1), c1.times(c3), EPS);
    }

    @Test
    public void testTimesDouble() {
        Assert.assertEquals(c1.times(c3), c1.times(3.14));
    }

    @Test
    public void testDivComplex() {
        assertEquals(cart(0.3615519000054986, 0.40554300816056576), c1.div(c2), EPS);
        assertEquals(cart(1.2248319286371856, -1.3738609168506026), c2.div(c1), EPS);
        assertEquals(cart(17.405732484076434, 2.331528662420382), c1.div(c3), EPS);
        assertEquals(cart(0.056439635813849275, -0.007560189076612701), c3.div(c1), EPS);
        assertEquals(Complex.ONE, c1.div(c1), EPS);
    }

    @Test
    public void testDivDouble() {
        assertEquals(c1.div(c3), c1.div(3.14), EPS);
    }

    @Test
    public void testToString() {
        Assert.assertEquals("(54.654 + 7.321i)", c1.toString());
        Assert.assertEquals("(77.0 - 66.12i)", c2.toString());
        Assert.assertEquals("(3.14 - 0.0i)", c3.toString());
    }
}
