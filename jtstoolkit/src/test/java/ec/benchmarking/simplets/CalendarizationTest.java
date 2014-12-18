/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 will be approved by the European Commission - subsequent
 versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 writing, software distributed under the Licence is
 distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 * See the Licence for the specific language governing
 permissions and limitations under the Licence.
 */
package ec.benchmarking.simplets;

import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataTable;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class CalendarizationTest {

    public CalendarizationTest() {
    }

    @Test
    public void testCalendarization() {
        Calendarization cal = new Calendarization();
        // Day = year / month / 0-based day !!!
        // The observation spans may be non contiguous
        // They can't overlap and they must be provided in the right order.
        // The "add" method will fail in the case of invalid information.
        cal.add(new Day(2009, Month.February, 17), new Day(2009, Month.March, 16), 9000);
        cal.add(new Day(2009, Month.March, 17), new Day(2009, Month.April, 13), 5000);
        cal.add(new Day(2009, Month.April, 14), new Day(2009, Month.May, 11), 9500);
        cal.add(new Day(2009, Month.May, 12), new Day(2009, Month.June, 8), 7000);
        // The complete span may be larger (at the beginning and/or at the end) than the given data 
        cal.setSpan(new Day(2009, Month.February, 17), new Day(2009, Month.June, 29));

        cal.setDailyWeights(null);
        double[] s = cal.getSmoothedData();
        double[] es = cal.getSmoothedStdev();
        Day start = cal.getStart();
        for (int i = 0; i < s.length; ++i) {
            System.out.print(start.plus(i));
            System.out.print('\t');
            System.out.print(s[i]);
            System.out.print('\t');
            System.out.print(es[i]);
            System.out.println();
        }
        System.out.println();
        TsData mdata = cal.getAggregates(TsFrequency.Monthly);
        TsData emdata = cal.getAggregatesStdev(TsFrequency.Monthly);

        TsDataTable table2 = new TsDataTable();
        table2.insert(-1, mdata);
        table2.insert(-1, emdata);
        System.out.println("table 2");
        System.out.println(table2);
        System.out.println();

        // Daily weights, starting with Sundays
        cal.setDailyWeights(new double[]{0, .6, .8, 1, 1.2, 1.8, 1.6});
        s = cal.getSmoothedData();
        es = cal.getSmoothedStdev();
        for (int i = 0; i < s.length; ++i) {
            System.out.print(start.plus(i));
            System.out.print('\t');
            System.out.print(s[i]);
            System.out.print('\t');
            System.out.print(es[i]);
            System.out.println();
        }
        System.out.println();
        mdata = cal.getAggregates(TsFrequency.Monthly);
        emdata = cal.getAggregatesStdev(TsFrequency.Monthly);

        TsDataTable table3 = new TsDataTable();
        table3.insert(-1, mdata);
        table3.insert(-1, emdata);
        System.out.println("table 3");
        System.out.println(table3);
        System.out.println();
        // stress test
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            cal = new Calendarization(false);
            cal.add(new Day(2009, Month.February, 17), new Day(2009, Month.March, 16), 9000);
            cal.add(new Day(2009, Month.March, 17), new Day(2009, Month.April, 13), 5000);
            cal.add(new Day(2009, Month.April, 14), new Day(2009, Month.May, 11), 9500);
            cal.add(new Day(2009, Month.May, 12), new Day(2009, Month.June, 8), 7000);
            // The complete span may be larger (at the beginning and/or at the end) than the given data 
            cal.setSpan(new Day(2009, Month.February, 17), new Day(2009, Month.June, 29));
            TsData aggregates = cal.getAggregates(TsFrequency.Monthly);
        }
        
        long t1 = System.currentTimeMillis();
        System.out.print("Fast processing: ");
        System.out.print(.0001 * (t1 - t0));
        System.out.println(" millisecond by estimation");
        t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            cal = new Calendarization(true);
            cal.add(new Day(2009, Month.February, 17), new Day(2009, Month.March, 16), 9000);
            cal.add(new Day(2009, Month.March, 17), new Day(2009, Month.April, 13), 5000);
            cal.add(new Day(2009, Month.April, 14), new Day(2009, Month.May, 11), 9500);
            cal.add(new Day(2009, Month.May, 12), new Day(2009, Month.June, 8), 7000);
            // The complete span may be larger (at the beginning and/or at the end) than the given data 
            cal.setSpan(new Day(2009, Month.February, 17), new Day(2009, Month.June, 29));
            cal.getAggregates(TsFrequency.Monthly);
            cal.getAggregatesStdev(TsFrequency.Monthly);
        }
        t1 = System.currentTimeMillis();
        System.out.print("Complete processing: ");
        System.out.print(.0001 * (t1 - t0));
        System.out.println(" millisecond by estimation");
    }
}