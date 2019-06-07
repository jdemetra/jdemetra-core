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
package jdplus.ucarima.estimation;

import demetra.data.Data;
import jdplus.ucarima.UcarimaModel;
import static jdplus.ucarima.UcarimaModelTest.ucmAirline;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class BurmanEstimatesTest {
    
    public BurmanEstimatesTest() {
    }

    @Test
    public void testAirline() {
        UcarimaModel ucm = ucmAirline(-.6, -.8);
        ucm = ucm.simplify();
        BurmanEstimates burman=new BurmanEstimates();
        burman.setData(Data.TS_PROD.getValues());
        burman.setUcarimaModel(ucm);
        double[] estimates = burman.estimates(0, true);
//        System.out.println(DataBlock.ofInternal(estimates));
        estimates = burman.estimates(1, true);
//        System.out.println(DataBlock.ofInternal(estimates));
        estimates = burman.estimates(2, true);
//        System.out.println(DataBlock.ofInternal(estimates));
    }
    
}
