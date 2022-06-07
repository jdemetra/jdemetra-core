/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.ssf.models;

import jdplus.arima.ssf.SsfAr;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.MatrixNorms;
import jdplus.ssf.StateComponent;
import jdplus.ssf.StationaryInitialization;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class SsfArTest {

    public SsfArTest() {
    }

    @Test
    public void testInitialization() {
        StateComponent cmp = SsfAr.of(new double[]{1.2, -.6}, 1, 5);
        int dim = cmp.initialization().getStateDim();
        FastMatrix I = StationaryInitialization.of(cmp.dynamics(), dim);
        FastMatrix P = FastMatrix.square(dim);
        cmp.initialization().Pf0(P);
        assertTrue(MatrixNorms.frobeniusNorm(I.minus(P)) < 1e-9);
    }


}
