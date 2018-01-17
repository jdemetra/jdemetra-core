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
package demetra.ucarima.estimation;

import demetra.data.Data;
import demetra.data.DataBlock;
import demetra.ucarima.UcarimaModel;
import static demetra.ucarima.UcarimaModelTest.ucmAirline;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class BurmanEstimatesCTest {
    
    public BurmanEstimatesCTest() {
    }

    @Test
    public void testAirline() {
        UcarimaModel ucm = ucmAirline(-.6, -.8);
        ucm = ucm.simplify();
        BurmanEstimatesC burman=new BurmanEstimatesC();
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
