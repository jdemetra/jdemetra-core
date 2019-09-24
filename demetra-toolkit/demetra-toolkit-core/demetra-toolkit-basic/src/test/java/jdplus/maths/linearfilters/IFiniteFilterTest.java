/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.linearfilters;

import java.util.Random;
import jdplus.data.DataBlock;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class IFiniteFilterTest {

    public IFiniteFilterTest() {
    }

    @Test
    public void testApply() {
        DataBlock in = DataBlock.make(5000);
        Random rnd = new Random();
        in.set(rnd::nextDouble);
        SymmetricFilter h = HendersonFilters.ofLength(365);
        DataBlock out = DataBlock.make(in.length() - h.length() + 1);
        long t0 = System.currentTimeMillis();
        h.apply(in, out);
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        out = DataBlock.make(in.length() - h.length() + 1);
        h.apply2(in, out);
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

}
