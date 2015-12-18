/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.modelling.arima.x13;

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.PeriodSelectorType;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jadoull
 */
public class EstimateSpecTest {

    public EstimateSpecTest() {
    }

    @Test
    public void testInformationSet() {
        EstimateSpec expected = new EstimateSpec();
        EstimateSpec actual = new EstimateSpec();
        InformationSet info;

        assertTrue(expected.isDefault());
        expected.setTol(0.05);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(0.05, actual.getTol(), .0);

        assertFalse(actual.isDefault());
        TsPeriodSelector tsPeriod = new TsPeriodSelector();
        tsPeriod.from(Day.BEG);
        expected.setSpan(tsPeriod);
        assertFalse(expected.isDefault());
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(PeriodSelectorType.From, actual.getSpan().getType());

        expected.reset();
        assertTrue(expected.isDefault());
        info = expected.write(true);
        actual.read(info);
        assertTrue(actual.isDefault());
        assertEquals(expected, actual);
    }

}
