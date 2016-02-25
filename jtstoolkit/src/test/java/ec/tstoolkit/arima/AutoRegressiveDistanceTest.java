/*
 * Copyright 2015 National Bank of Belgium
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
/*
 */
package ec.tstoolkit.arima;

import ec.tstoolkit.arima.estimation.Forecasts;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.modelling.arima.x13.UscbForecasts;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaModelBuilder;
import ec.tstoolkit.sarima.SarimaSpecification;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class AutoRegressiveDistanceTest {

    public AutoRegressiveDistanceTest() {
    }

    @Test
    public void testDistance() {
        SarimaModelBuilder builder = new SarimaModelBuilder();
        SarimaModel tmp1 = builder.createAirlineModel(11, -.8, -.1);
        SarimaModel tmp2 = builder.createAirlineModel(11, -.5, -.3);
        assertTrue(Math.abs(AutoRegressiveDistance.compute(tmp1, tmp2, 200) - AutoRegressiveDistance.compute2(tmp1, tmp2, 200)) < 1e-9);
    }
}
