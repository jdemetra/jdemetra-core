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
package jdplus.stats.tests.seasonal;

import demetra.data.Data;
import demetra.timeseries.TsData;
import jdplus.stats.tests.StatisticalTest;
import jdplus.timeseries.simplets.TsDataToolkit;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class FTestTest {
    
    public FTestTest() {
    }

    @Test
    public void testWN() {
        TsData s=TsDataToolkit.delta(Data.TS_PROD, 12);
        FTest test=new FTest(s.getValues(),12);
        StatisticalTest f = test.model(FTest.Model.WN).build();
        assertFalse(f.isSignificant(0.01));
        System.out.println(test.build());
        s=TsDataToolkit.delta(Data.TS_PROD, 1);
        test=new FTest(s.getValues(),12);
        f = test.model(FTest.Model.WN).build();
        assertTrue(f.isSignificant(0.01));
        System.out.println(test.build());
    }
    
    @Test
    public void testAR() {
        TsData s=TsDataToolkit.delta(Data.TS_PROD, 12);
        FTest test=new FTest(s.getValues(),12);
        StatisticalTest f = test.model(FTest.Model.AR).build();
        assertFalse(f.isSignificant(0.01));
        System.out.println(test.build());
        s=TsDataToolkit.delta(Data.TS_PROD, 1);
        test=new FTest(s.getValues(),12);
        f = test.model(FTest.Model.AR).build();
        assertTrue(f.isSignificant(0.01));
        System.out.println(test.build());
    }
    
    @Test
    public void testD1() {
        TsData s=TsDataToolkit.delta(Data.TS_PROD, 12);
        FTest test=new FTest(s.getValues(),12);
        StatisticalTest f = test.model(FTest.Model.D1).build();
        assertFalse(f.isSignificant(0.01));
        System.out.println(test.build());
        s=Data.TS_PROD;
        test=new FTest(s.getValues(),12);
        f = test.model(FTest.Model.D1).build();
        assertTrue(f.isSignificant(0.01));
        System.out.println(test.build());
    }
}
