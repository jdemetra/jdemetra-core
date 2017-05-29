/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.timeseries;

import java.time.LocalDate;
import org.junit.Test;
import static org.junit.Assert.*;

public class DailyPeriodTest {

    @Test
    public void testDay() {
        LocalDate ld0 = LocalDate.now();
        Day d0 = Day.of(ld0);
        System.out.println(d0.toString());
        LocalDate ld1 = LocalDate.now();
        Day d1 = Day.of(ld1);
        assertTrue(d0.equals(d1));
        assertTrue(d0.hashCode() == d1.hashCode());
        assertTrue(d0.toString().equals(d1.toString()));
        assertTrue(d0.firstDay().equals(d0.lastDay()));
    }

    @Test
    public void testDays() {
        LocalDate ld0 = LocalDate.now();
        LocalDate ld1 = LocalDate.now().plusDays(10);
        DailyPeriod d0 = DailyPeriod.of(ld0, ld1);
        System.out.println(d0.toString());
        IDatePeriod d1 = DailyPeriod.of(ld0, ld0.plusDays(10));
        assertTrue(d0.equals(d1));
        assertTrue(d0.hashCode() == d1.hashCode());
        assertTrue(d0.toString().equals(d1.toString()));
//        assertTrue(d0.firstDay().until(d0.lastDay()).getDays()==11);
    }
}
