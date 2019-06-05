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
package jdplus.stats.samples;

import demetra.data.DoubleSeqCursor;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class DefaultSample implements Sample {

    private final DoubleSeq data;
    private final double mean, var, ssq;
    private final Population population;

    /**
     * @param data The sample (no missing values)
     * @param population The underlying population. Might be null, which means
     * that the population is unknown.
     */
    public DefaultSample(DoubleSeq data, Population population) {
        this.data = data;
        this.population = population == null ? Population.UNKNOWN : population;
        double sx = 0, sxx = 0;
        DoubleSeqCursor reader = data.cursor();
        int n = data.length();
        for (int i = 0; i < n; ++i) {
            double x = reader.getAndNext();
            sx += x;
            sxx += x * x;
        }
        mean = sx / n;
        var = (sxx - sx * sx / n) / (n - 1);
        ssq = sxx;
    }

    public DoubleSeq data() {
        return data;
    }

    /**
     * Usual sample mean
     *
     * @return
     */
    @Override
    public double mean() {
        return mean;
    }

    @Override
    public int size() {
        return data.length();
    }

    @Override
    public Population population() {
        return population;
    }

    /**
     * Variance of the sample, considering the mean of the population when it is
     * known. For instance, when the mean of the population is 0 (residuals),
     * the variance is the mean square error.
     *
     * @return
     */
    @Override
    public double variance() {
        return var;
    }
    
    public double ssq(){
        return ssq;
    }

}
