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
package jdplus.modelling.regression;

import demetra.timeseries.regression.PeriodicOutlier;
import jdplus.data.DataBlock;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsUnit;
import demetra.timeseries.TsPeriod;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate
 */
public class PeriodicOutlierTest {

    public PeriodicOutlierTest() {
    }

    @Test
    public void testData() {
        TsDomain days = TsDomain.of(TsPeriod.of(TsUnit.DAY, LocalDate.now()), 20);
        for (int i = -1; i < 30; ++i) {
            PeriodicOutlier po = new PeriodicOutlier(days.get(0).plus(i).start(), 5, true);
            DataBlock buffer = Regression.x(days, po);
            assertTrue(buffer.sum() < 1.0001);
        }
    }

    @Test
    public void testData2() {
        TsDomain days = TsDomain.of(TsPeriod.of(TsUnit.DAY, LocalDate.now()), 20);
        for (int i = -10; i < 30; ++i) {
            PeriodicOutlier po = new PeriodicOutlier(days.get(0).plus(i).start(), 5, false);
            DataBlock buffer = Regression.x(days, po);
            assertTrue(buffer.sum() < 1.0001);
        }
    }
}
