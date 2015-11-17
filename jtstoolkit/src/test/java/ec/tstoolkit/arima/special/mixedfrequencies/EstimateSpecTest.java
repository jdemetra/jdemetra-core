/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.arima.special.mixedfrequencies;

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.PeriodSelectorType;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jadoull
 */
public class EstimateSpecTest {
    
    public EstimateSpecTest() {
    }

    @Test
    public void testInformationSet() {
        EstimateSpec expected = new EstimateSpec();
        EstimateSpec actual = new EstimateSpec();
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
        
        expected = new EstimateSpec();
        actual = new EstimateSpec();
        assertEquals(expected, actual);
        expected.setMethod(EstimateSpec.Method.Matrix);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(EstimateSpec.Method.Matrix, actual.getMethod());
        
        expected = new EstimateSpec();
        actual = new EstimateSpec();
        assertEquals(expected, actual);
        expected.setTol(0.0001);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(.0001, actual.getTol(), .0);
    }
    
}
