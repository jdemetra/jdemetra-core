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

package ec.tstoolkit.timeseries.simplets;

import data.Data;
import ec.tstoolkit.data.DescriptiveStatistics;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class PeriodIteratorTest {

    TsData s;

    public PeriodIteratorTest() {
        s = new TsData(TsFrequency.Monthly, 1980, 3, 97);
        for (int i = 0; i < s.getLength(); ++i) {
            s.set(i, i + 1);
        }
    }

    @Test
    public void testFull() {

        PeriodIterator p = PeriodIterator.fullYears(s);
        int[] x = new int[s.getFrequency().intValue()];
        int i = 0;
        while (p.hasMoreElements()) {
            x[i++] = (int) p.nextElement().data.sum();
        }

        // should be 322, 329, 336, 343...
        assertEquals(x[0], 322);
        for (int j = 1; j < x.length; ++j) {
            assertEquals(x[j] - x[j - 1], 7);
        }
    }

    @Test
    public void testPeriod() {

        PeriodIterator p = new PeriodIterator(s);
        int[] x = new int[s.getFrequency().intValue()];
        int i = 0;
        while (p.hasMoreElements()) {
            x[i++] = (int) p.nextElement().data.sum();
        }

        // should be 416, 
        assertEquals(x[0], 416);
        assertEquals(x[5], 360);
        assertEquals(x[11], 408);
    }

    //@Test
    public void demo() {

        // Computes a few statistics by period
        PeriodIterator p = new PeriodIterator(Data.X);
        while (p.hasMoreElements()) {
            TsDataBlock cur = p.nextElement();
            DescriptiveStatistics stats = new DescriptiveStatistics(cur.data);
            System.out.print(cur.start.getPeriodString());
            System.out.print('\t');
            System.out.print(stats.getAverage());
            System.out.print('\t');
            System.out.print(stats.getMedian());
            System.out.print('\t');
            System.out.print(stats.getMin());
            System.out.print('\t');
            System.out.println(stats.getMax());
        }
        System.out.println();

        // Same exercise by year
        YearIterator y = new YearIterator(Data.X);
        while (y.hasMoreElements()) {
            TsDataBlock cur = y.nextElement();
            DescriptiveStatistics stats = new DescriptiveStatistics(cur.data);
            System.out.print(cur.start.getYear());
            System.out.print('\t');
            System.out.print(stats.getAverage());
            System.out.print('\t');
            System.out.print(stats.getMedian());
            System.out.print('\t');
            System.out.print(stats.getMin());
            System.out.print('\t');
            System.out.println(stats.getMax());
        }
    }
}
