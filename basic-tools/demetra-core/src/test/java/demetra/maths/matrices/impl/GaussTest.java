/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths.matrices.impl;

import java.util.Random;
import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class GaussTest {
    
    public GaussTest() {
    }

    @Test
    public void testRegular() {
        int N=10;
        Matrix M=Matrix.square(N);
        Random rnd=new Random(0);
        M.set(()->rnd.nextDouble());
        DataBlock x=DataBlock.make(N);
        x.set(()->rnd.nextDouble());
        DataBlock y=DataBlock.make(N);
        y.product(M.rowsIterator(), x);
        Gauss lu=new Gauss();
        lu.decompose(M);
        lu.solve(y);
        assertTrue(y.distance(x)<1e-9);
    }
}
