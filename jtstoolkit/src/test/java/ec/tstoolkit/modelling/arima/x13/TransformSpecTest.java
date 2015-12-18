/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.modelling.arima.x13;

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jadoull
 */
public class TransformSpecTest {
    
    public TransformSpecTest() {
    }
    
    @Test
    public void testInformationSet() {
        TransformSpec expected = new TransformSpec();
        TransformSpec actual = new TransformSpec();
        InformationSet info;
        
        assertTrue(expected.isDefault());
        assertEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertTrue(actual.isDefault());
        
        expected.setAICDiff(1.2);
        assertFalse(expected.isDefault());
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(1.2, actual.getAICDiff(), .0);
        
        expected.setFunction(DefaultTransformationType.Log);
        expected.setAdjust(LengthOfPeriodType.LengthOfPeriod);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(DefaultTransformationType.Log, actual.getFunction());
        assertEquals(LengthOfPeriodType.LengthOfPeriod, actual.getAdjust());
        
        expected.setConst(1.5);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(1.5, actual.getConst(), .0);
        
        expected.reset();
        assertTrue(expected.isDefault());
        assertFalse(actual.isDefault());
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertTrue(actual.isDefault());
    }
}
