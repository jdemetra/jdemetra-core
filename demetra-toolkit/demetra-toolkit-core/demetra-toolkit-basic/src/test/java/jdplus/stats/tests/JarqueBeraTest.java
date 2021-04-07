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
package jdplus.stats.tests;

import demetra.stats.StatisticalTest;
import java.util.Random;
import jdplus.data.DataBlock;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class JarqueBeraTest {
    
    public JarqueBeraTest() {
    }

    @Test
    public void testLegacy() {
        int N = 100;
        DataBlock X = DataBlock.make(N);
        Random rnd = new Random();
        X.set(i->1/(10+rnd.nextDouble()));

        StatisticalTest test = new BowmanShenton(X)
                .build();

        StatisticalTest test1 = new JarqueBera(X)
                .degreeOfFreedomCorrection(0) // not necessary: 0 is the default)
                .build();

        StatisticalTest test2 = new JarqueBera(X)
                .correctionForSample()
                .build();

        assertEquals(test.getPvalue(), test1.getPvalue(), 1e-9);
        
        System.out.println(test1.getPvalue());
        System.out.println(test2.getPvalue());
    }
    
}
