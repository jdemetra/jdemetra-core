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
import demetra.data.DoubleSequence;
import java.util.LinkedHashMap;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class ArimaEstimationTest {
    
    public ArimaEstimationTest() {
    }

    @Test
    public void testProd() {
        ArimaEstimation arima=new ArimaEstimation();
        arima.setOrder(new int[]{3,1,1});
        arima.setPeriod(12);
        arima.setSeasonalOrder(new int[]{1,1,1});
        arima.setY(Data.RETAIL_BOOKSTORES);
        ArimaEstimation.Results rslt = arima.process();
//        System.out.println(rslt.getArima());
        
        LinkedHashMap<String, Class> dic=new LinkedHashMap<>();
        ArimaEstimation.Results.getMapping().fillDictionary(null, dic, true);
//        dic.keySet().forEach(n->System.out.println(n));
        assertTrue(null != rslt.getData("arima.parameters", double[].class));
//        System.out.println(DoubleSequence.of(rslt.getData("arima.parameters", double[].class)));
//        
//        System.out.println(rslt.getParametersCovariance().diagonal());
    }
    
}
