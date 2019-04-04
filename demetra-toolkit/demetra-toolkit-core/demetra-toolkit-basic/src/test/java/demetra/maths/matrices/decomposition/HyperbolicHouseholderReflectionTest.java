/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths.matrices.decomposition;

import demetra.data.DataBlock;
import org.junit.Test;
import static org.junit.Assert.*;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
public class HyperbolicHouseholderReflectionTest {

    private static DoubleSeq X = DoubleSeq.copyOf(1, 2, 3, 4, 5);

    public HyperbolicHouseholderReflectionTest() {
    }

    @Test
    public void testSomeMethod() {
        DataBlock z = DataBlock.of(X);
        HyperbolicHouseholderReflection hr = HyperbolicHouseholderReflection.of(z, 3);
        assertEquals(hr.getNrm2(), Math.sqrt(-27), 1e-12);
    }

}
