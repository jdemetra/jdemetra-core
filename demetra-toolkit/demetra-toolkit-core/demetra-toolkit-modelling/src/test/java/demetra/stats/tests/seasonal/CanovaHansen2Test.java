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
package demetra.stats.tests.seasonal;

import demetra.data.WeeklyData;
import org.junit.Test;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
public class CanovaHansen2Test {
    
    public CanovaHansen2Test() {
    }

    @Test
    public void test_USClaims() {
         double[] x = new double[WeeklyData.US_CLAIMS.length - 1];
        for (int i = 0; i < x.length; ++i) {
            x[i] = Math.log(WeeklyData.US_CLAIMS[i + 1]) - Math.log(WeeklyData.US_CLAIMS[i]);
        }
//        double[] x=WeeklyData.US_CLAIMS;
         for (int i=2; i<=553; ++i){
            double z = CanovaHansen2.of(DoubleSeq.of(x))
                    .periodicity(i)
                    .compute();
//            System.out.println(z);
        }
    }
    
}
