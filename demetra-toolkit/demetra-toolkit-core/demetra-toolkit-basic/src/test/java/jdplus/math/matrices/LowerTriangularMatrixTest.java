/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices;

import ec.tstoolkit.maths.matrices.SubMatrix;
import jdplus.data.DataBlock;
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

        Matrix B = Matrix.make(10, 5);
        B.set((i, j) -> i * 2 + j * 3);
        ec.tstoolkit.maths.matrices.Matrix oB
                = new ec.tstoolkit.maths.matrices.Matrix(B.toArray(), 10, 5);
        Matrix L = Matrix.square(10);
        L.set((i, j) -> i < j ? 0 : (i + 10 * j + 1));
        ec.tstoolkit.maths.matrices.Matrix oL
                = new ec.tstoolkit.maths.matrices.Matrix(L.toArray(), 10, 10);
        LowerTriangularMatrix.rmul(L, B);
        ec.tstoolkit.maths.matrices.LowerTriangularMatrix.rmul(oL, oB.all());

        DataBlock b = DataBlock.of(B.getStorage());
        DataBlock ob = DataBlock.of(oB.internalStorage());
        assertTrue(b.distance(ob) < 1e-9);

        Matrix M = L.extract(2, 5, 2, 5);
        Matrix N = B.extract(2, 5, 0, 5);

        LowerTriangularMatrix.rmul(M, N);
        ec.tstoolkit.maths.matrices.Matrix oN = new ec.tstoolkit.maths.matrices.Matrix(oB.subMatrix(2, 7, 0, 5));
        ec.tstoolkit.maths.matrices.Matrix oM = new ec.tstoolkit.maths.matrices.Matrix(oL.subMatrix(2, 7, 2, 7));
        ec.tstoolkit.maths.matrices.LowerTriangularMatrix.rmul(oM, oN.all());

        DataBlock n = DataBlock.of(N.toArray());
        DataBlock on = DataBlock.of(oN.internalStorage());
        assertTrue(n.distance(on) < 1e-9);
    }

    @Test
    public void testLmul() {

        Matrix B = Matrix.make(5, 10);
        B.set((i, j) -> i * 2 + j * 3);
        ec.tstoolkit.maths.matrices.Matrix oB
                = new ec.tstoolkit.maths.matrices.Matrix(B.toArray(), 5, 10);
        Matrix L = Matrix.square(10);
        L.set((i, j) -> i < j ? 0 : (i + 10 * j + 1));
        ec.tstoolkit.maths.matrices.Matrix oL
                = new ec.tstoolkit.maths.matrices.Matrix(L.toArray(), 10, 10);
        LowerTriangularMatrix.lmul(L, B);
        ec.tstoolkit.maths.matrices.LowerTriangularMatrix.lmul(oL, oB.all());
        DataBlock b = DataBlock.of(B.getStorage());
        DataBlock ob = DataBlock.of(oB.internalStorage());
        assertTrue(b.distance(ob) < 1e-9);

        Matrix M = L.extract(2, 5, 2, 5);
        Matrix N = B.extract(0, 5, 2, 5);

        LowerTriangularMatrix.lmul(M, N);
        ec.tstoolkit.maths.matrices.Matrix oN = new ec.tstoolkit.maths.matrices.Matrix(oB.subMatrix(0, 5, 2, 7));
        ec.tstoolkit.maths.matrices.Matrix oM = new ec.tstoolkit.maths.matrices.Matrix(oL.subMatrix(2, 7, 2, 7));
        ec.tstoolkit.maths.matrices.LowerTriangularMatrix.lmul(oM, oN.all());

        DataBlock n = DataBlock.of(N.toArray());
        DataBlock on = DataBlock.of(oN.internalStorage());
        assertTrue(n.distance(on) < 1e-9);
    }

    @Test
    public void testRsolve() {

        Matrix B = Matrix.make(10, 5);
        B.set((i, j) -> i * 2 + j * 3);
        ec.tstoolkit.maths.matrices.Matrix oB
                = new ec.tstoolkit.maths.matrices.Matrix(B.toArray(), 10, 5);
        Matrix L = Matrix.square(10);
        L.set((i, j) -> i < j ? 0 : (i + 10 * j + 1));
        ec.tstoolkit.maths.matrices.Matrix oL
                = new ec.tstoolkit.maths.matrices.Matrix(L.toArray(), 10, 10);
        LowerTriangularMatrix.rsolve(L, B);
        ec.tstoolkit.maths.matrices.LowerTriangularMatrix.rsolve(oL, oB.all());

        DataBlock b = DataBlock.of(B.getStorage());
        DataBlock ob = DataBlock.of(oB.internalStorage());
        assertTrue(b.distance(ob) < 1e-9);

        Matrix M = L.extract(2, 5, 2, 5);
        Matrix N = B.extract(2, 5, 0, 5);

        LowerTriangularMatrix.rsolve(M, N);
        ec.tstoolkit.maths.matrices.Matrix oN = new ec.tstoolkit.maths.matrices.Matrix(oB.subMatrix(2, 7, 0, 5));
        ec.tstoolkit.maths.matrices.Matrix oM = new ec.tstoolkit.maths.matrices.Matrix(oL.subMatrix(2, 7, 2, 7));
        ec.tstoolkit.maths.matrices.LowerTriangularMatrix.rsolve(oM, oN.all());

        DataBlock n = DataBlock.of(N.toArray());
        DataBlock on = DataBlock.of(oN.internalStorage());
        assertTrue(n.distance(on) < 1e-9);
    }

    @Test
    public void testLsolve() {

        Matrix B = Matrix.make(5, 10);
        B.set((i, j) -> i * 2 + j * 3);
        ec.tstoolkit.maths.matrices.Matrix oB
                = new ec.tstoolkit.maths.matrices.Matrix(B.toArray(), 5, 10);
        Matrix L = Matrix.square(10);
        L.set((i, j) -> i < j ? 0 : (i + 10 * j + 1));
        ec.tstoolkit.maths.matrices.Matrix oL
                = new ec.tstoolkit.maths.matrices.Matrix(L.toArray(), 10, 10);
        LowerTriangularMatrix.lsolve(L, B);
        ec.tstoolkit.maths.matrices.LowerTriangularMatrix.lsolve(oL, oB.all());
        DataBlock b = DataBlock.of(B.getStorage());
        DataBlock ob = DataBlock.of(oB.internalStorage());
        assertTrue(b.distance(ob) < 1e-9);

        Matrix M = L.extract(2, 5, 2, 5);
        Matrix N = B.extract(0, 5, 2, 5);

        LowerTriangularMatrix.lsolve(M, N);
        ec.tstoolkit.maths.matrices.Matrix oN = new ec.tstoolkit.maths.matrices.Matrix(oB.subMatrix(0, 5, 2, 7));
        ec.tstoolkit.maths.matrices.Matrix oM = new ec.tstoolkit.maths.matrices.Matrix(oL.subMatrix(2, 7, 2, 7));
        ec.tstoolkit.maths.matrices.LowerTriangularMatrix.lsolve(oM, oN.all());

        DataBlock n = DataBlock.of(N.toArray());
        DataBlock on = DataBlock.of(oN.internalStorage());
        assertTrue(n.distance(on) < 1e-9);
    }
    
    @Test
    public void testToLower() {
        Matrix L = Matrix.square(10);
        L.set((i, j) -> i + 10 * j + 1);
        LowerTriangularMatrix.toLower(L);
        System.out.println(L);
        
        Matrix M=L.extract(5, 4, 0, 4);
        LowerTriangularMatrix.toLower(L);
        System.out.println(M);
    }   

    public static void testMul() {
        int K = 10000000;
        Matrix L = Matrix.square(10);
        L.set((i, j) -> i < j ? 0 : (i + 10 * j + 1));
        ec.tstoolkit.maths.matrices.Matrix oL
                = new ec.tstoolkit.maths.matrices.Matrix(L.toArray(), 10, 10);
        Matrix B = Matrix.make(10, 5);
        Matrix C = Matrix.make(5, 10);

        B.set((i, j) -> i * 2 + j * 3);
        C.set((i, j) -> i * 2 + j * 3);
        ec.tstoolkit.maths.matrices.Matrix oB
                = new ec.tstoolkit.maths.matrices.Matrix(B.toArray(), 10, 5);
        ec.tstoolkit.maths.matrices.Matrix oC
                = new ec.tstoolkit.maths.matrices.Matrix(C.toArray(), 5, 10);
        for (int k = 0; k < 1000; ++k) {
            LowerTriangularMatrix.rmul(L, B.deepClone());
        }
        for (int k = 0; k < 1000; ++k) {
            ec.tstoolkit.maths.matrices.LowerTriangularMatrix.rmul(oL, oB.clone().all());
        }
        long t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            LowerTriangularMatrix.rmul(L, B.deepClone());
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
            LowerTriangularMatrix.lmul(L, C.deepClone());
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
        Matrix L = Matrix.square(10);
        L.set((i, j) -> i < j ? 0 : (i + 10 * j + 1));
        ec.tstoolkit.maths.matrices.Matrix oL
                = new ec.tstoolkit.maths.matrices.Matrix(L.toArray(), 10, 10);
        Matrix B = Matrix.make(10, 5);
        Matrix C = Matrix.make(5, 10);

        B.set((i, j) -> i * 2 + j * 3);
        C.set((i, j) -> i * 2 + j * 3);
        ec.tstoolkit.maths.matrices.Matrix oB
                = new ec.tstoolkit.maths.matrices.Matrix(B.toArray(), 10, 5);
        ec.tstoolkit.maths.matrices.Matrix oC
                = new ec.tstoolkit.maths.matrices.Matrix(C.toArray(), 5, 10);
        for (int k = 0; k < 1000; ++k) {
            LowerTriangularMatrix.rsolve(L, B.deepClone());
        }
        for (int k = 0; k < 1000; ++k) {
            ec.tstoolkit.maths.matrices.LowerTriangularMatrix.rsolve(oL, oB.clone().all());
        }
        long t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            LowerTriangularMatrix.rsolve(L, B.deepClone());
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
            LowerTriangularMatrix.lsolve(L, C.deepClone());
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
    
    public static void main(String[] arg){
        testMul();
        testSolve();
    }
}