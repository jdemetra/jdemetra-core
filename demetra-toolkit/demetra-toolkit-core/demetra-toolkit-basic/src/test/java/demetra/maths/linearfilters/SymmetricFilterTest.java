/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths.linearfilters;

import demetra.data.DataBlock;
import demetra.data.DoubleSeq;
import demetra.data.DoubleVector;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

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
        DataBlock Z = DataBlock.ofInternal(z);
        Random rnd = new Random();
        Z.set(rnd::nextDouble);
        double[] q = new double[N - K + 1];
        double[] q2 = new double[N - K + 1];
        DataBlock Q = DataBlock.ofInternal(q2);
        DoubleVector GQ = DoubleVector.of(q);
        sf.apply((DoubleSeq)Z, GQ);
        sf.apply(Z, Q);
        assertTrue(GQ.distance(Q)<1e-9);
    }

}
