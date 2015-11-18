/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.satoolkit.special;

import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.Method;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jadoull
 */
public class PreprocessingSpecificationTest {
    
    public PreprocessingSpecificationTest() {
    }

    @Test
    public void testInformationSet() {
        PreprocessingSpecification expected = new PreprocessingSpecification();
        PreprocessingSpecification actual = new PreprocessingSpecification();
        InformationSet info;
        assertEquals(expected, actual);
        expected.method = Method.Regarima;
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(Method.Regarima, actual.method);
        
        expected = new PreprocessingSpecification();
        actual = new PreprocessingSpecification();
        assertEquals(expected, actual);
        expected.transform = DefaultTransformationType.Log;
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(DefaultTransformationType.Log, actual.transform);
        
        expected = new PreprocessingSpecification();
        actual = new PreprocessingSpecification();
        assertEquals(expected, actual);
        expected.dtype = TradingDaysType.TradingDays;
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(TradingDaysType.TradingDays, actual.dtype);
        
        expected = new PreprocessingSpecification();
        actual = new PreprocessingSpecification();
        assertEquals(expected, actual);
        expected.ltype = LengthOfPeriodType.LengthOfPeriod;
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(LengthOfPeriodType.LengthOfPeriod, actual.ltype);
        
        expected = new PreprocessingSpecification();
        actual = new PreprocessingSpecification();
        assertEquals(expected, actual);
        expected.easter = false;
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertFalse(actual.easter);
        
        expected = new PreprocessingSpecification();
        actual = new PreprocessingSpecification();
        assertEquals(expected, actual);
        expected.pretest = false;
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertFalse(actual.pretest);
        
        expected = new PreprocessingSpecification();
        actual = new PreprocessingSpecification();
        assertEquals(expected, actual);
        expected.ao = false;
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertFalse(actual.ao);
        
        expected = new PreprocessingSpecification();
        actual = new PreprocessingSpecification();
        assertEquals(expected, actual);
        expected.ls = false;
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertFalse(actual.ls);
        
        expected = new PreprocessingSpecification();
        actual = new PreprocessingSpecification();
        assertEquals(expected, actual);
        expected.tc = false;
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertFalse(actual.tc);
        
        expected = new PreprocessingSpecification();
        actual = new PreprocessingSpecification();
        assertEquals(expected, actual);
        expected.so = true;
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertTrue(actual.so);
        
    }
    
}
