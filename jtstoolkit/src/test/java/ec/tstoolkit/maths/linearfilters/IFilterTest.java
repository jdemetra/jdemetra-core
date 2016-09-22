/*
 * Copyright 2016 National Bank of Belgium
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
package ec.tstoolkit.maths.linearfilters;

import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.maths.realfunctions.RealFunction;
import java.util.function.DoubleFunction;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class IFilterTest {
    
    public IFilterTest() {
    }

    @Test
    public void testFunctions() {
        SymmetricFilter hf = HendersonFilters.instance.create(13);
        DoubleFunction<Complex> fr = hf.frequencyResponse();
        RealFunction gf = hf.gainFunction();
        RealFunction gf2 = hf.squaredGainFunction();
        RealFunction pf=hf.phaseFunction();
//        for (double w=0; w<=Math.PI; w+=0.001){
//            System.out.print(w);
//            System.out.print('\t');
//            System.out.print(gf.apply(w));
//            System.out.print('\t');
//            System.out.print(gf2.apply(w));
//            System.out.print('\t');
//            System.out.println(pf.apply(w));
//        }
    }
    
}
