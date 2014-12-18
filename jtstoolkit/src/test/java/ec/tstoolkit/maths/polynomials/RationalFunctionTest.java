/*
 * Copyright 2013 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */

package ec.tstoolkit.maths.polynomials;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class RationalFunctionTest {
    
    public RationalFunctionTest() {
    }

    @Test
    public void testDrop() {
        Polynomial n=Polynomial.valueOf(1,-.2,-.3), d=Polynomial.valueOf(1,-1.8,1);
        RationalFunction fn=new RationalFunction(n, d);
        RationalFunction fn2=fn.drop(10);
        for (int i=0; i<50; ++i){
            assertTrue(Math.abs(fn.get(i+10)-fn2.get(i))<1e-9);
        }
        
    }
    
}
