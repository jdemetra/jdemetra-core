/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.ml;

import demetra.data.DoubleSeq;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;
import demetra.math.matrices.Matrix;

/**
 *
 * @author PALATEJ
 */
public class IsolationForestsTest {

    public IsolationForestsTest() {
    }

    @Test
    public void testSample() {

        Random rnd = new Random();
        int[] rslt = IsolationForests.sampleWithoutReplacement(15, 20, true, rnd);
        for (int i = 0; i < rslt.length; ++i) {
//            System.out.println(rslt[i]);
        }
    }

    //@Test
    public void testXForest() {
        
        gaussianTree();
        
        Random rnd = new Random(0);
        int n = 1000, m = 10;
        double[] data = new double[n * m];
        for (int i = 0; i < data.length; ++i) {
            data[i] = rnd.nextGaussian();
        }
        int[] outliers = new int[n];
        for (int i = 0; i < 20; ++i) {
            int o = rnd.nextInt(data.length);
            data[o] += 20;
            outliers[o / m]++;
        }

        Matrix M = Matrix.of(data, m, n);

        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 100; ++i) {
            IsolationForests.Forest forest = IsolationForests.Forest.builder()
                    .X(M)
                    .treeBuilder(IsolationForests.TreeBuilder.smooth())
                    .build();
            forest.fit(200, 512);
            double[] S = forest.predict(null);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

    public static void gaussianTree() {

        Random rnd = new Random();
        int n = 5000, m = 5, k = 2000;
        double[] data = new double[n * m];
        for (int i = m; i < data.length; ++i) {
            data[i] = rnd.nextGaussian();
            if (i % 3 == 1) {
                data[i] *= Math.sqrt(.1);
            }
        }
        for (int i = 0; i < m; ++i) {
            data[i] = 3.3;
        }
        Matrix M = Matrix.of(data, m, n);
        long t0 = System.currentTimeMillis();
        IsolationForests.Forest forest = IsolationForests.Forest.builder()
                .X(M)
                .treeBuilder(IsolationForests.TreeBuilder.smooth())
                .build();
        forest.fit(k, 256);
        double[] S = forest.predict(null);
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        System.out.println(DoubleSeq.of(S));

    }

    static int[] gaussianTree1(int k, int s, IsolationForests.TreeBuilder builder) {

        Random rnd = new Random(0);
        int n = 1000, m = 10;
        double[] data = new double[n * m];
        for (int i = 0; i < data.length; ++i) {
            data[i] = rnd.nextGaussian();
        }
        int[] outliers = new int[n];
        for (int i = 0; i < 20; ++i) {
            int o = rnd.nextInt(data.length);
            data[o] += 20;
            outliers[o / m]++;
        }

        Matrix M = Matrix.of(data, m, n);
        IsolationForests.Forest forest = IsolationForests.Forest.builder()
                .X(M)
                .treeBuilder(builder)
                .build();
        forest.fit(k, s);
        double[] S = forest.predict(null);
        System.out.println(DoubleSeq.of(S));
        return outliers;
    }

    public static void gaussianTree2(int k, int s) {

        Random rnd = new Random(0);
        int n = 1000, m = 20;
        double[] data = new double[n * m];
        for (int i = 0; i < data.length; ++i) {
            data[i] = rnd.nextGaussian();
        }

        for (int i = 0; i < 100; ++i) {
            data[rnd.nextInt(data.length)] += 20;
        }

        Matrix M = Matrix.of(data, m, n);
        IsolationForests.Forest forest = IsolationForests.Forest.builder()
                .X(M)
                .treeBuilder(IsolationForests.TreeBuilder.extended(m - 1))
                .build();
        forest.fit(k, s);
        double[] S = forest.predict(null);
        System.out.println(DoubleSeq.of(S));
    }

    public static void gaussianTree3(int k, int s) {

        Random rnd = new Random(0);
        int n = 1000, m = 20;
        double[] data = new double[n * m];
        for (int i = 0; i < data.length; ++i) {
            data[i] = rnd.nextGaussian();
        }

        for (int i = 0; i < 100; ++i) {
            data[rnd.nextInt(data.length)] += 20;
        }

        Matrix M = Matrix.of(data, m, n);
        IsolationForests.Forest forest = IsolationForests.Forest.builder()
                .X(M)
                .treeBuilder(IsolationForests.TreeBuilder.smooth())
                .build();
        forest.fit(k, s);
        double[] S = forest.predict(null);
        System.out.println(DoubleSeq.of(S));
    }

    public static void main(String[] args) {
        //gaussianTree();
        int[] o = gaussianTree1(50, 512, IsolationForests.TreeBuilder.legacy());
        for (int i = 0; i < o.length; ++i) {
            System.out.print(o[i]);
            System.out.print('\t');
        }
        System.out.println();
        System.out.println();
        int q = 512;
        for (int k = 5; k <= 20; k += 5) {
            gaussianTree1(k * 10, q, IsolationForests.TreeBuilder.legacy());
        }
        System.out.println();
        for (int k = 5; k <= 20; k += 5) {
            gaussianTree1(k * 10, q, IsolationForests.TreeBuilder.extended(-1));
        }
        System.out.println();
        for (int k = 5; k <= 20; k += 5) {
            gaussianTree1(k * 10, q, IsolationForests.TreeBuilder.smooth());
        }
    }
}
