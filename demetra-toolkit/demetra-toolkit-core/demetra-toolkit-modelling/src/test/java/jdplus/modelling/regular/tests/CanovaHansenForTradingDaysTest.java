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
package jdplus.modelling.regular.tests;

import demetra.data.Data;
import org.junit.jupiter.api.Test;

/**
 *
 * @author PALATEJ
 */
public class CanovaHansenForTradingDaysTest {
    
    public CanovaHansenForTradingDaysTest() {
    }

    @Test
    public void testSomeMethod() {
        CanovaHansenForTradingDays ch = CanovaHansenForTradingDays
                .test(Data.TS_ABS_RETAIL.log())
                .differencingLags(1, 12)
                .truncationLag(15)
                .build();
        double all = ch.testAll();
//        for (int i = 0; i < 6; ++i) {
//            System.out.println(ch.test(i, 1));
//        }
//        System.out.println(all);
    }
    
}
