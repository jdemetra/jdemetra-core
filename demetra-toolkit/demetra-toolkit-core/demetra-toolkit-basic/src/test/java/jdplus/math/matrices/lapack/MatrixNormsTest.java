/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

import jdplus.math.matrices.MatrixNorms;
import java.util.Random;
import jdplus.math.matrices.FastMatrix;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class MatrixNormsTest {

    public MatrixNormsTest() {
    }

    @Test
    public void testNorms() {
        FastMatrix M = FastMatrix.make(10, 5);
        FastMatrix N = FastMatrix.make(10, 10);
        Random rnd = new Random();
        M.set((i, j) -> rnd.nextDouble());
        FastMatrix Q = N.extract(3, 5, 0, 10);
        Q.copyTranspose(M);

        assertEquals(MatrixNorms.frobeniusNorm(M), MatrixNorms.frobeniusNorm(Q), 1e-9);
        assertEquals(MatrixNorms.absNorm(M), MatrixNorms.absNorm(Q), 1e-9);
        assertEquals(MatrixNorms.norm1(M), MatrixNorms.infinityNorm(Q), 1e-9);
        assertEquals(MatrixNorms.infinityNorm(M), MatrixNorms.norm1(Q), 1e-9);
    }

    public static void main(String[] arg) {
        FastMatrix M = FastMatrix.make(200, 100);
        FastMatrix N = FastMatrix.make(200, 200);
        Random rnd = new Random();
        M.set((i, j) -> rnd.nextDouble());
        FastMatrix Q = N.extract(30, 100, 0, 200);
        Q.copyTranspose(M);
        int K = 100000;
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            double n = MatrixNorms.frobeniusNorm(Q);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            double n = MatrixNorms.frobeniusNorm(M);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            double n = MatrixNorms.frobeniusNorm2(M);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

}
