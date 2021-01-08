/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.data;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class UtilityTest {
    
    public UtilityTest() {
    }

    @Test
    public void testIterable() {
        DoubleSeq seq=DoubleSeq.of(1,2,3,4,5,6,7,8,9,10);
        Iterable<Double> iter = Utility.asIterable(seq);
        int n=0;
        for (double s : iter){
            ++n;
        }
        assertTrue(n == 10);
    }
    
}
