/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.spi;

import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.spi.MatrixDecompositions;
import demetra.maths.matrices.spi.MatrixOperations;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.SymmetricMatrix;
import static jdplus.spi.MatrixOperationsProcessorTest.random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class MatrixDecompositionsProcessorTest {

    public MatrixDecompositionsProcessorTest() {
    }

    @Test
    public void testCholesky() {
        int n = 50, m = 20;
        Matrix A = random(m, n);
        Matrix S = MatrixOperations.XXt(A);
        Matrix L = MatrixDecompositions.cholesky(S);
        Matrix P = MatrixOperations.XXt(L);
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < m; ++j) {
                assertEquals(P.get(i, j), S.get(i, j), 1e-12);
            }
        }
    }

    @Test
    public void testCholesky2() {
        // singular matrix
        int n = 20, m = 50;
        Matrix A = random(m, n);
        Matrix S = MatrixOperations.XXt(A);
        Matrix L = MatrixDecompositions.cholesky(S);
        Matrix P = MatrixOperations.XXt(L);
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < m; ++j) {
                assertEquals(P.get(i, j), S.get(i, j), 1e-12);
            }
        }
    }

    public static void main(String[] arg) {
        stressCholesky();
    }

    private static void stressCholesky() {
        int n = 70, m = 50;
        Matrix A = random(m, n);
        Matrix S = MatrixOperations.XXt(A);
        Matrix L = MatrixDecompositions.cholesky(S);
        Matrix P = MatrixOperations.XXt(L);
        CanonicalMatrix a = CanonicalMatrix.of(A);
        CanonicalMatrix s = SymmetricMatrix.XXt(a);
        SymmetricMatrix.lcholesky(s, 1e-9);
        CanonicalMatrix p = SymmetricMatrix.LLt(s);

        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            s = SymmetricMatrix.XXt(a);
            SymmetricMatrix.lcholesky(s, 1e-9);
            p = SymmetricMatrix.LLt(s);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);

        t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            S = MatrixOperations.XXt(A);
            L = MatrixDecompositions.cholesky(S);
            P = MatrixOperations.XXt(L);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }
}
