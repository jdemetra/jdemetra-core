/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.information.InformationSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jadoull
 */
public class CalendarSpecTest {
    
    public CalendarSpecTest() {
    }

    @Test
    public void testInformationSet() {
        CalendarSpec expected = new CalendarSpec();
        CalendarSpec actual = new CalendarSpec();
        InformationSet info;
        
        assertEquals(expected, actual);
        info = expected.write(true);
        actual.read(info);
        assertEquals(expected, actual);
        
        TradingDaysSpec tradspec = new TradingDaysSpec();
        tradspec.setAutomaticMethod(TradingDaysSpec.AutoMethod.WaldTest);
        expected.setTradingDays(tradspec);
        info = expected.write(true);
        actual.read(info);
        assertEquals(TradingDaysSpec.AutoMethod.WaldTest, actual.getTradingDays().getAutomaticMethod());
        
        EasterSpec eastspec = new EasterSpec();
        eastspec.setDuration(3);
        expected.setEaster(eastspec);
        info = expected.write(true);
        actual.read(info);
        assertEquals(3, actual.getEaster().getDuration());
        
        assertTrue(actual.isUsed());
        assertFalse(actual.isDefault());
    }
    
}
