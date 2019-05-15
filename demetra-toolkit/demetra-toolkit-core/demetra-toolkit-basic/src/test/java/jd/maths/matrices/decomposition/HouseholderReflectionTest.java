/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jd.maths.matrices.decomposition;

import jd.data.DataBlock;
import org.junit.Test;
import static org.junit.Assert.*;
import demetra.data.DoubleSeq;

/**
 *
 * @author palatej
 */
public class HouseholderReflectionTest {
    
    private static DoubleSeq X=DoubleSeq.copyOf(new double[]{1,2,3,4,5});
    
    public HouseholderReflectionTest() {
    }

    @Test
    public void testHouseholder() {
        DataBlock z = DataBlock.of(X);
        HouseholderReflection hr = HouseholderReflection.of(z);
        assertEquals(hr.getNrm2(), Math.sqrt(55), 1e-12);
    }
    
}
