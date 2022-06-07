/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.MatrixTransformation;
import org.junit.jupiter.api.Test;

/**
 *
 * @author palatej
 */
public class GEMMTest {

    public GEMMTest() {
    }

    @Test
    public void testSomeMethod() {
    }

    public static void main(String[] arg) {
        FastMatrix C = FastMatrix.make(1000, 50);
        C.set((i, j) -> i + j);

        FastMatrix A = FastMatrix.make(1000, 200);
        FastMatrix B = FastMatrix.make(200, 50);
        A.set((i, j) -> i - j);
        B.set((i, j) -> i / (j + 1.0));

        int K = 1000;
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            GEMM.apply(20, A, B, 10, C.deepClone());
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            GEMM.apply(20, A, B, 10, C, MatrixTransformation.None, MatrixTransformation.None);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

}
