/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.timeseries;

import internal.Demo;
import java.time.LocalDate;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class CalendarTsDataTest {
    
    public CalendarTsDataTest() {
    }

    @Demo
    public static void main(String[] args) {
        Weeks weeks = Weeks.of(LocalDate.now(), 20);
        System.out.println(CalendarTsData.random(weeks, 0));
    }
    
}
