/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.ssf.models;

import demetra.arima.ssf.SsfAr;
import demetra.maths.matrices.FastMatrix;
import demetra.ssf.SsfComponent;
import demetra.maths.matrices.SymmetricMatrix;
import java.util.Random;
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
        SsfComponent cmp = SsfAr.of(new double[]{.3, -.4, .2}, 0.7, 10);
        FastMatrix z=FastMatrix.square(cmp.initialization().getStateDim());
        Random rnd=new Random();
        z.set(rnd::nextDouble);
        FastMatrix V=SymmetricMatrix.XXt(z);
        FastMatrix W=V.deepClone();
        cmp.dynamics().TVT(0, V);
        cmp.dynamics().TM(0, W);
        cmp.dynamics().TM(0, W.transpose());
        assertTrue(V.minus(W).frobeniusNorm()<1e-9);
    }
    
}
