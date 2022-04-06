/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.linearsystem;

import jdplus.data.DataBlock;
import jdplus.math.matrices.FastMatrix;
import java.util.Random;
import java.util.function.DoubleSupplier;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class LeastSquaresSolverTest {

    public LeastSquaresSolverTest() {
    }

    public static void main(String[] args) {
        testPerformance();
    }

    public static void testPerformance() {
        int N = 300, M = 10, K = 500000;
        FastMatrix A = FastMatrix.make(N, M);
        Random rnd = new Random(0);
        A.set((i, j) -> rnd.nextDouble());
        DataBlock y = DataBlock.make(N);
        y.set((DoubleSupplier)rnd::nextDouble);
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            QRLeastSquaresSolver.fastLeastSquares(y, A);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            QRLeastSquaresSolver.robustLeastSquares(y, A);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

}
