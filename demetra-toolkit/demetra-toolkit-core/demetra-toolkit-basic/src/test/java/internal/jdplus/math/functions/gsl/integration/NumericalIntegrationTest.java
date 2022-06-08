/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package internal.jdplus.math.functions.gsl.integration;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

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
    public void testInvDemetra() {
        double q = jdplus.math.functions.NumericalIntegration.integrate(x -> x - 1 / x, 1e-10, 10);
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

    @Test
    public void testZero() {
        double q = NumericalIntegration.integrate(x -> 0, 0, 1);
        assertEquals(q, 0, 1e-9);

    }

    public static void main(String[] args) {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 100000; ++i) {
            double q = NumericalIntegration.integrate(x -> x - 1 / x, 1e-10, 10);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
         t0 = System.currentTimeMillis();
        for (int i = 0; i < 100000; ++i) {
            double q = jdplus.math.functions.NumericalIntegration.integrate(x -> x - 1 / x, 1e-10, 10);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
   }

}
