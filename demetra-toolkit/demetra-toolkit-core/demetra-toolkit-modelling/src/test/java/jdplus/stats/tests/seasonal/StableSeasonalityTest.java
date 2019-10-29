/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.stats.tests.seasonal;

import demetra.data.Data;
import demetra.data.DoubleSeq;
import demetra.timeseries.TsData;
import jdplus.stats.tests.AnovaTest;
import jdplus.stats.tests.OneWayAnova;
import jdplus.stats.tests.StatisticalTest;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class StableSeasonalityTest {

    public StableSeasonalityTest() {
    }

    @Test
    public void testProd() {
        TsData ts = Data.TS_PROD.delta(1);
        DoubleSeq t = ts.getValues();
        StableSeasonality ss = new StableSeasonality(t, 12);
        AnovaTest ss2 = CombinedSeasonalityTest.stableSeasonality(t, 12);
        assertEquals(ss.build().getValue(), ss2.asTest().getValue(), 1e-9);

        OneWayAnova anova = new OneWayAnova();
        int n = t.length();
        for (int i = 0; i < 12; ++i) {
            anova.add(new OneWayAnova.Group(null, t.extract(i, (n + 11 - i) / 12, 12)));
        }
        StatisticalTest st = anova.build();
        assertEquals(st.getValue(), ss2.asTest().getValue(), 1e-9);
    }

}
