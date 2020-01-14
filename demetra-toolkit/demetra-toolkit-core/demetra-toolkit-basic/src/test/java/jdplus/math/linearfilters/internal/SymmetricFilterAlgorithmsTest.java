/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.linearfilters.internal;

import jdplus.math.linearfilters.internal.SymmetricFilterAlgorithms;
import jdplus.math.linearfilters.SymmetricFilter;
import jdplus.math.polynomials.Polynomial;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class SymmetricFilterAlgorithmsTest {

    public SymmetricFilterAlgorithmsTest() {
    }

    @Test
    public void test0() {
        Polynomial R = Polynomial.of(.6);
        SymmetricFilter SF=SymmetricFilter.convolutionOf(R, 1);
        SymmetricFilter.Factorization f1 = SymmetricFilterAlgorithms.robustFactorizer().factorize(SF);
        SymmetricFilter.Factorization f2 = SymmetricFilterAlgorithms.evFactorizer().factorize(SF);
        SymmetricFilter.Factorization f3 = SymmetricFilterAlgorithms.fastFactorizer().factorize(SF);
        assertTrue(f1.factor.asPolynomial().equals(f2.factor.asPolynomial(), 1e-9));
        assertTrue(f1.factor.asPolynomial().equals(f3.factor.asPolynomial(), 1e-9));
        assertEquals(f1.scaling, f2.scaling, 1e-9);
        assertEquals(f1.scaling, f3.scaling, 1e-9);
    }
    
    @Test
    public void test1() {
        Polynomial R = Polynomial.of(1, -.6);
        SymmetricFilter SF=SymmetricFilter.convolutionOf(R, 1);
        SymmetricFilter.Factorization f1 = SymmetricFilterAlgorithms.robustFactorizer().factorize(SF);
        SymmetricFilter.Factorization f2 = SymmetricFilterAlgorithms.evFactorizer().factorize(SF);
        SymmetricFilter.Factorization f3 = SymmetricFilterAlgorithms.fastFactorizer().factorize(SF);
        assertTrue(f1.factor.asPolynomial().equals(f2.factor.asPolynomial(), 1e-9));
        assertTrue(f1.factor.asPolynomial().equals(f3.factor.asPolynomial(), 1e-9));
        assertEquals(f1.scaling, f2.scaling, 1e-9);
        assertEquals(f1.scaling, f3.scaling, 1e-9);
    }

   @Test
    public void testn() {
        Polynomial R = Polynomial.of(1, -.6);
        Polynomial S = Polynomial.of(1, 0, 0, 0, 0, 0, .5, 0, 0, 0, 0, 0, 0, 0, 0, -.3);
        Polynomial M=R.times(S);
        SymmetricFilter SF=SymmetricFilter.convolutionOf(M, 1);
        SymmetricFilter.Factorization f1 = SymmetricFilterAlgorithms.robustFactorizer().factorize(SF);
        SymmetricFilter.Factorization f2 = SymmetricFilterAlgorithms.evFactorizer().factorize(SF);
        SymmetricFilter.Factorization f3 = SymmetricFilterAlgorithms.fastFactorizer().factorize(SF);
        assertTrue(f1.factor.asPolynomial().equals(f2.factor.asPolynomial(), 1e-9));
        assertTrue(f1.factor.asPolynomial().equals(f3.factor.asPolynomial(), 1e-9));
        assertEquals(f1.scaling, f2.scaling, 1e-9);
        assertEquals(f1.scaling, f3.scaling, 1e-9);
    }
}
