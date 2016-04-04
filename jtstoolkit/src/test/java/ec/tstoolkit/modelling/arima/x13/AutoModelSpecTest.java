/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.modelling.arima.x13;

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
        AutoModelSpec actual = new AutoModelSpec();
        InformationSet info;
        
        assertEquals(expected, actual);
        assertTrue(expected.isDefault());
        
        expected.setAcceptDefault(true);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertTrue(actual.isAcceptDefault());
                
        expected.setCheckMu(false);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertFalse(actual.isCheckMu());
        
        OrderSpec orderSpec = new OrderSpec(5, 1, OrderSpec.Type.Fixed);
        expected.setArma(orderSpec);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(orderSpec, actual.getArma());
        orderSpec = new OrderSpec(3, 1, OrderSpec.Type.Max);
        expected.setDiff(orderSpec);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(orderSpec, actual.getDiff());
        
        expected.setMixed(false);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertFalse(actual.isMixed());
        
        expected.setLjungBoxLimit(1.45);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(1.45, actual.getLjungBoxLimit(),.0);
        
        expected.setArmaSignificance(0.55);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(0.55, actual.getArmaSignificance(),.0);
        
        expected.setPercentRSE(1.2);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(1.2, actual.getPercentRSE(),.0);
        
        expected.setBalanced(false);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertFalse(actual.isBalanced());
        
        expected.setHannanRissanen(true);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertTrue(actual.isHannanRissannen());
        
        expected.setPercentReductionCV(.25);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(.25, actual.getPercentReductionCV(),.0);

        expected.setInitialUnitRootLimit(1.5);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(1.5, actual.getInitialUnitRootLimit(),.0);
        
        expected.setFinalUnitRootLimit(0.5);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(0.5, actual.getFinalUnitRootLimit(),.0);
        
        expected.setCancelationLimit(0.01);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(0.01, actual.getCancelationLimit(),.0);
        
        expected.setUnitRootLimit(1.4);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(1.4, actual.getUnitRootLimit(),.0);
        
        expected.setEnabled(true);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertTrue(actual.isEnabled());
        
        expected.reset();
        assertTrue(expected.isDefault());
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertTrue(actual.isDefault());
    }
    
    @Test(expected = X13Exception.class)
    public void testSetArmaSignificanceLowerBound() {
        AutoModelSpec expected = new AutoModelSpec();
        expected.setArmaSignificance(.45);
    }
    
    @Test(expected = X13Exception.class)
    public void testSetPercentRSELowerBound() {
        AutoModelSpec expected = new AutoModelSpec();
        expected.setPercentRSE(.1);
    }
    
    @Test(expected = X13Exception.class)
    public void testSetPercentReductionCVLowerBound() {
        AutoModelSpec expected = new AutoModelSpec();
        expected.setPercentReductionCV(.01);
    }
    
    @Test(expected = X13Exception.class)
    public void testSetPercentReductionCVUpperBound() {
        AutoModelSpec expected = new AutoModelSpec();
        expected.setPercentReductionCV(.4);
    }
    
    @Test(expected = X13Exception.class)
    public void testSetInitialUnitRootLimitLowerBound() {
        AutoModelSpec expected = new AutoModelSpec();
        expected.setInitialUnitRootLimit(1);
    }
    
    @Test(expected = X13Exception.class)
    public void testSetFinalUnitRootLimitUpperBound() {
        AutoModelSpec expected = new AutoModelSpec();
        expected.setFinalUnitRootLimit(1);
    }
    
    @Test(expected = X13Exception.class)
    public void testSetCancelationLimitLowerBound() {
        AutoModelSpec expected = new AutoModelSpec();
        expected.setCancelationLimit(-1);
    }
    
    @Test(expected = X13Exception.class)
    public void testSetCancelationLimitUpperBound() {
        AutoModelSpec expected = new AutoModelSpec();
        expected.setCancelationLimit(.4);
    }
    
    @Test(expected = X13Exception.class)
    public void testSetUnitRootLimitLowerBound() {
        AutoModelSpec expected = new AutoModelSpec();
        expected.setUnitRootLimit(.9);
    }
}
