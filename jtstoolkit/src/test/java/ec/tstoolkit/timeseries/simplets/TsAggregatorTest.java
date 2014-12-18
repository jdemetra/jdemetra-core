/*
 * Copyright 2013-2014 National Bank of Belgium
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

package ec.tstoolkit.timeseries.simplets;

import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class TsAggregatorTest {
    
    public TsAggregatorTest() {
    }

//    @Test
    public void demoTest() {
            int K = 100000;
            long t0 = System.currentTimeMillis();
            TsAggregator agg = new TsAggregator();
            agg.setRescalingWeights(true);
            agg.setMissingsEqualsToZero(true);
            Random rnd = new Random(0);
            for (int i = 0; i < K; ++i)
            {
                TsData s = new TsData(TsFrequency.Monthly, 2000 + rnd.nextInt(10), rnd.nextInt(12), 120+rnd.nextInt(480));
                for (int k=0; k<s.getLength(); ++k)
                    s.set(k, (k + 1) * 10);
                agg.add(s);
            }
            TsData sum = agg.sum();
            long t1 = System.currentTimeMillis();
            System.out.println(t1 - t0);
            System.out.println(sum);
    }
    
}
