/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.stats;

import ec.tstoolkit.data.DataBlock;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class StabilityTestTest {
    
    public StabilityTestTest() {
    }

    @Test
    public void testZeroMean() {
        DataBlock x=new DataBlock(1000);
        x.randomize(0);
        StabilityTest test=new StabilityTest();
        test.process(x);
//        System.out.println(test.getFullMeanTest().getPValue());
//        System.out.println(test.getStartMeanTest().getPValue());
//        System.out.println(test.getEndMeanTest().getPValue());
        assertTrue(test.is0EndMean() && test.is0StartMean()  && test.isSameVariance() && test.isSameMean());
    }
    
}
