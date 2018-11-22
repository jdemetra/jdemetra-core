/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.stats.samples;

import demetra.data.DoubleReader;
import java.util.stream.DoubleStream;
import demetra.data.DoubleSequence;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class DefaultSample implements Sample {

    private final DoubleSequence data;
    private final double mean, var;
    private final Population population;

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
    }

    @Override
    public double mean() {
        return mean;
    }

    public DoubleSequence data() {
        return data;
    }

    @Override
    public int size() {
        return data.length();
    }

    @Override
    public Population population() {
        return population;
    }

    @Override
    public DoubleStream all() {
        return data.stream();
    }

    @Override
    public double variance() {
        return var;
    }

}
