/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.msts;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class VarianceParameterTest {
    
    public VarianceParameterTest() {
    }

    @Test
    public void testStd() {
        
        VarianceInterpreter var=new VarianceInterpreter("v", 3, false, true);
        double stde = var.stde();
        var.variance(stde*stde);
        assertEquals(var.variance(), 3, 1e-9);
    }
    
}
