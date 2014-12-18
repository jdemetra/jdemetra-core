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

import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author pcuser
 */
public class TaperTest {

    public TaperTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void test() {
        TukeyHanningTaper taper = new TukeyHanningTaper();
        for (int i = 120; i < 200; ++i) {
            double[] x = new double[i];
            for (int j = 0; j < x.length; ++j) {
                x[j] = j + 1;
            }
            taper.process(x);
            int d = 0;
            for (int j = 0; j < x.length; ++j) {
                if (x[j] != j + 1) {
                    ++d;
                }
            }
            assertTrue(d <= .1*x.length);
        }
    }
}
