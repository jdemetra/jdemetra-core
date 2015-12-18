/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.modelling.arima;

import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.information.InformationSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author JADOULL
 */
public class DefaultArimaSpecTest {

    public DefaultArimaSpecTest() {
    }

    @Test
    public void testInformationSet() {
        DefaultArimaSpec expected = new DefaultArimaSpec();
        DefaultArimaSpec actual = new DefaultArimaSpec();
        InformationSet info;

        assertEquals(expected, actual);
        expected.airline();
        assertTrue(expected.isDefault());
        assertTrue(expected.isAirline());
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertTrue(actual.isDefault());
        assertTrue(actual.isAirline());

        DefaultArimaSpec clone = expected.clone();
        assertEquals(expected, clone);
        clone.airlineWithMean();
        assertNotEquals(expected, clone);
        info = clone.write(true);
        actual.read(info);
        assertEquals(clone, actual);
        assertNotEquals(expected, actual);
        assertTrue(actual.isMean());

        expected.setP(10);
        info = expected.write(true);
        actual.read(info);
        assertEquals(10, actual.getP());

        expected.setD(20);
        info = expected.write(true);
        actual.read(info);
        assertEquals(20, actual.getD());

        expected.setQ(20);
        info = expected.write(true);
        actual.read(info);
        assertEquals(20, actual.getQ());

        expected.setBP(10);
        info = expected.write(true);
        actual.read(info);
        assertEquals(10, actual.getBP());

        expected.setBD(20);
        info = expected.write(true);
        actual.read(info);
        assertEquals(20, actual.getBD());

        expected.setBQ(20);
        info = expected.write(true);
        actual.read(info);
        assertEquals(20, actual.getBQ());

        assertFalse(expected.hasParameters());
        Parameter param1 = new Parameter(1.2, ParameterType.Fixed);
        Parameter param2 = new Parameter(0.2, ParameterType.Estimated);
        Parameter param3 = new Parameter(0.5, ParameterType.Undefined);
        Parameter[] params = new Parameter[]{param1,param2};
        
        expected.setPhi(params);
        assertTrue(expected.hasParameters());
        assertTrue(expected.hasFixedParameters());
        assertTrue(expected.hasFreeParameters());
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(1.2, actual.getPhi()[0].getValue(),.0);
        assertEquals(ParameterType.Fixed, actual.getPhi()[0].getType());
        assertTrue(actual.hasFreeParameters());
        
        params = new Parameter[] {param2};
        expected.setTheta(params);
        assertTrue(expected.hasParameters());
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertTrue(actual.hasFreeParameters());
        assertEquals(.2, actual.getTheta()[0].getValue(),.0);
        assertEquals(ParameterType.Estimated, actual.getTheta()[0].getType());
        
        params = new Parameter[]{param1,param3};
        expected.setBPhi(params);
        assertTrue(expected.hasParameters());
        assertTrue(expected.hasFixedParameters());
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(1.2, actual.getBPhi()[0].getValue(),.0);
        assertEquals(ParameterType.Fixed, actual.getBPhi()[0].getType());
        
        expected.setBTheta(params);
        assertTrue(expected.hasParameters());
        assertTrue(expected.hasFixedParameters());
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(1.2, actual.getBTheta()[0].getValue(),.0);
        assertEquals(ParameterType.Fixed, actual.getBTheta()[0].getType());
        
        expected.setParameterType(ParameterType.Initial);
        assertFalse(expected.hasFixedParameters());
        assertTrue(expected.hasParameters());
        info = expected.write(true);
        actual.read(info);
        assertFalse(actual.hasFixedParameters());
        assertTrue(actual.hasParameters());
        
        expected.clearParameters();
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
    }

}
