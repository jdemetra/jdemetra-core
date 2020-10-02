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
package jdplus.revisions.parametric;

import ec.tstoolkit.random.JdkRNG;
import java.util.Random;
import jdplus.math.matrices.Matrix;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class VECMComputerTest {
    
    public VECMComputerTest() {
    }

    @Test
    public void testRandom() {
        Matrix X = Matrix.make(10000, 5);
        Random rnd=new Random(0);
        X.set((i, j) -> rnd.nextGaussian());
        
        VECMComputer computer = VECMComputer.builder()
                .errorCorrectionModel(VECMComputer.ECDet.cnt)
                .build();
        computer.process(X, null);
            System.out.print(computer.traceCriticalValue(0));
            System.out.print('\t');
            System.out.println(computer.maxCriticalValue(0));
            System.out.println("");
        for (int i=0; i<5; ++i){
            System.out.print(computer.traceTest(i));
            System.out.print('\t');
            System.out.print('\t');
            System.out.println(computer.maxTest(i));
        }
    }
    
}
