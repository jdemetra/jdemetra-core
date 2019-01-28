/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths.matrices.decomposition;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class ElementaryTransformationsTest {
    
    public ElementaryTransformationsTest() {
    }

    @Test
    public void testJHypothenuse() {
        
        double small=1, big=1e15;
        double z = ElementaryTransformations.jhypotenuse(big+small, big);
        z*=z;
        assertEquals(2*big+1, z, 1);
    }
    
}
