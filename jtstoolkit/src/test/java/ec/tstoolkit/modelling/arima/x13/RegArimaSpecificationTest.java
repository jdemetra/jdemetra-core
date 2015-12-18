/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.modelling.arima.x13;

import ec.tstoolkit.ParameterType;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jadoull
 */
public class RegArimaSpecificationTest {
    
    public RegArimaSpecificationTest() {
    }

    @Test
    public void testInformationSet() {
        RegArimaSpecification expected = new RegArimaSpecification();
        RegArimaSpecification actual = new RegArimaSpecification();
        InformationSet info;
        assertEquals(expected, actual);
        
        OutlierSpec oSpec = new OutlierSpec();
        oSpec.setDefaultCriticalValue(1.2);
        expected.setOutliers(oSpec);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(1.2, actual.getOutliers().getDefaultCriticalValue(),.0);
        
        AutoModelSpec amSpec = new AutoModelSpec();
        amSpec.setPercentRSE(1.2);
        expected.setAutoModel(amSpec);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(1.2, actual.getAutoModel().getPercentRSE(),.0);
        
        ArimaSpec aSpec = new ArimaSpec();
        aSpec.setBD(1);
        expected.setArima(aSpec);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(1, actual.getArima().getBD());
        
        TransformSpec tSpec = new TransformSpec();
        tSpec.setAICDiff(1.2);
        expected.setTransform(tSpec);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(1.2, actual.getTransform().getAICDiff(),.0);
        
        RegressionSpec rSpec = new RegressionSpec();
        rSpec.setAICCDiff(1.2);
        TradingDaysSpec tdSpec= new TradingDaysSpec();
        tdSpec.setTradingDaysType(TradingDaysType.WorkingDays);
        rSpec.setTradingDays(tdSpec);
        expected.setRegression(rSpec);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(1.2, actual.getRegression().getAICCDiff(),.0);
        
        EstimateSpec eSpec = new EstimateSpec();
        eSpec.setTol(1.2);
        expected.setEstimate(eSpec);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(1.2, actual.getEstimate().getTol(),.0);
        
        assertFalse(actual.isUsingAutoModel());
        expected.setUsingAutoModel(true);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertTrue(actual.isUsingAutoModel());
        
    }
    
    @Test (expected = java.lang.IllegalArgumentException.class)
    public void testSetOutliers(){
        RegArimaSpecification expected = new RegArimaSpecification();
        expected.setOutliers(null);
    }
    
    @Test (expected = java.lang.IllegalArgumentException.class)
    public void testSetAutoModel(){
        RegArimaSpecification expected = new RegArimaSpecification();
        expected.setAutoModel(null);
    }
    
    @Test (expected = java.lang.IllegalArgumentException.class)
    public void testSetArima(){
        RegArimaSpecification expected = new RegArimaSpecification();
        expected.setArima(null);
    }
    
    @Test (expected = java.lang.IllegalArgumentException.class)
    public void testSetTransform(){
        RegArimaSpecification expected = new RegArimaSpecification();
        expected.setTransform(null);
    }
    
    @Test (expected = java.lang.IllegalArgumentException.class)
    public void testSetRegression(){
        RegArimaSpecification expected = new RegArimaSpecification();
        expected.setRegression(null);
    }
    
    @Test (expected = java.lang.IllegalArgumentException.class)
    public void testSetEstimate(){
        RegArimaSpecification expected = new RegArimaSpecification();
        expected.setEstimate(null);
    }
}
