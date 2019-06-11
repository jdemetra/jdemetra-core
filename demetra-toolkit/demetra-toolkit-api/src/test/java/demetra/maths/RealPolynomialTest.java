/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class RealPolynomialTest {
    
    public RealPolynomialTest() {
    }

    @Test
    public void testConstructor() {
        assertTrue(RealPolynomial.of(new double[]{0,0,0}) == RealPolynomial.ZERO);
        assertTrue(RealPolynomial.of(new double[]{1,0,0}) == RealPolynomial.ONE);
        assertTrue(RealPolynomial.of(new double[]{1}) == RealPolynomial.ONE);
    }
    
    @Test
    public void testOperations() {
        RealPolynomial P1 = RealPolynomial.of(new double[]{1,2,3});
        RealPolynomial P2 = RealPolynomial.of(new double[]{1,2});
        assertTrue( P1.times(P2).degree() == 3);
        assertTrue( P1.plus(P2).degree() == 2);
        assertTrue( P1.minus(P2).degree() == 2);
        assertTrue( P1.plus(-3).degree() == 1);
        assertTrue( P1.times(P2).minus(P2.times(P1)) == RealPolynomial.ZERO);
    }
}
