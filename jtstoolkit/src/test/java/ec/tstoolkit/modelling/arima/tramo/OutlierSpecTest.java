/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.PeriodSelectorType;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.regression.OutlierType;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jadoull
 */
public class OutlierSpecTest {
    
    public OutlierSpecTest() {
    }

    @Test
    public void testInformationSet() {
        OutlierSpec expected = new OutlierSpec();
        OutlierSpec actual = new OutlierSpec();
        InformationSet info;
        
        assertTrue(expected.isDefault());
        info = expected.write(true);
        actual.read(info);
        assertTrue(actual.isDefault());
        assertFalse(actual.isUsed());
        assertEquals(expected, actual);
        
        expected.reset();
        expected.setEML(true);
        info = expected.write(true);
        actual.read(info);
        assertTrue(actual.isEML());
        
        expected.reset();
        expected.setCriticalValue(2.5);
        info = expected.write(true);
        actual.read(info);
        assertEquals(2.5, actual.getCriticalValue(),0.0);
        
        expected.reset();
        expected.setDeltaTC(.5);
        info = expected.write(true);
        actual.read(info);
        assertEquals(.5, actual.getDeltaTC(),0.0);
        
        TsPeriodSelector span = new TsPeriodSelector();
        span.from(Day.BEG);
        expected.reset();
        expected.setSpan(span);
        info = expected.write(true);
        actual.read(info);
        assertEquals(PeriodSelectorType.From, actual.getSpan().getType());
        
        OutlierSpec clone = new OutlierSpec();
        clone = expected.clone();
        info = clone.write(true);
        actual.read(info);
        assertEquals(expected, clone);
        assertEquals(clone, actual);
        assertEquals(expected, actual);
        
        OutlierType[] types = new OutlierType[]{OutlierType.AO,OutlierType.IO};
        assertNull(actual.getTypes());
        expected.setTypes(types);
        assertTrue(expected.isUsed());
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected,actual);
        assertTrue(actual.isUsed());
        assertEquals(2, actual.getTypes().length);
        assertEquals(OutlierType.AO, actual.getTypes()[0]);
        assertEquals(OutlierType.IO, actual.getTypes()[1]);
        assertTrue(actual.contains(OutlierType.AO));
        expected.clearTypes();
        assertNull(expected.getTypes());
    }
    
    @Test(expected = TramoException.class)
    public void testSetCriticalValueLowerBound() {
        OutlierSpec spec = new OutlierSpec();
        spec.setCriticalValue(1.9);
    }
    
    @Test(expected = TramoException.class)
    public void testSetDeltaTCLowerBound() {
        OutlierSpec spec = new OutlierSpec();
        spec.setCriticalValue(.2);
    }
    
    @Test(expected = TramoException.class)
    public void testSetDeltaUpperBound() {
        OutlierSpec spec = new OutlierSpec();
        spec.setCriticalValue(1.0);
    }
}
