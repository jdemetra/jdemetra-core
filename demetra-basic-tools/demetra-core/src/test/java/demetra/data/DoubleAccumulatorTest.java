/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.data;

import demetra.data.accumulator.KahanAccumulator;
import demetra.data.transformation.DoubleAccumulator;
import demetra.data.accumulator.NeumaierAccumulator;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Kahan summation
 * @author Jean Palate <jean.palate@nbb.be>
 */
public  class DoubleAccumulatorTest {
    
    public DoubleAccumulatorTest() {
        Random rnd=new Random();
        int N=1000;
        double s=0;
        DataBlock block=DataBlock.make(N);
        block.set(()->rnd.nextDouble()-.5);
        DoubleAccumulator acc=new NeumaierAccumulator();
        DoubleAccumulator acc2=new KahanAccumulator();
        for (int i=0; i<N; ++i){
            double t=block.get(i);
            s+=t;
            acc.add(t);
            acc2.add(t);
        }
    }

    @Test
    public void testSomeMethod() {
    }
    
}
