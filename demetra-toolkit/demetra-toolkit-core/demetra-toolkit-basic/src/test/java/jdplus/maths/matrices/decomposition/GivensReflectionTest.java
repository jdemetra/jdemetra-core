/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.matrices.decomposition;

import jdplus.data.DataBlock;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class GivensReflectionTest {
    
    public GivensReflectionTest() {
    }

    @Test
    public void testReflection() {
        double a=1, b=-3;
        DataBlock z=DataBlock.make(2);
        z.set(0, a);
        z.set(1, b);
        GivensReflection gr = GivensReflection.of(z, 0, 1, true);
        assertEquals(z.get(0), Math.sqrt(a*a+b*b), 1e-9);
        assertEquals(z.get(1), 0, 1e-9);
        gr.transform(z);
        assertEquals(z.get(0), a, 1e-9);
        assertEquals(z.get(1), b, 1e-9);
        a=-5;
        b=2;
        z.set(0, a);
        z.set(1, b);
        gr.transform(z);
        gr.transform(z);
        assertEquals(z.get(0), a, 1e-9);
        assertEquals(z.get(1), b, 1e-9);
    }
    
}
