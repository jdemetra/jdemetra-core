/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices;

import jdplus.data.DataBlock;
import static jdplus.math.matrices.GeneralMatrix.transpose;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class UpperTriangularMatrixTest {

    public UpperTriangularMatrixTest() {
    }

    @Test
    public void testRmul() {

        FastMatrix B = FastMatrix.make(10, 5);
        B.set((i, j) -> i * 2 + j * 3);
        ec.tstoolkit.maths.matrices.Matrix oB
                = new ec.tstoolkit.maths.matrices.Matrix(B.toArray(), 10, 5);
        FastMatrix U = FastMatrix.square(10);
        U.set((i, j) -> i > j ? 0 : (i + 10 * j + 1));
        ec.tstoolkit.maths.matrices.Matrix oU
                = new ec.tstoolkit.maths.matrices.Matrix(U.toArray(), 10, 10);
        UpperTriangularMatrix.UM(U, B);
        ec.tstoolkit.maths.matrices.UpperTriangularMatrix.rmul(oU, oB.all());

        DataBlock b = DataBlock.of(B.getStorage());
        DataBlock ob = DataBlock.of(oB.internalStorage());
        assertTrue(b.distance(ob) < 1e-9);

        FastMatrix M = U.extract(2, 5, 2, 5);
        FastMatrix N = B.extract(2, 5, 0, 5);

        UpperTriangularMatrix.UM(M, N);
        ec.tstoolkit.maths.matrices.Matrix oN = new ec.tstoolkit.maths.matrices.Matrix(oB.subMatrix(2, 7, 0, 5));
        ec.tstoolkit.maths.matrices.Matrix oM = new ec.tstoolkit.maths.matrices.Matrix(oU.subMatrix(2, 7, 2, 7));
        ec.tstoolkit.maths.matrices.UpperTriangularMatrix.rmul(oM, oN.all());

        DataBlock n = DataBlock.of(N.toArray());
        DataBlock on = DataBlock.of(oN.internalStorage());
        assertTrue(n.distance(on) < 1e-9);
    }

    @Test
    public void testmul() {

        FastMatrix B = FastMatrix.make(5, 10);
        B.set((i, j) -> i * 2 + j * 3);
        ec.tstoolkit.maths.matrices.Matrix oB
                = new ec.tstoolkit.maths.matrices.Matrix(B.toArray(), 5, 10);
        FastMatrix U = FastMatrix.square(10);
        U.set((i, j) -> i > j ? 0 : (i + 10 * j + 1));
        ec.tstoolkit.maths.matrices.Matrix oU
                = new ec.tstoolkit.maths.matrices.Matrix(U.toArray(), 10, 10);
        UpperTriangularMatrix.MU(U, B);
        ec.tstoolkit.maths.matrices.UpperTriangularMatrix.lmul(oU, oB.all());
        DataBlock b = DataBlock.of(B.getStorage());
        DataBlock ob = DataBlock.of(oB.internalStorage());
        assertTrue(b.distance(ob) < 1e-9);

        FastMatrix M = U.extract(2, 5, 2, 5);
        FastMatrix N = B.extract(0, 5, 2, 5);

        UpperTriangularMatrix.MU(M, N);
        ec.tstoolkit.maths.matrices.Matrix oN = new ec.tstoolkit.maths.matrices.Matrix(oB.subMatrix(0, 5, 2, 7));
        ec.tstoolkit.maths.matrices.Matrix oM = new ec.tstoolkit.maths.matrices.Matrix(oU.subMatrix(2, 7, 2, 7));
        ec.tstoolkit.maths.matrices.UpperTriangularMatrix.lmul(oM, oN.all());

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
        FastMatrix U = FastMatrix.square(10);
        U.set((i, j) -> i > j ? 0 : (i + 10 * j + 1));
        ec.tstoolkit.maths.matrices.Matrix oU
                = new ec.tstoolkit.maths.matrices.Matrix(U.toArray(), 10, 10);
        UpperTriangularMatrix.solveUX(U, B);
        ec.tstoolkit.maths.matrices.UpperTriangularMatrix.rsolve(oU, oB.all());

        DataBlock b = DataBlock.of(B.getStorage());
        DataBlock ob = DataBlock.of(oB.internalStorage());
        assertTrue(b.distance(ob) < 1e-9);

        FastMatrix M = U.extract(2, 5, 2, 5);
        FastMatrix N = B.extract(2, 5, 0, 5);

        UpperTriangularMatrix.solveUX(M, N);
        ec.tstoolkit.maths.matrices.Matrix oN = new ec.tstoolkit.maths.matrices.Matrix(oB.subMatrix(2, 7, 0, 5));
        ec.tstoolkit.maths.matrices.Matrix oM = new ec.tstoolkit.maths.matrices.Matrix(oU.subMatrix(2, 7, 2, 7));
        ec.tstoolkit.maths.matrices.UpperTriangularMatrix.rsolve(oM, oN.all());

        DataBlock n = DataBlock.of(N.toArray());
        DataBlock on = DataBlock.of(oN.internalStorage());
        assertTrue(n.distance(on) < 1e-9);
    }

    @Test
    public void testUsolve() {

        FastMatrix B = FastMatrix.make(5, 10);
        B.set((i, j) -> i * 2 + j * 3);
        ec.tstoolkit.maths.matrices.Matrix oB
                = new ec.tstoolkit.maths.matrices.Matrix(B.toArray(), 5, 10);
        FastMatrix U = FastMatrix.square(10);
        U.set((i, j) -> i > j ? 0 : (i + 10 * j + 1));
        ec.tstoolkit.maths.matrices.Matrix oU
                = new ec.tstoolkit.maths.matrices.Matrix(U.toArray(), 10, 10);
        UpperTriangularMatrix.solveXU(U, B);
        ec.tstoolkit.maths.matrices.UpperTriangularMatrix.lsolve(oU, oB.all());
        DataBlock b = DataBlock.of(B.getStorage());
        DataBlock ob = DataBlock.of(oB.internalStorage());
        assertTrue(b.distance(ob) < 1e-9);

        FastMatrix M = U.extract(2, 5, 2, 5);
        FastMatrix N = B.extract(0, 5, 2, 5);

        UpperTriangularMatrix.solveXU(M, N);
        ec.tstoolkit.maths.matrices.Matrix oN = new ec.tstoolkit.maths.matrices.Matrix(oB.subMatrix(0, 5, 2, 7));
        ec.tstoolkit.maths.matrices.Matrix oM = new ec.tstoolkit.maths.matrices.Matrix(oU.subMatrix(2, 7, 2, 7));
        ec.tstoolkit.maths.matrices.UpperTriangularMatrix.lsolve(oM, oN.all());

        DataBlock n = DataBlock.of(N.toArray());
        DataBlock on = DataBlock.of(oN.internalStorage());
        assertTrue(n.distance(on) < 1e-9);
    }

    @Test
    public void testOperations() {
        FastMatrix B = FastMatrix.make(5, 10);
        B.set((i, j) -> i * 2 + j * 3);
        FastMatrix U = FastMatrix.square(10);
        U.set((i, j) -> i > j ? 0 : (i + 10 * j + 1));
        FastMatrix C = B.deepClone();
        UpperTriangularMatrix.MU(U, C);
        UpperTriangularMatrix.solveXU(U, C);
        C.sub(B);
        assertTrue(MatrixNorms.frobeniusNorm(C) < 1e-9);
        C = B.deepClone();
        UpperTriangularMatrix.MUt(U, C);
        UpperTriangularMatrix.solveXUt(U, C);
        C.sub(B);
        assertTrue(MatrixNorms.frobeniusNorm(C) < 1e-9);
        C = transpose(B);
        UpperTriangularMatrix.UtM(U, C);
        UpperTriangularMatrix.solveUtX(U, C);
        C.sub(transpose(B));
        assertTrue(MatrixNorms.frobeniusNorm(C) < 1e-9);
        C = transpose(B);
        UpperTriangularMatrix.UM(U, C);
        UpperTriangularMatrix.solveUX(U, C);
        C.sub(transpose(B));
        assertTrue(MatrixNorms.frobeniusNorm(C) < 1e-9);
    }

    @Test
    public void testToUpper() {
        FastMatrix U = FastMatrix.square(10);
        U.set((i, j) -> i + 10 * j + 1);
        UpperTriangularMatrix.toUpper(U);

        FastMatrix M = U.extract(5, 4, 0, 4);
        UpperTriangularMatrix.toUpper(M);
    }

    public static void testMul() {
        int K = 10000000;
        FastMatrix U = FastMatrix.square(10);
        U.set((i, j) -> i > j ? 0 : (i + 10 * j + 1));
        ec.tstoolkit.maths.matrices.Matrix oU
                = new ec.tstoolkit.maths.matrices.Matrix(U.toArray(), 10, 10);
        FastMatrix B = FastMatrix.make(10, 5);
        FastMatrix C = FastMatrix.make(5, 10);

        B.set((i, j) -> i * 2 + j * 3);
        C.set((i, j) -> i * 2 + j * 3);
        ec.tstoolkit.maths.matrices.Matrix oB
                = new ec.tstoolkit.maths.matrices.Matrix(B.toArray(), 10, 5);
        ec.tstoolkit.maths.matrices.Matrix oC
                = new ec.tstoolkit.maths.matrices.Matrix(C.toArray(), 5, 10);
        for (int k = 0; k < 1000; ++k) {
            UpperTriangularMatrix.UM(U, B.deepClone());
        }
        for (int k = 0; k < 1000; ++k) {
            ec.tstoolkit.maths.matrices.UpperTriangularMatrix.rmul(oU, oB.clone().all());
        }
        long t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            UpperTriangularMatrix.UM(U, B.deepClone());
        }
        long t1 = System.currentTimeMillis();
        System.out.println("New");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            ec.tstoolkit.maths.matrices.UpperTriangularMatrix.rmul(oU, oB.clone().all());
        }
        t1 = System.currentTimeMillis();
        System.out.println("Old");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            UpperTriangularMatrix.MU(U, C.deepClone());
        }
        t1 = System.currentTimeMillis();
        System.out.println("New");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            ec.tstoolkit.maths.matrices.UpperTriangularMatrix.lmul(oU, oC.clone().all());
        }
        t1 = System.currentTimeMillis();
        System.out.println("Old");
        System.out.println(t1 - t0);
    }

    public static void testSolve() {
        int K = 10000000;
        FastMatrix U = FastMatrix.square(10);
        U.set((i, j) -> i > j ? 0 : (i + 10 * j + 1));
        ec.tstoolkit.maths.matrices.Matrix oU
                = new ec.tstoolkit.maths.matrices.Matrix(U.toArray(), 10, 10);
        FastMatrix B = FastMatrix.make(10, 5);
        FastMatrix C = FastMatrix.make(5, 10);

        B.set((i, j) -> i * 2 + j * 3);
        C.set((i, j) -> i * 2 + j * 3);
        ec.tstoolkit.maths.matrices.Matrix oB
                = new ec.tstoolkit.maths.matrices.Matrix(B.toArray(), 10, 5);
        ec.tstoolkit.maths.matrices.Matrix oC
                = new ec.tstoolkit.maths.matrices.Matrix(C.toArray(), 5, 10);
        for (int k = 0; k < 1000; ++k) {
            UpperTriangularMatrix.solveUX(U, B.deepClone());
        }
        for (int k = 0; k < 1000; ++k) {
            ec.tstoolkit.maths.matrices.UpperTriangularMatrix.rsolve(oU, oB.clone().all());
        }
        long t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            UpperTriangularMatrix.solveUX(U, B.deepClone());
        }
        long t1 = System.currentTimeMillis();
        System.out.println("New");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            ec.tstoolkit.maths.matrices.UpperTriangularMatrix.rsolve(oU, oB.clone().all());
        }
        t1 = System.currentTimeMillis();
        System.out.println("Old");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            UpperTriangularMatrix.solveXU(U, C.deepClone());
        }
        t1 = System.currentTimeMillis();
        System.out.println("New");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            ec.tstoolkit.maths.matrices.UpperTriangularMatrix.lsolve(oU, oC.clone().all());
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
