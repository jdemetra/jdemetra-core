/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.decomposition;

import demetra.data.DataSets;
import static demetra.data.DataSets.lre;
import demetra.data.DoubleSeq;
import ec.tstoolkit.maths.Constants;
import java.util.Random;
import jdplus.data.DataBlock;
import jdplus.leastsquares.QRSolution;
import jdplus.leastsquares.QRSolver;
import jdplus.math.matrices.FastMatrix;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class HouseholderWithPivotingTest {

    public HouseholderWithPivotingTest() {
    }

    @Test
    public void testRandom() {
        int M = 10, N = 3;
        FastMatrix A = FastMatrix.make(M, N);
        Random rnd = new Random(0);
        A.set((i, j) -> rnd.nextDouble());

        DataBlock Y = DataBlock.make(M);
        Y.set(rnd::nextDouble);

        HouseholderWithPivoting H2 = new HouseholderWithPivoting();
        QRDecomposition qr = H2.decompose(A, 0);
        QRSolution ls = QRSolver.leastSquares(qr, Y, 1e-15);
        DoubleSeq beta = ls.getB();
    }

    @Test
    public void testSingular() {
        int M = 100, N = 10;
        FastMatrix A = FastMatrix.make(M, N);
        Random rnd = new Random(0);
        A.set((i, j) -> rnd.nextDouble());

        A.column(4).setAY(.5, A.column(2));
        A.column(4).addAY(.5, A.column(1));
        A.column(7).setAY(-500, A.column(0));

        DataBlock Y = DataBlock.make(M);
        Y.set(rnd::nextDouble);

        HouseholderWithPivoting H2 = new HouseholderWithPivoting();
        QRDecomposition qr = H2.decompose(A, 0);
        System.out.println("");
        System.out.println(qr.rawR());
        System.out.println("");
        QRSolution ls = QRSolver.leastSquares(qr, Y, Constants.getEpsilon());
        System.out.println(ls.getB());
    }

    public static void testFilip() {
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

        HouseholderWithPivoting H2 = new HouseholderWithPivoting();
        QRDecomposition qr = H2.decompose(M, 0);
        QRSolution ls = QRSolver.leastSquares(qr, DoubleSeq.of(y), 1e-15);
        DoubleSeq beta = ls.getB();
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
        FastMatrix M = FastMatrix.make(y.length, 6);
        DataBlock x = DataBlock.of(DataSets.Wampler3.x);
        M.column(0).set(1);
        M.column(1).copy(x);
        M.column(2).set(x, a -> a * a);
        M.column(3).set(x, a -> a * a * a);
        M.column(4).set(x, a -> a * a * a * a);
        M.column(5).set(x, a -> a * a * a * a * a);

        HouseholderWithPivoting H2 = new HouseholderWithPivoting();
        QRDecomposition qr = H2.decompose(M, 0);
        QRSolution ls = QRSolver.leastSquares(qr, DoubleSeq.of(y), 1e-15);
        DoubleSeq beta = ls.getB();
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
        FastMatrix M = FastMatrix.make(y.length, 6);
        DataBlock x = DataBlock.of(DataSets.Wampler4.x);
        M.column(0).set(1);
        M.column(1).copy(x);
        M.column(2).set(x, a -> a * a);
        M.column(3).set(x, a -> a * a * a);
        M.column(4).set(x, a -> a * a * a * a);
        M.column(5).set(x, a -> a * a * a * a * a);

        HouseholderWithPivoting H2 = new HouseholderWithPivoting();
        QRDecomposition qr = H2.decompose(M, 0);
        QRSolution ls = QRSolver.leastSquares(qr, DoubleSeq.of(y), 1e-15);
        DoubleSeq beta = ls.getB();
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
        FastMatrix M = FastMatrix.make(y.length, 6);
        DataBlock x = DataBlock.of(DataSets.Wampler5.x);
        M.column(0).set(1);
        M.column(1).copy(x);
        M.column(2).set(x, a -> a * a);
        M.column(3).set(x, a -> a * a * a);
        M.column(4).set(x, a -> a * a * a * a);
        M.column(5).set(x, a -> a * a * a * a * a);

        HouseholderWithPivoting H2 = new HouseholderWithPivoting();
        QRDecomposition qr = H2.decompose(M, 0);
        QRSolution ls = QRSolver.leastSquares(qr, DoubleSeq.of(y), 1e-15);
        DoubleSeq beta = ls.getB();
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
        stressTest();
    }

    public static void stressTest() {
        int M = 300, N = 20, K = 100000;
        FastMatrix A = FastMatrix.make(M, N);
        Random rnd = new Random(0);
        A.set((i, j) -> rnd.nextDouble());

        DataBlock Y = DataBlock.make(M);
        Y.set(rnd::nextDouble);

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
            HouseholderWithPivoting H2 = new HouseholderWithPivoting();
            QRDecomposition qr = H2.decompose(A, 0);
            QRSolution ls = QRSolver.leastSquares(qr, Y, 1e-15);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

}
