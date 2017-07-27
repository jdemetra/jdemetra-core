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

import demetra.data.Data;
import demetra.data.Doubles;
import org.junit.Test;
import static org.junit.Assert.*;
import static demetra.timeseries.simplets.TsDataToolkit.log;
import static demetra.timeseries.simplets.TsDataToolkit.commit;
import static demetra.timeseries.simplets.TsDataToolkit.add;
import static demetra.timeseries.simplets.TsDataToolkit.delta;
import static demetra.timeseries.simplets.TsDataToolkit.drop;
import static demetra.timeseries.simplets.TsDataToolkit.fastFn;
import static demetra.timeseries.simplets.TsDataToolkit.fn;
import static demetra.timeseries.simplets.TsDataToolkit.normalize;
import static demetra.timeseries.simplets.TsDataToolkit.pctVariation;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class TsDataToolkitTest {

    TsData series = Data.TS_PROD;
    ec.tstoolkit.timeseries.simplets.TsData oldSeries = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, Data.PROD, false);

    public TsDataToolkitTest() {
    }

    @Test
    public void testUnaryOperator() {
        TsData t1 = fn(series, x -> Math.log(x));
        TsData t2 = fastFn(series, x -> Math.log(x));
        assertTrue(0 == (Doubles.of(t1.values()).distance(Doubles.of(t2.values()))));
    }

    @Test
    public void testOperators() {
        TsData s1 = Data.TS_PROD;
        TsData s2 = drop(s1, 13, 56);
        TsData lsum = log(add(s1, s2));
        lsum = commit(lsum);
    }

    @Test
    //@Ignore
    public void stressTest() {
        int K = 1000000;
        long t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            ec.tstoolkit.timeseries.simplets.TsData c = 
                    ec.tstoolkit.timeseries.simplets.TsData.add(oldSeries, oldSeries.drop(20, 50)).log();
            c.normalize();
            c = c.delta(12);
            double v = c.average();
            if (k == 0) {
                System.out.println(v);
            }
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            TsData s = delta(normalize(log(add(series, drop(series, 20, 50)))), 12);
            Doubles values = Doubles.of(s.values());
            double v = values.average();
            if (k == 0) {
                System.out.println(v);
            }
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

    @Test
    public void testNormalize() {
        TsData s1 = normalize(series);
        ec.tstoolkit.timeseries.simplets.TsData nc = oldSeries.clone();
        nc.normalize();
        assertTrue(distance(s1, nc) < 1e-9);
    }

    @Test
    public void testPct() {
        TsData s = pctVariation(series, 12);
        ec.tstoolkit.timeseries.simplets.TsData c = oldSeries.pctVariation(12);
        assertTrue(distance(s, c) < 1e-9);
    }

    @Test
    public void testDelta() {
        TsData s = delta(series, 12);
        ec.tstoolkit.timeseries.simplets.TsData c = oldSeries.delta(12);
        assertTrue(distance(s, c) < 1e-9);
    }

    @Test
    public void testCombined() {
        TsData s = delta(log(normalize(series)), 12);
        ec.tstoolkit.timeseries.simplets.TsData c = oldSeries.clone();
        c.normalize();
        c = c.log().delta(12);
        assertTrue(distance(s, c) < 1e-9);
    }

    private double distance(TsData s1, ec.tstoolkit.timeseries.simplets.TsData s2) {
        return Doubles.of(s1.values()).distance(Doubles.ofInternal(s2.internalStorage()));
    }

}
