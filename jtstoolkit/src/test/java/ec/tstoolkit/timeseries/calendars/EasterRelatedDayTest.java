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

package ec.tstoolkit.timeseries.calendars;

import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class EasterRelatedDayTest {

    public EasterRelatedDayTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testLongTermCorrection() {
        double w = .88;
        EasterRelatedDay day = new EasterRelatedDay(-39, w);
        double[][] le = day.getLongTermMeanEffect(12);
        double p = 0;
        for (int i = 0; i < 6; ++i) {
            for (int j = 0; j < le.length; ++j) {
                if (le[j] != null) {
                    p += le[j][i];
                }
            }
        }
        org.junit.Assert.assertTrue(Math.abs(p - w) < 1e-9);
    }

    @Test
    public void testprobEaster() {
        double p = 0;
        for (int i = 0; i < 35; ++i) {
            double q = Utilities.probEaster(i);
            p += q;
        }
        org.junit.Assert.assertTrue(Math.abs(p - 1) < 1e-9);
    }
    
    @Test
    public void testprobJulianEaster() {
        double p = 0;
        for (int i = 0; i < 35; ++i) {
            double q = Utilities.probJulianEaster(i);
            p += q;
        }
        org.junit.Assert.assertTrue(Math.abs(p - 1) < 1e-9);
    }
    
    @Test
    public void testCorpusChristi() {
        Day cc=EasterRelatedDay.CorpusChristi.calcDay(2015);
        assertTrue(cc.equals(new Day(2015, Month.June, 3)));
    }

    @Test
    public void testAshWednesday() {
        Day cc=EasterRelatedDay.AshWednesday.calcDay(2016);
        assertTrue(cc.equals(new Day(2016, Month.February, 9)));
    }
}
