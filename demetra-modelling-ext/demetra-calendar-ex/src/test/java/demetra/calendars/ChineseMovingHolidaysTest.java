/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.calendars;

import java.time.LocalDate;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author palatej
 */
public class ChineseMovingHolidaysTest {
    
    public ChineseMovingHolidaysTest() {
    }

    @Test
    public void testNewYear() {
        
        LocalDate d0=LocalDate.of(1980,1,1);
        LocalDate d1=LocalDate.of(2030,12,31);
        
        LocalDate[] holidays = ChineseMovingHolidays.newYear().holidays(d0, d1);
        assertTrue(holidays.length == 51);
//        for (int i=0; i<51; ++i)
//            System.out.println(holidays[i]);
    }
    
}
