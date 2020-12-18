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

import demetra.arima.SarimaModel;
import demetra.data.Data;
import demetra.data.DoubleSeq;
import demetra.math.matrices.MatrixType;
import demetra.tramo.TramoSpec;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class TramoTest {

    public TramoTest() {
    }

    @Test
    public void testFull() {
        Tramo.Results rslt = Tramo.process(Data.TS_PROD, "TR5");
        assertTrue(rslt.getData("span.n", Integer.class) == Data.TS_PROD.length());
//        System.out.println(DoubleSeq.of(rslt.getData("sarima.parameters", double[].class)));

        SarimaModel model = rslt.getData("model", SarimaModel.class);
        String[] desc = rslt.getData("regression.description", String[].class);
//        System.out.println(model);
        assertTrue(desc != null);
    }

     
   @Test
    public void testForecast0() {
        MatrixType terror = Tramo.forecast(Data.TS_PROD, TramoSpec.TR0, null, 12);
        assertTrue(terror != null);
        System.out.println(terror);
    }

    @Test
    public void testForecast() {
        MatrixType terror = Tramo.forecast(Data.TS_PROD, TramoSpec.TRfull, null, 12);
        assertTrue(terror != null);
 //       System.out.println(terror);
    }
    
}
