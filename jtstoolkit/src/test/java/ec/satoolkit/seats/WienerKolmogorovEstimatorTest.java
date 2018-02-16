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

package ec.satoolkit.seats;

import data.Data;
import ec.satoolkit.DefaultSeriesDecomposition;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.modelling.ComponentInformation;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaModelBuilder;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.ucarima.UcarimaModel;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author Jean Palate
 */
public class WienerKolmogorovEstimatorTest {

    private static final double EPS = 1e-5;

    public WienerKolmogorovEstimatorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testAirline() {
        DefaultModelDecomposer decomposer = new DefaultModelDecomposer(false);
        DescriptiveStatistics tstats = new DescriptiveStatistics(Data.X);
        double range = tstats.getMax() - tstats.getMin();
        for (int i = 1; i <= 19; i+=2) {
            for (int j = 1; j <= 19; j+=2) {
                SarimaModel arima = new SarimaModelBuilder().createAirlineModel(12, i * .1 - 1, j * .1 - 1);
                SeatsModel seats = new SeatsModel(Data.X, arima, false);
                UcarimaModel ucm = decomposer.decompose(seats, null, null);
                //System.out.println(arima);
                if (ucm != null) {
                    WienerKolmogorovEstimator wk = new WienerKolmogorovEstimator(-1);
                    DefaultSeriesDecomposition sa1 = wk.decompose(seats, ucm, null, null);
                    KalmanEstimator kl = new KalmanEstimator(-1);
                    DefaultSeriesDecomposition sa2 = kl.decompose(seats, ucm, null, null);
                    TsData t1 = sa1.getSeries(ComponentType.Trend, ComponentInformation.Value);
                    TsData t2 = sa2.getSeries(ComponentType.Trend, ComponentInformation.Value);
                    double d = t1.distance(t2) / range;
//                    System.out.println(d);
//                    System.out.print(i);
//                    System.out.print('\t');
//                    System.out.print(j);
//                    System.out.print('\t');
//                    System.out.print(d);
                    assertTrue(d < EPS);
                    t1 = sa1.getSeries(ComponentType.Seasonal, ComponentInformation.Value);
                    t2 = sa2.getSeries(ComponentType.Seasonal, ComponentInformation.Value);
                    d = t1.distance(t2) / range;
                    //                   System.out.println(d);
//                    System.out.print('\t');
//                    System.out.print(d);
                    assertTrue(d < EPS);
                    seats.setMeanCorrection(true);
                    sa1 = wk.decompose(seats, ucm, null, null);
                    sa2 = kl.decompose(seats, ucm, null, null);
                    t1 = sa1.getSeries(ComponentType.Trend, ComponentInformation.Value);
                    t2 = sa2.getSeries(ComponentType.Trend, ComponentInformation.Value);
                    d = t1.distance(t2) / range;
                    //                   System.out.println(d);
//                    System.out.print('\t');
//                    System.out.print(d);
                    assertTrue(d < EPS);
                    t1 = sa1.getSeries(ComponentType.Seasonal, ComponentInformation.Value);
                    t2 = sa2.getSeries(ComponentType.Seasonal, ComponentInformation.Value);
                    d = t1.distance(t2) / range;
                    //                  System.out.println(d);
//                    System.out.print('\t');
//                    System.out.println(d);
                    assertTrue(d < EPS);
                }
            }
        }
    }
}
