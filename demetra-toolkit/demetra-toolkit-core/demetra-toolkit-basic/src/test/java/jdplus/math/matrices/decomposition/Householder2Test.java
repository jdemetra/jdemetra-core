/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.decomposition;

import demetra.data.DataSets;
import demetra.data.DoubleSeq;
import java.util.Random;
import java.util.function.DoubleSupplier;

import jdplus.data.DataBlock;
import jdplus.math.linearsystem.QRLeastSquaresSolution;
import jdplus.math.linearsystem.QRLeastSquaresSolver;
import jdplus.math.matrices.FastMatrix;
import org.junit.jupiter.api.Test;

/**
 *
 * @author palatej
 */
public class Householder2Test {

    public Householder2Test() {
    }

    @Test
    public void testRandom() {
        int M = 10, N = 3;
        FastMatrix A = FastMatrix.make(M, N);
        Random rnd = new Random(0);
        A.set((i, j) -> rnd.nextDouble());

        DataBlock Y = DataBlock.make(M);
        Y.set((DoubleSupplier)rnd::nextDouble);

        Householder2 H2 = new Householder2();
        QRDecomposition qr = H2.decompose(A);
        QRLeastSquaresSolution ls = QRLeastSquaresSolver.leastSquares(qr, Y, 1e-15);
//        System.out.println(ls.getB());
//        assertTrue(B.distance(B2) < 1e-9);
    }

    @Test
    public void testFilip() {
        double[] y = DataSets.Filip.y;
        FastMatrix M = FastMatrix.make(y.length, 11);
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

        Householder2 H2 = new Householder2();
        QRDecomposition qr = H2.decompose(M);
        QRLeastSquaresSolution ls = QRLeastSquaresSolver.leastSquares(qr, DoubleSeq.of(y), 1e-15);
        DoubleSeq beta = ls.getB();
//        System.out.println("Filip");
//        System.out.println(beta);
//        for (int i = 0; i < beta.length(); ++i) {
//            System.out.print(lre(beta.get(i), DataSets.Filip.expectedBeta[i]));
//            System.out.print('\t');
//        }
//        System.out.println("");
    }

    @Test
    public void testWampler4() {
        double[] y = DataSets.Wampler4.y;
        FastMatrix M = FastMatrix.make(y.length, 6);
        DataBlock x = DataBlock.of(DataSets.Wampler4.x);
        M.column(0).set(1);
        M.column(1).copy(x);
        M.column(2).set(x, a -> a * a);
        M.column(3).set(x, a -> a * a * a);
        M.column(4).set(x, a -> a * a * a * a);
        M.column(5).set(x, a -> a * a * a * a * a);

        Householder2 H2 = new Householder2();
        QRDecomposition qr = H2.decompose(M);
        QRLeastSquaresSolution ls = QRLeastSquaresSolver.leastSquares(qr, DoubleSeq.of(y), 1e-15);
        DoubleSeq beta = ls.getB();
//        System.out.println("Wampler4");
//        System.out.println(beta);
//        for (int i = 0; i < beta.length(); ++i) {
//            System.out.print(lre(beta.get(i), DataSets.Wampler4.expectedBeta[i]));
//            System.out.print('\t');
//        }
//        System.out.println("");
    }

    @Test
    public void testWampler5() {
        double[] y = DataSets.Wampler5.y;
        FastMatrix M = FastMatrix.make(y.length, 6);
        DataBlock x = DataBlock.of(DataSets.Wampler5.x);
        M.column(0).set(1);
        M.column(1).copy(x);
        M.column(2).set(x, a -> a * a);
        M.column(3).set(x, a -> a * a * a);
        M.column(4).set(x, a -> a * a * a * a);
        M.column(5).set(x, a -> a * a * a * a * a);

        Householder2 H2 = new Householder2();
        QRDecomposition qr = H2.decompose(M);
        QRLeastSquaresSolution ls = QRLeastSquaresSolver.leastSquares(qr, DoubleSeq.of(y), 1e-15);
        DoubleSeq beta = ls.getB();
//        System.out.println("Wampler5");
//        System.out.println(beta);
//        for (int i = 0; i < beta.length(); ++i) {
//            System.out.print(lre(beta.get(i), DataSets.Wampler5.expectedBeta[i]));
//            System.out.print('\t');
//        }
//        System.out.println("");
    }

    public static void main(String[] args) {
        int M = 300, N = 20, K = 100000;
        FastMatrix A = FastMatrix.make(M, N);
        Random rnd = new Random(0);
        A.set((i, j) -> rnd.nextDouble());

        DataBlock Y = DataBlock.make(M);
        Y.set((DoubleSupplier)rnd::nextDouble);

        long t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            Householder H = new Householder(A);
            DataBlock B = DataBlock.make(N);
//            H.leastSquares(Y, B, null);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);

        t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            Householder2 H2 = new Householder2();
            QRDecomposition qr = H2.decompose(A);
            QRLeastSquaresSolution ls = QRLeastSquaresSolver.leastSquares(qr, Y, 1e-15);
            DoubleSeq beta = ls.getB();
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

}
