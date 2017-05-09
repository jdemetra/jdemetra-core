/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths.matrices.impl;

import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import java.util.Random;
import demetra.maths.matrices.MatrixComparator;
import demetra.maths.matrices.SymmetricMatrix;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class FastSymmetricMatrixAlgorithmsTest {

    FastSymmetricMatrixAlgorithms algo = new FastSymmetricMatrixAlgorithms();

    @Test
    public void testCholesky() {
        int N = 40, K = 1000000;
        Matrix L = Matrix.square(N);
//        L.set((i, j) -> i >= j ? (i + 1) * (j + 1) * (j + 1) : 0);
        Random rnd = new Random(0);
        L.set((i, j) -> i >= j ? rnd.nextDouble() : 0);
        Matrix M = algo.XXt(L);

        Matrix O = M.deepClone();
        ec.tstoolkit.maths.matrices.Matrix m = MatrixComparator.toLegacy(M);
        ec.tstoolkit.maths.matrices.Matrix o = m.clone();
        ec.tstoolkit.maths.matrices.SymmetricMatrix.lcholesky(m, 1e-9);
        algo.lcholesky(M, 1e-9);
        assertTrue(MatrixComparator.distance(M, m) == 0);

//        long t0 = System.currentTimeMillis();
//        for (int k = 0; k < K; ++k) {
//            ec.tstoolkit.maths.matrices.Matrix p = o.clone();
//            ec.tstoolkit.maths.matrices.SymmetricMatrix.lcholesky(p, 1e-9);
//        }
//        long t1 = System.currentTimeMillis();
//        System.out.println("Old LCholesky");
//        System.out.println(t1 - t0);
//        t0 = System.currentTimeMillis();
//        for (int k = 0; k < K; ++k) {
//            Matrix P = O.deepClone();
//            algo.lcholesky(P, 1e-9);
//        }
//        t1 = System.currentTimeMillis();
//        System.out.println("New LCholesky");
//        System.out.println(t1 - t0);
    }

    @Test
    public void testSingularCholesky() {
        int N = 20;
        Matrix L = Matrix.square(N);
//        L.set((i, j) -> i >= j ? (i + 1) * (j + 1) * (j + 1) : 0);
        Random rnd = new Random(0);
        L.set((i, j) -> i >= j && (i + 1) % 5 != 0 ? rnd.nextDouble() : 0);
        Matrix M = algo.XXt(L);

        Matrix O = M.deepClone();
        ec.tstoolkit.maths.matrices.Matrix m = MatrixComparator.toLegacy(M);
        ec.tstoolkit.maths.matrices.Matrix o = m.clone();
        ec.tstoolkit.maths.matrices.SymmetricMatrix.lcholesky(m, 1e-9);
        algo.lcholesky(M, 1e-9);
        assertTrue(MatrixComparator.distance(M, m) == 0);
    }

    @Test
    public void testXXt() {
        int N = 40, K = 100000;
        Matrix X = Matrix.square(N);
        X.set((i, j) -> (i + 1) * (j + 1) * (j + 1));
        Matrix M = algo.XXt(X);

        ec.tstoolkit.maths.matrices.Matrix x = MatrixComparator.toLegacy(X);
        ec.tstoolkit.maths.matrices.Matrix m = ec.tstoolkit.maths.matrices.SymmetricMatrix.XXt(x);
        assertTrue(MatrixComparator.distance(M, m) == 0);

//        long t0 = System.currentTimeMillis();
//        for (int k = 0; k < K; ++k) {
//            ec.tstoolkit.maths.matrices.SymmetricMatrix.XXt(x);
//        }
//        long t1 = System.currentTimeMillis();
//        System.out.println("Old XXt");
//        System.out.println(t1 - t0);
//        t0 = System.currentTimeMillis();
//        for (int k = 0; k < K; ++k) {
//             algo.XXt(X);
//        }
//        t1 = System.currentTimeMillis();
//        System.out.println("New XXt");
//        System.out.println(t1 - t0);
    }

    @Test
    public void testXtX() {
        int N = 40, K = 100000;
        Matrix X = Matrix.square(N);
        X.set((i, j) -> (i + 1) * (j + 1) * (j + 1));
        Matrix M = algo.XtX(X);

        ec.tstoolkit.maths.matrices.Matrix x = MatrixComparator.toLegacy(X);
        ec.tstoolkit.maths.matrices.Matrix m = ec.tstoolkit.maths.matrices.SymmetricMatrix.XtX(x);
        assertTrue(MatrixComparator.distance(M, m) == 0);

//        long t0 = System.currentTimeMillis();
//        for (int k = 0; k < K; ++k) {
//            ec.tstoolkit.maths.matrices.SymmetricMatrix.XtX(x);
//        }
//        long t1 = System.currentTimeMillis();
//        System.out.println("Old XtX");
//        System.out.println(t1 - t0);
//        t0 = System.currentTimeMillis();
//        for (int k = 0; k < K; ++k) {
//             algo.XtX(X);
//        }
//        t1 = System.currentTimeMillis();
//        System.out.println("New XtX");
//        System.out.println(t1 - t0);
    }

    @Test
    public void testLLt() {
        int N = 20, K = 1000000;
        Matrix L = Matrix.square(N);
        L.set((i, j) -> i >= j ? (i + 1) * (j + 1) * (j + 1) : 0);
        Matrix M = algo.LLt(L);

        ec.tstoolkit.maths.matrices.Matrix l = MatrixComparator.toLegacy(L);
        ec.tstoolkit.maths.matrices.Matrix m = ec.tstoolkit.maths.matrices.SymmetricMatrix.LLt(l);
        assertTrue(MatrixComparator.distance(M, m) == 0);

//        long t0 = System.currentTimeMillis();
//        for (int k = 0; k < K; ++k) {
//            ec.tstoolkit.maths.matrices.SymmetricMatrix.LLt(l);
//        }
//        long t1 = System.currentTimeMillis();
//        System.out.println("Old LLt");
//        System.out.println(t1 - t0);
//        t0 = System.currentTimeMillis();
//        for (int k = 0; k < K; ++k) {
//             algo.LLt(L);
//        }
//        t1 = System.currentTimeMillis();
//        System.out.println("New LLt");
//        System.out.println(t1 - t0);
    }

    @Test
    public void testUtU() {
        int N = 20, K = 1000000;
        Matrix U = Matrix.square(N);
        U.set((i, j) -> i <= j ? (i + 1) * (j + 1) * (j + 1) : 0);
        Matrix M = algo.UtU(U);

        ec.tstoolkit.maths.matrices.Matrix u = MatrixComparator.toLegacy(U);
        ec.tstoolkit.maths.matrices.Matrix m = ec.tstoolkit.maths.matrices.SymmetricMatrix.UtU(u);
        assertTrue(MatrixComparator.distance(M, m) == 0);
//        long t0 = System.currentTimeMillis();
//        for (int k = 0; k < K; ++k) {
//            ec.tstoolkit.maths.matrices.SymmetricMatrix.UtU(u);
//        }
//        long t1 = System.currentTimeMillis();
//        System.out.println("Old UtU");
//        System.out.println(t1 - t0);
//        t0 = System.currentTimeMillis();
//        for (int k = 0; k < K; ++k) {
//             algo.UtU(U);
//        }
//        t1 = System.currentTimeMillis();
//        System.out.println("New UtU");
//        System.out.println(t1 - t0);
    }

    @Test
    public void testUUt() {
        int N = 20, K = 1000000;
        Matrix U = Matrix.square(N);
        U.set((i, j) -> i <= j ? (i + 1) * (j + 1) * (j + 1) : 0);
        Matrix M = algo.UUt(U);
        Matrix X = algo.XXt(U);

        assertTrue(MatrixComparator.distance(M, X) == 0);
//        long t0 = System.currentTimeMillis();
//        for (int k = 0; k < K; ++k) {
//            ec.tstoolkit.maths.matrices.SymmetricMatrix.UtU(u);
//        }
//        long t1 = System.currentTimeMillis();
//        System.out.println("Old UtU");
//        System.out.println(t1 - t0);
//        t0 = System.currentTimeMillis();
//        for (int k = 0; k < K; ++k) {
//             algo.UtU(U);
//        }
//        t1 = System.currentTimeMillis();
//        System.out.println("New UtU");
//        System.out.println(t1 - t0);
    }

    @Test
    public void testTransposeCholesky() {
        int N = 40;
        Matrix L = Matrix.square(N);
        Random rnd = new Random(0);
        L.set((i, j) -> i >= j ? rnd.nextDouble() : 0);
        Matrix M = algo.XXt(L);

        ec.tstoolkit.maths.matrices.Matrix m = MatrixComparator.toLegacy(M);
        ec.tstoolkit.maths.matrices.SymmetricMatrix.lcholesky(m, 1e-9);
        algo.lcholesky(M.transpose(), 1e-9);
        assertTrue(MatrixComparator.distance(M.transpose(), m) == 0);

    }

    @Test
    public void testTransposeSingularCholesky() {
        int N = 20;
        Matrix L = Matrix.square(N);
        Random rnd = new Random(0);
        L.set((i, j) -> i >= j && (i + 1) % 5 != 0 ? rnd.nextDouble() : 0);
        Matrix M = algo.XXt(L);

        ec.tstoolkit.maths.matrices.Matrix m = MatrixComparator.toLegacy(M);
        ec.tstoolkit.maths.matrices.SymmetricMatrix.lcholesky(m, 1e-9);
        algo.lcholesky(M.transpose(), 1e-9);
        assertTrue(MatrixComparator.distance(M.transpose(), m) == 0);
    }

    @Test
    public void testxxt() {
        int N = 40;
        DataBlock x = DataBlock.make(N);
        x.set(i -> i + 1);
        Matrix X = Matrix.builder(x.getStorage()).nrows(N).ncolumns(1).build();
        Matrix M = algo.XXt(X);
        Matrix m = algo.xxt(x);

        assertTrue(MatrixComparator.distance(M, m) == 0);
        // System.out.println(m);
    }

    @Test
    public void testXSXt() {
        int N = 30, M = 50;
        Matrix X = Matrix.make(N, M);
        Matrix S = Matrix.square(N);
        S.set((i, j) -> (i + j + 1));
        X.set((i, j) -> (i + 1) * (j - 3));
        Matrix M1 = SymmetricMatrix.XtSX(S, X);
        Matrix M2 = X.transpose().times(S).times(X);
        assertTrue(MatrixComparator.distance(M1, M2) < 1e-9);

//        int K = 100000;
//        ec.tstoolkit.maths.matrices.Matrix s = MatrixComparator.toLegacy(S);
//        ec.tstoolkit.maths.matrices.Matrix x = MatrixComparator.toLegacy(X);
//        long t0 = System.currentTimeMillis();
//
//        for (int k = 0; k < K; ++k) {
//            ec.tstoolkit.maths.matrices.Matrix q = ec.tstoolkit.maths.matrices.SymmetricMatrix.quadraticForm(s, x);
//        }
//        long t1 = System.currentTimeMillis();
//        System.out.println("Old XtSX");
//        System.out.println(t1 - t0);
//        t0 = System.currentTimeMillis();
//        for (int k = 0; k < K; ++k) {
//            Matrix Q = SymmetricMatrix.XtSX(S, X);
//        }
//        t1 = System.currentTimeMillis();
//        System.out.println("New XtSX");
//        System.out.println(t1 - t0);

    }

}
