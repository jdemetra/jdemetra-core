/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.stats.tests;

import jd.data.DataBlock;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class BowmanShentonTest {
    
    public BowmanShentonTest() {
    }

    @Test
    public void testLegacy() {
        int N = 100;
        DataBlock X = DataBlock.make(N);
        Random rnd = new Random();
        X.set(rnd::nextDouble);

        BowmanShenton bs = new BowmanShenton(X);

        StatisticalTest test = bs.build();

        ec.tstoolkit.stats.BowmanShentonTest bs2 = new ec.tstoolkit.stats.BowmanShentonTest();
        bs2.test(new ec.tstoolkit.data.ReadDataBlock(X.getStorage()));

        assertEquals(test.getPValue(), bs2.getPValue(), 1e-9);
    }
}
