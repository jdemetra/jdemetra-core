/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.stats.samples;

import demetra.data.DoubleSeqCursor;
import demetra.dstats.F;
import demetra.dstats.T;
import demetra.stats.tests.StatisticalTest;
import demetra.stats.tests.TestType;
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
