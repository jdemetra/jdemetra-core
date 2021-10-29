/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices;

import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.random.JdkRNG;
import jdplus.data.DataBlock;
import static jdplus.math.matrices.GeneralMatrix.transpose;
import static jdplus.math.matrices.LowerTriangularMatrix.solveLX;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class LowerTriangularMatrixTest {

    public LowerTriangularMatrixTest() {
    }

    @Test
    public void testRmul() {

        FastMatrix B = FastMatrix.make(10, 5);
        B.set((i, j) -> i * 2 + j * 3);
        ec.tstoolkit.maths.matrices.Matrix oB
                = new ec.tstoolkit.maths.matrices.Matrix(B.toArray(), 10, 5);
        FastMatrix L = FastMatrix.square(10);
        L.set((i, j) -> i < j ? 0 : (i + 10 * j + 1));
        ec.tstoolkit.maths.matrices.Matrix oL
                = new ec.tstoolkit.maths.matrices.Matrix(L.toArray(), 10, 10);
        LowerTriangularMatrix.LM(L, B);
        ec.tstoolkit.maths.matrices.LowerTriangularMatrix.rmul(oL, oB.all());

        DataBlock b = DataBlock.of(B.getStorage());
        DataBlock ob = DataBlock.of(oB.internalStorage());
        assertTrue(b.distance(ob) < 1e-9);

        FastMatrix M = L.extract(2, 5, 2, 5);
        FastMatrix N = B.extract(2, 5, 0, 5);

        LowerTriangularMatrix.LM(M, N);
        ec.tstoolkit.maths.matrices.Matrix oN = new ec.tstoolkit.maths.matrices.Matrix(oB.subMatrix(2, 7, 0, 5));
        ec.tstoolkit.maths.matrices.Matrix oM = new ec.tstoolkit.maths.matrices.Matrix(oL.subMatrix(2, 7, 2, 7));
        ec.tstoolkit.maths.matrices.LowerTriangularMatrix.rmul(oM, oN.all());

        DataBlock n = DataBlock.of(N.toArray());
        DataBlock on = DataBlock.of(oN.internalStorage());
        assertTrue(n.distance(on) < 1e-9);
    }

    @Test
    public void testLmul() {

        FastMatrix B = FastMatrix.make(5, 10);
        B.set((i, j) -> i * 2 + j * 3);
        ec.tstoolkit.maths.matrices.Matrix oB
                = new ec.tstoolkit.maths.matrices.Matrix(B.toArray(), 5, 10);
        FastMatrix L = FastMatrix.square(10);
        L.set((i, j) -> i < j ? 0 : (i + 10 * j + 1));
        ec.tstoolkit.maths.matrices.Matrix oL
                = new ec.tstoolkit.maths.matrices.Matrix(L.toArray(), 10, 10);
        LowerTriangularMatrix.ML(L, B);
        ec.tstoolkit.maths.matrices.LowerTriangularMatrix.lmul(oL, oB.all());
        DataBlock b = DataBlock.of(B.getStorage());
        DataBlock ob = DataBlock.of(oB.internalStorage());
        assertTrue(b.distance(ob) < 1e-9);

        FastMatrix M = L.extract(2, 5, 2, 5);
        FastMatrix N = B.extract(0, 5, 2, 5);

        LowerTriangularMatrix.ML(M, N);
        ec.tstoolkit.maths.matrices.Matrix oN = new ec.tstoolkit.maths.matrices.Matrix(oB.subMatrix(0, 5, 2, 7));
        ec.tstoolkit.maths.matrices.Matrix oM = new ec.tstoolkit.maths.matrices.Matrix(oL.subMatrix(2, 7, 2, 7));
        ec.tstoolkit.maths.matrices.LowerTriangularMatrix.lmul(oM, oN.all());

        DataBlock n = DataBlock.of(N.toArray());
        DataBlock on = DataBlock.of(oN.internalStorage());
        assertTrue(n.distance(on) < 1e-9);
    }

    @Test
    public void testRsolve() {

        FastMatrix B = FastMatrix.make(10, 5);
        B.set((i, j) -> i * 2 + j * 3);
        ec.tstoolkit.maths.matrices.Matrix oB
                = new ec.tstoolkit.maths.matrices.Matrix(B.toArray(), 10, 5);
        FastMatrix L = FastMatrix.square(10);
        L.set((i, j) -> i < j ? 0 : (i + 10 * j + 1));
        ec.tstoolkit.maths.matrices.Matrix oL
                = new ec.tstoolkit.maths.matrices.Matrix(L.toArray(), 10, 10);
        LowerTriangularMatrix.solveLX(L, B);
        ec.tstoolkit.maths.matrices.LowerTriangularMatrix.rsolve(oL, oB.all());

        DataBlock b = DataBlock.of(B.getStorage());
        DataBlock ob = DataBlock.of(oB.internalStorage());
        assertTrue(b.distance(ob) < 1e-9);

        FastMatrix M = L.extract(2, 5, 2, 5);
        FastMatrix N = B.extract(2, 5, 0, 5);

        LowerTriangularMatrix.solveLX(M, N);
        ec.tstoolkit.maths.matrices.Matrix oN = new ec.tstoolkit.maths.matrices.Matrix(oB.subMatrix(2, 7, 0, 5));
        ec.tstoolkit.maths.matrices.Matrix oM = new ec.tstoolkit.maths.matrices.Matrix(oL.subMatrix(2, 7, 2, 7));
        ec.tstoolkit.maths.matrices.LowerTriangularMatrix.rsolve(oM, oN.all());

        DataBlock n = DataBlock.of(N.toArray());
        DataBlock on = DataBlock.of(oN.internalStorage());
        assertTrue(n.distance(on) < 1e-9);
    }

    @Test
    public void testLsolve() {

        FastMatrix B = FastMatrix.make(5, 10);
        B.set((i, j) -> i * 2 + j * 3);
        ec.tstoolkit.maths.matrices.Matrix oB
                = new ec.tstoolkit.maths.matrices.Matrix(B.toArray(), 5, 10);
        FastMatrix L = FastMatrix.square(10);
        L.set((i, j) -> i < j ? 0 : (i + 10 * j + 1));
        ec.tstoolkit.maths.matrices.Matrix oL
                = new ec.tstoolkit.maths.matrices.Matrix(L.toArray(), 10, 10);
        LowerTriangularMatrix.solveXL(L, B);
        ec.tstoolkit.maths.matrices.LowerTriangularMatrix.lsolve(oL, oB.all());
        DataBlock b = DataBlock.of(B.getStorage());
        DataBlock ob = DataBlock.of(oB.internalStorage());
        assertTrue(b.distance(ob) < 1e-9);

        FastMatrix M = L.extract(2, 5, 2, 5);
        FastMatrix N = B.extract(0, 5, 2, 5);

        LowerTriangularMatrix.solveXL(M, N);
        ec.tstoolkit.maths.matrices.Matrix oN = new ec.tstoolkit.maths.matrices.Matrix(oB.subMatrix(0, 5, 2, 7));
        ec.tstoolkit.maths.matrices.Matrix oM = new ec.tstoolkit.maths.matrices.Matrix(oL.subMatrix(2, 7, 2, 7));
        ec.tstoolkit.maths.matrices.LowerTriangularMatrix.lsolve(oM, oN.all());

        DataBlock n = DataBlock.of(N.toArray());
        DataBlock on = DataBlock.of(oN.internalStorage());
        assertTrue(n.distance(on) < 1e-9);
    }

    @Test
    public void testToLower() {
        FastMatrix L = FastMatrix.square(10);
        L.set((i, j) -> i + 10 * j + 1);
        LowerTriangularMatrix.toLower(L);

        FastMatrix M = L.extract(5, 4, 0, 4);
        LowerTriangularMatrix.toLower(L);
    }

    @Test
    public void testOperations() {
        FastMatrix L = FastMatrix.square(10);
        L.set((i, j) -> i < j ? 0 : (i + 10 * j + 1));
        FastMatrix M = FastMatrix.make(10, 5);
        M.set((i, j) -> i * i + j * j);
        FastMatrix N = transpose(M);
        FastMatrix iL = LowerTriangularMatrix.inverse(L);
        FastMatrix X = GeneralMatrix.AB(iL, M);
        FastMatrix Z = M.deepClone();
        LowerTriangularMatrix.solveLX(L, Z);
        Z.sub(X);
        assertTrue(MatrixNorms.absNorm(Z) < 1e-9);
        Z = M.deepClone(); // 10 x 5
        X = GeneralMatrix.AtB(iL, M); // 5 x 10
        LowerTriangularMatrix.solveLtX(L, Z);
        Z.sub(X);
        assertTrue(MatrixNorms.absNorm(Z) < 1e-9);
        Z = N.deepClone(); // 5 x 10
        X = GeneralMatrix.AtB(M, iL); // 5 x 10
        LowerTriangularMatrix.solveXL(L, Z);
        Z.sub(X);
        assertTrue(MatrixNorms.absNorm(Z) < 1e-9);
        Z = N.deepClone(); // 5 x 10
        X = GeneralMatrix.AtBt(M, iL); // 5 x 10
        LowerTriangularMatrix.solveXLt(L, Z);
        Z.sub(X);
        assertTrue(MatrixNorms.absNorm(Z) < 1e-9);
    }

    @Test
    public void testOperations2() {
        FastMatrix B = FastMatrix.make(5, 10);
        B.set((i, j) -> i * 2 + j * 3);
        FastMatrix L = FastMatrix.square(10);
        L.set((i, j) -> i < j ? 0 : (i + 10 * j + 1));
        FastMatrix C = B.deepClone();
        LowerTriangularMatrix.ML(L, C);
        LowerTriangularMatrix.solveXL(L, C);
        C.sub(B);
        assertTrue(MatrixNorms.frobeniusNorm(C) < 1e-9);
        C = B.deepClone();
        LowerTriangularMatrix.MLt(L, C);
        LowerTriangularMatrix.solveXLt(L, C);
        C.sub(B);
        assertTrue(MatrixNorms.frobeniusNorm(C) < 1e-9);
        C = transpose(B);
        LowerTriangularMatrix.LtM(L, C);
        LowerTriangularMatrix.solveLtX(L, C);
        C.sub(transpose(B));
        assertTrue(MatrixNorms.frobeniusNorm(C) < 1e-9);
        C = transpose(B);
        LowerTriangularMatrix.LM(L, C);
        LowerTriangularMatrix.solveLX(L, C);
        C.sub(transpose(B));
        assertTrue(MatrixNorms.frobeniusNorm(C) < 1e-9);
    }

    public static void testMul() {
        int K = 10000000;
        FastMatrix L = FastMatrix.square(10);
        L.set((i, j) -> i < j ? 0 : (i + 10 * j + 1));
        ec.tstoolkit.maths.matrices.Matrix oL
                = new ec.tstoolkit.maths.matrices.Matrix(L.toArray(), 10, 10);
        FastMatrix B = FastMatrix.make(10, 5);
        FastMatrix C = FastMatrix.make(5, 10);

        B.set((i, j) -> i * 2 + j * 3);
        C.set((i, j) -> i * 2 + j * 3);
        ec.tstoolkit.maths.matrices.Matrix oB
                = new ec.tstoolkit.maths.matrices.Matrix(B.toArray(), 10, 5);
        ec.tstoolkit.maths.matrices.Matrix oC
                = new ec.tstoolkit.maths.matrices.Matrix(C.toArray(), 5, 10);
        for (int k = 0; k < 1000; ++k) {
            LowerTriangularMatrix.LM(L, B.deepClone());
        }
        for (int k = 0; k < 1000; ++k) {
            ec.tstoolkit.maths.matrices.LowerTriangularMatrix.rmul(oL, oB.clone().all());
        }
        long t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            LowerTriangularMatrix.LM(L, B.deepClone());
        }
        long t1 = System.currentTimeMillis();
        System.out.println("New");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            ec.tstoolkit.maths.matrices.LowerTriangularMatrix.rmul(oL, oB.clone().all());
        }
        t1 = System.currentTimeMillis();
        System.out.println("Old");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            LowerTriangularMatrix.ML(L, C.deepClone());
        }
        t1 = System.currentTimeMillis();
        System.out.println("New");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            ec.tstoolkit.maths.matrices.LowerTriangularMatrix.lmul(oL, oC.clone().all());
        }
        t1 = System.currentTimeMillis();
        System.out.println("Old");
        System.out.println(t1 - t0);
    }

    public static void testSolve() {
        int K = 10000000;
        FastMatrix L = FastMatrix.square(10);
        L.set((i, j) -> i < j ? 0 : (i + 10 * j + 1));
        ec.tstoolkit.maths.matrices.Matrix oL
                = new ec.tstoolkit.maths.matrices.Matrix(L.toArray(), 10, 10);
        FastMatrix B = FastMatrix.make(10, 5);
        FastMatrix C = FastMatrix.make(5, 10);

        B.set((i, j) -> i * 2 + j * 3);
        C.set((i, j) -> i * 2 + j * 3);
        ec.tstoolkit.maths.matrices.Matrix oB
                = new ec.tstoolkit.maths.matrices.Matrix(B.toArray(), 10, 5);
        ec.tstoolkit.maths.matrices.Matrix oC
                = new ec.tstoolkit.maths.matrices.Matrix(C.toArray(), 5, 10);
        for (int k = 0; k < 1000; ++k) {
            LowerTriangularMatrix.solveLX(L, B.deepClone());
        }
        for (int k = 0; k < 1000; ++k) {
            ec.tstoolkit.maths.matrices.LowerTriangularMatrix.rsolve(oL, oB.clone().all());
        }
        long t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            LowerTriangularMatrix.solveLX(L, B.deepClone());
        }
        long t1 = System.currentTimeMillis();
        System.out.println("New");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            ec.tstoolkit.maths.matrices.LowerTriangularMatrix.rsolve(oL, oB.clone().all());
        }
        t1 = System.currentTimeMillis();
        System.out.println("Old");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            LowerTriangularMatrix.solveXL(L, C.deepClone());
        }
        t1 = System.currentTimeMillis();
        System.out.println("New");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            ec.tstoolkit.maths.matrices.LowerTriangularMatrix.lsolve(oL, oC.clone().all());
        }
        t1 = System.currentTimeMillis();
        System.out.println("Old");
        System.out.println(t1 - t0);
    }

    public static void main(String[] arg) {
        testMul();
        testSolve();
    }
}
