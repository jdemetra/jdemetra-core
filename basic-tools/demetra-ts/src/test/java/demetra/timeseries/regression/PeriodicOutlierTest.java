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
package demetra.timeseries.regression;

import demetra.data.DataBlock;
import demetra.timeseries.Day;
import demetra.timeseries.Days;
import java.time.LocalDate;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class PeriodicOutlierTest {
    
    public PeriodicOutlierTest() {
    }

    @Test
    public void testData() {
        DataBlock buffer = DataBlock.make(20);
        Days days = Days.of(LocalDate.now(), buffer.length());
        for (int i = -10; i < buffer.length()+10; ++i) {
            PeriodicOutlier<Day> po = new PeriodicOutlier<>(days.get(0).plus(i).start(), 5, true);
            po.data(days.getStart(), buffer);
            assertTrue(buffer.sum() <  1.0001);
        }
    }
    
    @Test
    public void testData2() {
        DataBlock buffer = DataBlock.make(20);
        Days days = Days.of(LocalDate.now(), buffer.length());
        for (int i = -10; i < buffer.length()+10; ++i) {
            PeriodicOutlier<Day> po = new PeriodicOutlier<>(days.get(0).plus(i).start(), 5, false);
            po.data(days.getStart(), buffer);
            assertTrue(buffer.sum() <  1.0001);
        }
    }
}
