/*
* Copyright 2013 National Bank of Belgium
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
package demetra.stats.tests.seasonal;

import demetra.design.BuilderPattern;
import demetra.design.Development;
import demetra.dstats.Chi2;
import demetra.stats.StatException;
import demetra.stats.tests.StatisticalTest;
import demetra.stats.tests.TestType;
import java.util.Arrays;
import demetra.data.DoubleSeq;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@BuilderPattern(StatisticalTest.class)
public class KruskalWallis {

    private double h;
    private int period;

    /**
     *
     * @param sample
     * @param period
     */
    public KruskalWallis(DoubleSeq sample, final int period) {
        this.period = period;
        if (period <= 1) {
            throw new StatException();
        }
        double[] data = sample.toArray();

        Item[] items = new Item[data.length];
        int N = 0;
        int[] nk = new int[period];
        int j = 0;
        for (int i = 0; i < items.length; ++i) {
            //
            double d = data[i];
            if (Double.isFinite(d)) {
                int k = i % period;
                items[j++] = new Item(k, d);
                nk[k]++;
            }
        }
        N = j;
        Arrays.sort(items, 0, N);

        double[] S = new double[period];

        for (int i = 0; i < N;) {
            int j0 = i, j1 = i + 1;
            while (j1 < N && items[j0].val == items[j1].val) {
                ++j1;
            }
            int n = j1 - j0;
            if (n == 1) {
                S[items[i].pos] += i + 1;
            } else {
                double dpos = j0 + .5 * (n + 1);
                for (int jcur = j0; jcur < j1; ++jcur) {
                    S[items[jcur].pos] += dpos;
                }
            }
            i = j1;
        }

        h = 0;
        for (int i = 0; i < period; ++i) {
            h += S[i] * S[i] / nk[i];
        }
        h = 12 * h / (N * (N + 1)) - 3 * (N + 1);
    }

    public StatisticalTest build() {
        return new StatisticalTest(new Chi2(period - 1), h, TestType.Upper, true);
    }
}
