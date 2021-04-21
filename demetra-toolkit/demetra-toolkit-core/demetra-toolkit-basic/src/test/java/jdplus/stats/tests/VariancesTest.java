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
package jdplus.stats.tests;

import demetra.data.DoubleSeq;
import java.util.Random;
import jdplus.dstats.Chi2;
import demetra.dstats.Distribution;
import jdplus.dstats.Normal;
import jdplus.dstats.Uniform;
import jdplus.random.MersenneTwister;
import demetra.dstats.RandomNumberGenerator;
import demetra.stats.StatisticalTest;
import jdplus.stats.DescriptiveStatistics;
import jdplus.stats.samples.Population;
import jdplus.stats.samples.Sample;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class VariancesTest {

    public VariancesTest() {
    }

    @Test
    public void testRandom1() {
        Random rnd = new Random(0);

        Sample[] samples = new Sample[12];
        Population population = Population.builder()
                .mean(0)
                .normal(true)
                .build();
        for (int i = 0; i < 12; ++i) {
            double[] data = new double[20];
            for (int j = 0; j < data.length; ++j) {
                data[j] = rnd.nextGaussian();
            }
            samples[i] = Sample.build(DoubleSeq.of(data), false, population);
        }

        StatisticalTest bartlett = Variances.bartlett(samples);
        assertTrue(bartlett.getPvalue() > 0.05);
        StatisticalTest levene = Variances.levene(samples).build();
        assertTrue(levene.getPvalue() > 0.05);
        StatisticalTest brown = Variances.brownForsythe(samples).build();
        assertTrue(brown.getPvalue() > 0.05);
        System.out.print("Bartlett:\t");
        System.out.println(bartlett.getPvalue());
        System.out.print("Levene:\t");
        System.out.println(levene.getPvalue());
        System.out.print("BrownForsythe:\t");
        System.out.println(brown.getPvalue());
    }

    @Test
    public void testBartlett2() {
        Random rnd = new Random(0);

        Sample[] samples = new Sample[12];
        Population population = Population.builder()
                .mean(0)
                .normal(true)
                .build();
        for (int i = 0; i < 12; ++i) {
            double[] data = new double[20];
            for (int j = 0; j < data.length; ++j) {
                data[j] = rnd.nextGaussian() * (i + 1);
            }
            samples[i] = Sample.build(DoubleSeq.of(data), false, population);
        }
        StatisticalTest bartlett = Variances.bartlett(samples);
        assertTrue(bartlett.getPvalue() < 0.01);
        StatisticalTest levene = Variances.levene(samples).build();
        assertTrue(levene.getPvalue() < 0.01);
        StatisticalTest brown = Variances.brownForsythe(samples).build();
        assertTrue(brown.getPvalue() < 0.01);
        System.out.print("Bartlett:\t");
        System.out.println(bartlett.getPvalue());
        System.out.print("Levene:\t");
        System.out.println(levene.getPvalue());
        System.out.print("BrownForsythe:\t");
        System.out.println(brown.getPvalue());
    }

    @Test
    public void testMonthly() {
        assertEquals(Variances.cochranCriticalValue(0.05, 12, 9), 0.2187, 1e-4);
    }

    @Test
    public void testCochran() {
        Random rnd = new Random(0);

        Sample[] samples = new Sample[12];
        Population population = Population.builder()
                .mean(0)
                .normal(true)
                .build();
        for (int i = 0; i < 12; ++i) {
            double[] data = new double[20];
            for (int j = 0; j < data.length; ++j) {
                data[j] = rnd.nextGaussian();
            }
            samples[i] = Sample.build(DoubleSeq.of(data), false, population);
        }

        double[] all = Variances.cochranC(samples);
        double cv = Variances.cochranCriticalValue(0.01, 12, 20);
        for (int i = 0; i < all.length; ++i) {
            assertTrue(all[i] < cv);
        }
    }

    @Test
    public void testCochran2() {
        Random rnd = new Random(0);

        Sample[] samples = new Sample[12];
        Population population = Population.builder()
                .mean(0)
                .normal(true)
                .build();
        for (int i = 0; i < 12; ++i) {
            double[] data = new double[20];
            for (int j = 0; j < data.length; ++j) {
                data[j] = rnd.nextGaussian() * (i + 1);
            }
            samples[i] = Sample.build(DoubleSeq.of(data), false, population);
        }
        double[] all = Variances.cochranC(samples);
        double cv = Variances.cochranCriticalValue(0.01, 12, 20);
        assertTrue(DoubleSeq.of(all).max() > cv);
    }

    public static void main(String[] arg) {
        RandomNumberGenerator rng=MersenneTwister.fromSystemNanoTime();
        Normal n=new Normal();
        Uniform u=new Uniform(-1,1);
        Chi2 chi2=new Chi2(4); 
        int K = 10000, M=100;
        simulate(n, rng, K, M);
        simulate(u, rng, K, M);
        simulate(chi2, rng, K, M);
    }
    
    public static void simulate(Distribution dist, RandomNumberGenerator rng, int K, int M){
        Random rnd = new Random(0);
        double[] B = new double[K];
        double[] L = new double[K];
        double[] BF = new double[K];

        for (int k = 0; k < K; ++k) {

            Sample[] samples = new Sample[12];
            for (int i = 0; i < 12; ++i) {
                double[] data = new double[M];
                for (int j = 0; j < data.length; ++j) {
                    data[j] = dist.random(rng);
                }
                samples[i] = Sample.build(DoubleSeq.of(data), false, null);
            }

            StatisticalTest bartlett = Variances.bartlett(samples);
            StatisticalTest levene = Variances.levene(samples).build();
            StatisticalTest brown = Variances.brownForsythe(samples).build();

            B[k] = bartlett.getValue();
            L[k] = levene.getValue();
            BF[k] = brown.getValue();
        }

        DescriptiveStatistics D = DescriptiveStatistics.of(DoubleSeq.of(B));
        int N = 100;
        double[] n = new double[N];
        double step = 24.0 / N;
        for (int i = 0; i < N; ++i) {
            n[i] = 1.0/K*D.countBetween(i * step, (i + 1) * step);
        }
        System.out.println(DoubleSeq.of(n));
        D = DescriptiveStatistics.of(DoubleSeq.of(L));
        step = 2.5 / N;
        for (int i = 0; i < N; ++i) {
            n[i] = 1.0/K*D.countBetween(i * step, (i + 1) * step);
        }
        System.out.println(DoubleSeq.of(n));
        D = DescriptiveStatistics.of(DoubleSeq.of(BF));
        step = 2.5 / N;
        for (int i = 0; i < N; ++i) {
            n[i] = 1.0/K*D.countBetween(i * step, (i + 1) * step);
        }
        System.out.println(DoubleSeq.of(n));
    }

}
