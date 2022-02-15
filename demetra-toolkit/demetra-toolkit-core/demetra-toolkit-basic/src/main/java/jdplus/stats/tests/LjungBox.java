/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.stats.tests;

import demetra.stats.TestType;
import nbbrd.design.BuilderPattern;
import nbbrd.design.Development;
import jdplus.dstats.Chi2;
import demetra.stats.AutoCovariances;
import java.util.function.IntToDoubleFunction;
import demetra.data.DoubleSeq;
import demetra.stats.StatisticalTest;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@BuilderPattern(StatisticalTest.class)
public class LjungBox {

    public static int defaultAutoCorrelationsCount(int period) {
        if (period >= 12) {
            return 2 * period;
        } else if (period == 1) {
            return 8;
        } else {
            return 4 * period;
        }
    }

    private int lag = 1;
    private int k = 12;
    private int nhp;
    private int sign;

    private final IntToDoubleFunction autoCorrelations;
    private final int n;

    public LjungBox(DoubleSeq sample) {
        this(sample, false);
    }

    public LjungBox(DoubleSeq sample, boolean correctForMean) {
        if (correctForMean) {
            this.autoCorrelations = AutoCovariances.autoCorrelationFunction(sample, sample.average());
            this.n = sample.length() - 1;
        } else {
            this.autoCorrelations = AutoCovariances.autoCorrelationFunction(sample, 0);
            this.n = sample.length();
        }
    }

    public LjungBox(IntToDoubleFunction autoCorrelations, int sampleSize) {
        this.autoCorrelations = autoCorrelations;
        this.n = sampleSize;
    }

    /**
     *
     * @param nhp
     * @return
     */
    public LjungBox hyperParametersCount(int nhp) {
        this.nhp = nhp;
        return this;
    }

    /**
     *
     * @param lag
     * @return
     */
    public LjungBox lag(int lag) {
        this.lag = lag;
        return this;
    }

    /**
     *
     * @param k
     * @return
     */
    public LjungBox autoCorrelationsCount(int k) {
        this.k = k;
        return this;
    }

    public LjungBox usePositiveAutoCorrelations() {
        this.sign = 1;
        return this;
    }

    public LjungBox useNegativeAutoCorrelations() {
        this.sign = -1;
        return this;
    }

    public LjungBox useAllAutoCorrelations() {
        this.sign = 0;
        return this;
    }

    /**
     *
     * @param s s=1: positive autocorr, s=-1: negative autocorr, s=0: all
     * autocorr
     * @return
     */
    public LjungBox sign(int s) {
        this.sign = s;
        return this;
    }

    public StatisticalTest build() {

        double res = 0.0;
        for (int i = 1; i <= k; i++) {
            double ai = autoCorrelations.applyAsDouble(i * lag);
            if (sign == 0 || (sign == 1 && ai > 0) || (sign == -1 && ai < 0)) {
                res += ai * ai / (n - i * lag);
            }
        }
        double val = res * n * (n + 2);
        Chi2 chi = new Chi2(lag == 1 ? (k - nhp) : k);
        return TestsUtility.testOf(val, chi, TestType.Upper);
    }

}
