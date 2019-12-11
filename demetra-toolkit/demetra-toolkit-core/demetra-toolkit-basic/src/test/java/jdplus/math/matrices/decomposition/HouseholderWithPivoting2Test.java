/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.decomposition;

import demetra.data.DataSets;
import static demetra.data.DataSets.lre;
import java.util.Random;
import jdplus.data.DataBlock;
import jdplus.math.matrices.Matrix;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class HouseholderWithPivoting2Test {

    public HouseholderWithPivoting2Test() {
    }

    @Test
    public void testRandom() {
        int M = 10, N = 3;
        Matrix A = Matrix.make(M, N);
        Random rnd = new Random(0);
        A.set((i, j) -> rnd.nextDouble());

        DataBlock Y = DataBlock.make(M);
        Y.set(rnd::nextDouble);

        Householder H = new Householder(A);
        DataBlock B = DataBlock.make(N);
        H.leastSquares(Y, B, null);

        HouseholderWithPivoting2 H2 = new HouseholderWithPivoting2();
        QRDecomposition qr = H2.decompose(A, 0);
        DataBlock B2 = DataBlock.make(N);
        qr.leastSquares(Y, B2, null);

        assertTrue(B.distance(B2) < 1e-9);
    }

    @Test
    public void testSingular() {
        int M = 100, N = 10;
        Matrix A = Matrix.make(M, N);
        Random rnd = new Random(0);
        A.set((i, j) -> rnd.nextDouble());

        A.column(4).setAY(.5, A.column(2));
        A.column(4).addAY(.5, A.column(1));
        A.column(7).setAY(-500, A.column(0));

        DataBlock Y = DataBlock.make(M);
        Y.set(rnd::nextDouble);
        Householder H = new Householder(A, true, 1e-12);
        DataBlock B = DataBlock.make(N-2);
        H.leastSquares(Y, B, null);
        System.out.println(B);
        
        HouseholderWithPivoting2 H2 = new HouseholderWithPivoting2();
        QRDecomposition qr = H2.decompose(A, 0);
        System.out.println("");
        System.out.println(qr.rawR());
        System.out.println("");
        DataBlock B2 = DataBlock.make(N);
        qr.leastSquares(Y, B2, null, 1e-14);
        System.out.println(B2);
    }

    public static void testFilip() {
        double[] y = DataSets.Filip.y;
        Matrix M = Matrix.make(y.length, 11);
        DataBlock x = DataBlock.of(DataSets.Filip.x);
        M.column(0).set(1);
        M.column(1).copy(x);
        M.column(2).set(x, a -> a * a);
        M.column(3).set(x, a -> a * a * a);
        M.column(4).set(x, a -> a * a * a * a);
        M.column(5).set(x, a -> a * a * a * a * a);
        M.column(6).set(x, a -> a * a * a * a * a * a);
        M.column(7).set(x, a -> a * a * a * a * a * a * a);
        M.column(8).set(x, a -> a * a * a * a * a * a * a * a);
        M.column(9).set(x, a -> a * a * a * a * a * a * a * a * a);
        M.column(10).set(x, a -> a * a * a * a * a * a * a * a * a * a);

        HouseholderWithPivoting2 H2 = new HouseholderWithPivoting2();
        QRDecomposition qr = H2.decompose(M, 0);
        DataBlock beta = DataBlock.make(M.getColumnsCount());
        qr.leastSquares(DataBlock.of(y), beta, null);
        System.out.println("Filip");
        System.out.println(beta);
        for (int i = 0; i < beta.length(); ++i) {
            System.out.print(lre(beta.get(i), DataSets.Filip.expectedBeta[i]));
            System.out.print('\t');
        }
        System.out.println("");
    }

    public static void testWampler3() {
        double[] y = DataSets.Wampler3.y;
        Matrix M = Matrix.make(y.length, 6);
        DataBlock x = DataBlock.of(DataSets.Wampler3.x);
        M.column(0).set(1);
        M.column(1).copy(x);
        M.column(2).set(x, a -> a * a);
        M.column(3).set(x, a -> a * a * a);
        M.column(4).set(x, a -> a * a * a * a);
        M.column(5).set(x, a -> a * a * a * a * a);

        HouseholderWithPivoting2 H2 = new HouseholderWithPivoting2();
        QRDecomposition qr = H2.decompose(M, 0);
        DataBlock beta = DataBlock.make(M.getColumnsCount());
        qr.leastSquares(DataBlock.of(y), beta, null);
        System.out.println("Wampler3");
        System.out.println(beta);
        for (int i = 0; i < beta.length(); ++i) {
            System.out.print(lre(beta.get(i), DataSets.Wampler3.expectedBeta[i]));
            System.out.print('\t');
        }
        System.out.println("");
    }

    public static void testWampler4() {
        double[] y = DataSets.Wampler4.y;
        Matrix M = Matrix.make(y.length, 6);
        DataBlock x = DataBlock.of(DataSets.Wampler4.x);
        M.column(0).set(1);
        M.column(1).copy(x);
        M.column(2).set(x, a -> a * a);
        M.column(3).set(x, a -> a * a * a);
        M.column(4).set(x, a -> a * a * a * a);
        M.column(5).set(x, a -> a * a * a * a * a);

        HouseholderWithPivoting2 H2 = new HouseholderWithPivoting2();
        QRDecomposition qr = H2.decompose(M, 0);
        DataBlock beta = DataBlock.make(M.getColumnsCount());
        qr.leastSquares(DataBlock.of(y), beta, null);
        System.out.println("Wampler4");
        System.out.println(beta);
        for (int i = 0; i < beta.length(); ++i) {
            System.out.print(lre(beta.get(i), DataSets.Wampler4.expectedBeta[i]));
            System.out.print('\t');
        }
        System.out.println("");
    }

    public static void testWampler5() {
        double[] y = DataSets.Wampler5.y;
        Matrix M = Matrix.make(y.length, 6);
        DataBlock x = DataBlock.of(DataSets.Wampler5.x);
        M.column(0).set(1);
        M.column(1).copy(x);
        M.column(2).set(x, a -> a * a);
        M.column(3).set(x, a -> a * a * a);
        M.column(4).set(x, a -> a * a * a * a);
        M.column(5).set(x, a -> a * a * a * a * a);

        HouseholderWithPivoting2 H2 = new HouseholderWithPivoting2();
        QRDecomposition qr = H2.decompose(M, 0);
        DataBlock beta = DataBlock.make(M.getColumnsCount());
        qr.leastSquares(DataBlock.of(y), beta, null);
        System.out.println("Wampler5");
        System.out.println(beta);
        for (int i = 0; i < beta.length(); ++i) {
            System.out.print(lre(beta.get(i), DataSets.Wampler5.expectedBeta[i]));
            System.out.print('\t');
        }
        System.out.println("");
    }

    public static void main(String[] args) {
        testFilip();
        testWampler3();
        testWampler4();
        testWampler5();
//        stressTest();
    }

    public static void stressTest() {
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
            HouseholderWithPivoting2 H2 = new HouseholderWithPivoting2();
            QRDecomposition qr = H2.decompose(A, 0);
            DataBlock B2 = DataBlock.make(N);
            qr.leastSquares(Y, B2, null);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

}
