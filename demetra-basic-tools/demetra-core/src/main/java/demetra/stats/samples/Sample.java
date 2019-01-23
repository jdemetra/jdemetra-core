/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.stats.samples;

import demetra.data.DoubleSequence;
import demetra.dstats.F;
import demetra.dstats.T;
import demetra.stats.tests.StatisticalTest;
import demetra.stats.tests.TestType;
import java.util.stream.DoubleStream;

/**
 * Basic statistics on a sample. The items of the sample can be retrieved by a
 * stream.
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface Sample {

    public static Sample of(DoubleSequence data) {
        return DefaultSample.of(data, Population.UNKNOWN);
    }

    public static Sample ofResiduals(DoubleSequence data, boolean meanCorrected) {
        return meanCorrected ? DefaultSample.ofMeanCorrected(data)
                : DefaultSample.of(data, Population.ZEROMEAN);
    }

    /**
     *
     * @param size Size of the sample
     * @param sampleMean Mean of the sample
     * @param sampleStandardError
     * @param estimatedVariance Estimated variance of the underlying population.
     * The estimation is usually based on the sample, but it could be estimated
     * otherwise. When the variance of the population is known, the
     * populationVariance
     * @param population
     * @return
     */
    public static Sample of(int size, double sampleMean, double sampleStandardError, double estimatedVariance, Population population) {
        return new Sample() {
            @Override
            public double mean() {
                return sampleMean;
            }

            @Override
            public double standardError() {
                return sampleStandardError;
            }

            @Override
            public int size() {
                return size;
            }

            @Override
            public double estimatedVariance() {
                return estimatedVariance;
            }

            @Override
            public Population population() {
                return population;
            }
        };
    }

    double mean();

    double estimatedVariance();

    double standardError();

    int size();

    Population population();

    public static StatisticalTest compareVariances(Sample s0, Sample s1) {
        F f = new F(s1.size() - 1, s0.size() - 1);
        return new StatisticalTest(f, s1.estimatedVariance() / s0.estimatedVariance(), TestType.Upper, false);
    }

    public static StatisticalTest compareMeans(Sample s0, Sample s1, boolean samevar) {
        int n0 = s0.size(), n1 = s1.size();
        double v0 = s0.estimatedVariance(), v1 = s1.estimatedVariance();
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
