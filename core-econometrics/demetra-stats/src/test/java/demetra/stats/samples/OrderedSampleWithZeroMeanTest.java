/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.stats.samples;

import demetra.data.DataBlock;
import java.util.Random;
import java.util.function.IntToDoubleFunction;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import demetra.data.DoubleSequence;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class OrderedSampleWithZeroMeanTest {

    public OrderedSampleWithZeroMeanTest() {
    }

    @Test
    public void testAutocovariances() {
        double[] x = new double[100];
        Random rnd = new Random();
        DataBlock X = DataBlock.ofInternal(x);
        X.set(rnd::nextDouble);

        OrderedSampleWithZeroMean sample = OrderedSampleWithZeroMean.of(X);

        IntToDoubleFunction fn = sample.autoCovarianceFunction();
        for (int i = 0; i < 20; ++i) {
            assertEquals(fn.applyAsDouble(i), ec.tstoolkit.data.DescriptiveStatistics.cov(i, x), 1e-9);
        }
    }

    @Test
    //@Ignore
    public void stressTestAutocovariances() {
        int N=200, K=1000000;
        double[] x = new double[N];
        Random rnd = new Random();
        DataBlock X = DataBlock.ofInternal(x);
        X.set(rnd::nextDouble);

        long t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            OrderedSampleWithZeroMean sample = OrderedSampleWithZeroMean.of(X);
            IntToDoubleFunction fn = sample.autoCovarianceFunction();
            double z = 0;
            for (int i = 0; i < 20; ++i) {
                z += fn.applyAsDouble(i);
            }
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            OrderedSampleWithZeroMean sample = OrderedSampleWithZeroMean.of(X);
            IntToDoubleFunction fn = sample.autoCovarianceFunction();
            double z = 0;
            for (int i = 0; i < 20; ++i) {
                z += ec.tstoolkit.data.DescriptiveStatistics.cov(i, x);
            }
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

}
