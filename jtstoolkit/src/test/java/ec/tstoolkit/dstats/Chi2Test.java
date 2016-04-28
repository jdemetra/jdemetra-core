/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.dstats;

import ec.tstoolkit.random.MersenneTwister;
import ec.tstoolkit.random.StochasticRandomizer;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class Chi2Test {

    public Chi2Test() {
    }

    @Test
    public void testProbinverse() {
        Chi2 dist = new Chi2();
        dist.setDegreesofFreedom(5);
        double step = 0.00001;
        for (double p = step; p <= 1 - step; p += step) {
            double x = dist.getProbabilityInverse(p, ProbabilityType.Lower);
            double np = dist.getProbability(x, ProbabilityType.Lower);
            double nx = dist.getProbabilityInverse(np, ProbabilityType.Lower);
            assertTrue(Math.abs(x - nx) < 1e-6);
        }
    }

    @Test
    @Ignore
    public void testRandoms() {
        double s = 0;
        int N = 10000000;
        long t0 = System.currentTimeMillis();
        Chi2 chi2 = new Chi2(20);
        MersenneTwister rng = MersenneTwister.fromSystemNanoTime();
        for (int i = 0; i < N; ++i) {
            s += chi2.random(rng);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("New");
        System.out.println(t1 - t0);
        System.out.println(s / N);
    }

    @Test
    @Ignore
    public void testRandomsOld() {
        double s = 0;
        int N = 1000000;
        long t0 = System.currentTimeMillis();
        MersenneTwister rng = MersenneTwister.fromSystemNanoTime();
        for (int i = 0; i < N; ++i) {
            s += StochasticRandomizer.chi2(rng, 20);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("Old");
        System.out.println(t1 - t0);
        System.out.println(s / N);
    }
}
