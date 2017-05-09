/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.data;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class DataBlockTest {

    public DataBlockTest() {
    }

    static long K = 100000000;

    @Test
    @Ignore
    public void testSomeMethod() {
        DataBlock x = DataBlock.make(50);
        DataBlock y = DataBlock.make(50);
        x.set(i -> i);
        y.set(i -> i);

        ec.tstoolkit.data.DataBlock X = new ec.tstoolkit.data.DataBlock(50);
        ec.tstoolkit.data.DataBlock Y = new ec.tstoolkit.data.DataBlock(50);
        X.set(i -> i);
        Y.set(i -> i);

        long t0 = System.currentTimeMillis();
        double s = 0;
        for (long k = 0; k < K; ++k) {
            s += X.dot(Y.reverse());
        }

        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        s = 0;
        for (long k = 0; k < K; ++k) {
            s += x.dot(y.reverse());
        }

        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

}
