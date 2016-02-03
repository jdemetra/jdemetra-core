/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.satoolkit.seats;

import ec.tstoolkit.information.InformationSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jadoull
 */
public class SeatsSpecificationTest {
    
    public SeatsSpecificationTest() {
    }

    @Test
    public void testInformationSet() {
        SeatsSpecification expected = new SeatsSpecification();
        SeatsSpecification actual = new SeatsSpecification();
        InformationSet info;
        
        assertTrue(expected.isDefault());
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        
        expected.setXlBoundary(.98);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(.98, actual.getXlBoundary(),.0);
        
        expected.setSeasTolerance(8.6);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(8.6, actual.getSeasTolerance(),.0);
        
        expected.setTrendBoundary(0.48);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(.48, actual.getTrendBoundary(),.0);
        
        expected.setSeasBoundary(.67);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(.67, actual.getSeasBoundary(),.0);
        
        expected.setSeasBoundary1(.67);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(.67, actual.getSeasBoundary1(),.0);

        expected.setApproximationMode(SeatsSpecification.ApproximationMode.Noisy);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(SeatsSpecification.ApproximationMode.Noisy, actual.getApproximationMode());
        
        expected.setMethod(SeatsSpecification.EstimationMethod.KalmanSmoother);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(SeatsSpecification.EstimationMethod.KalmanSmoother, actual.getMethod());
        
    }
    
    @Test(expected = SeatsException.class)
    public void testSetXlBoundaryUpperBound() {
        SeatsSpecification spec = new SeatsSpecification();
        spec.setXlBoundary(1.5);
    }

    @Test(expected = SeatsException.class)
    public void testSetXlBoundaryLowerBound() {
        SeatsSpecification spec = new SeatsSpecification();
        spec.setXlBoundary(.89);
    }
    
    @Test(expected = SeatsException.class)
    public void testSetSeasToleranceUpperBound() {
        SeatsSpecification spec = new SeatsSpecification();
        spec.setSeasTolerance(11.5);
    }

    @Test(expected = SeatsException.class)
    public void testSetSeasToleranceLowerBound() {
        SeatsSpecification spec = new SeatsSpecification();
        spec.setSeasTolerance(-0.5);
    }
    
    @Test(expected = SeatsException.class)
    public void testSetTrendBoundaryUpperBound() {
        SeatsSpecification spec = new SeatsSpecification();
        spec.setTrendBoundary(1.2);
    }

    @Test(expected = SeatsException.class)
    public void testSetTrendBoundaryLowerBound() {
        SeatsSpecification spec = new SeatsSpecification();
        spec.setTrendBoundary(-0.5);
    }
    
    @Test(expected = SeatsException.class)
    public void testSetSeasBoundaryUpperBound() {
        SeatsSpecification spec = new SeatsSpecification();
        spec.setSeasBoundary(1.2);
    }

    @Test(expected = SeatsException.class)
    public void testSetSeasBoundaryLowerBound() {
        SeatsSpecification spec = new SeatsSpecification();
        spec.setSeasBoundary(-0.5);
    }
}
