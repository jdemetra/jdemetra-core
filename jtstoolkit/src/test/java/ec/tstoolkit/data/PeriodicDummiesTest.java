/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.data;

import ec.tstoolkit.maths.matrices.Matrix;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class PeriodicDummiesTest {
    
    public PeriodicDummiesTest() {
    }
    
    @Test
    public void testMatrix() {
        PeriodicDummies dummies = new PeriodicDummies(5);
        for (int len = 3; len < 12; ++len) {
            Matrix matrix = dummies.matrix(len);
            for (DataBlock row : matrix.rowList()) {
                assertTrue(row.sum() == 1);
            };
        }
    }

    @Ignore
    @Test
    public void testDisplay() {
        System.out.println(new PeriodicDummies(5).matrix(13));
    }
    
}
