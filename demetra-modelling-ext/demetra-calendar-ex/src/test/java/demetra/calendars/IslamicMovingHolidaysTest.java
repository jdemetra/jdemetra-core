/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.calendars;

import java.time.LocalDate;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class IslamicMovingHolidaysTest {
    
    public IslamicMovingHolidaysTest() {
    }

    @Test
    public void testRasElAm() {
        LocalDate d0=LocalDate.of(1980,1,1);
        LocalDate d1=LocalDate.of(2030,12,31);
        
        LocalDate[] holidays = IslamicMovingHolidays.rasElAm().holidays(d0, d1);
        assertTrue(holidays.length == 52);
//        for (int i=0; i<holidays.length; ++i)
//            System.out.println(holidays[i]);
    }
    
}
