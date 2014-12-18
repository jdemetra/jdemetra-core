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
package ec.tstoolkit.modelling.arima;

import data.Data;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.dstats.ProbabilityType;
import ec.tstoolkit.dstats.T;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataCollector;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author pcuser
 */
public class CheckLastTest {

    public CheckLastTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    //@Test
    public void demo() {

        CheckLast tramoTerror = new CheckLast(TramoSpecification.TR5.build());
        tramoTerror.check(Data.X);
        System.out.println("Tramo-Terror");
        System.out.println(tramoTerror.getScore(0));

        CheckLast xTerror = new CheckLast(RegArimaSpecification.RG5.build());
        xTerror.check(Data.X);
        System.out.println("X13-Terror");
        System.out.println(xTerror.getScore(0));
    }

    //@Test
    public void demoRandom() {
        int n = 1000;
        List<TsData> rndAirlines = Data.rndAirlines(n, 240, -.6, -.8);
        CheckLast terror = new CheckLast(TramoSpecification.TR0.build());

        double[] scores = new double[n];
        int i = 0;
        for (TsData s : rndAirlines) {
            if (terror.check(s)) {
                scores[i++] = terror.getScore(0);
            }
        }
        DescriptiveStatistics stats = new DescriptiveStatistics(scores);

        T t = new T();
        t.setDegreesofFreedom(238);
        double[] quantiles = stats.quantiles(20);
        DecimalFormat df3 = new DecimalFormat("0.000");
        for (int j = 0; j < quantiles.length; ++j) {
            System.out.print(df3.format(quantiles[j]));
            System.out.print('\t');
            System.out.println(df3.format(t.getProbabilityInverse((j + 1) * .05, ProbabilityType.Lower)));
        }
    }

//    @Test
    public void demoPR() {

        // time series creation
        // Method 1: add couples (date, value) in a "TsDataCollector" object
        TsDataCollector collector = new TsDataCollector();
        int n = 240;
        Day day = Day.toDay();
        Random rnd = new Random();
        double val = 100;
        for (int i = 0; i < n; ++i) {
            val += rnd.nextDouble();
            // Add a new observation (date, value)
            collector.addObservation(day.getTime(), val);
            day = day.plus(31 + rnd.nextInt(10));
        }
        // Creates a new time series. The most suitable frequency is automatically choosen
        // If the frequency is known, it should be used in the first parameter
        // The creation will fail if the collector contains less than 2 observations
        TsData s0 = collector.make(TsFrequency.Undefined, TsAggregationType.None);

        // Method 2: 
        val = 100;
        double[] values = new double[n];
        for (int i = 0; i < n; ++i) {
            val += (.1*i)*rnd.nextDouble();
            values[i] = val;
        }
        // Missing values are identified by Double.NaN
        values[n / 3] = Double.NaN;

        // The monthly series will start in January 2012
        TsPeriod start = new TsPeriod(TsFrequency.Monthly, 2012, 0);
        // Creates directly the series. The last parameter indicates that the data are cloned.
        TsData s1 = new TsData(start, values, true);

        // Creates the module that will detect outliers for the last period(s)
        // By default, Tramo (TR4) should be used for automatically identifying 
        // the most suitable RegArima model.
        // The module is able to check more than 1 last observations
        CheckLast tramoTerror = new CheckLast(TramoSpecification.TR4.build());
        tramoTerror.setBackCount(3);
        System.out.println("Tramo-Terror");
        tramoTerror.check(s0);
        // The scores (corresponding respectively to the periods
        // s0[s0.length - 3], s0[s0.length - 2], s0[s0.length - 1])  
        System.out.println(tramoTerror.getScore(0));
        System.out.println(tramoTerror.getScore(1));
        System.out.println(tramoTerror.getScore(2));
        tramoTerror.check(s1);
        double[] scores = tramoTerror.getScores(), fcasts = tramoTerror.getForecastsValues(),
                vals = tramoTerror.getValues(), actuals = tramoTerror.getActualValues();
        for (int i = 0; i < tramoTerror.getBackCount(); ++i) {
            System.out.print(actuals[i]);
            System.out.print('\t');
            System.out.print(fcasts[i]);
            System.out.print('\t');
            System.out.print(vals[i]);
            System.out.print('\t');
            System.out.println(scores[i]);
        }

        System.out.println(tramoTerror.getScore(0));

        System.out.println("X13-Terror");
        CheckLast xTerror = new CheckLast(RegArimaSpecification.RG4.build());
        xTerror.check(s0);
        System.out.println(xTerror.getScore(0));
        xTerror.check(s1);
        System.out.println(xTerror.getScore(0));
    }

}
