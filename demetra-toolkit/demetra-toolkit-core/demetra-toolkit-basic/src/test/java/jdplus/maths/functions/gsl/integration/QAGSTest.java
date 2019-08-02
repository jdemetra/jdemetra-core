/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.functions.gsl.integration;

import jdplus.maths.polynomials.Polynomial;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class QAGSTest {

    public QAGSTest() {
    }

    @Test
    public void testPolynomial() {
        QAGS qags = QAGS.builder().build();
        qags.integrate(x -> x * x * x - 3 * x * x + 5 * x - 11, -2, 3);
        Polynomial p = Polynomial.of(0, -11, 2.5, -1, .25);
        double z = p.evaluateAt(3) - p.evaluateAt(-2);
        assertEquals(qags.getResult(), z, 1e-9);
    }
    
    @Test
    public void testPolynomial_QK15() {
        QAGS qags = QAGS.builder().integrationRule(QK15.rule()).build();
        qags.integrate(x -> x * x * x - 3 * x * x + 5 * x - 11, -2, 3);
        Polynomial p = Polynomial.of(0, -11, 2.5, -1, .25);
        double z = p.evaluateAt(3) - p.evaluateAt(-2);
        assertEquals(qags.getResult(), z, 1e-9);
    }
    
    @Test
    public void testPolynomial_QK51() {
        QAGS qags = QAGS.builder().integrationRule(QK51.rule()).build();
        qags.integrate(x -> x * x * x - 3 * x * x + 5 * x - 11, -2, 3);
        Polynomial p = Polynomial.of(0, -11, 2.5, -1, .25);
        double z = p.evaluateAt(3) - p.evaluateAt(-2);
        assertEquals(qags.getResult(), z, 1e-9);
    }
    
    @Test
    public void testPolynomial_QK61() {
        QAGS qags = QAGS.builder().integrationRule(QK61.rule()).build();
        qags.integrate(x -> x * x * x - 3 * x * x + 5 * x - 11, -2, 3);
        Polynomial p = Polynomial.of(0, -11, 2.5, -1, .25);
        double z = p.evaluateAt(3) - p.evaluateAt(-2);
        assertEquals(qags.getResult(), z, 1e-9);
    }

    @Test
    public void testLog() {
        QAGS qags = QAGS.builder().build();
        qags.integrate(x -> Math.log(x), 2, 3);
        double z = 3 * Math.log(3) - 2 * Math.log(2) - 1;
        assertEquals(qags.getResult(), z, 1e-9);
    }

    @Test
    public void testInv() {
        QAGS qags = QAGS.builder().build();
        qags.integrate(x -> 1 / x, 1e-6, 1);
        double z = -Math.log(1e-6);
        assertEquals(qags.getResult(), z, 1e-6);
    }

}
