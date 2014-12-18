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
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class UtilitiesTest {

    public UtilitiesTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    //@Test
    public void testEaster() {
        int[] count = new int[35];
        for (int i = 0; i < 5700000; ++i) {
            Day easter = Utilities.easter2(1900 + i);
            Day lbound = new Day(1900 + i, Month.March, 21);
            count[easter.difference(lbound)]++;
        }

        for (int i = 0; i < 35; ++i) {
            System.out.println(count[i] / 5700000.0);
        }
    }

    @Test
    public void testEaster2() {
        for (int i = 1900; i < 4100; ++i) {
            Day easter = Utilities.easter(i);
            Day easter2 = Utilities.easter2(i);
//            System.out.print(easter);
//            System.out.print("  ");
//            System.out.println(easter2);
            Assert.assertEquals(easter, easter2);
        }
    }
   
    @Test
    public void testJulianEaster() {
        Assert.assertEquals(Utilities.julianEaster(2008, true), new Day(2008, Month.April, 26));
         Assert.assertEquals(Utilities.julianEaster(2009, true), new Day(2009, Month.April, 18));
         Assert.assertEquals(Utilities.julianEaster(2010, true), new Day(2010, Month.April, 3));
         Assert.assertEquals(Utilities.julianEaster(2011, true), new Day(2011, Month.April, 23));
    }
}
