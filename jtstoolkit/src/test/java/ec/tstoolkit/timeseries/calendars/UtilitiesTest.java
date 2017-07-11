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
import org.junit.Ignore;
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
        Assert.assertEquals(Utilities.julianEaster3(2008, true), new Day(2008, Month.April, 26));
        Assert.assertEquals(Utilities.julianEaster3(2009, true), new Day(2009, Month.April, 18));
        Assert.assertEquals(Utilities.julianEaster3(2010, true), new Day(2010, Month.April, 3));
        Assert.assertEquals(Utilities.julianEaster3(2011, true), new Day(2011, Month.April, 23));
    }

    @Test
    public void testJulianEaster2() {
        for (int i = 2000; i < 2100; ++i) {
            assertTrue(Utilities.julianEaster(i, true).getDayOfWeek() == DayOfWeek.Sunday);
        }
    }

    @Test
    public void testJulianEaster3() {
        for (int i = 2000; i < 3000; ++i) {
            assertTrue(Utilities.julianEaster3(i, true).getDayOfWeek() == DayOfWeek.Sunday);
        }
    }

    @Test
    public void testJulianEaster2_3() {
        for (int i = 2000; i < 2100; ++i) {
            assertTrue(Utilities.julianEaster3(i, true).equals(Utilities.julianEaster2(i, true)));
        }
    }

    @Test
    @Ignore
    public void computeJulianEasterDistribution() {
        System.out.println("");
        int[] prob = new int[50];
        for (int i = 0; i < 2 * Utilities.CYCLE; ++i) {
            Day julianEaster = Utilities.julianEaster3(1583 + i, true);
            Day first = new Day(1583 + i, Month.April, 0);
            prob[julianEaster.difference(first)]++;
        }
        for (int i = 0; i < 50; ++i) {
            System.out.println(prob[i]);
            //assertTrue(Utilities.PROB[i] == prob[i]);
        }
        System.out.println("");
    }

    @Test
    @Ignore
    public void computeJulianEasterMeanCorrections() {
        // we compute the occurences of julian Easter (in gregorian dates)
        // from 1 April to 20 Mey, from 1583 to 1583*2*532
        // of course, that choice is arbitrary...
        int[] prob = new int[50];
        for (int i = 0; i < 2 * Utilities.CYCLE; ++i) {
            Day julianEaster = Utilities.julianEaster3(1583 + i, true);
            Day first = new Day(1583 + i, Month.April, 0);
            prob[julianEaster.difference(first)]++;
        }
        // now, we compute the average length of Easter effect in the different months

//        // March
        // effect length from 1 to 30
        double[] march = new double[30], april = new double[30], may = new double[30];
        for (int l = 1; l <= 30; ++l) {
            for (int ie = 0; ie < prob.length; ++ie) {
                double p = prob[ie];
                if (p > 0) {
                    int d0 = ie - l, d1 = ie; // d0 is the first day of the Easter effect (included), d1 the last day (excluded)
                    
                    // number of days in march:
                    int n0 = -d0;
                    if (n0 > 0) {
                        march[l - 1]  +=p * n0;
                    }else{
                        n0=0;
                    }
                    // number of days in april
                    int n1=Math.min(d1, 30)-Math.max(0, d0);
                    if (n1 > 0) {
                        april[l - 1] += p * n1;
                    }else
                        n1=0;
                    // number of day in may
                    int n2=l-n1-n0;
                    if (n2 > 0) {
                        may[l - 1] += p * n2;
                    }
                    

                }
            }
        }
        for (int i = 0; i < march.length; ++i) {
            System.out.print(march[i]);
            System.out.print('\t');
        }
        System.out.println("");
        // April
        for (int i = 0; i < april.length; ++i) {
            System.out.print(april[i]);
            System.out.print('\t');
        }
        System.out.println("");
        for (int i = 0; i < may.length; ++i) {
            System.out.print(may[i]);
            System.out.print('\t');
        }
        System.out.println("");
    }

    @Test
    public void testJulianMeanCorrection() {
        EasterRelatedDay day = new EasterRelatedDay(-5, true);
        double[][] lte = day.getLongTermMeanEffect(12);
        int[] month = new int[12];
        for (int i = 1583; i < 1583 + 2*532; ++i) {
            month[day.calcDay(i).getMonth()]++;
        }
        double denom = 2*532;
        for (int i = 0; i < 12; ++i) {
            if (lte[i] != null) {
                assertTrue(Math.abs((month[i] / denom) + lte[i][6]) < 1e-9);
            }
        }
    }
}
