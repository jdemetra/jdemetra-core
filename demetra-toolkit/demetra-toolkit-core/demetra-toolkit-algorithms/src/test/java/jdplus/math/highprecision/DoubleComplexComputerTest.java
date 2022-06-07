/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.highprecision;

import demetra.math.Complex;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate
 */
public class DoubleComplexComputerTest {

    public DoubleComplexComputerTest() {
    }

    @Test
    public void testDiv() {
        Complex a = Complex.cart(2.3, 4.5), b = Complex.cart(4, -2);
        DoubleComplex da = DoubleComplex.of(a), db = DoubleComplex.of(b);
        Complex ab = a.div(b);
        DoubleComplex dab = da.dividedBy(db);
        assertEquals(ab.getRe(), dab.getRe().asDouble(), 1e-9);
        assertEquals(ab.getIm(), dab.getIm().asDouble(), 1e-9);
        ab = a.times(b);
        dab = da.times(db);
        assertEquals(ab.getRe(), dab.getRe().asDouble(), 1e-9);
        assertEquals(ab.getIm(), dab.getIm().asDouble(), 1e-9);
        ab = a.plus(b);
        dab = da.plus(db);
        assertEquals(ab.getRe(), dab.getRe().asDouble(), 1e-9);
        assertEquals(ab.getIm(), dab.getIm().asDouble(), 1e-9);
        ab = a.minus(b);
        dab = da.minus(db);
        assertEquals(ab.getRe(), dab.getRe().asDouble(), 1e-9);
        assertEquals(ab.getIm(), dab.getIm().asDouble(), 1e-9);
    }

}
