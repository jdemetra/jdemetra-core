/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

import jdplus.data.DataBlock;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class SCALTest {
    
    public SCALTest() {
    }

    @Test
    public void testSomeMethod() {
        double[] a=new double[10];
        DataBlock A=DataBlock.of(a);
        A.set(i->i*i);
        DataBlock B=A.reverse();
        DataBlock O=A.deepClone();
        SCAL.apply(10, -2, DataPointer.of(A));
        SCAL.apply(10, -1, DataPointer.of(A));
        SCAL.apply(10, .5, DataPointer.of(A));
        assertTrue(O.distance(A)<1e-9);
        SCAL.apply(10, -2, DataPointer.of(B));
        SCAL.apply(10, -1, DataPointer.of(B));
        SCAL.apply(10, .5, DataPointer.of(B));
        assertTrue(O.reverse().distance(B)<1e-9);
    }
    
}
