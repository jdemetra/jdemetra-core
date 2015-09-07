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
import ec.tstoolkit.maths.Complex;
import static ec.tstoolkit.maths.ComplexTest.assertEquals;
import static ec.tstoolkit.maths.polynomials.MullerNewtonSolver.computeA2;
import static ec.tstoolkit.maths.polynomials.MullerNewtonSolver.computeB2;
import static ec.tstoolkit.maths.polynomials.MullerNewtonSolver.computeC2;
import static ec.tstoolkit.maths.polynomials.MullerNewtonSolver.computeDiscr;
import ec.tstoolkit.random.IRandomNumberGenerator;
import ec.tstoolkit.random.XorshiftRNG;
import org.junit.Test;

/**
 *
 * @author pcuser
 */
public class MullerNewtonSolverTest {

//    @Test
    public void demoSolver() {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 3; ++i) {
            DataBlock q = new DataBlock(200);
            q.randomize();
            Polynomial p = Polynomial.fromData(q);
            MullerNewtonSolver s = new MullerNewtonSolver();
            //s.setLeastSquaresDivision(true);
            s.factorize(p);
            Complex[] roots = s.roots();
            Complex.lejaOrder(roots);
            Polynomial x = Polynomial.fromComplexRoots(roots, p.get(p.getDegree()));
            System.out.println(x.minus(p));
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

    private final IRandomNumberGenerator rng = new XorshiftRNG(0);

    private Complex random() {
        return Complex.cart(rng.nextDouble(), rng.nextDouble());
    }

    @Test
    public void testComputeA2() {
        for (int i = 0; i < 10; i++) {
            Complex q2 = random(), f0 = random(), f1 = random(), f2 = random();
            Complex result = q2.times(f2.plus(q2.times(f0)).minus(q2.plus(1).times(f1)));
            assertEquals(result, computeA2(q2, f2, f0, f1), 0);
        }
    }

    @Test
    public void testComputeB2() {
        for (int i = 0; i < 10; i++) {
            Complex q2 = random(), f0 = random(), f1 = random(), f2 = random();
            Complex result = f2.minus(f1).plus(q2.times(q2.times(f0.minus(f1)).plus(f2.minus(f1).times(2))));
            assertEquals(result, computeB2(f2, f1, q2, f0), 0);
        }
    }

    @Test
    public void testComputeC2() {
        for (int i = 0; i < 10; i++) {
            Complex q2 = random(), f2 = random();
            Complex result = q2.plus(1).times(f2);
            assertEquals(result, computeC2(q2, f2), 0);
        }
    }

    @Test
    public void testComputeDiscr() {
        for (int i = 0; i < 10; i++) {
            Complex A2 = random(), B2 = random(), C2 = random();
            Complex result = B2.times(B2).minus(A2.times(C2).times(4));
            assertEquals(result, computeDiscr(B2, A2, C2), 0);
        }
    }
}
