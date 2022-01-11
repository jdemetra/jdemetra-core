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
package jdplus.dstats;

import demetra.stats.ProbabilityType;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class InverseGammaTest {
    
    public InverseGammaTest() {
    }

    @Test
    public void testDensity() {
        InverseGamma ig=new InverseGamma(0.5, 1);
        for (int i=0; i<300; ++i){
            double density = ig.getDensity((i+1)*.01);
//            System.out.println(density);
        }
    }
    
    @Test
    public void testcdf() {
        InverseGamma ig=new InverseGamma(3, .5);
        for (int i=0; i<300; ++i){
            double probability = ig.getProbability(i*.01, ProbabilityType.Lower);
//            System.out.println(probability);
        }
    }
}
