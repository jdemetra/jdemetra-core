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

import java.time.DayOfWeek;
import java.time.LocalDate;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class WeeksTest {

    @Test
    public void testFactory() {
        LocalDate start = LocalDate.of(2017, 1, 1);
        LocalDate end = LocalDate.of(2017, 12, 31);
        Weeks d = Weeks.between(DayOfWeek.MONDAY, start, end);
        assertTrue(d.length() == 52);
        System.out.println(d);
    }

    @Test
    public void testSearch() {
        LocalDate start = LocalDate.of(2017, 1, 1);
        Weeks d = Weeks.of(start, 59);
        for (int i = 0; i < d.length(); ++i) {
            assertTrue(d.search(d.get(i).firstDay()) == i);
        }
        assertTrue(d.search(LocalDate.of(2016, 12, 31)) == -1);
        assertTrue(d.search(LocalDate.of(2018, 3, 1)) == -d.length());
        for (int i = 0; i < d.length(); ++i) {
            assertTrue(d.search(d.get(i).firstDay().atStartOfDay().plusMinutes(655)) == i);
        }
        assertTrue(d.search(LocalDate.of(2016, 12, 31).atStartOfDay().plusMinutes(655)) == -1);
        assertTrue(d.search(LocalDate.of(2018, 3, 1).atStartOfDay().plusMinutes(655)) == -d.length());
    }

}
