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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class TsDataCollectorTest {

    public TsDataCollectorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
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
        TsData s =  collector.make(TsFrequency.Quarterly, TsAggregationType.Sum);
        DescriptiveStatistics stats=new DescriptiveStatistics(s);
        assertTrue(Math.round(stats.getSum()) == n*(n-1)/2 );
        //System.out.println(s);
    }
}
