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
import demetra.timeseries.RegularDomain;
import demetra.timeseries.TsFrequency;
import demetra.timeseries.TsPeriod;
import java.time.LocalDate;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class TransitoryChangeTest {

    public TransitoryChangeTest() {
    }

    @Test
    public void testSomeMethod() {
    }

    @Test
    public void testData() {
        DataBlock buffer = DataBlock.make(20);
        RegularDomain days = RegularDomain.of(TsPeriod.of(TsFrequency.DAILY, LocalDate.now()), buffer.length());
        for (int i = -10; i < buffer.length() + 10; ++i) {
            TransitoryChange tc = new TransitoryChange(days.get(0).plus(i).start(), .7);
            tc.data(days.getStartPeriod(), buffer);
            assertTrue(buffer.sum() <= 1 / (1 - tc.getCoefficient()) + 1e-9);
        }
    }

}
