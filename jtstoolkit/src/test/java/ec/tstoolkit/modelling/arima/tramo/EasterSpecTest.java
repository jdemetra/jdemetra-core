/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.information.InformationSet;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
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
        
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        
        expected.setDuration(5);
        info = expected.write(true);
        actual.read(info);
        assertEquals(5, actual.getDuration());
        
        expected.setTest(true);
        info = expected.write(true);
        actual.read(info);
        assertTrue(actual.isTest());
        
        expected.setOption(EasterSpec.Type.IncludeEaster);
        info = expected.write(true);
        actual.read(info);
        assertEquals(EasterSpec.Type.IncludeEaster, actual.getOption());
        assertTrue(actual.isUsed());
        assertFalse(actual.isDefault());
        
        expected.reset();
        assertTrue(expected.isDefault());
        info = expected.write(true);
        actual.read(info);
        assertTrue(actual.isDefault());        
    }

    @Test
    public void testDurationToUpperBound() {
        assertThatExceptionOfType(TramoException.class).isThrownBy(() -> new EasterSpec().setDuration(17));
    }

    @Test
    public void testDurationToLowerBound() {
        assertThatExceptionOfType(TramoException.class).isThrownBy(() -> new EasterSpec().setDuration(0));
    }
}
