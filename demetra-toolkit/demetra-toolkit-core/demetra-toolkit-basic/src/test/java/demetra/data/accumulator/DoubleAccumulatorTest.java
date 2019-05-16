/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.data.accumulator;

import jdplus.data.DataBlock;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Kahan summation
 * @author Jean Palate <jean.palate@nbb.be>
 */
public  class DoubleAccumulatorTest {
    
    public DoubleAccumulatorTest() {
    }

    @Test
    public void testSomeMethod() {
        int N=1000;
        double s=0;
        DataBlock block=DataBlock.make(N);
        block.set(i-> (i+1)/7999.0);
        DoubleAccumulator acc=new NeumaierAccumulator();
        DoubleAccumulator acc2=new KahanAccumulator();
        for (int i=0; i<N; ++i){
            double t=block.get(i);
            s+=t;
            acc.add(t);
            acc2.add(t);
        }
        double r=1001*500/7999.0;
        assertTrue(Math.abs(acc.sum()-r)<Math.abs(s-r));
        assertTrue(Math.abs(acc2.sum()-r)<Math.abs(s-r));
    }
    
}
