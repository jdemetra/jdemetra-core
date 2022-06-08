/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.linearfilters;

import jdplus.data.DataBlock;
import demetra.data.DoubleSeq;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.function.DoubleSupplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate
 */
public class SymmetricFilterTest {

    public SymmetricFilterTest() {
    }

    @Test
    public void testApply() {
        int N = 300, K = 13;
        SymmetricFilter sf = HendersonFilters.ofLength(K);
        double[] z = new double[N];
        DataBlock Z = DataBlock.of(z);
        Random rnd = new Random();
        Z.set((DoubleSupplier)rnd::nextDouble);
        double[] q = new double[N - K + 1];
        double[] q2 = new double[N - K + 1];
        DataBlock Q = DataBlock.of(q2);
        DataBlock GQ = DataBlock.of(q);
        sf.apply((DoubleSeq)Z, GQ);
        sf.apply(Z, Q);
        assertTrue(GQ.distance(Q)<1e-9);
    }

}
