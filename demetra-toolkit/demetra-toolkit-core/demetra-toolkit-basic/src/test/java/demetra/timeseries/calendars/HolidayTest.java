/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.timeseries.calendars;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class HolidayTest {
    
    public HolidayTest() {
    }

    @Test
    public void testConstructor() {
        Holiday h1=new Holiday(FixedDay.ALLSAINTSDAY, null);
        Holiday h2=new Holiday(FixedDay.ALLSAINTSDAY);
        assertEquals(h1, h2);
    }
    
}
