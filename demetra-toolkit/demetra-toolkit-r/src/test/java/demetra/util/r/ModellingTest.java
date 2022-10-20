/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package demetra.util.r;

import demetra.timeseries.calendars.Calendar;
import demetra.timeseries.calendars.DayEvent;
import demetra.timeseries.calendars.FixedDay;
import demetra.timeseries.calendars.Holiday;
import demetra.timeseries.calendars.PrespecifiedHoliday;
import demetra.timeseries.regression.ModellingContext;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class ModellingTest {
    
    public ModellingTest() {
    }

    @Test
    public void testCalendar() {
        Holiday hol1=new FixedDay(7,21);
        Holiday hol2=PrespecifiedHoliday.builder().event(DayEvent.NewYear).build();
        Calendar cal=new Calendar(new Holiday[]{hol1, hol2});
        ModellingContext context=new ModellingContext();
        context.getCalendars().set("BE", cal);
        
        byte[] toBuffer = Modelling.toBuffer(context);
        ModellingContext context2 = Modelling.of(toBuffer);
        assertTrue(context2 != null);
    }
    
}
