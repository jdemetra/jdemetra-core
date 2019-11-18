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
 * @author palatej
 */
public class DOTTest {

    public DOTTest() {
    }

    @Test
    public void testDot() {
        for (int i = 0; i < 15; ++i) {
            DataBlock y = DataBlock.make(i);
            y.set(k -> k);
            DataBlock z = DataBlock.make(i);
            z.set(1);
            assertEquals(DOT.apply(i, DataPointer.of(y), DataPointer.of(z)), (i * (i - 1)) / 2, 1e-15);
        }
    }

    public static void main(String[] args) {
        int K = 1000000, M = 50, N = 200;
        Matrix A = Matrix.make(M, N);
        JdkRNG rng = JdkRNG.newRandom(0);
        MatrixUtility.randomize(A, rng);
        DataBlock x = DataBlock.make(A.getRowsCount());
        x.set(rng::nextDouble);
        DataPointer px=DataPointer.of(x);
        for (int k = 0; k < 100; ++k) {
            DOT.apply(M, px, px);
        }
        long t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            DataBlockIterator cols = A.columnsIterator();
            while (cols.hasNext()) {
                DataBlock c = cols.next();
                c.dot(x);
            }
        }
        long t1 = System.currentTimeMillis();
        System.out.println("old dot");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            int m=A.getRowsCount(), n=A.getColumnsCount();
            DataPointer cptr=DataPointer.of(A.getStorage(), A.getStartPosition(), 1);
            for (int c=0; c<n; ++c){
                DOT.apply(m, cptr, px);
                cptr.pos+=A.getColumnIncrement();
            }
        }
        t1 = System.currentTimeMillis();
        System.out.println("DOT");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            int m=A.getRowsCount(), n=A.getColumnsCount();
            DataPointer cptr=DataPointer.of(A.getStorage(), A.getStartPosition(), 1);
            for (int c=0; c<n; ++c){
                cptr.dot(m, px);
                cptr.pos+=A.getColumnIncrement();
            }
        }
        t1 = System.currentTimeMillis();
        System.out.println("new dot");
        System.out.println(t1 - t0);

        x = DataBlock.make(A.getColumnsCount());
        x.set(rng::nextDouble);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            DataBlockIterator rows = A.rowsIterator();
            while (rows.hasNext()) {
                DataBlock c = rows.next();
                c.dot(x);
            }
        }
        t1 = System.currentTimeMillis();
        System.out.println("old dot");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        px=DataPointer.of(x);
        for (int k = 0; k < K; ++k) {
            int m=A.getRowsCount(), n=A.getColumnsCount();
            DataPointer rptr=DataPointer.of(A.getStorage(), A.getStartPosition(), A.getColumnIncrement());
            for (int r=0; r<m; ++r){
                DOT.apply(n, rptr, px);
                ++rptr.pos;
            }
        }
        t1 = System.currentTimeMillis();
        System.out.println("DOT");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        px=DataPointer.of(x);
        for (int k = 0; k < K; ++k) {
            int m=A.getRowsCount(), n=A.getColumnsCount();
            DataPointer rptr=DataPointer.of(A.getStorage(), A.getStartPosition(), A.getColumnIncrement());
            for (int r=0; r<m; ++r){
                rptr.dot(n, px);
                ++rptr.pos;
            }
        }
        t1 = System.currentTimeMillis();
        System.out.println("new dot");
        System.out.println(t1 - t0);

    }

}
