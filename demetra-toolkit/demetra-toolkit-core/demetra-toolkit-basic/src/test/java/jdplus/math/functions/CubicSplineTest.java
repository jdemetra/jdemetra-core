/*
 * Copyright 2017 National Bank of Belgium
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
package jdplus.math.functions;

import java.util.Random;
import java.util.function.DoubleUnaryOperator;
import jdplus.data.DataBlock;
import jdplus.math.matrices.Matrix;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class CubicSplineTest {

    public CubicSplineTest() {
    }

    @Test
    public void testConstant() {
        DoubleUnaryOperator fn = CubicSpline.of(new double[]{5, 10, 15, 20}, new double[]{1, 1, 1, 1});
        for (int i = 0; i < 25; ++i) {
            assertEquals(1, fn.applyAsDouble(i), 1e-9);
        }
    }

    @Test
    public void testLinear() {
        DoubleUnaryOperator fn = CubicSpline.of(new double[]{5, 10, 15, 20}, new double[]{5, 10, 15, 20});
        for (int i = 0; i < 25; ++i) {
            assertEquals(i, fn.applyAsDouble(i), 1e-9);
        }
    }

}
