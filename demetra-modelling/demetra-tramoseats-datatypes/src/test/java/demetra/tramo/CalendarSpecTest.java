/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramo;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class CalendarSpecTest {
    
    public CalendarSpecTest() {
    }

    @Test
    public void testClone() {
        CalendarSpec spec=new CalendarSpec();
        CalendarSpec cspec = spec.clone();
        assertTrue(spec.equals(cspec));
    }
    
}
