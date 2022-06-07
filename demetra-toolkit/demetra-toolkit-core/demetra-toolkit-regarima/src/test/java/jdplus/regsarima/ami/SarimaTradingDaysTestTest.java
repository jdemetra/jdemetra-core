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
package jdplus.regsarima.ami;

import demetra.arima.SarimaOrders;
import demetra.data.Data;
import demetra.stats.StatisticalTest;
import jdplus.sarima.SarimaModel;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author PALATEJ
 */
public class SarimaTradingDaysTestTest {
    
    public SarimaTradingDaysTestTest() {
    }

    @Test
    public void testProd() {
        SarimaOrders orders=SarimaOrders.airline(12);
        SarimaModel arima=SarimaModel.builder(orders)
                .setDefault().build();
        StatisticalTest test=SarimaTradingDaysTest.sarimaTest(Data.TS_PROD, arima, false);
        assertTrue(test.getPvalue()<1e-4);
        test=SarimaTradingDaysTest.sarimaTest(Data.TS_PROD, arima, true);
        assertTrue(test.getPvalue()<1e-4);
    }
    
}
