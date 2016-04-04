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
import ec.tstoolkit.timeseries.DayOfWeek;
import ec.tstoolkit.timeseries.Month;
import java.util.HashMap;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
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
    public void demoEaster() {
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
        Assert.assertEquals(Utilities.julianEaster2(2008, true), new Day(2008, Month.April, 26));
        Assert.assertEquals(Utilities.julianEaster2(2009, true), new Day(2009, Month.April, 18));
        Assert.assertEquals(Utilities.julianEaster2(2010, true), new Day(2010, Month.April, 3));
        Assert.assertEquals(Utilities.julianEaster2(2011, true), new Day(2011, Month.April, 23));
    }

    @Test
    public void testJulianEaster2() {
        for (int i=2000; i<2100; ++i){
            assertTrue(Utilities.julianEaster(i, true).getDayOfWeek()==DayOfWeek.Sunday);
        }
    }
    
    
    @Test
    public void computeJulianEasterDistribution() {
        int[] prob = new int[35];
        for (int i = 0; i < Utilities.CYCLE; ++i) {
            Day julianEaster = Utilities.julianEaster(1980 + i, false);
            Day first = new Day(1980 + i, Month.March, 21);
            prob[julianEaster.difference(first)]++;
        }
        for (int i = 0; i < 35; ++i) {
            assertTrue(Utilities.PROB[i] == prob[i]);
        }
    }

    //@Test
    public void computeJulianEasterMeanCorrections() {

//        // March
        for (int i = 15; i < 29; ++i) {
            int n = 0;
            for (int j = 0; j < Utilities.PROB.length; ++j) {
                int d0 = j - i, d1 = -3;
                if (d0 < d1) {
                    n += Utilities.PROB[j] * (d1 - d0);
                }

            }
            System.out.println(n);
        }
        // April
        for (int i = 15; i < 29; ++i) {
            int n = 0;
            for (int j = 0; j < Utilities.PROB.length; ++j) {
                int d0 = Math.max(j - i, -3), d1 = Math.min(j, 27);
                if (d0 < d1) {
                    n += Utilities.PROB[j] * (d1 - d0);
                }

            }
            System.out.println(n);
        }
        // May
        for (int i = 15; i < 29; ++i) {
            int n = 0;
            for (int j = 0; j < Utilities.PROB.length; ++j) {
                int d0 = Math.max(j - i, 27), d1 = j;
                if (d0 < d1) {
                    n += Utilities.PROB[j] * (d1 - d0);
                }

            }
            System.out.println(n);
        }
    }

    @Test
    public void testJulianMeanCorrection() {
        EasterRelatedDay day = new EasterRelatedDay(-5, 1, true);
        double[][] lte = day.getLongTermMeanEffect(12);
        int[] month = new int[12];
        for (int i = 1900; i < 1900 + 532; ++i) {
            month[day.calcDay(i).getMonth()]++;
        }
        double denom = 532;
        for (int i = 0; i < 12; ++i) {
            if (lte[i] != null) {
                assertTrue(Math.abs((month[i] / denom) - lte[i][1]) < 1e-9);
            }
        }
    }
}
