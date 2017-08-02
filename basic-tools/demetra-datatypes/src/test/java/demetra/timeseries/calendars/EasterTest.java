/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.timeseries.calendars;

import demetra.timeseries.Day;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class EasterTest {

    public EasterTest() {
    }

    @Test
    public void testEaster() {
        for (int i = 1900; i < 4100; ++i) {
            LocalDate easter = Easter.easter(i);
            LocalDate easter2 = Easter.easter2(i);
            Assert.assertEquals(easter, easter2);
        }
    }

    @Test
    public void testJulianEaster() {
        Assert.assertEquals(Easter.julianEaster(2008, true), LocalDate.of(2008, 4, 27));
        Assert.assertEquals(Easter.julianEaster(2009, true), LocalDate.of(2009, 4, 19));
        Assert.assertEquals(Easter.julianEaster(2010, true), LocalDate.of(2010, 4, 4));
        Assert.assertEquals(Easter.julianEaster(2011, true), LocalDate.of(2011, 4, 24));
    }

    @Test
    public void testJulianEaster2() {
        for (int i = 2000; i < 2100; ++i) {
            assertTrue(Easter.julianEaster(i, true).getDayOfWeek() == DayOfWeek.SUNDAY);
        }
    }

    @Test
    @Ignore
    public void computeJulianEasterDistribution() {
        System.out.println("");
        int[] prob = new int[50];
        for (int i = 0; i < 2 * Easter.CYCLE; ++i) {
            LocalDate julianEaster = Easter.julianEaster(1583 + i, true);
            LocalDate first = LocalDate.of(1583 + i, 4, 1);
            long del=first.until(julianEaster, ChronoUnit.DAYS);
            prob[(int)del]++;
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
        for (int i = 0; i < 2 * Easter.CYCLE; ++i) {
            LocalDate julianEaster = Easter.julianEaster(1583 + i, true);
            LocalDate first = LocalDate.of(1583 + i, 4, 1);
            int del=(int)first.until(julianEaster, ChronoUnit.DAYS);
            prob[del]++;
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
                        march[l - 1] += p * n0;
                    } else {
                        n0 = 0;
                    }
                    // number of days in april
                    int n1 = Math.min(d1, 30) - Math.max(0, d0);
                    if (n1 > 0) {
                        april[l - 1] += p * n1;
                    } else {
                        n1 = 0;
                    }
                    // number of day in may
                    int n2 = l - n1 - n0;
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

//    @Test
//    public void testJulianMeanCorrection() {
//        EasterRelatedDay day = new EasterRelatedDay(-5, true);
//        double[][] lte = day.getLongTermMeanEffect(12);
//        int[] month = new int[12];
//        for (int i = 1583; i < 1583 + 2 * 532; ++i) {
//            month[day.calcDay(i).getMonth()]++;
//        }
//        double denom = 2 * 532;
//        for (int i = 0; i < 12; ++i) {
//            if (lte[i] != null) {
//                assertTrue(Math.abs((month[i] / denom) + lte[i][6]) < 1e-9);
//            }
//        }
//    }
}
