/*
 * Copyright 2016 National Bank of Belgium
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
package ec.tstoolkit.modelling;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.dstats.Normal;
import ec.tstoolkit.random.MersenneTwister;
import ec.tstoolkit.timeseries.regression.AbstractOutlierVariable;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class IRobustStandardDeviationComputerTest {
    
    public IRobustStandardDeviationComputerTest() {
    }

    @Test
    public void testOldMethods() {
        DataBlock z=new DataBlock(120);
        z.randomize(0);
        IRobustStandardDeviationComputer mad = IRobustStandardDeviationComputer.mad(true);
        mad.compute(z);
        assertEquals(mad.get(), AbstractOutlierVariable.mad(z, true), 1e-3);
    }
    
    @Test
    @Ignore
    public void testCentile() {
        DataBlock z = new DataBlock(10000);
        Normal N=new Normal();
        MersenneTwister rnd = MersenneTwister.fromSystemNanoTime();
        z.set(()->N.random(rnd));
        for (int i = 25; i < 100; ++i) {
            IRobustStandardDeviationComputer mad = IRobustStandardDeviationComputer.mad(i, true);
            mad.compute(z);
            System.out.println(mad.get());
        }
        
    }
 }
