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

package ec.tstoolkit.data;

import ec.tstoolkit.arima.ArimaModelBuilder;
import ec.tstoolkit.maths.polynomials.Polynomial;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author Jean Palate
 */
public class DurbinAlgorithmTest {

    public DurbinAlgorithmTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void test() {
        ArimaModelBuilder builder = new ArimaModelBuilder();
        int n = 240, m = 10;
        for (int i = 0; i < m; ++i) {
            double[] x = builder.generate(builder.createModel(Polynomial.of(new double[]{1, -.7, .5}), Polynomial.ONE, 1), n);
            DurbinAlgorithm durbin = new DurbinAlgorithm();
            //Peaks peaks=new Peaks(new TsData(TsFrequency.Monthly, 1980, 0, x, true), 240, false);
            assertTrue(durbin.solve(new ReadDataBlock(x), 30));
        }
    }
}
