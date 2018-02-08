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

import demetra.stats.tests.seasonal.CanovaHansen;
import demetra.data.Data;
import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import static demetra.timeseries.simplets.TsDataToolkit.delta;
import static demetra.timeseries.simplets.TsDataToolkit.log;
import org.junit.Test;
import static org.junit.Assert.*;

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
        CanovaHansen ch = CanovaHansen.test(DoubleSequence.of(Data.US_UNEMPL), 4)
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
        CanovaHansen ch = CanovaHansen.test(DoubleSequence.of(Data.US_UNEMPL), 4)
                .variables(CanovaHansen.Variables.Trigonometric)
                .truncationLag(4)
                .build();
//        System.out.println(ch.test(0, 2));
//        System.out.println(ch.test(2));
//        System.out.println(ch.testAll());
    }

    @Test
    public void testP_dummy() {
        DoubleSequence y = delta(log(Data.TS_PROD), 1).getValues();
//        System.out.println("dummies");
        CanovaHansen ch = CanovaHansen.test(y, 12)
                .lag1(false)
                .truncationLag(12)
                .startPosition(1)
                .build();

//        for (int i = 0; i < 12; ++i) {
//            System.out.println(ch.test(i));
//        }
//        System.out.println(ch.testAll());
    }

    @Test
    public void testP_trig() {
        DoubleSequence y = delta(log(Data.TS_PROD), 1).getValues();
//        System.out.println("dummies");
        CanovaHansen ch = CanovaHansen.test(y, 12)
                .lag1(false)
                .truncationLag(12)
                .startPosition(1)
                .variables(CanovaHansen.Variables.Trigonometric)
                .build();
//        for (int i = 0; i < 5; ++i) {
//            System.out.println(ch.test(2 * i, 2));
//        }
//        System.out.println(ch.testAll());
    }

}
