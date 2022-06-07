/*
 * Copyright 2020 National Bank of Belgium
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
package internal.jdplus.math.functions.gsl.derivation;

import java.util.function.DoubleUnaryOperator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author PALATEJ
 */
public class GslDerivationTest {
    
    public GslDerivationTest() {
    }

    @Test
    public void test0() {
        double z=0.01, h = 1e-8;
        DoubleUnaryOperator fn=x -> x - 1 / x;
        double dfc = GslDerivation.centralDerivation(fn, z, h);
        assertEquals(dfc-1-1/(z*z), 0, 1e-5);

        double dff = GslDerivation.forwardDerivation(fn, z, h);
        double dfb = GslDerivation.backwardDerivation(fn, z, h);
        
        System.out.println(1+1/(z*z));
        System.out.println(dfc);
        System.out.println(dff);
        System.out.println(dfb);
        System.out.println((fn.applyAsDouble(z)-fn.applyAsDouble(z-h))/h);
     }
    
    public static void main(String[] args) {
        double z=0.01;
        DoubleUnaryOperator fn=x -> x - 1 / x;
        double h=.001;
        System.out.println(1+1/(z*z));
        for (int i=0; i<20; ++i){
            System.out.println(GslDerivation.centralDerivation(fn, z, h));
            h/=2;
        }
     }
    
}
