/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x13.r;

import demetra.x11.X11Spec;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class X11BufferTest {
    
    public X11BufferTest() {
    }

    @Test
    public void testDefault() {
        X11Spec spec=X11Spec.DEFAULT;
        X11Buffer buffer=new X11Buffer(spec);
        X11Spec nspec=buffer.build();
        assertTrue(spec.equals(nspec));
    }
    
}
