/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.arima.special.mixedfrequencies;

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.DataType;
import ec.tstoolkit.timeseries.PeriodSelectorType;
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
        assertEquals(expected, actual);
        assertTrue(expected.isDefault());
        TsPeriodSelector tsps1 = new TsPeriodSelector();
        tsps1.excluding(1, 5);
        expected.setSpan(tsps1);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(PeriodSelectorType.Excluding, actual.getSpan().getType());
        
        expected = new BasicSpec();
        actual = new BasicSpec();
        assertEquals(expected, actual);
        assertTrue(expected.isDefault());
        expected.setLog(true);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertTrue(actual.isLog());
        
        expected = new BasicSpec();
        actual = new BasicSpec();
        assertEquals(expected, actual);
        assertTrue(expected.isDefault());
        expected.setDataType(DataType.Stock);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(DataType.Stock, actual.getDataType());
        
        expected.reset();
        assertNotEquals(expected, actual);
        assertTrue(expected.isDefault());
    }
    
}
