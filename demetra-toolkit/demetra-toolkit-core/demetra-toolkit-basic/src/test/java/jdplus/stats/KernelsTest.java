/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.stats;

import jdplus.maths.polynomials.Polynomial;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class KernelsTest {
    
    public KernelsTest() {
    }

    @Test
    public void testHenderson() {
        Polynomial p0=Kernels.phenderson(11);
        assertEquals(p0.integrate(-1,1), 1, 1e-9);
    }
    
}
