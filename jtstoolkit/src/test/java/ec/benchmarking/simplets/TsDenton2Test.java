/*
 * Copyright 2013-2014 National Bank of Belgium
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
package ec.benchmarking.simplets;

import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class TsDenton2Test {

    public final TsData y, m, ym;

    public TsDenton2Test() {
        m = data.Data.X;
        ym = m.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true);
        y = ym.clone();
        for (int i = 0; i < y.getLength(); ++i) {
            y.set(i, y.get(i) * (1 + 0.01 * i));
        }
    }

    @Test
    public void testMul() {
        TsDenton2 denton = new TsDenton2();
        denton.setMultiplicative(true);
        for (int i = 1; i < 3; ++i) {
            denton.setDifferencingOrder(i);
            TsData mc = denton.benchmark(m, y);
            assertTrue(mc.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true).distance(y) < 1e-9);
        }
        denton.setModified(false);
        for (int i = 1; i < 3; ++i) {
            denton.setDifferencingOrder(i);
            TsData mc = denton.benchmark(m, y);
            assertTrue(mc.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true).distance(y) < 1e-9);
        }
    }

    @Test
    public void testAdd() {
        TsDenton2 denton = new TsDenton2();
        denton.setMultiplicative(false);
        for (int i = 1; i < 3; ++i) {
            denton.setDifferencingOrder(i);
            TsData mc = denton.benchmark(m, y);
            assertTrue(mc.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true).distance(y) < 1e-9);
        }
        denton.setModified(false);
        for (int i = 1; i < 3; ++i) {
            denton.setDifferencingOrder(i);
            TsData mc = denton.benchmark(m, y);
            assertTrue(mc.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true).distance(y) < 1e-9);
        }
    }
}
