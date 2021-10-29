/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices;

import demetra.data.DoubleSeq;
import demetra.util.IntList;
import java.util.Random;
import jdplus.data.LogSign;
import jdplus.math.matrices.decomposition.Gauss;
import jdplus.math.matrices.decomposition.Householder2;
import jdplus.math.matrices.decomposition.LUDecomposition;
import jdplus.math.matrices.decomposition.QRDecomposition;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class FastMatrixTest {

    public FastMatrixTest() {
    }

    @Test
    public void testDeterminant() {
        Random rnd = new Random(0);
        for (int N = 2; N < 20; ++N) {
            FastMatrix M = FastMatrix.square(N);
            M.set((i, j) -> rnd.nextDouble());
            LUDecomposition lu = Gauss.decompose(M);
            LogSign ls = lu.logDeterminant();
            double val = Math.exp(ls.getValue());
            double det1 = ls.isPositive() ? val : -val;

            Householder2 hous = new Householder2();
            QRDecomposition qr = hous.decompose(M);
            ls = LogSign.of(qr.rawRdiagonal(), N % 2 == 1);
            double det2 = ls.isPositive() ? val : -val;

            assertEquals(det1, det2, 1e-9);
        }
    }

    @Test
    public void testDeterminant2() {
        Random rnd = new Random(0);
        FastMatrix M = FastMatrix.square(2);
        M.set((i, j) -> rnd.nextDouble());
        assertEquals(FastMatrix.determinant(M), M.get(0, 0) * M.get(1, 1) - M.get(0, 1) * M.get(1, 0), 1e-9);
        M = FastMatrix.square(3);
        M.set((i, j) -> rnd.nextDouble());
        double det = 0;
        det += M.get(0, 0) * M.get(1, 1) * M.get(2, 2);
        det += M.get(0, 1) * M.get(1, 2) * M.get(2, 0);
        det += M.get(0, 2) * M.get(1, 0) * M.get(2, 1);
        det -= M.get(0, 2) * M.get(1, 1) * M.get(2, 0);
        det -= M.get(0, 1) * M.get(1, 0) * M.get(2, 2);
        det -= M.get(0, 0) * M.get(1, 2) * M.get(2, 1);
        assertEquals(FastMatrix.determinant(M), det, 1e-9);
    }

    @Test
    public void testSelection() {
        FastMatrix M = FastMatrix.make(10, 5);
        M.set((i, j) -> i + j);

        IntList srows = new IntList(), scols = new IntList();
        FastMatrix S = MatrixFactory.select(M, srows, scols);
        assertTrue(S.isEmpty());

        srows.add(2);
        srows.add(3);
        srows.add(5);
        srows.add(5);
        S = MatrixFactory.select(M, srows, scols);
        assertTrue(S.isEmpty());

        scols.add(2);
        scols.add(0);
        S = MatrixFactory.select(M, srows, scols);
        assertTrue(!S.isEmpty());
        assertEquals(S.get(2, 1), 5, 0);

        S = MatrixFactory.selectRows(M, srows);
        assertEquals(S.get(2, 1), 6, 0);

        S = MatrixFactory.selectColumns(M, scols);
        assertEquals(S.get(2, 1), 2, 0);
    }

    @Test
    public void testEmbed() {
        DoubleSeq q = DoubleSeq.onMapping(20, i -> i + 1);
        FastMatrix M = MatrixFactory.embed(q, 5);
//        System.out.println(M);
        assertEquals(M.get(14, 1), 18, 0);

        FastMatrix N = MatrixFactory.embed(M, 2);
//        System.out.println(N);
    }

    @Test
    public void testDelta() {
        FastMatrix M = FastMatrix.make(20, 5);
        M.set((r, c) -> r + c);

        FastMatrix D = MatrixFactory.delta(M, 1, 1);
        assertTrue(D.test(z -> z == 1));
        D = MatrixFactory.delta(M, 3, 1);
        assertTrue(D.test(z -> z == 3));
        D = MatrixFactory.delta(M, 1, 21);
        assertTrue(D.test(z -> z == 0));
    }

}
