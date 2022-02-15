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
package jdplus.linearmodel;

import jdplus.stats.linearmodel.Ols;
import jdplus.stats.linearmodel.LinearModel;
import jdplus.stats.linearmodel.LeastSquaresResults;
import jdplus.stats.linearmodel.HeteroskedasticityTest;
import demetra.data.DoubleSeq;
import java.util.Random;
import jdplus.data.DataBlock;
import jdplus.dstats.Chi2;
import demetra.dstats.Distribution;
import jdplus.dstats.Normal;
import jdplus.dstats.Uniform;
import jdplus.math.matrices.FastMatrix;
import jdplus.random.MersenneTwister;
import demetra.dstats.RandomNumberGenerator;
import demetra.stats.StatisticalTest;
import demetra.stats.TestType;
import jdplus.stats.DescriptiveStatistics;
import jdplus.stats.tests.TestsUtility;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class HeteroskedasticityTestTest {

    public HeteroskedasticityTestTest() {
    }

    @Test
    public void testRandom() {
        Random rnd = new Random(0);
        int N = 100, NX = 5;

        FastMatrix X = FastMatrix.make(N, NX);
        DataBlock y = DataBlock.make(N);
        y.set(rnd::nextDouble);

        X.set((r, c) -> rnd.nextDouble());
        LinearModel lm = LinearModel.builder()
                .y(y)
                .addX(X)
                .meanCorrection(true)
                .build();

        LeastSquaresResults lsr = Ols.compute(lm);

        StatisticalTest test = HeteroskedasticityTest.builder(lsr)
                .build();

        assertTrue(test.getPvalue() > 1e-1);
//        System.out.println(test.getPValue());

        StatisticalTest test2 = HeteroskedasticityTest.builder(lsr)
                .fisherTest(true)
                .build();

        assertTrue(test2.getPvalue() > 1e-1);
//        System.out.println(test2.getPValue());

        StatisticalTest test3 = HeteroskedasticityTest.builder(lsr)
                .type(HeteroskedasticityTest.Type.White)
                .build();

        assertTrue(test3.getPvalue() > 1e-1);
//        System.out.println(test3.getPValue());

        StatisticalTest test4 = HeteroskedasticityTest.builder(lsr)
                .studentizedResiduals(true)
                .build();

        assertTrue(test4.getPvalue() > 1e-1);
//        System.out.println(test4.getPValue());

        StatisticalTest test5 = HeteroskedasticityTest.builder(lsr)
                .studentizedResiduals(true)
                .fisherTest(true)
                .build();

        assertTrue(test5.getPvalue() > 1e-1);
//        System.out.println(test5.getPValue());

        StatisticalTest test6 = HeteroskedasticityTest.builder(lsr)
                .studentizedResiduals(true)
                .type(HeteroskedasticityTest.Type.White)
                .build();

        assertTrue(test6.getPvalue() > 1e-1);
//        System.out.println(test6.getPValue());
    }

    @Test
    public void testRandom2() {
        Random rnd = new Random(0);
        int N = 100, NX = 5;

        FastMatrix X = FastMatrix.make(N, NX);
        DataBlock y = DataBlock.make(N);
        y.set(i -> (i + 1) * rnd.nextDouble());

        X.set((r, c) -> (r + 1) * rnd.nextDouble());
        LinearModel lm = LinearModel.builder()
                .y(y)
                .addX(X)
                .meanCorrection(true)
                .build();

        LeastSquaresResults lsr = Ols.compute(lm);

        StatisticalTest test = HeteroskedasticityTest.builder(lsr)
                .build();

        assertTrue(test.getPvalue() < 1e-2);
//        System.out.println(test.getPValue());

        StatisticalTest test2 = HeteroskedasticityTest.builder(lsr)
                .fisherTest(true)
                .build();

        assertTrue(test2.getPvalue() < 1e-2);
//        System.out.println(test2.getPValue());

        StatisticalTest test3 = HeteroskedasticityTest.builder(lsr)
                .type(HeteroskedasticityTest.Type.White)
                .build();

        assertTrue(test3.getPvalue() < 1e-2);
//        System.out.println(test3.getPValue());

        StatisticalTest test4 = HeteroskedasticityTest.builder(lsr)
                .studentizedResiduals(true)
                .build();

        assertTrue(test4.getPvalue() < 1e-2);
//        System.out.println(test4.getPValue());

        StatisticalTest test5 = HeteroskedasticityTest.builder(lsr)
                .studentizedResiduals(true)
                .fisherTest(true)
                .build();

        assertTrue(test5.getPvalue() < 1e-2);
//        System.out.println(test5.getPValue());

        StatisticalTest test6 = HeteroskedasticityTest.builder(lsr)
                .studentizedResiduals(true)
                .type(HeteroskedasticityTest.Type.White)
                .build();

        assertTrue(test6.getPvalue() < 1e-2);
//        System.out.println(test6.getPValue());
    }

    public static void main(String[] arg) {
        RandomNumberGenerator rng = MersenneTwister.fromSystemNanoTime();
        Normal n = new Normal();
        Uniform u = new Uniform(-1, 1);
        Chi2 chi2 = new Chi2(4);
        int K = 10000, M = 50;
        simulate(n, rng, K, M);
        simulate(u, rng, K, M);
        simulate(chi2, rng, K, M);
    }

    public static void simulate(Distribution dist, RandomNumberGenerator rng, int K, int M) {
        double[] W = new double[K];
        double[] Ws = new double[K];
        double[] BP = new double[K];
        double[] BPs = new double[K];

        for (int k = 0; k < K; ++k) {

            FastMatrix X = FastMatrix.make(M, 4);
            DataBlock y = DataBlock.make(M);
            y.set(i->rng.nextDouble());

            X.set((r, c) -> rng.nextDouble());
            LinearModel lm = LinearModel.builder()
                    .y(y)
                    .addX(X)
                    .meanCorrection(true)
                    .build();

            LeastSquaresResults lsr = Ols.compute(lm);

            StatisticalTest bp = HeteroskedasticityTest.builder(lsr)
                    .build();
            StatisticalTest bps = HeteroskedasticityTest.builder(lsr)
                    .studentizedResiduals(true)
                    .build();

            BP[k] = bp.getValue();
            BPs[k] = bps.getValue();

            StatisticalTest w = HeteroskedasticityTest.builder(lsr)
                    .type(HeteroskedasticityTest.Type.White)
                    .build();
            StatisticalTest ws = HeteroskedasticityTest.builder(lsr)
                    .type(HeteroskedasticityTest.Type.White)
                    .studentizedResiduals(true)
                    .build();

            W[k] = w.getValue();
            Ws[k] = ws.getValue();
        }

        DescriptiveStatistics D = DescriptiveStatistics.of(DoubleSeq.of(BP));
        int N = 100;
        double[] n = new double[N];
        double step = 8.0 / N;
        for (int i = 0; i < N; ++i) {
            n[i] = 1.0 / K * D.countBetween(i * step, (i + 1) * step);
        }
        System.out.println(DoubleSeq.of(n));
        D = DescriptiveStatistics.of(DoubleSeq.of(BPs));
        step = 8.0 / N;
        for (int i = 0; i < N; ++i) {
            n[i] = 1.0 / K * D.countBetween(i * step, (i + 1) * step);
        }
        System.out.println(DoubleSeq.of(n));
        D = DescriptiveStatistics.of(DoubleSeq.of(W));
        step = 30.0 / N;
        for (int i = 0; i < N; ++i) {
            n[i] = 1.0 / K * D.countBetween(i * step, (i + 1) * step);
        }
        System.out.println(DoubleSeq.of(n));
        D = DescriptiveStatistics.of(DoubleSeq.of(Ws));
        step = 30.0 / N;
        for (int i = 0; i < N; ++i) {
            n[i] = 1.0 / K * D.countBetween(i * step, (i + 1) * step);
        }
        System.out.println(DoubleSeq.of(n));
    }

}
