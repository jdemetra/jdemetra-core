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

import demetra.data.Data;
import demetra.data.WeeklyData;
import static jdplus.timeseries.simplets.TsDataToolkit.delta;
import static jdplus.timeseries.simplets.TsDataToolkit.log;
import org.junit.Test;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
public class CanovaHansenTest {

    public CanovaHansenTest() {
    }

    @Test
    public void testUnempl_dummy() {
//        System.out.println("dummies");
        CanovaHansen ch = CanovaHansen.test(DoubleSeq.copyOf(Data.US_UNEMPL))
                .dummies(4)
                .truncationLag(4)
                .build();

//        for (int i = 0; i < 4; ++i) {
//            System.out.println(ch.test(i));
//        }
//        System.out.println(ch.testAll());
    }

    @Test
    public void testUnempl_trig() {
//        System.out.println("trig");
        CanovaHansen ch = CanovaHansen.test(DoubleSeq.copyOf(Data.US_UNEMPL))
                .trigonometric(4)
                .truncationLag(4)
                .build();
//        System.out.println(ch.test(0, 2));
//        System.out.println(ch.test(2));
//        System.out.println(ch.testAll());
    }

    @Test
    public void testP_dummy() {
        DoubleSeq y = delta(log(Data.TS_PROD), 1).getValues();
//        System.out.println("dummies");
        CanovaHansen ch = CanovaHansen.test(y)
                .dummies(12)
                .lag1(false)
                .truncationLag(12)
                .startPosition(1)
                .build();
 //       System.out.println(ch.robustTestCoefficients());

//        for (int i = 0; i < 12; ++i) {
//            System.out.println(ch.test(i));
//        }
//        System.out.println(ch.testAll());
    }

    @Test
    public void testP_trig() {
        DoubleSeq y = delta(log(Data.TS_PROD), 1).getValues();
//        System.out.println("dummies");
        CanovaHansen ch = CanovaHansen
                .test(y)
                .lag1(false)
                .truncationLag(15)
                .startPosition(1)
                .trigonometric(12)
                .build();
        double all = ch.testAll();
//        for (int i = 0; i < 5; ++i) {
//            System.out.println(ch.test(2 * i, 2));
//        }
        //  System.out.println(ch.robustTestCoefficients());
//        System.out.println(all);
    }

    @Test
    public void testW_trig() {
        double[] x = new double[WeeklyData.US_CLAIMS.length - 1];
        for (int i = 0; i < x.length; ++i) {
            x[i] = Math.log(WeeklyData.US_CLAIMS[i + 1]) - Math.log(WeeklyData.US_CLAIMS[i]);
        }
         DoubleSeq y = DoubleSeq.of(x);
           CanovaHansen ch = CanovaHansen.test(y)
                    .lag1(false)
                    .truncationLag(5)
                    .startPosition(1)
                    .trigonometric(53)
                    .build();
        for (int i = 0; i < 25; ++i) {
//            System.out.println(ch.test(1+2 * i, 2));
//        }
//        System.out.println(ch.testAll());
//            System.out.println(ch.robustTestCoefficients(i*2, 2).getValue());
        }
    }
    
        @Test
    public void testW_trig2() {
        double[] x = new double[WeeklyData.US_CLAIMS.length - 1];
        for (int i = 0; i < x.length; ++i) {
            x[i] = Math.log(WeeklyData.US_CLAIMS[i + 1]) - Math.log(WeeklyData.US_CLAIMS[i]);
        }
//        double[] x=WeeklyData.US_CLAIMS;
DoubleSeq y = DoubleSeq.of(x);
          for (int i = 2; i <= 553; ++i) {
         CanovaHansen ch = CanovaHansen.test(y)
                    .lag1(false)
                    .truncationLag(12)
                    .startPosition(1)
                    .specific(i, 1)
                    .build();
//        for (int i = 0; i < 5; ++i) {
//            System.out.println(ch.test(2 * i, 2));
//        }
//        System.out.println(ch.testAll());
//            System.out.println(ch.testAll());
        }
    }

}
