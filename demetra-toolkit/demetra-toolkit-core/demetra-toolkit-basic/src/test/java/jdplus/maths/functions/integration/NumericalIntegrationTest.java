/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.functions.integration;

import jdplus.maths.functions.gsl.integration.QAGS;
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
        double q=NumericalIntegration.integrate(x->1/x, 1e-10, 10);
        double z=Math.log(10)-Math.log(1e-10);
        assertEquals(q, z, 1e-9);
    }
    
}
