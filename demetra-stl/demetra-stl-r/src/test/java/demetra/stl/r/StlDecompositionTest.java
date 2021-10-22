/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.stl.r;

import demetra.data.Data;
import org.junit.Test;
import static org.junit.Assert.*;
import demetra.math.matrices.Matrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class StlDecompositionTest {
    
    public StlDecompositionTest() {
    }

    @Test
    public void testFilter() {
        assertTrue(null != StlDecomposition.loess(Data.ABS_RETAIL, 13, 1, 1));
    }
    
    @Test
    public void testStl() {
        Matrix decomp = StlDecomposition.process(Data.ABS_RETAIL, 12, true, 7, 23, true);
//        System.out.println(decomp);
        assertTrue(null != decomp);
    }
}
