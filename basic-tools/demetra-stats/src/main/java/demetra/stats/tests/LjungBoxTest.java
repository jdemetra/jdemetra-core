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
package demetra.stats.tests;

import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.design.IBuilder;
import demetra.dstats.Chi2;
import demetra.stats.AutoCovariances;
import java.util.function.IntToDoubleFunction;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class LjungBoxTest implements IBuilder<StatisticalTest> {

    private int lag = 1;
    private int k = 12;
    private int nhp;
    private int sign;

    private final IntToDoubleFunction autoCorrelations;
    private final int n;

    public LjungBoxTest(DoubleSequence sample) {
        this.autoCorrelations = AutoCovariances.autoCorrelationFunction(sample, 0);
        this.n = sample.length();
    }

    /**
     *
     * @param nhp
     * @return
     */
    public LjungBoxTest hyperParametersCount(int nhp) {
        this.nhp = nhp;
        return this;
    }

    /**
     *
     * @param lag
     * @return
     */
    public LjungBoxTest lag(int lag) {
        this.lag = lag;
        return this;
    }

    /**
     *
     * @param k
     * @return
     */
    public LjungBoxTest autoCorrelationsCount(int k) {
        this.k = k;
        return this;
    }

    public LjungBoxTest usePositiveAutoCorrelations() {
        this.sign = 1;
        return this;
    }

    public LjungBoxTest useNegativeAutoCorrelations() {
        this.sign = -1;
        return this;
    }

    public LjungBoxTest useAllAutoCorrelations() {
        this.sign = 0;
        return this;
    }

    @Override
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
        return new StatisticalTest(chi, val, TestType.Upper, true);
    }

}
