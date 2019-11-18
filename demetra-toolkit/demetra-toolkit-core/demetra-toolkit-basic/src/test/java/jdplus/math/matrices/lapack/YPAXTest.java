/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

import demetra.data.DoubleSeq;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.MatrixUtility;
import jdplus.random.JdkRNG;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class YPAXTest {

    public YPAXTest() {
    }

    @Test
    public void testSomeMethod() {
        
    }

    public static void main(String[] args) {
        int K = 1000000, M = 50, N = 200;
        Matrix A = Matrix.make(M, N);
        JdkRNG rng = JdkRNG.newRandom(0);
        MatrixUtility.randomize(A, rng);
        DataBlock x = DataBlock.make(A.getRowsCount()), y = DataBlock.make(A.getRowsCount());
        x.set(rng::nextDouble);
        y.set(rng::nextDouble);
        for (int k = 0; k < 100; ++k) {
            YPAX.apply(y, 0.001, x);
        }
        long t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            DataBlockIterator cols = A.columnsIterator();
            while (cols.hasNext()) {
                DataBlock c = cols.next();
                x.addAY(.001, c);
            }
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        DataPointer dx = DataPointer.of(x);
        for (int k = 0; k < K; ++k) {
            int m=A.getRowsCount(), n=A.getColumnsCount();
            DataPointer cptr=DataPointer.of(A.getStorage(), A.getStartPosition());
            for (int c=0; c<n; ++c){
                AXPY.apply(m, .001, cptr, dx);
                cptr.pos+=A.getColumnIncrement();
            }
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            DataBlockIterator cols = A.columnsIterator();
            while (cols.hasNext()) {
                DataBlock c = cols.next();
                YPAX.apply(x, 0.001, c);
            }
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);

        x = DataBlock.make(A.getColumnsCount());
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            DataBlockIterator rows = A.rowsIterator();
            while (rows.hasNext()) {
                DataBlock c = rows.next();
                x.addAY(.001, c);
            }
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        dx=DataPointer.of(x);
        for (int k = 0; k < K; ++k) {
            int m=A.getRowsCount(), n=A.getColumnsCount();
            DataPointer rptr=DataPointer.of(A.getStorage(), A.getStartPosition(), A.getColumnIncrement());
            for (int r=0; r<m; ++r){
                AXPY.apply(n, .001, rptr, dx);
                ++rptr.pos;
            }
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            DataBlockIterator rows = A.rowsIterator();
            while (rows.hasNext()) {
                DataBlock c = rows.next();
                YPAX.apply(x, 0.001, c);
            }
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);

    }
}
