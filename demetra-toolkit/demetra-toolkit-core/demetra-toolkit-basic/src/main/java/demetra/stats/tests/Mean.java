/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or â€“ as soon they will be approved 
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

import demetra.design.BuilderPattern;
import demetra.design.Development;
import demetra.dstats.T;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@BuilderPattern(StatisticalTest.class)
public class Mean {

    private final double mean, emean, var;
    private final int n;

    private Mean(final double mean, final double emean, final double var, final int n) {
        this.mean = mean;
        this.emean = emean;
        this.var = var;
        this.n = n;
    }

    /**
     *
     * @param data
     * @return
     */
    public static Mean zeroMean(DoubleSeq data) {
        int nmissing = data.count(x -> !Double.isFinite(x));
        int m = data.length() - nmissing;
        if (nmissing > 0) {
            double av = data.sumWithMissing() / m;
            double v = data.ssqWithMissing() / m;
            return new Mean(av, 0, v, m);
        } else {
            double av = data.sum() / m;
            double v = data.ssq() / m;
            return new Mean(av, 0, v, m);
        }
    }

    /**
     *
     * @param data
     * @param mu
     * @return
     */
    public static Mean mean(DoubleSeq data, final double mu) {
        int nmissing = data.count(x -> !Double.isFinite(x));
        int m = data.length() - nmissing;
        if (nmissing > 0) {
            double av = data.sumWithMissing() / m;
            double v = data.ssqcWithMissing(mu) / m;
            return new Mean(av, mu, v, m);
        } else {
            double av = data.sum() / m;
            double v = data.ssqc(mu) / m;
            return new Mean(av, mu, v, m);
        }
    }

    public StatisticalTest build() {
        double val = (mean - emean) / Math.sqrt(var / n);
        return new StatisticalTest(new T(n), val, TestType.TwoSided, false);
    }

}
