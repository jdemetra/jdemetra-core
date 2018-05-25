/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.data;

import static demetra.data.Doubles.op;
import static demetra.data.Doubles.fastOp;
import demetra.maths.matrices.Matrix;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class DoublesTest {

    public DoublesTest() {
    }

    @Test
    public void testOp() {
        Matrix m = Matrix.make(200, 20);
        Random rnd = new Random();
        m.set(rnd::nextDouble);
        DoubleSequence a = m.column(0);
        for (int i = 1; i < m.getColumnsCount(); ++i) {
            a = op(a, m.column(i), (x, y) -> x + y);
        }
        DoubleSequence b = m.column(0);
        for (int i = 1; i < m.getColumnsCount(); ++i) {
            b = fastOp(b, m.column(i), (x, y) -> x + y);
        }
        assertTrue(Doubles.distance(a, b) < 1e-12);
    }

    @Test
    @Ignore
    public void stressTestOp() {
        int K = 100000;
        Matrix m = Matrix.make(200, 20);
        Random rnd = new Random();
        m.set(rnd::nextDouble);
        long t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            DoubleSequence a = m.column(0);
            for (int i = 1; i < m.getColumnsCount(); ++i) {
                a = op(a, m.column(i), (x, y) -> x + y);
            }
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            DoubleSequence b = m.column(0);
            for (int i = 1; i < m.getColumnsCount(); ++i) {
                b = fastOp(b, m.column(i), (x, y) -> x + y);
            }
            Doubles.commit(b);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }
}
