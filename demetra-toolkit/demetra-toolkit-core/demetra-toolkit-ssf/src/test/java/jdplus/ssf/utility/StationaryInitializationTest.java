/*
 * Copyright 2022 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.ssf.utility;

import jdplus.ssf.arima.SsfArima;
import jdplus.sarima.SarimaModel;
import demetra.arima.SarimaOrders;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.MatrixNorms;
import jdplus.ssf.StateComponent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
        StateComponent cmp = SsfArima.stateComponent(arima);
        int dim = cmp.initialization().getStateDim();
        FastMatrix I = StationaryInitialization.of(cmp.dynamics(), dim);
        FastMatrix P = FastMatrix.square(dim);
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
        StateComponent cmp = SsfArima.stateComponent(arima);
        int dim = cmp.initialization().getStateDim();
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            FastMatrix I = StationaryInitialization.of(cmp.dynamics(), dim);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1-t0);
    }
}
