/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

import java.util.Random;
import jdplus.math.matrices.FastMatrix;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class GEQP3Test {
    
    public GEQP3Test() {
    }

    @Test
    public void testRandom() {
        FastMatrix A = FastMatrix.make(100, 20);
        Random rnd = new Random(0);
        A.set((i, j) -> rnd.nextDouble());
        new GEQP3().apply(A);
    }
    
}
