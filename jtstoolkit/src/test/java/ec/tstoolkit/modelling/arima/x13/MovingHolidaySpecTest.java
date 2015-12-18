/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.modelling.arima.x13;

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.ChangeOfRegimeSpec;
import ec.tstoolkit.modelling.RegressionTestSpec;
import ec.tstoolkit.timeseries.Day;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jadoull
 */
public class MovingHolidaySpecTest {
    
    public MovingHolidaySpecTest() {
    }

    @Test
    public void testInformationSet() {
        MovingHolidaySpec expected = new MovingHolidaySpec();
        MovingHolidaySpec actual = new MovingHolidaySpec();
        InformationSet info;
        
        assertEquals(expected, actual);
        expected.setType(MovingHolidaySpec.Type.Labor);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(MovingHolidaySpec.Type.Labor, actual.getType());
        
        expected.setW(2);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(2, actual.getW());
        
        ChangeOfRegimeSpec corSpec = new ChangeOfRegimeSpec(Day.toDay());
        expected.setChangeOfRegime(corSpec);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        corSpec.setType(ChangeOfRegimeSpec.Type.Partial_ZeroStart);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        corSpec.setType(ChangeOfRegimeSpec.Type.Partial_ZeroEnd);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(ChangeOfRegimeSpec.Type.Partial_ZeroEnd, actual.getChangeOfRegime().getType());
        
        expected.setTest(RegressionTestSpec.Remove);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(RegressionTestSpec.Remove, actual.getTest());
        
        MovingHolidaySpec clone;
        clone = expected.clone();
        assertEquals(expected, clone);
        info = clone.write(true);
        actual.read(info);
        assertEquals(clone, actual);
    }
    
}
