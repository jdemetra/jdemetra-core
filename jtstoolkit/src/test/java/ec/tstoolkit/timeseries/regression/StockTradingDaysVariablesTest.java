/*
 * Copyright 2015 National Bank of Belgium
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
/*
 */
package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PCUser
 */
public class StockTradingDaysVariablesTest {
    
    public StockTradingDaysVariablesTest() {
    }

    @Test
    public void test1() {
        StockTradingDaysVariables td=new StockTradingDaysVariables(31);
        TsDomain dom=new TsDomain(TsFrequency.Monthly, 2014, 0, 12);
        Matrix m=new Matrix(12, 6);
        td.data(dom, m.columnList());
        assertTrue(m.get(0, 4)==1);
        assertTrue(m.get(1, 4)==1);
        assertTrue(m.get(2, 0)==1);
        assertTrue(m.get(3, 2)==1);
        assertTrue(m.get(4, 5)==1);
    }
    
}
