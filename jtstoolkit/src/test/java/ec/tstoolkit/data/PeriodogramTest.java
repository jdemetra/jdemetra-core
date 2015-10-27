/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.data;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class PeriodogramTest {

    public PeriodogramTest() {
    }

    @Test
    public void testSum() {
        for (int K = 12; K < 120; ++K) {
            DataBlock z = new DataBlock(K);
            z.randomize(0);
            Periodogram p = new Periodogram(z, false);
            DataBlock q = new DataBlock(p.getP());
            assertTrue(Math.abs(q.sum() - K) < 1e-6);
            p = new Periodogram(z, true);
            q = new DataBlock(p.getP());
            assertTrue(Math.abs(q.sum() - K) < 1e-6);
        }
    }

}
