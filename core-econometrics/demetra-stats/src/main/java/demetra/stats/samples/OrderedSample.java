/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.stats.samples;

import demetra.data.DoubleSequence;
import java.util.function.IntToDoubleFunction;
import java.util.stream.DoubleStream;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface OrderedSample extends Sample {

    double autoCovariance(int lag);

    DoubleSequence data();

    default IntToDoubleFunction autoCorrelationFunction() {
        final double v = variance();
        return i -> autoCovariance(i) / v;
    }

    default IntToDoubleFunction autoCovarianceFunction() {
        return i -> autoCovariance(i);
    }

    @Override
    default double variance() {
        return autoCovariance(0);
    }

    @Override
    default DoubleStream all() {
        return data().stream();
    }

}
