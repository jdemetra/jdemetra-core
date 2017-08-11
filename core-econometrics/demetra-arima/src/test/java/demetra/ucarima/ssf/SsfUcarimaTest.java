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
package demetra.ucarima.ssf;

import demetra.data.Data;
import demetra.data.DataBlockStorage;
import demetra.ssf.dk.DkToolkit;
import demetra.ssf.univariate.DefaultSmoothingResults;
import demetra.ssf.univariate.SsfData;
import demetra.ucarima.UcarimaModel;
import static demetra.ucarima.UcarimaModelTest.ucmAirline;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class SsfUcarimaTest {

    public SsfUcarimaTest() {
    }

    @Test
    public void testDkSmoother() {
        UcarimaModel ucm = ucmAirline(-.6, -.8);
        ucm = ucm.simplify();
        SsfUcarima ssf = SsfUcarima.of(ucm);
        SsfData data = new SsfData(Data.PROD);
        DefaultSmoothingResults sd = DkToolkit.smooth(ssf, data, true);
        DataBlockStorage ds = DkToolkit.fastSmooth(ssf, data);
        for (int i = 0; i < 3; ++i) {
//            System.out.println(sd.getComponent(ssf.getComponentPosition(i)));
            assertTrue(ds.item(ssf.getComponentPosition(i)).distance(sd.getComponent(ssf.getComponentPosition(i))) < 1e-9);
        }
    }

}
