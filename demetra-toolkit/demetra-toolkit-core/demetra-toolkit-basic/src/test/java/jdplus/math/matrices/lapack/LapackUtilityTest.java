/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.MatrixUtility;
import jdplus.random.JdkRNG;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author palatej
 */
public class LapackUtilityTest {

    public LapackUtilityTest() {
    }

    @Test
    public void testDlapy2() {
        assertTrue(0 == LapackUtility.lapy2(0, 0));
        assertTrue(Double.isInfinite(LapackUtility.lapy2(0, Double.NEGATIVE_INFINITY)));
        assertTrue(Double.isNaN(LapackUtility.lapy2(Double.NaN, 5)));
        assertEquals(LapackUtility.lapy2(5, -2), Math.sqrt(29), 1e-15);
        assertEquals(LapackUtility.lapy2(25, 0), 25, 1e-15);
        assertEquals(LapackUtility.lapy2(1000, 0.001), Math.sqrt(1000.0 * 1000.0 + 0.001 * 0.001), 1e-15);
    }

    public static void main(String[] args) {
        int K = 1000000, M = 200, N = 50;
        FastMatrix A = FastMatrix.make(M, N);
        JdkRNG rng = JdkRNG.newRandom(0);
        MatrixUtility.randomize(A, rng);
        FastMatrix B = A.deepClone();
        DataBlock x = DataBlock.make(A.getRowsCount()), y = DataBlock.make(A.getColumnsCount());
        x.set(rng::nextDouble);
        y.set(rng::nextDouble);
        for (int k = 0; k < 100; ++k) {
            DataBlock c = A.column(0);
            LapackUtility.nrm2(c.length(), c.getStorage(), c.getStartPosition(), c.getIncrement());
        }
        for (int k = 0; k < 100; ++k) {
            DataBlock c = A.row(0);
            LapackUtility.nrm2(c);
        }
        DataBlock a = A.column(0);
        long t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            DataBlockIterator cols = A.columnsIterator();
            while (cols.hasNext()) {
                DataBlock c = cols.next();
                a.addAY(1e-9, c);
 //               LapackUtility.dasum(c.length(), c.getStorage(), c.getStartPosition(), c.getIncrement());
                
            }
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            DataBlockIterator cols = A.columnsIterator();
            while (cols.hasNext()) {
//                LapackUtility.asum(cols.next());
                LapackUtility.axpy(1e-9, cols.next(), a);
            }
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);

    }

}
