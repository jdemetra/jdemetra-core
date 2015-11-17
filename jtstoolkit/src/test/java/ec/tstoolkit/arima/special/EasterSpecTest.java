/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.arima.special;

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.TsException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jadoull
 */
public class EasterSpecTest {
    
    public EasterSpecTest() {
    }

    @Test
    public void testInformationSet() {
        EasterSpec expected = new EasterSpec();
        EasterSpec actual = new EasterSpec();
        InformationSet info;
        assertEquals(expected, actual);
        assertTrue(expected.isDefault());
        
        expected.setDuration(1);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(1, actual.getDuration());
        
        expected.setOption(ec.tstoolkit.modelling.arima.tramo.EasterSpec.Type.Standard);
        assertNotEquals(expected, actual);
        assertFalse(expected.isDefault());
        assertTrue(expected.isUsed());
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(ec.tstoolkit.modelling.arima.tramo.EasterSpec.Type.Standard, actual.getOption());
        assertFalse(actual.isDefault());
        assertTrue(actual.isUsed());
        
        expected.reset();
        assertTrue(expected.isDefault());
        assertEquals(6, expected.getDuration());
        assertEquals(ec.tstoolkit.modelling.arima.tramo.EasterSpec.Type.Unused, expected.getOption());
        info = expected.write(true);
        actual.read(info);
        assertTrue(actual.isDefault());
        assertEquals(6, actual.getDuration());
        assertEquals(ec.tstoolkit.modelling.arima.tramo.EasterSpec.Type.Unused, actual.getOption());
        
    }
    
    @Test (expected = TsException.class)
    public void testSetDurationLowerBound() {
        EasterSpec spec = new EasterSpec();
        spec.setDuration(0);
    }
    
    @Test (expected = TsException.class)
    public void testSetDurationUpperBound() {
        EasterSpec spec = new EasterSpec();
        spec.setDuration(16);
    }
}
