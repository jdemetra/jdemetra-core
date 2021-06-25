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
package demetra.tramoseats.r;

import demetra.data.Data;
import demetra.timeseries.TsData;
import jdplus.seats.SeatsResults;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class SeatsTest {
    
    public SeatsTest() {
    }

    @Test
    public void testProd() {
        SeatsResults rslts = Seats.process(Data.TS_PROD, true, new int[]{3,1,1}, new int[]{0,1,1}, false, -1, -2);
//        System.out.println(rslts.getCore().getInitialComponents());
//        Map<String, Class> dictionary = rslts.getDictionary();
//        dictionary.keySet().forEach(s->System.out.println(s));
        TsData data = rslts.getData("t_lin_f", TsData.class);
        assertTrue(data.length() == 24);
    }
    
}
