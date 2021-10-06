/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.decomposition;

import demetra.data.Doubles;
import jdplus.data.DataBlock;
import org.junit.Test;
import static org.junit.Assert.*;
import demetra.data.DoubleSeq;

/**
 *
 * @author palatej
 */
public class HouseholderReflectionTest {
    
    private static DoubleSeq X= Doubles.of(new double[]{1, 2, 3, 4, 5});
    private static DoubleSeq Xm= Doubles.of(new double[]{-1, 2, 3, 4, 5});

    public HouseholderReflectionTest() {
    }

    @Test
    public void testHouseholder() {
        DataBlock z = DataBlock.of(X);
        HouseholderReflection hr = HouseholderReflection.of(z, false);
        hr.transform(z);
        assertEquals(Math.abs(hr.getAlpha()), Math.sqrt(55), 1e-12);
    }
    
    @Test
    public void testHouseholder2() {
        DataBlock z = DataBlock.of(X);
        DataBlock z2=z.deepClone();
        HouseholderReflection hr = HouseholderReflection.of(z2, true);
        hr.transform(z);
        z.sub(z2);
        assertTrue(z.ssq()<1e-15);
        z = DataBlock.of(Xm);
        z2=z.deepClone();
        hr = HouseholderReflection.of(z2, true);
        hr.transform(z);
        z.sub(z2);
        assertTrue(z.ssq()<1e-15);
    }
}
