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
package jdplus.stats.samples;

import demetra.data.DoubleSeqCursor;
import demetra.data.DoubleSeq;
import jdplus.dstats.F;
import jdplus.dstats.T;
import jdplus.stats.tests.StatisticalTest;
import jdplus.stats.tests.TestType;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.Accessors(fluent = true)
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@lombok.Value
public class Sample {

    private DoubleSeq data;
    private int observationsCount;
    private double mean, variance;
    private Population population;
    
    public double standardDeviation(){
        return Math.sqrt(variance);
    }

    /**
     * @param sample The given sample
     * @param skipMissing Should be false if the sample doesn't contain any
     * missing value (optimization option)
     * @param population Population. Could be null if unknown
     * @return
     */
    public static Sample build(DoubleSeq sample, boolean skipMissing, Population population) {
        if (!skipMissing) {
            return buildNoMissing(sample, population == null ? Population.UNKNOWN : population);
        } else {
            return buildMissing(sample, population == null ? Population.UNKNOWN : population);
        }
    }
    
    public static Sample ofResiduals(DoubleSeq residuals){
        return buildNoMissing(residuals, Population.ZEROMEAN);
    }

    private static Sample buildNoMissing(DoubleSeq sample, Population population) {
        DoubleSeqCursor reader = sample.cursor();
        int n = sample.length();
        double populationMean = population.getMean();
        if (Double.isNaN(populationMean)) {
            // slower but safer solution
            double m = sample.average();
            double ssqc=sample.ssqc(m);
            double v = ssqc / (n - 1);
            return new Sample(sample, n, m, v, population);
        } else if (populationMean == 0) {
            double sx = 0, sxx = 0;
            for (int i = 0; i < n; ++i) {
                double x = reader.getAndNext();
                sx += x;
                sxx += x * x;
            }
            double m = sx / n;
            double v = sxx / n;
            return new Sample(sample, n, m, v, population);
        } else {
            double sx = 0, sxxc = 0;
            for (int i = 0; i < n; ++i) {
                double x = reader.getAndNext();
                double xc = x - populationMean;
                sx += x;
                sxxc += xc * xc;
            }
            double m = sx / n;
            double v = sxxc / n;
            return new Sample(sample, n, m, v, population);
        }
    }

    private static Sample buildMissing(DoubleSeq sample, Population population) {
        DoubleSeqCursor reader = sample.cursor();
        int n = sample.length();
        double populationMean = population.getMean();
        if (Double.isNaN(populationMean)) {
            // slower but safer solution
            int nobs = 0;
            double sx = 0;
            for (int i = 0; i < n; ++i) {
                double x = reader.getAndNext();
                if (Double.isFinite(x)) {
                    sx += x;
                    ++nobs;
                }
            }
            double m = sx / nobs;
            double sxxc=0;
            reader.moveTo(0);
            for (int i = 0; i < n; ++i) {
                double x = reader.getAndNext();
                if (Double.isFinite(x)) {
                    double z=x-m;
                    sxxc+=z*z;
                }
            }
            double v = sxxc / (nobs - 1);
            return new Sample(sample, nobs, m, v, population);
        } else if (populationMean == 0) {
            int nobs = 0;
            double sx = 0, sxx = 0;
            for (int i = 0; i < n; ++i) {
                double x = reader.getAndNext();
                if (Double.isFinite(x)) {
                    sx += x;
                    sxx += x * x;
                    ++nobs;
                }
            }
            double m = sx / nobs;
            double v = sxx / nobs;
            return new Sample(sample, nobs, m, v, population);
        } else {
            int nobs = 0;
            double sx = 0, sxxc = 0;
            for (int i = 0; i < n; ++i) {
                double x = reader.getAndNext();
                if (Double.isFinite(x)) {
                    double xc = x - populationMean;
                    sx += x;
                    sxxc += xc * xc;
                }
            }
            double m = sx / nobs;
            double v = sxxc / nobs;
            return new Sample(sample, nobs, m, v, population);
        }

    }

    public static StatisticalTest compareVariances(Sample s0, Sample s1) {
        F f = new F(s1.observationsCount - 1, s0.observationsCount - 1);
        return new StatisticalTest(f, s1.variance / s0.variance, TestType.Upper, false);
    }

    public static StatisticalTest compareMeans(Sample s0, Sample s1, boolean samevar) {
        int n0 = s0.observationsCount, n1 = s1.observationsCount;
        double v0 = s0.variance, v1 = s1.variance;
        double t;
        int df;
        if (samevar) {
            double v = (v0 * (n0 - 1) + v1 * (n1 - 1)) / (n0 + n1 - 2);
            t = (s1.mean - s0.mean) / Math.sqrt(v / n0 + v / n1);
            df = n0 + n1 - 2;
        } else {
            t = (s1.mean - s0.mean) / Math.sqrt(v0 / n0 + v1 / n1);
            df = Math.min(n0 - 1, n1 - 1);
//            double f=v1/v0;
//            double z=1.0/n0+f/n1;
//            df=(z*z)/(1.0/(n0*n0*(n0-1))+f*f/(n1*n1*(n1-1)));
        }
        T dist = new T(df);
        return new StatisticalTest(dist, t, TestType.TwoSided, false);
    }
}
