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
package jdplus.ssf.models;

import jdplus.ssf.arima.SsfAr2;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.MatrixNorms;
import jdplus.ssf.StateComponent;
import jdplus.ssf.utility.StationaryInitialization;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class SsfAr2Test {

    public SsfAr2Test() {
    }

    @Test
    public void testInitialization() {
        StateComponent cmp = SsfAr2.of(new double[]{1.2, -.6}, 1, 5, 6);
        int dim = cmp.initialization().getStateDim();
        FastMatrix I = StationaryInitialization.of(cmp.dynamics(), dim);
        FastMatrix P = FastMatrix.square(dim);
        cmp.initialization().Pf0(P);
        assertTrue(MatrixNorms.frobeniusNorm(I.minus(P))<1e-9);
    }

}
