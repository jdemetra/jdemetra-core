/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.stats.samples;

import demetra.data.Doubles;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class OrderedSampleWithZeroMeanTest {

    public OrderedSampleWithZeroMeanTest() {
    }

    @Test
    public void stressTestCov() {
        int K = 1000000, N = 10000;
        double[] x = new double[N];
        double[] y = new double[N];
        Random rnd = new Random();
        for (int i = 0; i < x.length; ++i) {
            x[i] = rnd.nextDouble() - .5;
            y[i] = rnd.nextDouble() - .5;
        }
        Doubles X = Doubles.ofInternal(x);
        Doubles Y = Doubles.ofInternal(y);
        long t0 = System.currentTimeMillis();
        double z=0;
        for (int i = 0; i < K; ++i) {
            z+=OrderedSampleWithZeroMean.cov(x, y, i % 20);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            z+=OrderedSampleWithZeroMean.covNoMissing(X, Y, i % 20);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            z+=OrderedSampleWithZeroMean.cov(X, Y, i % 20);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

}
