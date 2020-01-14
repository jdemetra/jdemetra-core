/*
 * Copyright 2016 National Bank of Belgium
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
package jdplus.math.polynomials;

import demetra.math.Complex;
import jdplus.math.ComplexMath;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
public class PolynomialTest {

    public PolynomialTest() {
    }

    @Test
    public void testEvaluate() {
        final int n = 100;
        double[] array = DoubleSeq.onMapping(n, i -> 1).toArray();
        Polynomial p = Polynomial.ofInternal(array);
        Complex x = Complex.cart(-.99, .1);
        Complex x1 = p.evaluateAt(x);
        Complex div = ComplexMath.pow(x, 100).minus(1).div(x.minus(1));
        assertTrue(x1.equals(div, 1e-9));

        double y = 1.0000055;
        double x2 = p.evaluateAt(y);
        assertEquals(x2, (1 - Math.pow(y, 100)) / (1 - y), 1e-9);
    }

    @Test
    public void testAdd() {
        final int n1 = 100, n2 = 10;
        double[] array1 = DoubleSeq.onMapping(n1, i -> i).toArray();
        Polynomial p1 = Polynomial.ofInternal(array1);
        double[] array2 = DoubleSeq.onMapping(n2, i -> i + 2).toArray();
        Polynomial p2 = Polynomial.ofInternal(array2);

        Polynomial s1 = p1.plus(p2);
        Polynomial s2 = p2.plus(p1);

        Polynomial d = s1.minus(s2);
        assertTrue(d.isZero());
    }

    @Test
    public void testSub() {
        final int n1 = 100, n2 = 10;
        double[] array1 = DoubleSeq.onMapping(n1, i -> i).toArray();
        Polynomial p1 = Polynomial.ofInternal(array1);
        double[] array2 = DoubleSeq.onMapping(n2, i -> i + 2).toArray();
        Polynomial p2 = Polynomial.ofInternal(array2);

        Polynomial s1 = p1.minus(p2);
        Polynomial s2 = p2.minus(p1);

        Polynomial d = s1.plus(s2);
        assertTrue(d.isZero());
    }

    @Test
    //@Ignore
    public void testRoots() {
        Polynomial p1 = Polynomial.valueOf(1, -.5), p2 = Polynomial.valueOf(1, -1.3, .5), p3 = Polynomial.valueOf(1, -.2);
        Polynomial p = p1.times(p1).times(p2).times(p3).times(p3);
        p = p.times(p).times(p3);
        Complex[] roots = p.roots();
//        for (Complex root : roots) {
//            System.out.println(root);
//        }
//        Complex[] rroots = p.roots(IRootsSolver.robustSolver());
//        for (Complex root : rroots) {
//            System.out.println(root);
//        }
    }

    @Test
    //@Ignore
    public void testUnitRoots() {
        int N = 100;
        double[] p = new double[N + 1];
        p[0] = 1;
        p[N] = -3;
        Polynomial P = Polynomial.ofInternal(p);

        double w = Math.pow(1.0 / 3, 1.0 / N);
        Complex[] roots = P.roots();
        for (Complex root : roots) {
            assertTrue(Math.abs(root.abs() - w) < 1e-9);
            double arg = root.arg() * N / (2 * Math.PI);
            assertTrue(Math.abs(arg - Math.round(arg)) < 1e-12);

        }
        Complex[] rroots = P.roots(RootsSolver.robustSolver());
        for (Complex root : rroots) {
            assertTrue(Math.abs(root.abs() - w) < 1e-9);
            double arg = root.arg() * N / (2 * Math.PI);
            assertTrue(Math.abs(arg - Math.round(arg)) < 1e-12);
        }
    }

    public static void main(String[] arg) {
        stressTestUnitRoots();
    }

    public static void stressTestUnitRoots() {
        int N = 200, K = 10;

        double[] p = new double[N + 1];
        p[0] = 1;
        p[N] = -3;
        Polynomial P = Polynomial.ofInternal(p);

        long t0 = System.currentTimeMillis();
        double w = Math.pow(1.0 / 3, 1.0 / N);
        for (int k = 0; k < K; ++k) {
            Complex[] roots = P.roots(new EigenValuesSolver());
            for (Complex root : roots) {
                assertTrue(Math.abs(root.abs() - w) < 1e-9);
                double arg = root.arg() * N / (2 * Math.PI);
                assertTrue(Math.abs(arg - Math.round(arg)) < 1e-12);
            }
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            Complex[] roots = P.roots(new MullerNewtonSolver());
            for (Complex root : roots) {
                assertTrue(Math.abs(root.abs() - w) < 1e-9);
                double arg = root.arg() * N / (2 * Math.PI);
                assertTrue(Math.abs(arg - Math.round(arg)) < 1e-12);
            }
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
 //           Polynomial Q = P.times(P);
            Complex[] roots = P.roots(RootsSolver.robustSolver());
            for (Complex root : roots) {
                assertTrue(Math.abs(root.abs() - w) < 1e-9);
                double arg = root.arg() * N / (2 * Math.PI);
                assertTrue(Math.abs(arg - Math.round(arg)) < 1e-12);
            }
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }
}
