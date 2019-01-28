/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramo;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class EasterSpecTest {
    
    public EasterSpecTest() {
    }

    @Test
    public void testClone() {
        EasterSpec spec=new EasterSpec();
        EasterSpec cspec = spec.clone();
        assertTrue(spec.equals(cspec));
        assertTrue(cspec.isDefault());
        spec.setOption(EasterSpec.Type.IncludeEaster);
        cspec = spec.clone();
        assertTrue(spec.equals(cspec));
        assertFalse(cspec.isDefault());
   }
    
}
