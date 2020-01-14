/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.ssf.models;

import jdplus.arima.ssf.SsfAr;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.SymmetricMatrix;
import java.util.Random;
import jdplus.math.matrices.MatrixNorms;
import jdplus.ssf.StateComponent;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class ARTest {
    
    public ARTest() {
    }

    @Test
    public void testTVT() {
        StateComponent cmp = SsfAr.of(new double[]{.3, -.4, .2}, 0.7, 10);
        Matrix z=Matrix.square(cmp.initialization().getStateDim());
        Random rnd=new Random();
        z.set((i,j)->rnd.nextDouble());
        Matrix V=SymmetricMatrix.XXt(z);
        Matrix W=V.deepClone();
        cmp.dynamics().TVT(0, V);
        cmp.dynamics().TM(0, W);
        cmp.dynamics().MTt(0, W);
        assertTrue(MatrixNorms.frobeniusNorm(V.minus(W))<1e-9);
    }
    
}
