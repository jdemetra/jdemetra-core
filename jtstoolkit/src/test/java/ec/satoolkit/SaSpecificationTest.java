/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.satoolkit;

import ec.tstoolkit.information.InformationSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jadoull
 */
public class SaSpecificationTest {
    
    public SaSpecificationTest() {
    }

    @Test
    public void testInformationSet() {
        SaSpecification expected = new SaSpecification();
        SaSpecification actual = new SaSpecification();
        InformationSet info;
        assertEquals(expected, actual);
        assertTrue(expected.isDefault());
        expected.setMethod(SaSpecification.Method.TramoSeats);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(SaSpecification.Method.TramoSeats, actual.getMethod());
        
        expected = new SaSpecification();
        actual = new SaSpecification();
        assertEquals(expected, actual);
        expected.setSpecification(SaSpecification.Spec.RSA2);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(SaSpecification.Spec.RSA2, actual.getSpecification());
    }
    
}
