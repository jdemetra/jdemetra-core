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
package demetra.stats.tests;

import demetra.data.DataBlock;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class LjungBoxTest {
    
    public LjungBoxTest() {
    }

    @Test
    public void testLegacy() {
        int N=100;
        DataBlock X=DataBlock.make(N);
        Random rnd=new Random();
        X.set(rnd::nextDouble);
        
        LjungBox lb=new LjungBox(X);
        
        StatisticalTest test = lb
                .lag(3)
                .autoCorrelationsCount(10)
                .build();
        
        ec.tstoolkit.stats.LjungBoxTest lb2=new ec.tstoolkit.stats.LjungBoxTest();
        lb2.setK(10);
        lb2.setLag(3);
        lb2.test(new ec.tstoolkit.data.ReadDataBlock(X.getStorage()));
        
        assertEquals(test.getPValue(), lb2.getPValue(), 1e-9);
    }
    
}
