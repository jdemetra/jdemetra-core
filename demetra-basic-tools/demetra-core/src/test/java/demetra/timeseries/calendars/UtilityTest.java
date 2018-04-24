/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.timeseries.calendars;

import demetra.timeseries.calendars.Utility;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class UtilityTest {
    
    public UtilityTest() {
    }

    @Test
    public void testEpoch() {
        assertTrue(Utility.calc(1970, 1, 1) == 0);
    }
    
    @Test
    public void testNow() {
        LocalDate now = LocalDate.now();
        assertTrue(Utility.calc(now.getYear(), now.getMonthValue(), now.getDayOfMonth()) == LocalDate.ofEpochDay(0).until(now, ChronoUnit.DAYS));
    }
}
