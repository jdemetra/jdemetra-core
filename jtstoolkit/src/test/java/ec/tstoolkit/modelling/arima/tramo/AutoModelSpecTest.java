/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.information.InformationSet;
import org.junit.Test;
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
        AutoModelSpec expected = new AutoModelSpec();
        AutoModelSpec actual = new AutoModelSpec(true);
        InformationSet info;
        
        assertTrue(expected.isDefault());
        assertFalse(actual.isDefault());
        assertNotEquals(expected, actual);
        
        expected.setPcr(.95);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(.95, actual.getPcr(),0.0);
        
        expected.setUb1(.9);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(.9, actual.getUb1(),0.0);
        
        expected.setUb2(.9);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(.9, actual.getUb2(),0.0);
        
        expected.setCancel(.06);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(.06, actual.getCancel(),0.0);
        
        expected.setTsig(.6);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(.6, actual.getTsig(),0.0);
        
        expected.setPc(.25);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(.25, actual.getPc(),0.0);
        
        expected.setEnabled(true);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertTrue(actual.isEnabled());
        
        expected.setAmiCompare(true);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertTrue(actual.isAmiCompare());
        
        expected.setAcceptDefault(true);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertTrue(actual.isAcceptDefault());
        
        AutoModelSpec clone;
        clone = expected.clone();
        clone.setPc(.28);
        info = clone.write(true);
        actual.read(info);
        assertNotEquals(expected, clone);
        assertEquals(clone, actual);
        assertNotEquals(expected, actual);
        assertEquals(.28, actual.getPc(),.0);
        
        expected.reset();
        assertTrue(expected.isDefault());
        info = expected.write(true);
        actual.read(info);
        assertTrue(actual.isDefault());
    }
    
    @Test(expected = TramoException.class)
    public void testSetPcrLowerBound() {
        AutoModelSpec spec = new AutoModelSpec();
        spec.setPcr(.79);
    }
    
    @Test(expected = TramoException.class)
    public void testSetPcrUpperBound() {
        AutoModelSpec spec = new AutoModelSpec();
        spec.setPcr(1.1);
    }
    
    @Test(expected = TramoException.class)
    public void testSetUb1LowerBound() {
        AutoModelSpec spec = new AutoModelSpec();
        spec.setPcr(.79);
    }
    
    @Test(expected = TramoException.class)
    public void testSetUb1UpperBound() {
        AutoModelSpec spec = new AutoModelSpec();
        spec.setPcr(1.1);
    }
    
    @Test(expected = TramoException.class)
    public void testSetUb2LowerBound() {
        AutoModelSpec spec = new AutoModelSpec();
        spec.setPcr(.79);
    }
    
    @Test(expected = TramoException.class)
    public void testSetUb2UpperBound() {
        AutoModelSpec spec = new AutoModelSpec();
        spec.setPcr(1.1);
    }
    
    @Test(expected = TramoException.class)
    public void testSetCancelLowerBound() {
        AutoModelSpec spec = new AutoModelSpec();
        spec.setPcr(-1);
    }
    
    @Test(expected = TramoException.class)
    public void testSetCancelUpperBound() {
        AutoModelSpec spec = new AutoModelSpec();
        spec.setPcr(0.5);
    }
    
    @Test(expected = TramoException.class)
    public void testSetTsigLowerBound() {
        AutoModelSpec spec = new AutoModelSpec();
        spec.setPcr(.5);
    }
    
    @Test(expected = TramoException.class)
    public void testSetPcLowerBound() {
        AutoModelSpec spec = new AutoModelSpec();
        spec.setPc(0.01);
    }
    
    @Test(expected = TramoException.class)
    public void testSetPcUpperBound() {
        AutoModelSpec spec = new AutoModelSpec();
        spec.setPcr(0.31);
    }
}
