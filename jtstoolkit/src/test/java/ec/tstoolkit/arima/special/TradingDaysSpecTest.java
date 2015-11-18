/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.arima.special;

import ec.tstoolkit.information.InformationSet;
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
        assertFalse(expected.isUsed());
        expected.setTradingDaysType(TradingDaysType.TradingDays);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(TradingDaysType.TradingDays, actual.getTradingDaysType());
        
        expected = new TradingDaysSpec();
        actual = new TradingDaysSpec();
        assertEquals(expected, actual);
        expected.setLeapYear(true);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertTrue(actual.isLeapYear());
        
        expected = new TradingDaysSpec();
        actual = new TradingDaysSpec();
        assertEquals(expected, actual);
        expected.setStockTradingDays(3);
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertTrue(actual.isStockTradingDays());
        assertEquals(3, actual.getStockTradingDays());
        
        expected = new TradingDaysSpec();
        actual = new TradingDaysSpec();
        assertEquals(expected, actual);
        expected.setHolidays("test1");
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals("test1", actual.getHolidays());
        
        expected = new TradingDaysSpec();
        actual = new TradingDaysSpec();
        assertEquals(expected, actual);
        expected.setUserVariables(new String[]{"uv1","uv2","uv3"});
        assertNotEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        assertEquals(3, actual.getUserVariables().length);
        assertEquals("uv2", actual.getUserVariables()[1]);
    }
    
}
