/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths.linearfilters;

import jdplus.data.DataBlock;
import demetra.data.DoubleSeq;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class FiniteFilterTest {

    FiniteFilter filter;
    DataBlock in;

    public FiniteFilterTest() {
        filter = new FiniteFilter(new double[]{1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 5, 6, 7, 8}, -9);
        in = DataBlock.make(240);
        in.set(i -> 1 / (1 + i));
    }

    @Test
    public void testApply() {
        DataBlock out = DataBlock.make(in.length() - filter.length() + 1);
        filter.apply(in, out);

        DataBlock out2 = DataBlock.make(in.length() - filter.length() + 1);
        filter.apply((DoubleSeq)in, out2);

        assertTrue(out.distance(out2) < 1e-9);
    }

    @Test
    @Ignore
    public void stressTestApply() {
        int K = 1000000;
        long t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            DataBlock out = DataBlock.make(in.length() - filter.length() + 1);
            filter.apply(in, out);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);


        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            DataBlock out = DataBlock.make(in.length() - filter.length() + 1);
            filter.apply((DoubleSeq)in, out);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }
}
