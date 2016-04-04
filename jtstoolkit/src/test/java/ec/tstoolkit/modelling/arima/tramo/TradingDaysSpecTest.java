/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.RegressionTestType;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jadoull
 */
public class TradingDaysSpecTest {

    public TradingDaysSpecTest() {
    }

    @Test
    public void testInformationSet() {
        TradingDaysSpec expected = new TradingDaysSpec();
        TradingDaysSpec actual = new TradingDaysSpec();
        InformationSet info;
        assertEquals(expected, actual);
        assertTrue(expected.isDefault());
        assertTrue(actual.isDefault());

        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertTrue(actual.isDefault());

        expected.setAutomatic(false);
        info = expected.write(true);
        actual.read(info);
        assertFalse(actual.isAutomatic());
        
        expected.setAutomaticMethod(TradingDaysSpec.AutoMethod.WaldTest);
        expected.setProbabibilityForFTest(0.05);
        info = expected.write(true);
        actual.read(info);
        assertEquals(0.05, actual.getProbabibilityForFTest(),0.0);
        
        expected.setAutomaticMethod(TradingDaysSpec.AutoMethod.Unused);
        expected.setTradingDaysType(TradingDaysType.TradingDays);
        expected.setLeapYear(false);
        expected.setRegressionTestType(RegressionTestType.Joint_F);
        info = expected.write(true);
        actual.read(info);
        assertEquals(TradingDaysType.TradingDays, actual.getTradingDaysType());
        assertEquals(RegressionTestType.Joint_F, actual.getRegressionTestType());
        assertFalse(actual.isLeapYear());
        
        expected.setStockTradingDays(15);
        info = expected.write(true);
        actual.read(info);
        assertEquals(15, actual.getStockTradingDays());
        
        assertTrue(expected.isValid());
        assertTrue(actual.isValid());
                
        expected.reset();
        assertTrue(expected.isDefault());
        info = expected.write(true);
        actual.read(info);
        assertTrue(actual.isDefault());
    }

}
