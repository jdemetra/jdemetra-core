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
import demetra.data.DoubleSeq;
import demetra.stats.StatException;
import demetra.stats.StatisticalTest;
import jdplus.stats.samples.Moments;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@BuilderPattern(StatisticalTest.class)
public class JarqueBera {

    private int k;
    private boolean corrected;
    private double skewness, kurtosis;

    private final DoubleSeq x;

    public JarqueBera(DoubleSeq data) {
        this.x = data;
    }

    /**
     *
     * @param k Correction of the degree of freedom. For instance, number of
     * regression variables
     * if the data correspond to the residuals of a linear model.
     * @return
     */
    public JarqueBera degreeOfFreedomCorrection(int k) {
        this.k = k;
        return this;
    }

    public JarqueBera correctionForSample() {
        corrected = true;
        return this;
    }

    public StatisticalTest build() {
        double n = x.length();
        if (n < 4) {
            throw new StatException("Invalid test: not enough observations");
        }
        double m = Moments.mean(x), v = Moments.variance(x, m, corrected);
        skewness = Moments.skewness(x, m, v, corrected);
        kurtosis = Moments.excessKurtosis(x, m, v, corrected);
        double val = (n - k) * (skewness * skewness / 6 + (kurtosis) * (kurtosis) / 24);
        Chi2 chi = new Chi2(2);
        return TestsUtility.testOf(val, chi, TestType.Upper);
    }

    public double getSkewness() {
        return skewness;
    }

    public double getKurtosis() {
        return kurtosis;
    }

}
