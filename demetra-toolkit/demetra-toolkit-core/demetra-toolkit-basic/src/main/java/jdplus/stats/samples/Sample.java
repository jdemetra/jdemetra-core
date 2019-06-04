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
import jdplus.dstats.F;
import jdplus.dstats.T;
import jdplus.stats.tests.StatisticalTest;
import jdplus.stats.tests.TestType;
import java.util.stream.DoubleStream;
import demetra.data.DoubleSeq;

/**
 * Basic statistics on a sample. The items of the sample can be retrieved by a
 * stream.
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface Sample {

    public static Sample of(DoubleSeq data) {
        return new DefaultSample(data, Population.UNKNOWN);
    }

    public static Sample ofResiduals(DoubleSeq data) {
        double sx = 0, sxx = 0;
        DoubleSeqCursor reader = data.cursor();
        int n = data.length();
        for (int i = 0; i < n; ++i) {
            double x = reader.getAndNext();
            sx += x;
            sxx += x * x;
        }
        return of(n, sx/n, sxx/n, Population.ZEROMEAN);
    }

    /**
     *
     * @param size Size of the sample
     * @param sampleMean Mean of the sample
     * @param sampleVariance 
     * @param population
     * @return
     */
    public static Sample of(int size, double sampleMean, double sampleVariance, Population population) {
        return new Sample() {
            
            @Override
            public int size() {
                return size;
            }
            @Override
            public double mean() {
                return sampleMean;
            }

            @Override
            public double variance() {
                return sampleVariance;
            }

            @Override
            public Population population() {
                return population;
            }
        };
    }

    int size();
    
    double mean();

    double variance();

    Population population();

    public static StatisticalTest compareVariances(Sample s0, Sample s1) {
        F f = new F(s1.size() - 1, s0.size() - 1);
        return new StatisticalTest(f, s1.variance() / s0.variance(), TestType.Upper, false);
    }

    public static StatisticalTest compareMeans(Sample s0, Sample s1, boolean samevar) {
        int n0 = s0.size(), n1 = s1.size();
        double v0 = s0.variance(), v1 = s1.variance();
        double t;
        int df;
        if (samevar) {
            double v = (v0 * (n0 - 1) + v1 * (n1 - 1)) / (n0 + n1 - 2);
            t = (s1.mean() - s0.mean()) / Math.sqrt(v / n0 + v / n1);
            df = n0 + n1 - 2;
        } else {
            t = (s1.mean() - s0.mean()) / Math.sqrt(v0 / n0 + v1 / n1);
            df = Math.min(n0 - 1, n1 - 1);
//            double f=v1/v0;
//            double z=1.0/n0+f/n1;
//            df=(z*z)/(1.0/(n0*n0*(n0-1))+f*f/(n1*n1*(n1-1)));
        }
        T dist = new T(df);
        return new StatisticalTest(dist, t, TestType.TwoSided, false);
    }

}
