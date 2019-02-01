/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.ssf.models;

import demetra.arima.ssf.SsfAr2;
import demetra.maths.matrices.Matrix;
import demetra.ssf.SsfComponent;
import demetra.ssf.StationaryInitialization;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class SsfAr2Test {

    public SsfAr2Test() {
    }

    @Test
    public void testInitialization() {
        SsfComponent cmp = SsfAr2.of(new double[]{1.2, -.6}, 1, 5, 6);
        int dim = cmp.initialization().getStateDim();
        Matrix I = StationaryInitialization.of(cmp.dynamics(), dim);
        Matrix P = Matrix.square(dim);
        cmp.initialization().Pf0(P);
        assertTrue(I.minus(P).frobeniusNorm() < 1e-12);

    }

}
