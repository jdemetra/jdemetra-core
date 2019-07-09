/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.polynomials;

import jdplus.maths.matrices.CanonicalMatrix;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class FastEigenValuesSolver2Test {

    public FastEigenValuesSolver2Test() {

    }

    @Test
    public void testSmall() {
        Polynomial P = Polynomial.of(.2, .3, .5, -.8, -.6, 1);
        FastEigenValuesSolver2 solver = new FastEigenValuesSolver2();
        solver.factorize(P);
    }

}
