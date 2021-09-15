/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.iforest;

import demetra.data.DoubleSeq;
import demetra.math.matrices.MatrixType;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

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

    @Test
    public void testGaussianTree() {

        Random rnd = new Random();
        int n = 500, m = 3, k = 2000;
        double[] data = new double[n * m];
        for (int i = 3; i < data.length; ++i) {
            data[i] = rnd.nextGaussian();
            if (i % 3 == 1) {
                data[i] *= Math.sqrt(.1);
            }
        }
        for (int i = 0; i < 3; ++i) {
            data[i] = 3.3;
        }
        MatrixType M = MatrixType.of(data, m, n);
        long t0 = System.currentTimeMillis();
        ExtendedIsolationForest.iForest forest = ExtendedIsolationForest.iForest.of(M, 0, 0, null);
        forest.fit(k, 256);
        double[] S = forest.predict(null);
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        System.out.println(DoubleSeq.of(S));
    }

    public static void main(String[] args) {
        Random rnd = new Random();
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
    }
}
