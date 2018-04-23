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

import demetra.data.DoubleSequence;
import demetra.data.Doubles;
import demetra.design.BuilderPattern;
import demetra.design.Development;
import demetra.dstats.T;

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
    public static Mean zeroMean(DoubleSequence data) {
        int m = data.length();
        double av = Doubles.sum(data) / m;
        double v = Doubles.ssq(data) / m;
        return new Mean(av, 0, v, m);
    }

    /**
     *
     * @param data
     * @param mu
     * @return
     */
    public static Mean mean(DoubleSequence data, final double mu) {
        int m = data.length();
        double av = Doubles.sum(data) / m;
        double v = Doubles.ssqc(data, mu) / m;
        return new Mean(av, mu, v, m);
    }

    public StatisticalTest build() {
        double val = (mean - emean) / Math.sqrt(var / n);
        return new StatisticalTest(new T(n), val, TestType.TwoSided, false);
    }
}
