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
import jdplus.dstats.Normal;
import jdplus.dstats.T;

/**
 * Test the mean of a sample. H0: mean(sample) == mean(population), H1:
 * mean(sample) != mean(population)
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@BuilderPattern(StatisticalTest.class)
public class SampleMean {

    public static final double SMALL = 1e-38;

    private final double sampleMean;
    private final int sampleSize;
    private double populationMean, populationVariance;
    private int sampleSizeForVariance;
    private boolean normalPopulation;

    public SampleMean(final double sampleMean, final int sampleSize) {
        this.sampleMean = sampleMean;
        this.sampleSize = sampleSize;
    }

    public SampleMean populationMean(double value) {
        this.populationMean = value;
        return this;
    }

    public SampleMean populationVariance(double value) {
        this.populationVariance = value;
        this.sampleSizeForVariance = 0;
        return this;
    }

    public SampleMean normalDistribution(boolean value) {
        this.normalPopulation=value;
        return this;
    }
    /**
     *
     * @param value
     * @param sampleSize The sample size is the size of the sample for
     * estimating the variance, which could be different of the size used for
     * the mean
     *
     * @return
     */
    public SampleMean estimatedPopulationVariance(double value, int sampleSize) {
        this.populationVariance = value;
        this.sampleSizeForVariance = sampleSize;
        return this;
    }

    /**
     * @return Normal distribution if the variance is known, T(n-1) if the
     * variance is estimated using the sample.
     */
    public StatisticalTest build() {
        if (this.populationVariance == 0) {
            throw new java.lang.IllegalStateException("undefined population variance");
        }
        double val = (sampleMean - populationMean) / Math.sqrt(populationVariance / sampleSize);
        if (sampleSizeForVariance>0) {
            return new StatisticalTest(new T(sampleSizeForVariance - 1), val, TestType.TwoSided, !normalPopulation);
        } else {
            return new StatisticalTest(new Normal(), val, TestType.TwoSided, !normalPopulation);
        }
    }

 }
