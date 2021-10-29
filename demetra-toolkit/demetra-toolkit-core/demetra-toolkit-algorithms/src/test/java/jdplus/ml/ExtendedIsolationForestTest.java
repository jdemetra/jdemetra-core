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
public class ExtendedIsolationForestTest {

    public ExtendedIsolationForestTest() {
    }

    @Test
    public void testSample() {

        Random rnd = new Random();
        int[] rslt = ExtendedIsolationForest.sampleWithoutReplacement(15, 20, true, rnd);
        for (int i = 0; i < rslt.length; ++i) {
            System.out.println(rslt[i]);
        }
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
        ExtendedIsolationForest.iForest forest = ExtendedIsolationForest.iForest.builder()
                .X(M)
//                .extensionLevel(0)
                .build();
        forest.fit(k, 256);
        double[] S = forest.predict(null);
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        System.out.println(DoubleSeq.of(S));
        
        
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
        long t0 = System.currentTimeMillis();
        ExtendedIsolationForest.iForest forest = ExtendedIsolationForest.iForest.builder()
                .X(M)
//                .extensionLevel(0)
                .build();
        forest.fit(k, s);
        double[] S = forest.predict(null);
        long t1 = System.currentTimeMillis();
        System.out.println(DoubleSeq.of(S));
    }

    public static void main(String[] args) {
        Random rnd = new Random(0);
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            ExtendedIsolationForest.sampleWithoutReplacementLegacy(90, 100, false, rnd);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);

        t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            ExtendedIsolationForest.sampleWithoutReplacement(90, 100, false, rnd);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);

        gaussianTree();
        for (int k = 5; k <= 100; k += 5) {
            gaussianTree2(k * 10, 512);
        }
    }
}
