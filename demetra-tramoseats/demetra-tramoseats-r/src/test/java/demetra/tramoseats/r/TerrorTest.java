/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramoseats.r;

import demetra.data.Data;
import demetra.math.matrices.MatrixType;
import demetra.tramo.TramoSpec;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class TerrorTest {
    
    public TerrorTest() {
    }

    @Test
    public void testTerror0() {
        MatrixType terror = Terror.process(Data.TS_PROD, TramoSpec.TR0, null, 12);
        assertTrue(terror != null);
 //       System.out.println(terror);
    }

    @Test
    public void testTerror() {
        MatrixType terror = Terror.process(Data.TS_PROD, TramoSpec.TRfull, null, 12);
        assertTrue(terror != null);
 //       System.out.println(terror);
    }
    
}
