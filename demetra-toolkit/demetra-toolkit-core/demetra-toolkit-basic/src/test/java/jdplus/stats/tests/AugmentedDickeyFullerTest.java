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
package jdplus.stats.tests;

import jdplus.data.DataBlock;
import jdplus.dstats.Normal;
import jdplus.linearmodel.LeastSquaresResults;
import jdplus.linearmodel.LinearModel;
import jdplus.linearmodel.Ols;
import jdplus.math.matrices.Matrix;
import jdplus.random.MersenneTwister;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import demetra.data.DoubleSeq;
import jdplus.stats.tests.DickeyFullerTable.DickeyFullerType;

/**
 *
 * @author Jean Palate
 */
public class AugmentedDickeyFullerTest {
    
    public AugmentedDickeyFullerTest() {
    }

    static final int K = 50;


    @Test
//    @Ignore
    public void testADF() {
        int N = 10000;
        double[] T = new double[N];
        double[] PT = new double[N];
        for (int i = 0; i < N; ++i) {
            DoubleSeq data = test(200);
            AugmentedDickeyFuller adf = AugmentedDickeyFuller.builder()
                    .data(data)
                    .numberOfLags(0)
                    .constant(true)
                    .linearTrend(true)
                    .build();
            T[i] = adf.getT();
            PT[i] = DickeyFullerTable.probability(200, T[i], DickeyFullerType.CT, true);
        }
        Arrays.sort(T);
        Arrays.sort(PT);
        int n01 = N / 100, n05 = N / 20, n10 = N / 10;
        System.out.println((T[n01] + T[n01 - 1]) / 2);
        System.out.println((T[n05] + T[n05 - 1]) / 2);
        System.out.println((T[n10] + T[n10 - 1]) / 2);
        
        System.out.println(DoubleSeq.of(T));
        System.out.println(DoubleSeq.of(PT));
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
            LinearModel reg = LinearModel.builder()
                    .y(Y.column(i))
                    .meanCorrection(true)
                    .addX(X)
                    .build();
            LeastSquaresResults result = Ols.compute(reg);
            System.out.println(result.getCoefficients());
        }
    }

    final static MersenneTwister rnd = new MersenneTwister(0);
    public static DoubleSeq test(int n) {

        Normal N = new Normal();
        DataBlock data = DataBlock.make(n + K);
        data.set(() -> N.random(rnd));
        data.applyRecursively(1, (a,b)->a+b);
        return data.drop(K, 0);
    }

    public static double[] simulate(int N, int len, boolean cnt, boolean trend) {
        double[] T = new double[N];
        Arrays.parallelSetAll(T, i -> {
            DoubleSeq data = test(len);
            AugmentedDickeyFuller adf = AugmentedDickeyFuller.builder()
                    .data(data)
                    .numberOfLags(1)
                    .constant(cnt)
                    .linearTrend(trend)
                    .build();
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
        DoubleSeq data = test(600);
            AugmentedDickeyFuller adf = AugmentedDickeyFuller.builder()
                    .data(data)
                    .numberOfLags(1)
                    .constant(true)
                    .linearTrend(true)
                    .build();
        boolean b0 = adf.isSignificant(.01);
        boolean b1 = adf.isSignificant(.05);
        boolean b2 = adf.isSignificant(.1);
        assertTrue(b1 || !b0);
        assertTrue(b2 || !b1);
//        System.out.println(AugmentedDickeyFullerTest.thresholdc(.01, 100));
//        System.out.println(AugmentedDickeyFullerTest.thresholdc(.05, 100));
//        System.out.println(AugmentedDickeyFullerTest.thresholdc(.1, 100));
    }

}
