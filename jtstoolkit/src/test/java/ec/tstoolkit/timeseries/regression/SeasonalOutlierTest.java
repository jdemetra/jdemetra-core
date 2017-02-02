/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class SeasonalOutlierTest {

    public SeasonalOutlierTest() {
    }

    @Test
    public void testBefore() {
        TsPeriod pos = new TsPeriod(TsFrequency.Monthly, 2000, 3);
        SeasonalOutlier so1 = new SeasonalOutlier(pos.firstday());
        // 
        for (int i = 1; i < 12; ++i) {
            DataBlock data = new DataBlock(i);
            so1.data(pos.minus(i), data);
            assertTrue(data.isZero());
        }
        SeasonalOutlier so2 = new SeasonalOutlier(pos.firstday());
        so2.setZeroEnded(true);
        for (int i = 1; i < 10; ++i) {
            DataBlock data = new DataBlock(i);
            so2.data(pos.minus(i), data);
            assertTrue(data.isConstant());
        }
    }

    @Test
    public void testAfter() {
        TsPeriod pos = new TsPeriod(TsFrequency.Monthly, 2000, 3);
        SeasonalOutlier so1 = new SeasonalOutlier(pos.firstday());
        // 
        for (int i = 1; i < 10; ++i) {
            DataBlock data = new DataBlock(12 - i);
            so1.data(pos.plus(i), data);
            assertTrue(data.isConstant());
        }
        SeasonalOutlier so2 = new SeasonalOutlier(pos.firstday());
        so2.setZeroEnded(true);
        for (int i = 1; i < 12; ++i) {
            DataBlock data = new DataBlock(i);
            so2.data(pos.plus(i), data);
            assertTrue(data.isZero());
        }
    }

    @Test
    public void testAround() {
        TsPeriod pos = new TsPeriod(TsFrequency.Monthly, 2000, 3);
        SeasonalOutlier so1 = new SeasonalOutlier(pos.firstday());
        // 
        for (int i = -6; i < 6; ++i) {
            DataBlock data = new DataBlock(12);
            so1.data(pos.minus(i), data);
        }
    }
}
