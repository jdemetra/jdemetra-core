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

package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * @author Jean Palate
 */
public class SeasonalDummiesTest {
    
    public SeasonalDummiesTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    //@Test
    public void test() {
        SeasonalDummies ds=new SeasonalDummies(TsFrequency.Monthly);
        TsDomain domain =new TsDomain(TsFrequency.Monthly, 1980, 3, 120);
        
        Matrix M=new Matrix(120, 11);
        ds.data(domain, M.columnList());
        System.out.println(M);
        
    }
}
