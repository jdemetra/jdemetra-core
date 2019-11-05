/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.matrices.decomposition;

import java.util.Random;
import jdplus.maths.matrices.Matrix;
import jdplus.maths.matrices.FastMatrix;
import jdplus.maths.matrices.decomposition.SimilarTransformations.Balancing;
import jdplus.maths.matrices.decomposition.SimilarTransformations.Hessenberg;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class SimilarTransformationsTest {

    public SimilarTransformationsTest() {
    }

    @Test
    public void testRandom() {
        Matrix A = Matrix.square(100);
        Random rnd = new Random(0);
        A.set(() -> rnd.nextDouble() - .5);
        Matrix h = A.deepClone();
        Hessenberg.householder(h);
        double d0 = FastMatrix.logDeterminant(h).getValue();
        h = A.deepClone();
        Hessenberg.gauss(h);
        double d1 = FastMatrix.logDeterminant(h).getValue();
        assertEquals(d0, d1, 1e-6);
        h = A.deepClone();
        Balancing.balance(h);
        double d2 = FastMatrix.logDeterminant(h).getValue();
        assertEquals(d0, d2, 1e-6);
       
    }

    public static void main(String[] args) {
        int N = 200, K = 100;
        Matrix A = Matrix.square(N);
        Random rnd = new Random(0);
        A.set(() -> rnd.nextDouble() - .5);
        long t0 = System.currentTimeMillis();
        Matrix h = null;
        for (int j = 0; j < K; ++j) {
            h = A.deepClone();
            Balancing.balance(h);
            Hessenberg.householder(h);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
//        System.out.println(h);
        double d0 = FastMatrix.logDeterminant(h).getValue();

        t0 = System.currentTimeMillis();
        for (int j = 0; j < K; ++j) {
            h = A.deepClone();
            Balancing.balance(h);
            Hessenberg.gauss(h);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
//        System.out.println(h);

        double d1 = FastMatrix.logDeterminant(h).getValue();
        assertEquals(d0, d1, 1e-6);
    }
}
