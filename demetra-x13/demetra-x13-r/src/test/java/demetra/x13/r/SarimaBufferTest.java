/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x13.r;

import demetra.arima.SarimaSpec;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class SarimaBufferTest {
    
    public SarimaBufferTest() {
    }

     @Test
    public void testDefault() {
        SarimaSpec spec=SarimaSpec.airline();
        SarimaBuffer buffer=new SarimaBuffer(spec);
        SarimaSpec nspec=buffer.build();
        assertTrue(spec.equals(nspec));
    }
    
}
