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

package ec.tstoolkit.maths.polynomials;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.polynomials.Polynomial.Division;
import ec.tstoolkit.maths.polynomials.Polynomial.Doubles;
import static ec.tstoolkit.maths.polynomials.Polynomial.valueOf;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class PolynomialTest {

    static final double NO_DELTA = 0;
    static final double A = 123.456, B = 654.123, C = 741.963;
    static final double EPS = Polynomial.getEpsilon();

    static void assertCoefficientsEquals(double[] expecteds, Polynomial p) {
        assertArrayEquals(expecteds, p.getCoefficients(), NO_DELTA);
    }

    @Test
    public void testOf() {
        double[] data = new double[]{A, B};
        Polynomial p1 = Polynomial.of(data);
        assertCoefficientsEquals(data, p1);
        data[0] = 666;
        assertEquals(666, p1.get(0), NO_DELTA);
    }

    @Test
    public void testOfNull() {
        Assertions.assertThatIllegalArgumentException().isThrownBy(() -> Polynomial.of(null));
    }

    @Test
    public void testOfEmpty() {
        Assertions.assertThatIllegalArgumentException().isThrownBy(() -> Polynomial.of(new double[]{}));
    }

    @Test
    public void testCopyOf() {
        double[] data = new double[]{A, B};
        Polynomial p1 = Polynomial.copyOf(data);
        assertCoefficientsEquals(data, p1);
        data[0] = 666;
        assertEquals(A, p1.get(0), NO_DELTA);
    }

    @Test
    public void testCopyOfNull() {
        Assertions.assertThatIllegalArgumentException().isThrownBy(() -> Polynomial.copyOf(null));
    }

    @Test
    public void testCopyOfEmpty() {
        Assertions.assertThatIllegalArgumentException().isThrownBy(() -> Polynomial.copyOf(new double[]{}));
    }

    @Test
    public void testValueOf() {
        assertCoefficientsEquals(new double[]{0}, valueOf(0));
        assertCoefficientsEquals(new double[]{1}, valueOf(1));
        assertCoefficientsEquals(new double[]{1}, valueOf(1, 0));
        assertCoefficientsEquals(new double[]{A, B, C}, valueOf(A, B, C));
        assertCoefficientsEquals(new double[]{A}, valueOf(A));
        assertCoefficientsEquals(new double[]{EPS}, valueOf(EPS));
    }

    @Test
    public void testNULL() {
        assertEquals(0, Polynomial.ZERO.getDegree());
        assertEquals(0, Polynomial.ZERO.get(0), NO_DELTA);
        assertTrue(Polynomial.ZERO.isZero());
        assertFalse(Polynomial.ZERO.isIdentity());
        assertCoefficientsEquals(new double[]{0}, Polynomial.ZERO);
        assertEquals(valueOf(0), Polynomial.ZERO);
    }

    @Test
    public void testONE() {
        assertEquals(0, Polynomial.ONE.getDegree());
        assertEquals(1, Polynomial.ONE.get(0), NO_DELTA);
        assertTrue(Polynomial.ONE.isIdentity());
        assertFalse(Polynomial.ONE.isZero());
        assertCoefficientsEquals(new double[]{1}, Polynomial.ONE);
        assertEquals(valueOf(1), Polynomial.ONE);
    }

    @Test
    public void testGetCoefficients() {
        double[] data = new double[]{A, B};
        Polynomial p1 = Polynomial.of(data);
        assertCoefficientsEquals(data, p1);
        assertNotSame(data, p1.getCoefficients());
    }

    @Test
    public void testGetDegree() {
        assertEquals(2, valueOf(A, B, C).getDegree());
        assertEquals(0, valueOf(1).getDegree());
        assertEquals(0, valueOf(0, 0, 0).getDegree());
    }

    @Test
    public void testDerivate() {
        assertCoefficientsEquals(new double[]{B, 2 * C}, valueOf(A, B, C).derivate());
        assertCoefficientsEquals(new double[]{0}, valueOf(A).derivate());
    }

    @Test
    public void testIsNull() {
        assertTrue(valueOf(0).isZero());
        assertFalse(valueOf(1).isZero());
        assertTrue(valueOf(0, 0).isZero());
        assertFalse(valueOf(0, 1).isZero());
    }

    @Test
    public void testIsIdentity() {
        assertTrue(valueOf(1).isIdentity());
        assertFalse(valueOf(0).isIdentity());
        assertTrue(valueOf(1, 0).isIdentity());
        assertFalse(valueOf(0, 1).isIdentity());
    }

    @Test
    public void testDivision() {
        Polynomial a = valueOf(1, 0, -1, 1);
        Polynomial b = valueOf(0, 0, 1);
        Division division = Polynomial.divide(a, b);
        assertCoefficientsEquals(new double[]{-1, 1}, division.getQuotient());
        assertCoefficientsEquals(new double[]{1}, division.getRemainder());
    }

    @Test
    public void testDivide() {
        // divideByDouble
        assertCoefficientsEquals(new double[]{A / 2, B / 2, C / 2}, valueOf(A, B, C).divide(2));
        assertCoefficientsEquals(new double[]{Double.POSITIVE_INFINITY}, valueOf(A).divide(0));
        assertCoefficientsEquals(new double[]{Double.NEGATIVE_INFINITY}, valueOf(-A).divide(0));
        // divideByPolynomial
        assertCoefficientsEquals(new double[]{-1, 1}, valueOf(1, 0, -1, 1).divide(valueOf(0, 0, 1)));
        assertCoefficientsEquals(new double[]{Double.POSITIVE_INFINITY}, valueOf(A).divide(valueOf(0)));
        assertCoefficientsEquals(new double[]{Double.NEGATIVE_INFINITY}, valueOf(-A).divide(valueOf(0)));
    }

    @Test
    public void testMinus() {
        assertCoefficientsEquals(new double[]{A, B, C}, valueOf(A, B, C).minus(0));
        assertCoefficientsEquals(new double[]{A - 2, B, C}, valueOf(A, B, C).minus(2));
        assertCoefficientsEquals(new double[]{0, 0, -C}, valueOf(A, B).minus(valueOf(A, B, C)));
    }

    @Test
    public void testMirror() {
        assertCoefficientsEquals(new double[]{A}, valueOf(A).mirror());
        assertCoefficientsEquals(new double[]{B, A}, valueOf(A, B).mirror());
        assertCoefficientsEquals(new double[]{C, B, A}, valueOf(A, B, C).mirror());
        assertCoefficientsEquals(new double[]{B, A}, valueOf(A, B, 0).mirror());
        assertCoefficientsEquals(new double[]{C, B}, valueOf(0, B, C).mirror());
    }

    @Test
    public void testNegate() {
        assertCoefficientsEquals(new double[]{-A, B, -C}, valueOf(A, -B, C).negate());
    }

    @Test
    public void testPlus() {
        // plusByDouble
        assertCoefficientsEquals(new double[]{A, B, C}, valueOf(A, B, C).plus(0));
        assertCoefficientsEquals(new double[]{A + C, B}, valueOf(A, B).plus(C));
        // plusByPolynomial
        assertCoefficientsEquals(new double[]{A, B}, valueOf(A, B).plus(valueOf(0)));
        assertCoefficientsEquals(new double[]{A, B}, valueOf(0).plus(valueOf(A, B)));
        assertCoefficientsEquals(new double[]{0}, valueOf(A, B).plus(valueOf(-A, -B)));
        assertCoefficientsEquals(new double[]{0, B * 2}, valueOf(A, B).plus(valueOf(-A, B)));
        assertCoefficientsEquals(new double[]{A, B}, valueOf(A).plus(valueOf(0, B)));
        assertCoefficientsEquals(new double[]{A + C, B - C, C}, valueOf(A, B, C).plus(valueOf(C, -C)));
        assertCoefficientsEquals(new double[]{A + C, B - C, C}, valueOf(C, -C).plus(valueOf(A, B, C)));
    }

    @Test
    public void testTimes() {
        // timesByDouble
        assertCoefficientsEquals(new double[]{A * C, B * C}, valueOf(A, B).times(C));
        assertCoefficientsEquals(new double[]{0}, valueOf(A, B, C).times(0));
        assertCoefficientsEquals(new double[]{A, B, C}, valueOf(A, B, C).times(1));
        // timesByPolynomial
        assertCoefficientsEquals(new double[]{-2, 6, -7, 1, 4, -3}, valueOf(-2, 4, -3).times(valueOf(1, -1, 0, 1)));
        assertCoefficientsEquals(new double[]{0}, valueOf(A, B, C).times(valueOf(0)));
        assertCoefficientsEquals(new double[]{0}, valueOf(0).times(valueOf(A, B, C)));
        assertCoefficientsEquals(new double[]{A, B, C}, valueOf(A, B, C).times(valueOf(1)));
        assertCoefficientsEquals(new double[]{A, B, C}, valueOf(1).times(valueOf(A, B, C)));
    }

    @Test
    public void testSmooth() {
        assertCoefficientsEquals(new double[]{666}, valueOf(666).smooth());

        assertCoefficientsEquals(new double[]{0}, valueOf(EPS).smooth());
        assertCoefficientsEquals(new double[]{EPS * 2}, valueOf(EPS * 2).smooth());
        assertCoefficientsEquals(new double[]{666}, valueOf(666 + EPS).smooth());
    }

    @Test
    public void testEquals() {
        assertTrue(valueOf(A, B, C).equals(valueOf(A, B, C)));
        assertFalse(valueOf(B, C, A).equals(valueOf(A, B, C)));
        assertFalse(valueOf(A, B).equals(valueOf(A, B, C)));
        assertTrue(valueOf(A, B).equals(valueOf(A, B, 0)));
        assertTrue(valueOf(0).equals(valueOf(0, 0, 0)));

        assertTrue(valueOf(0).equals(valueOf(EPS)));
        assertTrue(valueOf(EPS * 2).equals(valueOf(EPS * 2)));
        assertTrue(valueOf(666).equals(valueOf(666 + EPS)));
    }

    @Test
    public void testAdjustDegree() {
        assertCoefficientsEquals(new double[]{A, B, C}, valueOf(A, B, C).adjustDegree());
        assertCoefficientsEquals(new double[]{A, B}, valueOf(A, B, 0).adjustDegree());
        assertCoefficientsEquals(new double[]{0}, valueOf(0).adjustDegree());

        assertCoefficientsEquals(new double[]{A}, valueOf(A, EPS).adjustDegree());
        assertCoefficientsEquals(new double[]{A, EPS * 2}, valueOf(A, EPS * 2).adjustDegree());
        assertCoefficientsEquals(new double[]{A, 666 + EPS}, valueOf(A, 666 + EPS).adjustDegree());
    }

    @Test
    public void testDoublesEquals() {
        assertTrue(Doubles.equals(0, 0, EPS));
        assertTrue(Doubles.equals(666, 666, EPS));
        assertTrue(Doubles.equals(666, 666 + EPS, EPS));
        assertTrue(Doubles.equals(666, 666 - EPS, EPS));
        assertFalse(Doubles.equals(666, 666 + EPS * 2, EPS));
        assertFalse(Doubles.equals(666, 666 - EPS * 2, EPS));
    }
    
    @Test
    public void testEvaluate() {
        double[] x=new double[20];
        DataBlock X=new DataBlock(x);
        X.randomize();
        double y0 = Polynomial.evaluate(x, .25);
        Polynomial p = Polynomial.of(x);
        double y1 = p.evaluateAt(.25);
        assertEquals(y0, y1, EPS);
    }
    
    @Test
    public void testRevaluate() {
        double[] x=new double[20];
        DataBlock X=new DataBlock(x);
        X.randomize();
        double y0 = Polynomial.revaluate(x, .25);
        Polynomial p = Polynomial.fromData(X.reverse());
        double y1 = p.evaluateAt(.25);
        assertEquals(y0, y1, EPS);
    }
    
    @Test
    public void testFnEvaluate() {
        final double[] x=new double[20];
        DataBlock X=new DataBlock(x);
        X.randomize();
        double y0 = Polynomial.evaluate(x.length-1, i->x[i], .25);
        Polynomial p = Polynomial.of(x);
        double y1 = p.evaluateAt(.25);
        assertEquals(y0, y1, EPS);
    }
}
