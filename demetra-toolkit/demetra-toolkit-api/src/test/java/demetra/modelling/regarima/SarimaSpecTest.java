/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.regarima;

import demetra.arima.SarimaSpec;
import demetra.data.ParameterSpec;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class SarimaSpecTest {
    
    public SarimaSpecTest() {
    }

    @Test
    public void testBuilder() {
        
        SarimaSpec spec = SarimaSpec.builder()
                .airline()
                .validator(null)
                .mu(ParameterSpec.undefined())
                .build();
        assertTrue(spec != null);
    }
    
}
