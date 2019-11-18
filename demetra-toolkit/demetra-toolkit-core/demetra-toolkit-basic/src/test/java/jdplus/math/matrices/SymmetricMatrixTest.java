/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class SymmetricMatrixTest {

 
    public SymmetricMatrixTest() {
    }

    @Test
    public void testRandomize() {
        Matrix S = Matrix.square(10);
        SymmetricMatrix.randomize(S, null);
        assertTrue(S.isSymmetric());
    }

}
