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
package demetra.tramo;

import demetra.tramo.TramoProcessor;
import demetra.data.Data;
import ec.tstoolkit.modelling.arima.IPreprocessor;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class TramoProcessorTest {
    
    public TramoProcessorTest() {
    }

    @Test
    public void testProd() {
        TramoProcessor processor=TramoProcessor.builder().build();
        processor.process(Data.TS_PROD, null);
    }
    
    @Test
    public void testProdLegacy() {
        IPreprocessor processor = ec.tstoolkit.modelling.arima.tramo.TramoSpecification.TRfull.build();
        ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, Data.PROD, true);
        processor.process(s, null);
    }
    
    
}
