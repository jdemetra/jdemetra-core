/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

import jdplus.math.matrices.MatrixNorms;
import ec.tstoolkit.random.JdkRNG;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.MatrixException;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.math.matrices.UpperTriangularMatrix;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class CholeskyTest {

    public CholeskyTest() {
    }

    @Test
    public void testUCholesky() {
        Matrix X = Matrix.make(10, 5);
        JdkRNG rng = JdkRNG.newRandom(0);
        X.set((i, j) -> rng.nextDouble());
        Matrix S = SymmetricMatrix.XtX(X);
        Matrix T = S.deepClone();
        SymmetricMatrix.ucholesky(T);
        UpperTriangularMatrix.toUpper(T);
        Matrix del = SymmetricMatrix.UtU(T).minus(S);
        assertTrue(MatrixNorms.absNorm(del) < 1e-9);
    }

    @Test
    public void testLCholesky() {
        Matrix X = Matrix.make(10, 5);
        JdkRNG rng = JdkRNG.newRandom(0);
        X.set((i, j) -> rng.nextDouble());
        Matrix S = SymmetricMatrix.XtX(X);
        Matrix T = S.deepClone();
        SymmetricMatrix.lcholesky(T);
        Matrix del = SymmetricMatrix.LLt(T).minus(S);
        assertTrue(MatrixNorms.absNorm(del) < 1e-9);
    }

    @Test
    public void testSingularLCholesky() {
        Matrix X = Matrix.make(3, 5);
        JdkRNG rng = JdkRNG.newRandom(0);
        X.set((i, j) -> rng.nextDouble());
        Matrix S = SymmetricMatrix.XtX(X);
        Matrix T = S.deepClone();
        boolean ok = true;
        try {
            SymmetricMatrix.lcholesky(T);
        } catch (MatrixException err) {
            ok = false;
        }
        assertTrue(!ok);

        T = S.deepClone();
        SymmetricMatrix.lcholesky(T, 1e-9);
        Matrix del = SymmetricMatrix.LLt(T).minus(S);
        assertTrue(MatrixNorms.absNorm(del) < 1e-9);
    }

    @Test
    public void testSingularUCholesky() {
        Matrix X = Matrix.make(3, 5);
        JdkRNG rng = JdkRNG.newRandom(0);
        X.set((i, j) -> rng.nextDouble());
        Matrix S = SymmetricMatrix.XtX(X);
        Matrix T = S.deepClone();
        boolean ok = true;
        try {
            SymmetricMatrix.ucholesky(T);

        } catch (MatrixException err) {
            ok = false;
        }
        assertTrue(!ok);

        T = S.deepClone();
        SymmetricMatrix.ucholesky(T, 1e-9);
        UpperTriangularMatrix.toUpper(T);
        Matrix del = SymmetricMatrix.UtU(T).minus(S);
        assertTrue(MatrixNorms.absNorm(del) < 1e-9);
    }

    public static void main(String[] args) {
        int N = 15, K = 1000000;
        Matrix X = Matrix.make(2 * N, N);
        JdkRNG rng = JdkRNG.newRandom(0);
        X.set((i, j) -> rng.nextDouble());
        Matrix S = SymmetricMatrix.XtX(X);

         Cholesky C=new Cholesky();
      long t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            Matrix T = S.deepClone();
             C.lcholesky(T);
       }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
         t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            Matrix T = S.deepClone();
            SymmetricMatrix.lcholesky(T);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            Matrix T = S.deepClone();
            C.ucholesky(T);
        } 
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            Matrix T = S.deepClone();
            SymmetricMatrix.ucholesky(T);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

}
