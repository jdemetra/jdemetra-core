/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jadoull
 */
public class InterventionVariableTest {

    public InterventionVariableTest() {
    }

    @Test
    public void testInformationSet() {
        InterventionVariable expected = new InterventionVariable();
        InterventionVariable actual = new InterventionVariable();
        InformationSet info;
        assertEquals(expected, actual);
        expected.setDelta(1.2);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(1.2, actual.getDelta(), .0);

        expected = new InterventionVariable();
        actual = new InterventionVariable();
        assertEquals(expected, actual);
        expected.setDeltaS(1.3);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(1.3, actual.getDeltaS(), .0);

        expected = new InterventionVariable();
        actual = new InterventionVariable();
        assertEquals(expected, actual);
        expected.add(new Day(2015, Month.January, 15), new Day(2015, Month.March, 15));
        expected.add(new Day(2015, Month.March, 17), new Day(2015, Month.April, 12));
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(2, expected.getSequences().length);
        
    }
}
