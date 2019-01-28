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
    private final double mean, var, ssq;
    private final Population population;

    /**
     * @param data The sample (no missing values)
     * @param population The underlying population. Might be null, which means
     * that the population is unknown.
     */
    public DefaultSample(DoubleSequence data, Population population) {
        this.data = data;
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
        var = (sxx - sx * sx / n) / (n - 1);
        ssq = sxx;
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
