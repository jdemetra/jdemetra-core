/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
*/

package ec.tstoolkit.timeseries;

import java.util.Calendar;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author pcuser
 */
public class DayTest {

    static final Day D_2001_01_03 = new Day(2001, Month.January, 3 - 1);
    static final Day D_2001_03_03 = new Day(2001, Month.March, 3 - 1);
    static final Day D_2001_03_10 = new Day(2001, Month.March, 10 - 1);

    @Test
    public void testGetTime() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(D_2001_01_03.getTime());
        assertEquals(2001, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(3, cal.get(Calendar.DAY_OF_MONTH));

        assertEquals(D_2001_01_03.getTime(), D_2001_01_03.getTime());
        assertNotSame(D_2001_01_03.getTime(), D_2001_01_03.getTime());
        assertNotEquals(D_2001_01_03.getTime(), D_2001_03_03.getTime());
    }

    @Test
    public void testToDay() {
        Calendar now = Calendar.getInstance();
        Day today = Day.toDay();
        assertEquals(now.get(Calendar.YEAR), today.getYear());
        assertEquals(now.get(Calendar.MONTH), today.getMonth());
        assertEquals(now.get(Calendar.DAY_OF_WEEK), DayOfWeek.toCalendar(today.getDayOfWeek()));
    }

    @Test
    public void testCompareTo() {
        assertTrue(D_2001_01_03.compareTo(D_2001_01_03) == 0);
        assertTrue(D_2001_03_03.compareTo(D_2001_01_03) > 0);
        assertTrue(D_2001_01_03.compareTo(D_2001_03_03) < 0);
    }

    @Test
    public void testGetMonth() {
        assertEquals(Month.March.intValue(), D_2001_03_03.getMonth());
    }

    @Test
    public void testGetYear() {
        assertEquals(2001, D_2001_03_03.getYear());
    }
}
