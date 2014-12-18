/*
* Copyright 2013 National Bank of Belgium
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

package ec.satoolkit.x11;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class TrendCycleFilterFactoryTest {
    
    
    public TrendCycleFilterFactoryTest() {
    }

    /**
     * Test of makeHendersonFilter method, of class TrendCycleFilterFactory.
     */
    @Test
    public void testMakeHendersonFilter() {
        for (int i=1; i<100; i+=2){
            SymmetricFilter filter = TrendCycleFilterFactory.makeHendersonFilter(i);
            DataBlock w=new DataBlock(filter.getWeights());
            assertTrue(Math.abs(w.sum()-1) < 1e-9);
        }
    }

    /**
     * Test of makeTrendFilter method, of class TrendCycleFilterFactory.
     */
    @Test
    public void testMakeTrendFilter() {
        for (int i=1; i<100; ++i){
            SymmetricFilter filter = TrendCycleFilterFactory.makeTrendFilter(i);
            DataBlock w=new DataBlock(filter.getWeights());
            assertTrue(Math.abs(w.sum()-1) < 1e-9);
        }
    }
}