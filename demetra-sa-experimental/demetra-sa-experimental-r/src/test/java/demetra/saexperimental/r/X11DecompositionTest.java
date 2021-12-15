/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.saexperimental.r;

import demetra.data.Data;
import demetra.data.DoubleSeq;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class X11DecompositionTest {
    
    public X11DecompositionTest() {
    }

    @Test
    public void testX11() {
        String t="d10";
        double[] s=Data.RETAIL_BOOKSTORES;
        X11Decomposition.Results r1 = X11Decomposition.cnX11(s, 12, true, 11, 2, "Henderson", 3, "Trapezoidal", 1.5, 2.5);
        assertTrue(r1 != null);
//        System.out.println(DoubleSeq.of(r1.getData(t, double[].class)));
        X11Decomposition.Results r2 = X11Decomposition.dafX11(s, 12, true, 11, 2, "Henderson", 3, "Trapezoidal", 1.5, 2.5);
        assertTrue(r2 != null);
 //       System.out.println(DoubleSeq.of(r2.getData(t, double[].class)));
        X11Decomposition.Results r3 = X11Decomposition.rkhsX11(s, 12, true, 11, 2, "BiWeight", true, "FrequencyResponse", true, Math.PI/8, 3, "Henderson", 1.5, 2.5);
        assertTrue(r3 != null);
//        System.out.println(DoubleSeq.of(r3.getData(t, double[].class)));
        X11Decomposition.Results r4 = X11Decomposition.lpX11(s, 12, true, 11, 2, "Henderson", 0, new double[]{}, 10, Math.PI/8, 3, "Henderson", 1.5, 2.5);
        assertTrue(r4 != null);
 //       System.out.println(DoubleSeq.of(r4.getData(t, double[].class)));
        X11Decomposition.Results r5 = X11Decomposition.process(s, 12, true, 11, 2, "Henderson", "MMSRE", "S3X5", "S3X5", 1.5, 2.5);
        assertTrue(r5 != null);
//        System.out.println(DoubleSeq.of(r5.getData(t, double[].class)));
    }
    
}
