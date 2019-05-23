/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths.linearfilters.internal;

import demetra.maths.Complex;
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.linearfilters.SymmetricFilter;
import jdplus.maths.polynomials.Polynomial;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class SymmetricMullerNewtonSolverTest {
    
    public SymmetricMullerNewtonSolverTest() {
    }

    @Test
    public void test1() {
        double[] coefficients=new double[]{1, -.5, -.3, .2, .1};
        BackFilter bf = BackFilter.ofInternal(coefficients);
        bf=bf.times(bf);
        SymmetricFilter sf=SymmetricFilter.fromFilter(bf);
        SymmetricMullerNewtonSolver solver=new SymmetricMullerNewtonSolver();
        solver.factorize(Polynomial.ofInternal(sf.weightsToArray()));
        Complex[] sroots = solver.getStableRoots();
        Polynomial p = Polynomial.fromComplexRoots(sroots, bf.get(bf.getDegree()));
        assertTrue(p.equals(bf.asPolynomial(), 1e-6));
    }
    
    @Test
    public void test2() {
        double[] coefficients=new double[]{1, -.5, -.3, .2, .1};
        BackFilter bf = BackFilter.ofInternal(coefficients);
        bf=bf.times(bf);
        bf=bf.times(BackFilter.D1);
        SymmetricFilter sf=SymmetricFilter.fromFilter(bf);
        SymmetricMullerNewtonSolver solver=new SymmetricMullerNewtonSolver();
        solver.factorize(Polynomial.ofInternal(sf.weightsToArray()));
        Complex[] sroots = solver.getStableRoots();
        Polynomial p = Polynomial.fromComplexRoots(sroots, bf.get(bf.getDegree()));
        assertTrue(p.equals(bf.asPolynomial(), 1e-6));
    }

    @Test
    public void test3() {
        // (1+x2)(1-.8x + .15 x2)
        double[] coefficients=new double[]{1, -.8, 1.15, -.8, .15};
        BackFilter bf = BackFilter.ofInternal(coefficients);
        bf=bf.times(BackFilter.D1);
        bf=bf.times(bf);
        SymmetricFilter sf=SymmetricFilter.fromFilter(bf);
        SymmetricMullerNewtonSolver solver=new SymmetricMullerNewtonSolver();
        solver.factorize(Polynomial.ofInternal(sf.weightsToArray()));
        Complex[] sroots = solver.getStableRoots();
//        for (Complex r : sroots)
//            System.out.println(r);
        Polynomial p = Polynomial.fromComplexRoots(sroots, bf.get(bf.getDegree()));
        assertTrue(p.equals(bf.asPolynomial(), 1e-9));
    }
}
