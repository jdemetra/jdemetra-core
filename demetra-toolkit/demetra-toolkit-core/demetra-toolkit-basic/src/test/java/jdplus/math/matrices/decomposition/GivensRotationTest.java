/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.decomposition;

import jdplus.data.DataBlock;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate
 */
public class GivensRotationTest {
    
    public GivensRotationTest() {
    }

    @Test
    public void testRotation() {
        double a=1, b=-3;
        DataBlock z=DataBlock.make(2);
        z.set(0, a);
        z.set(1, b);
        GivensRotation gr = GivensRotation.of(z, 0, 1, true);
        assertEquals(z.get(0), Math.sqrt(a*a+b*b), 1e-9);
        assertEquals(z.get(1), 0, 1e-9);
        gr.transform(z.reverse());
        assertEquals(z.get(0), a, 1e-9);
        assertEquals(z.get(1), b, 1e-9);
        a=-5;
        b=2;
        z.set(0, a);
        z.set(1, b);
        gr.transform(z);
        gr.reverse().transform(z);
        assertEquals(z.get(0), a, 1e-9);
        assertEquals(z.get(1), b, 1e-9);
        
    }
    
}
