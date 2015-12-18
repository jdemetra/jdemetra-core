/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.satoolkit.benchmarking;

import ec.satoolkit.benchmarking.MultiSaBenchmarkingSpec.ConstraintType;
import ec.tstoolkit.information.InformationSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jadoull
 */
public class MultiSaBenchmarkingSpecTest {

    public MultiSaBenchmarkingSpecTest() {
    }

    @Test
    public void testInformationSet() {
        MultiSaBenchmarkingSpec expected = new MultiSaBenchmarkingSpec();
        MultiSaBenchmarkingSpec actual = new MultiSaBenchmarkingSpec();
        InformationSet info;
        assertEquals(expected, actual);
        assertTrue(expected.isEnabled());

        expected.setTarget(SaBenchmarkingSpec.Target.CalendarAdjusted);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(SaBenchmarkingSpec.Target.CalendarAdjusted, actual.getTarget());

        expected.setRho(2.2);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(2.2, actual.getRho(), .0);

        expected.setLambda(2.1);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(2.1, actual.getLambda(), .0);

        assertFalse(expected.isAnnualConstraint());
        expected.setAnnualConstraint(true);
        assertTrue(expected.isAnnualConstraint());
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertTrue(actual.isAnnualConstraint());

        assertEquals(ConstraintType.Fixed, expected.getContemporaneousConstraintType());
        expected.setContemporaneousConstraintType(ConstraintType.None);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(ConstraintType.None, actual.getContemporaneousConstraintType());
    }
}
