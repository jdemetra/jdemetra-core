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
package jdplus.regarima.outlier;

import jdplus.regarima.outlier.CriticalValueComputer;
import java.util.function.IntToDoubleFunction;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class CriticalValueComputerTest {
    
    public CriticalValueComputerTest() {
    }

    @Test
    public void testByLength() {
        IntToDoubleFunction sc = CriticalValueComputer.simpleComputer();
        IntToDoubleFunction ac = CriticalValueComputer.advancedComputer();
        for (int i=40; i<600; i+=20){
            assertTrue(sc.applyAsDouble(i)<=4.3 && sc.applyAsDouble(i)>=3.3);
            assertTrue(ac.applyAsDouble(i)<=4.3 && ac.applyAsDouble(i)>=3.5);
//            System.out.print(i);
//            System.out.print('\t');
//            System.out.print(sc.applyAsDouble(i));
//            System.out.print('\t');
//            System.out.println(ac.applyAsDouble(i));
        }
    }
    
}
