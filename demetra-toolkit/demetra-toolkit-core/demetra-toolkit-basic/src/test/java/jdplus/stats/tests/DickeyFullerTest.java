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
import java.util.Arrays;
import jdplus.data.DataBlock;
import jdplus.dstats.Normal;
import jdplus.random.MersenneTwister;
import jdplus.stats.tests.DickeyFuller.DickeyFullerType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author PALATEJ
 */
public class DickeyFullerTest {

    static final int K = 0;
    static final MersenneTwister RND = new MersenneTwister(0);

    public static DoubleSeq test(int n) {

        Normal N = new Normal();
        DataBlock data = DataBlock.make(n + K);
        data.set(() -> N.random(RND));
        data.applyRecursively(1, (a, b) -> a + b);
        return data.drop(K, 0);
    }

    public DickeyFullerTest() {
    }

    @Test
    public void test1() {
        DoubleSeq y = test(100);
        DickeyFuller dft = DickeyFuller.builder()
                .data(y).build();
        DickeyFuller dfz = DickeyFuller.builder()
                .data(y).zstat(true).build();
        DickeyFuller ppt = DickeyFuller.builder()
                .data(y).phillipsPerron(true).build();
        DickeyFuller ppz = DickeyFuller.builder()
                .data(y).phillipsPerron(true).zstat(true).build();
        assertTrue(ppt.getPvalue() < .2);
        assertTrue(ppz.getPvalue() < .2);
    }

    public static double[] simulatet(int N, int len, DickeyFullerType type) {
        double[] T = new double[N];
        Arrays.setAll(T, i -> {
            DoubleSeq data = test(len);
            DickeyFuller df = DickeyFuller.builder()
                    .data(data)
                    .numberOfLags(0)
                    .type(type)
                    .build();
            return df.getTest();
        });
        Arrays.sort(T);
        return T;
    }

    public static double[] simulatez(int N, int len, DickeyFullerType type) {
        double[] T = new double[N];
        Arrays.setAll(T, i -> {
            DoubleSeq data = test(len);
            DickeyFuller df = DickeyFuller.builder()
                    .data(data)
                    .numberOfLags(0)
                    .type(type)
                    .zstat(true)
                    .build();
            return df.getTest();
        });
        Arrays.sort(T);
        return T;
    }

    private static final int[] NOBS = new int[]{25, 50, 100, 250, 500, 2000};
    private static final double[] PROB = new double[]{0.001, 0.01, 0.025, 0.05, 0.1, 0.9, 0.95, 0.975, 0.99, .999};

    public static void main(String[] args) {
        int N = 100000, K=50;
//        for (int i = 0; i < NOBS.length; ++i) {
//            for (int k = 0; k < K; ++k) {
//                double[] t = simulatet(N, NOBS[i], DickeyFullerType.NC);
//                for (int j = 0; j < PROB.length; ++j) {
//                    System.out.print(t[(int) (t.length * PROB[j] - .5)]);
//                    System.out.print('\t');
//                }
//                System.out.println();
//            }
//            System.out.println();
//        }
//        System.out.println();
//        for (int i = 0; i < NOBS.length; ++i) {
//            for (int k = 0; k < K; ++k) {
//                double[] t = simulatet(N, NOBS[i], DickeyFullerType.C);
//                for (int j = 0; j < PROB.length; ++j) {
//                    System.out.print(t[(int) (t.length * PROB[j] - .5)]);
//                    System.out.print('\t');
//                }
//                System.out.println();
//            }
//            System.out.println();
//        }
//        System.out.println();
//        for (int i = 0; i < NOBS.length; ++i) {
//            for (int k = 0; k < K; ++k) {
//                double[] t = simulatet(N, NOBS[i], DickeyFullerType.CT);
//                for (int j = 0; j < PROB.length; ++j) {
//                    System.out.print(t[(int) (t.length * PROB[j] - .5)]);
//                    System.out.print('\t');
//                }
//                System.out.println();
//            }
//            System.out.println();
//        }
//        System.out.println();
//        for (int i = 0; i < NOBS.length; ++i) {
//            for (int k = 0; k < K; ++k) {
//                double[] t = simulatez(N, NOBS[i], DickeyFullerType.NC);
//                for (int j = 0; j < PROB.length; ++j) {
//                    System.out.print(t[(int) (t.length * PROB[j] - .5)]);
//                    System.out.print('\t');
//                }
//                System.out.println();
//            }
//            System.out.println();
//        }
//        System.out.println();
//        for (int i = 0; i < NOBS.length; ++i) {
//            for (int k = 0; k < K; ++k) {
//                double[] t = simulatez(N, NOBS[i], DickeyFullerType.C);
//                for (int j = 0; j < PROB.length; ++j) {
//                    System.out.print(t[(int) (t.length * PROB[j] - .5)]);
//                    System.out.print('\t');
//                }
//                System.out.println();
//            }
//            System.out.println();
//        }
        System.out.println();
        for (int i = 0; i < NOBS.length; ++i) {
            for (int k = 0; k < K; ++k) {
                double[] t = simulatez(N, NOBS[i], DickeyFullerType.CT);
                for (int j = 0; j < PROB.length; ++j) {
                    System.out.print(t[(int) (t.length * PROB[j] - .5)]);
                    System.out.print('\t');
                }
                System.out.println();
            }
            System.out.println();
        }
    }

}
