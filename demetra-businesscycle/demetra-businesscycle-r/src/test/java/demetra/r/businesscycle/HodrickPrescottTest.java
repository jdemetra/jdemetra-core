/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.r.businesscycle;

import demetra.data.Data;
import demetra.data.DoubleSeq;
import demetra.math.matrices.MatrixType;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class HodrickPrescottTest {
    
    public HodrickPrescottTest() {
    }

    @Test
    public void testHP() {
        MatrixType tc = HodrickPrescott.filter(Data.NILE, 0, 20);
        DoubleSeq sum = tc.column(0).op(tc.column(1), (a,b)->a+b);
        assertTrue(sum.distance(DoubleSeq.of(Data.NILE))<1e-9);
    }
    
}
