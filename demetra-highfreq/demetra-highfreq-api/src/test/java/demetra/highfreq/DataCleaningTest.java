/*
 * Copyright 2022 National Bank of Belgium
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
package demetra.highfreq;

import demetra.data.DoubleSeq;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class DataCleaningTest {

    public DataCleaningTest() {
    }

    @Test
    public void testSundays() {
        for (int i = 1; i <= 7; ++i) {
            TsData z = TsData.of(TsPeriod.daily(2000, 1, i), DoubleSeq.onMapping(5000, j -> j));
            CleanedData cdata=CleanedData.of(z, DataCleaning.SUNDAYS);
            TsData zc = DataCleaning.withMissingSundays(z.getDomain(), cdata.getData());
            assertTrue(DataCleaning.of(zc) == DataCleaning.SUNDAYS);
        }
    }
    
    @Test
    public void testWeekEnds() {
        for (int i = 1; i <= 7; ++i) {
            TsData z = TsData.of(TsPeriod.daily(2000, 1, i), DoubleSeq.onMapping(5000, j -> j));
            CleanedData cdata=CleanedData.of(z, DataCleaning.WEEKENDS);
            TsData zc = DataCleaning.withMissingWeekEnds(z.getDomain(), cdata.getData());
            assertTrue(DataCleaning.of(zc) == DataCleaning.WEEKENDS);
        }
    }

}
