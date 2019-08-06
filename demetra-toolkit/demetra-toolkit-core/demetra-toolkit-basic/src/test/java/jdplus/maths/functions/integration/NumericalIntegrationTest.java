/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.functions.integration;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class NumericalIntegrationTest {

    public NumericalIntegrationTest() {
    }

    @Test
    public void testInv() {
        double q = NumericalIntegration.integrate(x -> x - 1 / x, 1e-10, 10);
        double z = -10 * 5 + 1e-10 * 1e-10 / 2 + Math.log(10) - Math.log(1e-10);
        assertEquals(q, -z, 1e-9);
    }

    @Test
    public void testInvQAGI() {
        double q = NumericalIntegration.integrateQAGI(x -> 1 / x);
        assertEquals(q, 0.0, 1e-9);
    }

    @Test
    public void testQAGI2() {
        double q = NumericalIntegration.integrateQAGI(x -> Math.exp(-.5 * x * x));
        assertEquals(q, Math.sqrt(2 * Math.PI), 1e-9);
    }

    @Test
    public void testInvQAGIU() {
        double q = NumericalIntegration.integrateQAGIU(x -> 1 / x, Double.POSITIVE_INFINITY);
        assertEquals(q, 0.0, 1e-9);
    }

    @Test
    public void testQAGIU1() {
        double q = NumericalIntegration.integrateQAGIU(x -> x == 0 ? 0 : Math.exp(-.5 * Math.pow(Math.log(x), 2)) / x, 0);
        assertEquals(q, Math.sqrt(2 * Math.PI), 1e-9);
    }

}
