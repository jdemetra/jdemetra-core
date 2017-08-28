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
package demetra.timeseries.simplets;

import demetra.data.DataBlock;
import demetra.timeseries.TsFrequency;
import demetra.timeseries.TsPeriod;
import java.time.LocalDate;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class TsDataViewTest {

    public TsDataViewTest() {
    }

    @Test
    public void testFullYears() {
        TsPeriod p = TsPeriod.of(TsFrequency.MONTHLY, LocalDate.now());
        for (int i = 0; i < 12; ++i) {
            for (int j = 0; j < 12; ++j) {
                DataBlock d = DataBlock.make(i + j + 36);
                final int beg = 7 - i;
                d.set(k -> beg + k);
                TsData s = TsData.of(p.plus(-i), d);
                TsDataView fy = TsDataView.fullYears(s);
                assertTrue(fy.getData().length() % 12 == 0);
                assertTrue(((int) fy.getData().get(0)) % 12 == 0);
            }
        }

    }

}
