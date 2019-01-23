/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.stats.samples;

import demetra.data.DoubleReader;
import demetra.data.DoubleSequence;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class DefaultSample implements Sample {

    private final DoubleSequence data;
    private final double mean, var, se;
    private final boolean meanCorrected;
    private final Population population;

    /**
     * The mean of the sample is sum(data)/data.length = sx/n The variance of
     * the sample is computed differently when the mean of the population is
     * known or not: - unknown population mean: var = (sxx-sx*sx/n)/(n-1) -
     * known population mean mu: var = s(x-mu)(x-mu)/n or, when mu=0, var =
     * sxx/n
     *
     * @param data The sample (no missing values)
     * @param population The underlying population. Might be null, which means
     * that the population is unknown.
     * @return
     */
    public static DefaultSample of(DoubleSequence data, Population population) {
        return new DefaultSample(data, false, population);
    }

    public static DefaultSample ofMeanCorrected(DoubleSequence data) {
        return new DefaultSample(data, true, Population.ZEROMEAN);
    }

    private DefaultSample(DoubleSequence data, boolean meanCorrected, Population population) {
        this.data = data;
        this.meanCorrected = meanCorrected;
        this.population = population == null ? Population.UNKNOWN : population;
        double sx = 0, sxx = 0;
        DoubleReader reader = data.reader();
        int n = data.length();
        for (int i = 0; i < n; ++i) {
            double x = reader.next();
            sx += x;
            sxx += x * x;
        }
        mean = sx / n;
        double mu = this.population.getMean();
        double svar = (sxx - sx * sx / n) / (n - 1);
        se = Math.sqrt(svar);
        if (Double.isNaN(mu)) {
            var = svar;
        } else if (mu == 0) {
            int nc = meanCorrected ? n - 1 : n;
            var = sxx / nc;
        } else {
            var = (sxx - mu * (2 * sx - n * mu)) / n;
        }
    }

    public DoubleSequence data() {
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
        return meanCorrected ? data.length() - 1 : data.length();
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
    public double estimatedVariance() {
        return var;
    }

    /**
     * Sample standard error, computed by sqrt((sxx-sx*sx/n)/(n-1))
     *
     * @return
     */
    @Override
    public double standardError() {
        return se;
    }

}
