/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.ssf;

import jdplus.ssf.StationaryInitialization;
import jdplus.ssf.StateComponent;
import jdplus.arima.ssf.SsfArima;
import jdplus.sarima.SarimaModel;
import demetra.arima.SarimaOrders;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.MatrixNorms;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class StationaryInitializationTest {

    public StationaryInitializationTest() {
    }

    @Test
    public void testArma() {
        SarimaOrders spec = new SarimaOrders(12);
        spec.setP(3);
        spec.setQ(1);
        spec.setBp(1);
        spec.setBq(1);
        SarimaModel arima = SarimaModel.builder(spec)
                .setDefault(-.3, -.9)
                .build();
        StateComponent cmp = SsfArima.of(arima);
        int dim = cmp.initialization().getStateDim();
        Matrix I = StationaryInitialization.of(cmp.dynamics(), dim);
        Matrix P = Matrix.square(dim);
        cmp.initialization().Pf0(P);
        assertTrue(MatrixNorms.frobeniusNorm(I.minus(P)) < 1e-12);
    }

    //@Test
    public void stressTestArma() {
        SarimaOrders spec = new SarimaOrders(12);
        spec.setP(3);
        spec.setQ(1);
        spec.setBp(1);
        spec.setBq(1);
        SarimaModel arima = SarimaModel.builder(spec)
                .setDefault(-.3, -.9)
                .build();
        StateComponent cmp = SsfArima.of(arima);
        int dim = cmp.initialization().getStateDim();
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            Matrix I = StationaryInitialization.of(cmp.dynamics(), dim);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1-t0);
    }
}
