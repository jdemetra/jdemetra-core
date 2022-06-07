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
package demetra.r;

import demetra.data.Data;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate
 */
public class ArimaEstimationTest {
    
    public ArimaEstimationTest() {
    }
    
    @Test
    public void testProd() {
        ArimaEstimation arima = new ArimaEstimation();
        arima.setOrder(new int[]{3, 1, 1});
        arima.setPeriod(12);
        arima.setSeasonalOrder(new int[]{0, 1, 1});
        arima.setY(Data.RETAIL_BOOKSTORES);
        ArimaEstimation.Results rslt = arima.process();
//        System.out.println(rslt.getArima());

        LinkedHashMap<String, Class> dic = new LinkedHashMap<>();
        ArimaEstimation.Results.getMapping().fillDictionary(null, dic, true);
        assertTrue(dic.size()>0);
//        dic.keySet().forEach(n -> System.out.println(n));
//        assertTrue(null != rslt.getData("sarima.parameters", double[].class));
//        long t0 = System.currentTimeMillis();
//        for (int i = 0; i < 1000000; ++i) {
//            rslt.getData("sarima.parameters", double[].class);
//        }
//        long t1 = System.currentTimeMillis();
//        System.out.println(t1 - t0);
        double[] sp = rslt.getData("sarima.spectrum", double[].class);
        assertTrue(sp != null);
    }
    
}
