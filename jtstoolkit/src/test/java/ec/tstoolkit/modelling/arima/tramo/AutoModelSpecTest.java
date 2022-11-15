/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.information.InformationSet;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.*;

/**
 *
 * @author jadoull
 */
public class AutoModelSpecTest {
    
    public AutoModelSpecTest() {
    }

    @Test
    public void testInformationSet() {
        boolean verbose=false;
        AutoModelSpec expected = new AutoModelSpec();
        AutoModelSpec actual = new AutoModelSpec(true);
        InformationSet info;
        
        assertTrue(expected.isDefault());
        assertFalse(actual.isDefault());
        assertNotEquals(expected, actual);
        
        expected.setPcr(.95);
        info = expected.write(verbose);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(.95, actual.getPcr(),0.0);
        
        expected.setUb1(.9);
        info = expected.write(verbose);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(.9, actual.getUb1(),0.0);
        
        expected.setUb2(.9);
        info = expected.write(verbose);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(.9, actual.getUb2(),0.0);
        
        expected.setCancel(.06);
        info = expected.write(verbose);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(.06, actual.getCancel(),0.0);
        
        expected.setTsig(.6);
        info = expected.write(verbose);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(.6, actual.getTsig(),0.0);
        
        expected.setPc(.25);
        info = expected.write(verbose);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(.25, actual.getPc(),0.0);
        
        expected.setEnabled(true);
        info = expected.write(verbose);
        actual.read(info);
        assertEquals(expected, actual);
        assertTrue(actual.isEnabled());
        
        expected.setAmiCompare(true);
        info = expected.write(verbose);
        actual.read(info);
        assertEquals(expected, actual);
        assertTrue(actual.isAmiCompare());
        
        expected.setAcceptDefault(true);
        info = expected.write(verbose);
        actual.read(info);
        assertEquals(expected, actual);
        assertTrue(actual.isAcceptDefault());
        
        AutoModelSpec clone;
        clone = expected.clone();
        clone.setPc(.28);
        info = clone.write(verbose);
        actual.read(info);
        assertNotEquals(expected, clone);
        assertEquals(clone, actual);
        assertNotEquals(expected, actual);
        assertEquals(.28, actual.getPc(),.0);
        
        expected.reset();
        assertTrue(expected.isDefault());
        info = expected.write(verbose);
        actual.read(info);
        assertTrue(actual.isDefault());
    }

    @Test
    public void testSetPcrLowerBound() {
        assertThatExceptionOfType(TramoException.class).isThrownBy(() -> new AutoModelSpec().setPcr(.79));
    }

    @Test
    public void testSetPcrUpperBound() {
        assertThatExceptionOfType(TramoException.class).isThrownBy(() -> new AutoModelSpec().setPcr(1.1));
    }

    @Test
    public void testSetUb1LowerBound() {
        assertThatExceptionOfType(TramoException.class).isThrownBy(() -> new AutoModelSpec().setPcr(.79));
    }

    @Test
    public void testSetUb1UpperBound() {
        assertThatExceptionOfType(TramoException.class).isThrownBy(() -> new AutoModelSpec().setPcr(1.1));
    }

    @Test
    public void testSetUb2LowerBound() {
        assertThatExceptionOfType(TramoException.class).isThrownBy(() -> new AutoModelSpec().setPcr(.79));
    }

    @Test
    public void testSetUb2UpperBound() {
        assertThatExceptionOfType(TramoException.class).isThrownBy(() -> new AutoModelSpec().setPcr(1.1));
    }

    @Test
    public void testSetCancelLowerBound() {
        assertThatExceptionOfType(TramoException.class).isThrownBy(() -> new AutoModelSpec().setPcr(-1));
    }

    @Test
    public void testSetCancelUpperBound() {
        assertThatExceptionOfType(TramoException.class).isThrownBy(() -> new AutoModelSpec().setPcr(0.5));
    }

    @Test
    public void testSetTsigLowerBound() {
        assertThatExceptionOfType(TramoException.class).isThrownBy(() -> new AutoModelSpec().setPcr(.5));
    }

    @Test
    public void testSetPcLowerBound() {
        assertThatExceptionOfType(TramoException.class).isThrownBy(() -> new AutoModelSpec().setPc(0.01));
    }

    @Test
    public void testSetPcUpperBound() {
        assertThatExceptionOfType(TramoException.class).isThrownBy(() -> new AutoModelSpec().setPcr(0.31));
    }
}
