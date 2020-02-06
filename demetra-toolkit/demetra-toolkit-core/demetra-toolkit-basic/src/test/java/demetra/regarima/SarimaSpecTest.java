/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.regarima;

import demetra.arima.SarimaSpec;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class SarimaSpecTest {
    
    public SarimaSpecTest() {
    }

    @Test
    public void testAirline() {
        SarimaSpec spec = SarimaSpec.builder()
                .airline()
                .build();
        assertTrue(spec.isAirline());
    }
    
}
