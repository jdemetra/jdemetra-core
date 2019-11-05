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
package jdplus.maths.functions;

import jdplus.math.functions.CubicSpline;
import java.util.function.DoubleUnaryOperator;
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
    public void testSimple() {
        System.out.println("Test");
        DoubleUnaryOperator fn = CubicSpline.of(new double[]{5, 10, 15, 20} , new double[]{-3, 20, -10, 5});
        for (int i=0; i<25; ++i){
            double f=fn.applyAsDouble(i);
//            System.out.println(f);
        }
    }
    
    @Test
    public void testConstant() {
        System.out.println("Constant");
        DoubleUnaryOperator fn = CubicSpline.of(new double[]{5, 10, 15, 20} , new double[]{1, 1, 1, 1});
        for (int i=0; i<25; ++i){
            assertEquals(1, fn.applyAsDouble(i), 1e-9);
        }
    }
    
    @Test
    public void testLinear() {
        System.out.println("Linear");
        DoubleUnaryOperator fn = CubicSpline.of(new double[]{5, 10, 15, 20} , new double[]{5, 10, 15, 20});
        for (int i=0; i<25; ++i){
            assertEquals(i, fn.applyAsDouble(i), 1e-9);
        }
    }
    
}
