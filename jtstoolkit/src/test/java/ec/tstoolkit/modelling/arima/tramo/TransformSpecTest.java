/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.DefaultTransformationType;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jadoull
 */
public class TransformSpecTest {

    public TransformSpecTest() {
    }

    @Test
    public void testInformation() {
        TransformSpec expected = new TransformSpec();
        TransformSpec actual = new TransformSpec();
        InformationSet info;
        assertEquals(expected, actual);
        
        expected.setFunction(DefaultTransformationType.Log);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(DefaultTransformationType.Log, actual.getFunction());

        expected.setFunction(DefaultTransformationType.Auto);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(DefaultTransformationType.Auto, actual.getFunction());

        expected.setFunction(DefaultTransformationType.Log);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(DefaultTransformationType.Log, actual.getFunction());

        expected.setFct(1.0);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(1.0, actual.getFct(), 0.0);
        
        expected.setPreliminaryCheck(false);
        assertFalse(expected.isPreliminaryCheck());
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertFalse(actual.isPreliminaryCheck());

        expected.reset();
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        

        assertEquals(true, expected.isDefault());
    }

}
