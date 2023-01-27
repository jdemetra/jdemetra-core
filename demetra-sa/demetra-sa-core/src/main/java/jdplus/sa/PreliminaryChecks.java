/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa;

import demetra.data.DoubleSeq;
import demetra.processing.ProcessingLog;
import demetra.sa.SaException;
import demetra.timeseries.TsData;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class PreliminaryChecks {

    @FunctionalInterface
    public static interface Tool {

        TsData check(TsData original, ProcessingLog log);
    }

    public final static int MAX_REPEAT_COUNT = 80, MAX_MISSING_COUNT = 33;

    public static void testSeries(final TsData y) {
        if (y == null) {
            throw new SaException("Missing series");
        }
        int nz = y.length();
        int period = y.getAnnualFrequency();
        if (nz < Math.max(8, 3 * period)) {
            throw new SaException("Not enough data");
        }
        DoubleSeq values = y.getValues();
        int nrepeat = repeatCount(values);
        if (nrepeat > MAX_REPEAT_COUNT * nz / 100) {
            throw new SaException("Too many identical values");
        }
        int nm = values.count(z -> !Double.isFinite(z));
        if (nm > MAX_MISSING_COUNT * nz / 100) {
            throw new SaException("Too many missing values");
        }
    }

    public int repeatCount(DoubleSeq values) {
        int i = 0;
        int n = values.length();
        while ((i < n) && !Double.isFinite(values.get(i))) {
            ++i;
        }
        if (i == n) {
            return 0;
        }
        int c = 0;
        double prev = values.get(i++);
        for (; i < n; ++i) {
            double cur = values.get(i);
            if (Double.isFinite(cur)) {
                if (cur == prev) {
                    ++c;
                } else {
                    prev = cur;
                }
            }
        }
        return c;
    }

}
