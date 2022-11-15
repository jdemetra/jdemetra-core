/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.satoolkit.seats;

import ec.tstoolkit.information.InformationSet;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
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
        boolean verbose=false;
        SeatsSpecification expected = new SeatsSpecification();
        SeatsSpecification actual = new SeatsSpecification();
        InformationSet info;
        
        assertTrue(expected.isDefault());
        info = expected.write(verbose);
        actual.read(info);
        assertEquals(expected, actual);
        
        expected.setXlBoundary(.98);
        info = expected.write(verbose);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(.98, actual.getXlBoundary(),.0);
        
        expected.setSeasTolerance(8.6);
        info = expected.write(verbose);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(8.6, actual.getSeasTolerance(),.0);
        
        expected.setTrendBoundary(0.48);
        info = expected.write(verbose);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(.48, actual.getTrendBoundary(),.0);
        
        expected.setSeasBoundary(.67);
        info = expected.write(verbose);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(.67, actual.getSeasBoundary(),.0);
        
        expected.setSeasBoundary1(.67);
        info = expected.write(verbose);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(.67, actual.getSeasBoundary1(),.0);

        expected.setApproximationMode(SeatsSpecification.ApproximationMode.Noisy);
        info = expected.write(verbose);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(SeatsSpecification.ApproximationMode.Noisy, actual.getApproximationMode());
        
        expected.setMethod(SeatsSpecification.EstimationMethod.KalmanSmoother);
        info = expected.write(verbose);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(SeatsSpecification.EstimationMethod.KalmanSmoother, actual.getMethod());
        
    }
    
    @Test
    public void testSetXlBoundaryUpperBound() {
        assertThatExceptionOfType(SeatsException.class).isThrownBy(() -> new SeatsSpecification().setXlBoundary(1.5));
    }

    @Test
    public void testSetXlBoundaryLowerBound() {
        assertThatExceptionOfType(SeatsException.class).isThrownBy(() -> new SeatsSpecification().setXlBoundary(.89));
    }

    @Test
    public void testSetSeasToleranceUpperBound() {
        assertThatExceptionOfType(SeatsException.class).isThrownBy(() -> new SeatsSpecification().setSeasTolerance(11.5));
    }

    @Test
    public void testSetSeasToleranceLowerBound() {
        assertThatExceptionOfType(SeatsException.class).isThrownBy(() -> new SeatsSpecification().setSeasTolerance(-0.5));
    }

    @Test
    public void testSetTrendBoundaryUpperBound() {
        assertThatExceptionOfType(SeatsException.class).isThrownBy(() -> new SeatsSpecification().setTrendBoundary(1.2));
    }

    @Test
    public void testSetTrendBoundaryLowerBound() {
        assertThatExceptionOfType(SeatsException.class).isThrownBy(() -> new SeatsSpecification().setTrendBoundary(-0.5));
    }

    @Test
    public void testSetSeasBoundaryUpperBound() {
        assertThatExceptionOfType(SeatsException.class).isThrownBy(() -> new SeatsSpecification().setSeasBoundary(1.2));
    }

    @Test
    public void testSetSeasBoundaryLowerBound() {
        assertThatExceptionOfType(SeatsException.class).isThrownBy(() -> new SeatsSpecification().setSeasBoundary(-0.5));
    }
}
