/*
 * Copyright 2013-2014 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */

package ec.tstoolkit.algorithm;

import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class ProcessingContextTest {
    
    public ProcessingContextTest() {
        ProcessingContext active=ProcessingContext.getActiveContext();
        TsVariables vars=new TsVariables();
        vars.set("Exports", new TsVariable(data.Data.X));
        active.getTsVariableManagers().set("Test", vars);
    }

    @Test
    public void testRegressionVariables() {
        ProcessingContext active=ProcessingContext.getActiveContext();
        assertTrue(active.getTsVariable("Test", "Exports") != null);
        assertTrue(active.getTsVariable("Test.Exports") != null);
        assertTrue(active.getTsVariable("Test", "Exports") ==active.getTsVariable("Test.Exports"));
    }
    
}
