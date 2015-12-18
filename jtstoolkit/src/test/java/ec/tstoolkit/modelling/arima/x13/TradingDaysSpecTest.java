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
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.ChangeOfRegime;
import org.junit.Assert;
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
        expected.setTradingDaysType(TradingDaysType.WorkingDays);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(TradingDaysType.WorkingDays, actual.getTradingDaysType());
        
        expected = new TradingDaysSpec();
        actual = new TradingDaysSpec();
        assertEquals(expected, actual);
        expected.setLengthOfPeriod(LengthOfPeriodType.LeapYear);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(LengthOfPeriodType.LeapYear, actual.getLengthOfPeriod());
        
        expected = new TradingDaysSpec();
        actual = new TradingDaysSpec();
        assertEquals(expected, actual);
        expected.setAutoAdjust(false);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertFalse(actual.isAutoAdjust());
        
        expected = new TradingDaysSpec();
        actual = new TradingDaysSpec();
        assertEquals(expected, actual);
        expected.setStockTradingDays(2);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(2, actual.getStockTradingDays());
        assertTrue(actual.isStockTradingDays());
        
        expected = new TradingDaysSpec();
        actual = new TradingDaysSpec();
        assertEquals(expected, actual);
        expected.setHolidays("test");
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals("test", actual.getHolidays());
        
        expected = new TradingDaysSpec();
        actual = new TradingDaysSpec();
        assertEquals(expected, actual);
        expected.setUserVariables(new String[]{"uservar1","uservar2"});
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        Assert.assertArrayEquals(new String[]{"uservar1","uservar2"},actual.getUserVariables());
        
        expected = new TradingDaysSpec();
        actual = new TradingDaysSpec();
        assertEquals(expected, actual);
        expected.setTest(RegressionTestSpec.Add);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(RegressionTestSpec.Add, actual.getTest());
        
        expected = new TradingDaysSpec();
        actual = new TradingDaysSpec();
        assertEquals(expected, actual);
        ChangeOfRegimeSpec cSpec = new ChangeOfRegimeSpec(new Day(2015, Month.November, 16));
        expected.setChangeOfRegime(cSpec);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(cSpec, actual.getChangeOfRegime());
    }
    
}
