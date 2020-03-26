/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.modelling.regular.tests;

import demetra.data.Data;
import demetra.timeseries.TsData;
import jdplus.stats.tests.StatisticalTest;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class TradingDaysTestTest {
    
    public TradingDaysTestTest() {
    }

    @Test
    public void testProd() {
        TsData s = Data.TS_PROD.log().delta(12);
        StatisticalTest olsTest2 = TradingDaysTest.olsTest2(s);
        assertTrue(olsTest2.getPValue() < 1e-3);

        s=s.delta(1);
        StatisticalTest olsTest = TradingDaysTest.olsTest(s);
        assertTrue(olsTest2.getPValue() < 1e-3);
    }
}
