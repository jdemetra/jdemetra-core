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
public class GammaTest {
    
    public GammaTest() {
    }

    @Test
    public void testChi2() {
        Chi2 chi2=new Chi2(12);
        Gamma gamma=new Gamma(6, 2);
        
        for (int i=1; i<=1000; ++i){
            double x=i*.01;
            assertEquals(chi2.getDensity(x), gamma.getDensity(x), 1e-9);
            assertEquals(chi2.getProbability(x, ProbabilityType.Lower), gamma.getProbability(x, ProbabilityType.Lower), 1e-9);
        }
    }
    
}
