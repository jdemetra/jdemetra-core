/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.data;

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.data.Doubles;
import jdplus.math.matrices.FastMatrix;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class DoublesTest {

    public DoublesTest() {
    }

    @Test
    public void testOp() {
        FastMatrix m = FastMatrix.make(200, 20);
        Random rnd = new Random();
        m.set((i, j) -> rnd.nextDouble());
        DoubleSeq a = m.column(0);
        for (int i = 1; i < m.getColumnsCount(); ++i) {
            a = a.op(m.column(i), (x, y) -> x + y);
        }
        DoubleSeq b = m.column(0);
        for (int i = 1; i < m.getColumnsCount(); ++i) {
            b = b.fastOp(m.column(i), (x, y) -> x + y);
        }
        assertTrue(a.distance(b) < 1e-12);
    }

    public static void stressTestOp() {
        int K = 100000;
        FastMatrix m = FastMatrix.make(200, 20);
        Random rnd = new Random();
        m.set((i, j) -> rnd.nextDouble());
        long t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            DoubleSeq a = m.column(0);
            for (int i = 1; i < m.getColumnsCount(); ++i) {
                a = a.op(m.column(i), (x, y) -> x + y);
            }
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            DoubleSeq b = m.column(0);
            for (int i = 1; i < m.getColumnsCount(); ++i) {
                b = b.fastOp(m.column(i), (x, y) -> x + y);
            }
            b = Doubles.of(b);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

    public static void main(String[] arg) {
        int K = 100000;
        FastMatrix m = FastMatrix.make(1000, 30);
        Random rnd = new Random();
        m.set((i, j) -> rnd.nextDouble());
        long t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            for (int c = 0; c < m.getRowsCount(); ++c) {
                DataBlock a = m.row(c);
                double z = a.sum();
            }
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        int nr=m.getRowsCount();
        for (int k = 0; k < K; ++k) {
            for (int c = 0; c < m.getRowsCount(); ++c) {
                DoubleSeq b = m.row(c);
                DoubleSeqCursor cursor = b.cursor();
                double z = 0;
                for (int j = 0; j < m.getColumnsCount(); ++j) {
                    z += cursor.getAndNext();
                }
            }
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);

    }
}
