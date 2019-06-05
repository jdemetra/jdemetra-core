/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.calendarization;

import demetra.calendarization.CalendarizationResults;
import demetra.calendarization.CalendarizationSpec;
import jdplus.calendarization.CalendarizationProcessor;
import demetra.timeseries.CalendarPeriodObs;
import demetra.timeseries.CalendarTimeSeries;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDataTable;
import demetra.timeseries.TsUnit;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class CalendarizationProcessorTest {

    public CalendarizationProcessorTest() {
    }

    @Test
    public void testCalendarization() {
        // create the Calendar series
        List<CalendarPeriodObs> data=new ArrayList<>();
        data.add(CalendarPeriodObs.of(LocalDate.of(2009, Month.FEBRUARY, 18)
                ,LocalDate.of(2009, Month.MARCH, 18)
                , 9000));
        data.add(CalendarPeriodObs.of(LocalDate.of(2009, Month.MARCH, 18)
                ,LocalDate.of(2009, Month.APRIL, 15)
                , 5000));
        data.add(CalendarPeriodObs.of(LocalDate.of(2009, Month.APRIL, 15)
                ,LocalDate.of(2009, Month.MAY, 13)
                , 9500));
        data.add(CalendarPeriodObs.of(LocalDate.of(2009, Month.MAY, 13)
                ,LocalDate.of(2009, Month.JUNE, 10)
                , 7000));
        CalendarTimeSeries series=CalendarTimeSeries.of(data);
        CalendarizationSpec spec = CalendarizationSpec.builder()
                .stdev(true)
                .aggregationUnit(TsUnit.MONTH)
                .start(LocalDate.of(2009, Month.FEBRUARY, 18))
                .end(LocalDate.of(2009, Month.JULY, 1))
                .build();
        CalendarizationProcessor processor=new CalendarizationProcessor();
        CalendarizationResults cal = processor.process(series, spec);
        assertTrue(cal != null);
//        double[] s = cal.getDailyData();
//        double[] es = cal.getDailyStdev();
//        LocalDate start = cal.getStart();
//        for (int i = 0; i < s.length; ++i) {
//            System.out.print(start.plusDays(i));
//            System.out.print('\t');
//            System.out.print(s[i]);
//            System.out.print('\t');
//            System.out.print(es[i]);
//            System.out.println();
//        }
//        System.out.println();
//        
//        TsData mdata = cal.getAggregatedSeries();
//        TsData emdata = cal.getStdevAggregatedSeries();
//        List<TsData> all=new ArrayList<>();
//        all.add(mdata);
//        all.add(emdata);
//        System.out.println(TsDataTable.of(all));

        spec = CalendarizationSpec.builder()
                .stdev(true)
                .aggregationUnit(TsUnit.MONTH)
                .start(LocalDate.of(2009, Month.FEBRUARY, 18))
                .end(LocalDate.of(2009, Month.JULY, 1))
                .dailyWeights(new double[]{.6, .8, 1, 1.2, 1.8, 1.6, 0})
                .build();

        cal = processor.process(series, spec);
        assertTrue(cal != null);
//        s = cal.getDailyData();
//        es = cal.getDailyStdev();
//        start = cal.getStart();
//        for (int i = 0; i < s.length; ++i) {
//            System.out.print(start.plusDays(i));
//            System.out.print('\t');
//            System.out.print(s[i]);
//            System.out.print('\t');
//            System.out.print(es[i]);
//            System.out.println();
//        }
//        System.out.println();
        
//        mdata = cal.getAggregatedSeries();
//        emdata = cal.getStdevAggregatedSeries();
//        all.clear();
//        all.add(mdata);
//        all.add(emdata);
//        System.out.println(TsDataTable.of(all));
        
    }

//    @Test
    public void testLegacy() {
        ec.benchmarking.simplets.Calendarization cal = new ec.benchmarking.simplets.Calendarization();
        // Day = year / month / 0-based day !!!
        // The observation spans may be non contiguous
        // They can't overlap and they must be provided in the right order.
        // The "add" method will fail in the case of invalid information.
        cal.add(new ec.tstoolkit.timeseries.Day(2009, ec.tstoolkit.timeseries.Month.February, 17), new ec.tstoolkit.timeseries.Day(2009, ec.tstoolkit.timeseries.Month.March, 16), 9000);
        cal.add(new ec.tstoolkit.timeseries.Day(2009, ec.tstoolkit.timeseries.Month.March, 17), new ec.tstoolkit.timeseries.Day(2009, ec.tstoolkit.timeseries.Month.April, 13), 5000);
        cal.add(new ec.tstoolkit.timeseries.Day(2009, ec.tstoolkit.timeseries.Month.April, 14), new ec.tstoolkit.timeseries.Day(2009, ec.tstoolkit.timeseries.Month.May, 11), 9500);
        cal.add(new ec.tstoolkit.timeseries.Day(2009, ec.tstoolkit.timeseries.Month.May, 12), new ec.tstoolkit.timeseries.Day(2009, ec.tstoolkit.timeseries.Month.June, 8), 7000);
        // The complete span may be larger (at the beginning and/or at the end) than the given data 
        cal.setSpan(new ec.tstoolkit.timeseries.Day(2009, ec.tstoolkit.timeseries.Month.February, 17), new ec.tstoolkit.timeseries.Day(2009, ec.tstoolkit.timeseries.Month.June, 29));

        cal.setDailyWeights(null);
        double[] s = cal.getSmoothedData();
        double[] es = cal.getSmoothedStdev();
        ec.tstoolkit.timeseries.Day start = cal.getStart();
        for (int i = 0; i < s.length; ++i) {
            System.out.print(start.plus(i));
            System.out.print('\t');
            System.out.print(s[i]);
            System.out.print('\t');
            System.out.print(es[i]);
            System.out.println();
        }
        System.out.println();
        ec.tstoolkit.timeseries.simplets.TsData mdata = cal.getAggregates(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly);
        ec.tstoolkit.timeseries.simplets.TsData emdata = cal.getAggregatesStdev(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly);

        ec.tstoolkit.timeseries.simplets.TsDataTable table2 = new ec.tstoolkit.timeseries.simplets.TsDataTable();
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
        mdata = cal.getAggregates(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly);
        emdata = cal.getAggregatesStdev(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly);

        ec.tstoolkit.timeseries.simplets.TsDataTable table3 = new ec.tstoolkit.timeseries.simplets.TsDataTable();
        table3.insert(-1, mdata);
        table3.insert(-1, emdata);
        System.out.println("table 3");
        System.out.println(table3);
        System.out.println();
    }
}
