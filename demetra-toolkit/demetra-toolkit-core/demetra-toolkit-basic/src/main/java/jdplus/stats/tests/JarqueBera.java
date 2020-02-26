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

import demetra.design.BuilderPattern;
import demetra.design.Development;
import jdplus.dstats.Chi2;
import demetra.data.DoubleSeq;
import demetra.stats.StatException;
import jdplus.stats.DescriptiveStatistics;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@BuilderPattern(StatisticalTest.class)
public class JarqueBera {
    
    private int nregs;
    private boolean corrected;
    
    private final DescriptiveStatistics stats;
    
    public JarqueBera(DoubleSeq data) {
        this.stats = DescriptiveStatistics.of(data);
    }
    
    public JarqueBera(DescriptiveStatistics stats) {
        this.stats = stats;
    }

    /**
     *
     * @param nregs
     * @return Number of regression variables
     */
    public JarqueBera regressionCount(int nregs) {
        this.nregs = nregs;
        return this;
    }
    
    public JarqueBera correctionForSample() {
        corrected = true;
        return this;
    }
    
    
    public StatisticalTest build() {
        double n = stats.getObservationsCount()-nregs;
        if (n<4)
            throw new StatException("Invalid test: not enough observations");
        double s = stats.getSkewness();
        double k = stats.getKurtosis() - 3.0;
        if (corrected) {
            s *= Math.sqrt(n * (n - 1)) / (n - 2);
            k = (n - 1) / ((n - 2) * (n - 3)) * ((n + 1) * k + 6);
        }
        double val = n * (s * s / 6 + k * k / 24);
        Chi2 chi = new Chi2(2);
        return new StatisticalTest(chi, val, TestType.Upper, true);
    }
    
}
