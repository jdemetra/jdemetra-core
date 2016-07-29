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

import ec.tstoolkit.data.DescriptiveStatistics;
import java.util.Random;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.TsAggregationType;
import static ec.tstoolkit.timeseries.TsAggregationType.Average;
import static ec.tstoolkit.timeseries.TsAggregationType.First;
import static ec.tstoolkit.timeseries.TsAggregationType.Last;
import static ec.tstoolkit.timeseries.TsAggregationType.Max;
import static ec.tstoolkit.timeseries.TsAggregationType.Min;
import static ec.tstoolkit.timeseries.TsAggregationType.None;
import static ec.tstoolkit.timeseries.TsAggregationType.Sum;
import ec.tstoolkit.timeseries.TsException;
import static ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly;
import static ec.tstoolkit.timeseries.simplets.TsFrequency.Quarterly;
import static ec.tstoolkit.timeseries.simplets.TsFrequency.Undefined;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import static java.util.EnumSet.allOf;
import static java.util.EnumSet.complementOf;
import static java.util.EnumSet.of;
import java.util.GregorianCalendar;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class TsDataCollectorTest {

    private static final Date JAN2010 = new GregorianCalendar(2010, Calendar.JANUARY, 1).getTime();
    private static final Date FEB2010 = new GregorianCalendar(2010, Calendar.FEBRUARY, 1).getTime();
    private static final Date APR2010 = new GregorianCalendar(2010, Calendar.APRIL, 1).getTime();
    private static final Date MAY2010 = new GregorianCalendar(2010, Calendar.MAY, 1).getTime();

    private static Date asDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    private static TsData data(TsFrequency freq, int firstyear, int firstperiod, double... data) {
        return new TsData(freq, firstyear, firstperiod, data, false);
    }

    @Test
    public void testMake() {
        TsDataCollector dc = new TsDataCollector();
        EnumSet<TsFrequency> defined = complementOf(of(Undefined));

        // defined with single value
        dc.clear();
        dc.addObservation(JAN2010, 10);
        defined.forEach(o -> assertThat(dc.make(o, None)).isEqualTo(data(o, 2010, 0, 10)));

        // monthly with missing values
        dc.clear();
        dc.addObservation(JAN2010, 10);
        dc.addObservation(APR2010, 40);
        assertThat(dc.make(Monthly, None)).isEqualTo(data(Monthly, 2010, 0, 10, Double.NaN, Double.NaN, 40));

        // quarterly
        dc.clear();
        dc.addObservation(JAN2010, 10);
        dc.addObservation(APR2010, 40);
        assertThat(dc.make(Quarterly, None)).isEqualTo(data(Quarterly, 2010, 0, 10, 40));

        // undefined to monthly
        dc.clear();
        dc.addObservation(JAN2010, 10);
        dc.addObservation(FEB2010, 20);
        assertThat(dc.make(Undefined, None)).isEqualTo(data(Monthly, 2010, 0, 10, 20));

        // undefined to monthly with missing values
        dc.clear();
        dc.addObservation(JAN2010, 10);
        dc.addObservation(FEB2010, 20);
        dc.addObservation(MAY2010, 50);
        assertThat(dc.make(Undefined, None)).isEqualTo(data(Monthly, 2010, 0, 10, 20, Double.NaN, Double.NaN, 50));

        // undefined to quarterly
        dc.clear();
        dc.addObservation(JAN2010, 10);
        dc.addObservation(APR2010, 40);
        assertThat(dc.make(Undefined, None)).isEqualTo(data(Quarterly, 2010, 0, 10, 40));

        // defined with aggregation
        dc.clear();
        dc.addObservation(JAN2010, 12);
        dc.addObservation(JAN2010, 13);
        dc.addObservation(JAN2010, 10);
        dc.addObservation(JAN2010, 11);
        defined.forEach(o -> {
            assertThat(dc.make(o, First)).isEqualTo(data(o, 2010, 0, 12));
            assertThat(dc.make(o, Last)).isEqualTo(data(o, 2010, 0, 11));
            assertThat(dc.make(o, Min)).isEqualTo(data(o, 2010, 0, 10));
            assertThat(dc.make(o, Max)).isEqualTo(data(o, 2010, 0, 13));
            assertThat(dc.make(o, Average)).isEqualTo(data(o, 2010, 0, 46d / 4));
            assertThat(dc.make(o, Sum)).isEqualTo(data(o, 2010, 0, 46));
        });

        // unordered daily to monthly
        dc.clear();
        dc.addObservation(asDate(LocalDate.of(2010, 2, 1)), 20);
        dc.addObservation(asDate(LocalDate.of(2010, 1, 3)), 10);
        dc.addObservation(asDate(LocalDate.of(2010, 1, 4)), 11);
        dc.addObservation(asDate(LocalDate.of(2010, 1, 1)), 12);
        dc.addObservation(asDate(LocalDate.of(2010, 1, 2)), 13);
        assertThat(dc.make(Monthly, First)).isEqualTo(data(Monthly, 2010, 0, 12, 20));
        assertThat(dc.make(Monthly, Last)).isEqualTo(data(Monthly, 2010, 0, 11, 20));
        assertThat(dc.make(Monthly, Min)).isEqualTo(data(Monthly, 2010, 0, 10, 20));
        assertThat(dc.make(Monthly, Max)).isEqualTo(data(Monthly, 2010, 0, 13, 20));
        assertThat(dc.make(Monthly, Average)).isEqualTo(data(Monthly, 2010, 0, 46d / 4, 20));
        assertThat(dc.make(Monthly, Sum)).isEqualTo(data(Monthly, 2010, 0, 46, 20));
    }

    @Test
    public void testMakeNull() {
        TsDataCollector dc = new TsDataCollector();

        // no data
        dc.clear();
        allOf(TsFrequency.class)
                .forEach(o -> assertThat(dc.make(o, None)).isNull());

        // invalid aggregation
        dc.clear();
        dc.addObservation(JAN2010, 10);
        complementOf(of(None))
                .forEach(o -> assertThatThrownBy(() -> dc.make(Undefined, o)).isInstanceOf(TsException.class));

        // guess single
        dc.clear();
        dc.addObservation(JAN2010, 10);
        assertThat(dc.make(Undefined, None)).isNull();

        // guess duplication
        dc.clear();
        dc.addObservation(JAN2010, 10);
        dc.addObservation(JAN2010, 10);
        assertThat(dc.make(Undefined, None)).isNull();

        // duplication without aggregation
        dc.clear();
        dc.addObservation(JAN2010, 10);
        dc.addObservation(JAN2010, 10);
        complementOf(of(Undefined))
                .forEach(o -> assertThat(dc.make(o, None)));
    }

//    @Test
    public void demoCreation1() {

        TsDataCollector collector = new TsDataCollector();
        int n = 100;
        Day day = Day.toDay();
        Random rnd = new Random();
        TsData s = null;
        for (int i = 0; i < n; ++i) {
            // Add a new observation (date, value)
            collector.addObservation(day.getTime(), i);
            // Creates a new time series. The most suitable frequency is automatically choosen
            // The creation will fail if the collector contains less than 2 observations
            s = collector.make(TsFrequency.Undefined, TsAggregationType.None);
            if (i >= 2) {
                assertTrue(s != null);
                assertTrue(s.getLength() >= i + 1);
            }
            day = day.plus(31 + rnd.nextInt(10));
        }
        System.out.println(s);
    }

    //@Test
    public void demoCreation2() {
        TsDataCollector collector = new TsDataCollector();
        int n = 10000;
        Day day = Day.toDay();
        Random rnd = new Random();
        for (int i = 0; i < n; ++i) {
            // Add a new observation (date, value)
            // New observation may belong to the same period (month, quarter...)
            collector.addObservation(day.getTime(), i);
            day = day.plus(rnd.nextInt(3));
        }
        // Creates a new time series. The frequency is specified
        // The observations belonging to the same period are aggregated following the 
        // specified method
        TsData s = collector.make(TsFrequency.Quarterly, TsAggregationType.Sum);
        DescriptiveStatistics stats = new DescriptiveStatistics(s);
        assertTrue(Math.round(stats.getSum()) == n * (n - 1) / 2);
        //System.out.println(s);
    }
}
