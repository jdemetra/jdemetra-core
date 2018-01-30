/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.tests;

import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.dstats.Normal;
import demetra.linearmodel.LeastSquaresResults;
import demetra.linearmodel.LinearModel;
import demetra.linearmodel.Ols;
import demetra.maths.matrices.Matrix;
import demetra.random.MersenneTwister;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class AugmentedDikkeyFullerTestTest {
    
    public AugmentedDikkeyFullerTestTest() {
    }

    static final int K = 50;


    @Test
    @Ignore
    public void testADF() {
        int N = 100000;
        double[] T = new double[N];
        for (int i = 0; i < N; ++i) {
            DoubleSequence data = test(600);
            AugmentedDikkeyFullerTest adf = new AugmentedDikkeyFullerTest();
            adf.setK(1);
            adf.setConstant(true);
            adf.setTrend(true);
            adf.test(data);
            T[i] = adf.getT();
        }
        Arrays.sort(T);
        int n01 = N / 100, n05 = N / 20, n10 = N / 10;
        System.out.println((T[n01] + T[n01 - 1]) / 2);
        System.out.println((T[n05] + T[n05 - 1]) / 2);
        System.out.println((T[n10] + T[n10 - 1]) / 2);
    }

    private final static int R = 1000000, S = 500;

    @Test
    @Ignore
    public void testSurface() {
        int M = 30;
        int[] L = new int[M];
        L[0] = 30;
        for (int i = 1; i < M; ++i) {
            int lprev = L[i - 1];
            if (lprev < 50) {
                L[i] = lprev + 5;
            } else if (lprev < 100) {
                L[i] = lprev + 10;
            } else if (lprev < 200) {
                L[i] = lprev + 20;
            } else if (lprev < 500) {
                L[i] = lprev + 50;
            } else {
                L[i] = lprev + 100;
            }
        }
        Matrix X = Matrix.make(M * S, 3);
        Matrix Y = Matrix.make(M * S, 9);
        for (int i = 0, k = 0; i < M; ++i) {
            for (int j = 0; j < S; ++j, ++k) {
                double[] pct = simulate(R, L[i], false, false);
                Y.row(k).range(0, 3).copyFrom(pct, 0);
                pct = simulate(R, L[i], true, false);
                Y.row(k).range(3, 6).copyFrom(pct, 0);
                pct = simulate(R, L[i], true, true);
                Y.row(k).range(6, 9).copyFrom(pct, 0);
                double t = L[i];
                X.set(k, 0, 1 / t);
                X.set(k, 1, 1 / (t * t));
                X.set(k, 2, 1 / (t * t * t));
            }
        }
        for (int i = 0; i < Y.getColumnsCount(); ++i) {
            LinearModel reg = LinearModel.of(Y.column(i))
                    .meanCorrection(true)
                    .addX(X)
                    .build();
            Ols ols = new Ols();
            LeastSquaresResults result = ols.compute(reg);
            System.out.println(result.getCoefficients());
        }
    }

    public static DoubleSequence test(int n) {

        Normal N = new Normal();
        MersenneTwister rnd = MersenneTwister.fromSystemNanoTime();
        DataBlock data = DataBlock.make(n + K);
        data.set(() -> N.random(rnd));
        data.autoApply(1, (a,b)->a+b);
        return data.drop(K, 0);
    }

    public static double[] simulate(int N, int len, boolean cnt, boolean trend) {
        double[] T = new double[N];
        Arrays.parallelSetAll(T, i -> {
            DoubleSequence data = test(len);
            AugmentedDikkeyFullerTest adf = new AugmentedDikkeyFullerTest();
            adf.setK(1);
            adf.setConstant(cnt);
            adf.setTrend(trend);
            adf.test(data);
            return adf.getT();
        });
        Arrays.sort(T);
        int n01 = N / 100, n05 = N / 20, n10 = N / 10;
        double[] pct = new double[]{
            (T[n01] + T[n01 - 1]) / 2,
            (T[n05] + T[n05 - 1]) / 2,
            (T[n10] + T[n10 - 1]) / 2};
        return pct;
    }

    @Test
    public void tesTable() {
        DoubleSequence data = test(600);
        AugmentedDikkeyFullerTest adf = new AugmentedDikkeyFullerTest();
        adf.setK(1);
        adf.setConstant(true);
        adf.setTrend(true);
        adf.test(data);
        boolean b0 = adf.isSignificant(.01);
        boolean b1 = adf.isSignificant(.05);
        boolean b2 = adf.isSignificant(.1);
        assertTrue(b1 || !b0);
        assertTrue(b2 || !b1);
        System.out.println(AugmentedDikkeyFullerTest.thresholdc(.01, 100));
        System.out.println(AugmentedDikkeyFullerTest.thresholdc(.05, 100));
        System.out.println(AugmentedDikkeyFullerTest.thresholdc(.1, 100));
    }

}
