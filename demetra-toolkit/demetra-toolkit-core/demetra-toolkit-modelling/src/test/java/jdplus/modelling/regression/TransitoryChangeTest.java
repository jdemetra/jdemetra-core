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

import demetra.timeseries.regression.TransitoryChange;
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
public class TransitoryChangeTest {

    public TransitoryChangeTest() {
    }

    @Test
    public void testSomeMethod() {
    }

    @Test
    public void testData() {
        TsDomain days = TsDomain.of(TsPeriod.of(TsUnit.DAY, LocalDate.now()), 20);
        for (int i = -10; i < 30; ++i) {
            TransitoryChange tc = new TransitoryChange(days.get(0).plus(i).start(), .7);
            DataBlock x = Regression.x(days, tc);
            assertTrue(x.sum() <= 1 / (1 - tc.getRate()) + 1e-9);
        }
    }

}
