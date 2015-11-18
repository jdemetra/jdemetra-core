/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.modelling.arima.x13;

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jadoull
 */
public class BasicSpecTest {
    
    public BasicSpecTest() {
    }

    @Test
    public void testInformationSet() {
        BasicSpec expected = new BasicSpec();
        BasicSpec actual = new BasicSpec();
        InformationSet info;
        
        assertTrue(expected.isDefault());
        assertEquals(expected, actual);
        expected.setPreprocessing(false);
        TsPeriodSelector per = new TsPeriodSelector();
        per.from(Day.BEG);
        expected.setSpan(per);
        
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertFalse(actual.isPreprocessing());
        
        expected.reset();
        assertTrue(expected.isDefault());
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertTrue(actual.isDefault());
    }
    
}
