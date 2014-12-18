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
package ec.satoolkit.diagnostics;

import ec.tstoolkit.random.IRandomNumberGenerator;
import ec.tstoolkit.dstats.Normal;
import ec.tstoolkit.random.MersenneTwister;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class SeasonalityTestsFactoryTest {

    public SeasonalityTestsFactoryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

//    @Test()
    public void demoQS() {
        Normal N = new Normal();
        IRandomNumberGenerator rng = new MersenneTwister(0);

        int n = 360;
        int m = 10000;
        double[] x = new double[n];
        double s = 0;
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j) {
                x[j] = N.random(rng);
            }
            s += QSTest.compute(x, 12, 2).getValue();
        }
        assertTrue(Math.abs(s / m - 2) < .2);
    }
}
