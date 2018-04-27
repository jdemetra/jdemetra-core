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
package ec.tstoolkit.jdr.regarima;

import data.Data;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.timeseries.simplets.TsData;
import jdr.spec.tramoseats.TramoSpec;
import jdr.spec.ts.Utility.Dictionary;
import jdr.spec.x13.RegArimaSpec;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class ProcessorTest {
    
    public ProcessorTest() {
    }

    @Test
    public void testFixedVar() {
        TsData x=Data.M1;
        TramoSpec spec=TramoSpec.of("TRfull");
        spec.getRegression().addUserDefinedVariable("m2", "Series", .1);
        Dictionary dic=new Dictionary();
        dic.add("m2", Data.M2);
        Processor.Results rslt = Processor.tramo(x, spec.getCore(), dic);
        assertTrue(rslt.getModel() != null);
    }
    
    @Test
    public void testFixedVar2() {
        TsData x=Data.M1;
        RegArimaSpec spec=RegArimaSpec.of("RG5c");
        spec.getRegression().addUserDefinedVariable("m2", "Series", .1);
        Dictionary dic=new Dictionary();
        dic.add("m2", Data.M2);
        Processor.Results rslt = Processor.x12(x, spec.getCore(), dic);
        assertTrue(rslt.getModel() != null);
    }
}
