/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.polynomials;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class FastEigenValuesSolverTest {
    
    public FastEigenValuesSolverTest() {
    }

    @Test
    public void testSmall() {
        Polynomial P=Polynomial.of(.2, .3, .5, -.8, -.6, 1);
        FastEigenValuesSolver solver=new FastEigenValuesSolver();
        solver.factorize(P);
    }
    
    @Test
    public void testSmall2() {
        Polynomial P=Polynomial.of(.2, .3, .5, -.8, -.6, 1);
        FastEigenValuesSolver solver=new FastEigenValuesSolver();
        solver.factorize(P);
    }
    
}
