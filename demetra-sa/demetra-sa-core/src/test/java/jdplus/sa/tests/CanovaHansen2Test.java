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
package jdplus.sa.tests;

import demetra.data.WeeklyData;
import org.junit.Test;
import demetra.data.DoubleSeq;
import jdplus.data.analysis.WindowFunction;

/**
 *
 * @author Jean Palate
 */
public class CanovaHansen2Test {
    
    public CanovaHansen2Test() {
    }

    @Test
    public void test_USClaims() {
         double[] x = new double[WeeklyData.US_CLAIMS2.length];
        for (int i = 0; i < x.length; ++i) {
            x[i] = WeeklyData.US_CLAIMS2[i];
        }
//        double[] x=WeeklyData.US_CLAIMS;
         for (double i=2; i<=105; ++i){
            double z = CanovaHansen2.of(DoubleSeq.of(x))
                    .periodicity(i)
                    .truncationLag(12)
                    .windowFunction(WindowFunction.Welch)
                    .lag1(true)
                    .compute();
//            System.out.println(z);
        }
         System.out.println();
            double z = CanovaHansen2.of(DoubleSeq.of(x))
                    .periodicity(365.25/7)
                    .truncationLag(53)
                    .windowFunction(WindowFunction.Welch)
                    .lag1(true)
                    .compute();
//            System.out.println(z);
   }
    
}
