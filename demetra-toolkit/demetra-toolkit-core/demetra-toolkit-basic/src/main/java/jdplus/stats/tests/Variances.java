/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.stats.tests;

import demetra.data.DoubleSeq;
import demetra.stats.ProbabilityType;
import jdplus.dstats.Chi2;
import jdplus.dstats.F;
import jdplus.stats.DescriptiveStatistics;
import jdplus.stats.samples.Sample;
import jdplus.stats.tests.StatisticalTest;
import jdplus.stats.tests.TestType;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Variances {
    
    public StatisticalTest bartlett(Sample[] seqs){
        // compute pooled variance
        double vp=0;
        int k=seqs.length;
        int N=0;
        double q=0;
        double lv=0;
        for (int i=0; i<k; ++i){
            int ni=seqs[i].observationsCount();
            double vi=seqs[i].variance();
            N+=ni;
            double nim1=ni-1;
            vp+=nim1*vi;
            q+=1/nim1;
            lv+=nim1*Math.log(vi);
        }
        vp/=N-k;
        q-=1.0/(N-k);
        double chi=((N-k)*Math.log(vp)-lv)/(1+q/(3*(k-1)));
        return new StatisticalTest(new Chi2(k-1), chi, TestType.Upper, true);
    }
    
    public double cochranC(Sample[] data, int sequence) {
        checkEqualLength(data);
        double num = 0, denom = 0;
        for (int i = 0; i < data.length; ++i) {
            double var = data[i].variance();
            if (i == sequence) {
                num = var;
            }
            denom += var;
        }
        return num / denom;
    }

    public double[] cochranC(Sample[] data) {
        checkEqualLength(data);
        double[] vars = new double[data.length];
        double all = 0;
        for (int i = 0; i < data.length; ++i) {
            double var = data[i].variance();
            all += var;
            vars[i] = var;
        }
        for (int i = 0; i < data.length; ++i) {
            vars[i] /= all;
        }
        return vars;
    }

    /**
     * Computes the critical value
     * @param alpha Significance level (for instance 0.01)
     * @param N Number of data sequences
     * @param n Number of points in each sequence
     * @return Critical value for the Cochran'C test
     */
    public double cochranCriticalValue(double alpha, int N, int n) {
        F f = new F(n - 1, (N - 1) * (n - 1));
        return 1.0 / (1 + (N - 1) / f.getProbabilityInverse(alpha / N, ProbabilityType.Upper));
    }
    
    public OneWayAnova levene(Sample[] samples){
        OneWayAnova anova=new OneWayAnova();
        for (int i=0; i<samples.length; ++i){
            final double ybar=samples[i].mean();
            DoubleSeq z=samples[i].data().fn(x->Math.abs(x-ybar));
            anova.add(new OneWayAnova.Group("g"+(i+1), z));
        }
        return anova;
    }

    public OneWayAnova brownForsythe(Sample[] samples){
        OneWayAnova anova=new OneWayAnova();
        for (int i=0; i<samples.length; ++i){
            final double ybar=DescriptiveStatistics.of(samples[i].data()).getMedian();
            DoubleSeq z=samples[i].data().fn(x->Math.abs(x-ybar));
            anova.add(new OneWayAnova.Group("g"+(i+1), z));
        }
        return anova;
    }

    private void checkEqualLength(Sample[] data) {
        if (data.length < 2) {
            throw new IllegalArgumentException();
        }
        int n = data[0].observationsCount();
        for (int i = 1; i < data.length; ++i) {
            if (data[i].observationsCount() != n) {
                throw new IllegalArgumentException();
            }
        }
    }
}
