/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.decomposition;

import java.util.Random;
import jdplus.data.DataBlock;
import jdplus.math.matrices.Matrix;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class Householder2Test {

    public Householder2Test() {
    }

    @Test
    public void testRandom() {
        int M = 100, N = 5;
        Matrix A = Matrix.make(M, N);
        Random rnd = new Random(0);
        A.set((i, j) -> rnd.nextDouble());

        DataBlock Y = DataBlock.make(M);
        Y.set(rnd::nextDouble);

        Householder H = new Householder(A);
        DataBlock B = DataBlock.make(N);
        H.leastSquares(Y, B, null);

        Householder2 H2 = new Householder2();
        QRDecomposition qr = H2.decompose(A, 1e-16);
        DataBlock B2 = DataBlock.make(N);
        qr.leastSquares(Y, B2, null);

        assertTrue(B.distance(B2) < 1e-9);
    }

    public static void main(String[] args) {
        int M = 300, N = 20, K = 100000;
        Matrix A = Matrix.make(M, N);
        Random rnd = new Random(0);
        A.set((i, j) -> rnd.nextDouble());

        DataBlock Y = DataBlock.make(M);
        Y.set(rnd::nextDouble);

        long t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            Householder H = new Householder(A);
            DataBlock B = DataBlock.make(N);
            H.leastSquares(Y, B, null);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);

        t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            Householder2 H2 = new Householder2();
            QRDecomposition qr = H2.decompose(A, 1e-16);
            DataBlock B2 = DataBlock.make(N);
            qr.leastSquares(Y, B2, null);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

}
