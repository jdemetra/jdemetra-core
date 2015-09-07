/*
 * Copyright 2015 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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

import static ec.tstoolkit.maths.Complex.cart;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class ComplexBuilderTest {

    private final Complex c1 = cart(54.654, 7.321);
    private final Complex c2 = cart(77, -66.12);
    private final Complex c3 = cart(3.14, 0);

    @Test
    public void testConstructorComplex() {
        assertEquals(c1, new ComplexBuilder(c1).build());
    }

    @Test
    public void testConstructorDouble() {
        assertEquals(c3, new ComplexBuilder(3.14).build());
    }

    @Test
    public void testConstructorDouble2() {
        assertEquals(c1, new ComplexBuilder(54.654, 7.321).build());
    }

    @Test
    public void testGetRe() {
        assertEquals(c1.getRe(), new ComplexBuilder(c1).getRe(), 0);
    }

    @Test
    public void testGetIm() {
        assertEquals(c1.getIm(), new ComplexBuilder(c1).getIm(), 0);
    }

    @Test
    public void testAddComplex() {
        assertEquals(c1.plus(c2), new ComplexBuilder(c1).add(c2).build());
    }

    @Test
    public void testAddDouble() {
        assertEquals(c1.plus(3.14), new ComplexBuilder(c1).add(3.14).build());
    }

    @Test
    public void testSubComplex() {
        assertEquals(c1.minus(c2), new ComplexBuilder(c1).sub(c2).build());
    }

    @Test
    public void testSubDouble() {
        assertEquals(c1.minus(3.14), new ComplexBuilder(c1).sub(3.14).build());
    }

    @Test
    public void testMulComplex() {
        assertEquals(c1.times(c2), new ComplexBuilder(c1).mul(c2).build());
        assertEquals(c2.times(c1), new ComplexBuilder(c2).mul(c1).build());
    }

    @Test
    public void testMulDouble() {
        assertEquals(c1.times(3.14), new ComplexBuilder(c1).mul(3.14).build());
    }

    @Test
    public void testDivComplex() {
        assertEquals(c1.div(c2), new ComplexBuilder(c1).div(c2).build());
    }

    @Test
    public void testDivDouble() {
        assertEquals(c1.div(3.14), new ComplexBuilder(c1).div(3.14).build());
    }

    @Test
    public void testChs() {
        assertEquals(c1.negate(), new ComplexBuilder(c1).chs().build());
    }

    @Test
    public void testInv() {
        assertEquals(c1.inv(), new ComplexBuilder(c1).inv().build());
    }

}
