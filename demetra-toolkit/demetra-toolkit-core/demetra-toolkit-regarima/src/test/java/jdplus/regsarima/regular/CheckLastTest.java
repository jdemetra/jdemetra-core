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
package jdplus.regsarima.regular;

import demetra.data.Data;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class CheckLastTest {
    
    public CheckLastTest() {
    }

    @Test
    public void testTR0() {
        ec.tstoolkit.modelling.arima.CheckLast ocl=
                new ec.tstoolkit.modelling.arima.CheckLast(ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TRfull.build());
        ocl.setBackCount(12);
        ec.tstoolkit.timeseries.simplets.TsData s=new ec.tstoolkit.timeseries.simplets.TsData(
        ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1992, 0, Data.RETAIL_BOOKSTORES, true);
        ocl.check(s);
    }
    
}
