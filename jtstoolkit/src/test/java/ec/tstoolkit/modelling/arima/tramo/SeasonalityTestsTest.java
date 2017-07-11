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

package ec.tstoolkit.modelling.arima.tramo;

import data.Data;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class SeasonalityTestsTest {
    
    public SeasonalityTestsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void demoRandom() {
        int n=240;
        DataBlock rnd=new DataBlock(n);
        rnd.randomize(0);
        rnd.sub(rnd.sum()/rnd.getLength());
        SeasonalityTests rtest = SeasonalityTests.residualSeasonalityTest(rnd.getData(), TsFrequency.Monthly);
        int score=rtest.getScore();
        assertTrue(score < 2);
    }
    
    @Test
    public void demo() {
        TsData s=Data.X.log().delta(1);
        SeasonalityTests rtest = SeasonalityTests.residualSeasonalityTest(s.internalStorage(), TsFrequency.Monthly);
        int score=rtest.getScore();
        assertTrue(score > 2);
    }
}
