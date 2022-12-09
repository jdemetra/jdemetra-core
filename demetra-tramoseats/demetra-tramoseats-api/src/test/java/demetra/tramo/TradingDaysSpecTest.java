/*
 * Copyright 2019 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package demetra.tramo;

import demetra.timeseries.calendars.LengthOfPeriodType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 *
 * @author Mats Maggi
 */
public class TradingDaysSpecTest {
    
    @Test
    public void test() {
        TradingDaysSpec spec1 = TradingDaysSpec.automaticHolidays("test", LengthOfPeriodType.LeapYear, TradingDaysSpec.AutoMethod.FTEST, .99, false);
        TradingDaysSpec spec2 = TradingDaysSpec.automatic(LengthOfPeriodType.LeapYear, TradingDaysSpec.AutoMethod.FTEST, .99, false);
       
        assertNotEquals(spec1, spec2);
    }
}
