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
package ec.benchmarking.ssf;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.ssf.DisturbanceSmoother;
import ec.tstoolkit.ssf.SmoothingResults;
import ec.tstoolkit.ssf.SsfData;
import ec.tstoolkit.ssf.arima.SsfArima;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class SsfSplineTest {
    
    public SsfSplineTest() {
    }

    @Test
    public void testSsfCalendarization() {
        // 
        double[] x = new double[134];
        for (int i = 0; i < x.length; ++i) {
            x[i] = Double.NaN;
        }

        x[0]=0;
        x[28] = 9000;
        x[56] = 14000;
        x[84] = 23500;
        x[112] = 30500;

        SsfData sx = new SsfData(x, null);

        DisturbanceSmoother smoother = new DisturbanceSmoother();
        smoother.setSsf(new SsfSpline());
        smoother.process(sx);
        SmoothingResults sstates = smoother.calcSmoothedStates();
        
        double[] c0 = sstates.component(0);
    }
    
    @Test
    public void testSsfI2() {
        // 
        double[] x = new double[134];
        for (int i = 0; i < x.length; ++i) {
            x[i] = Double.NaN;
        }

        x[0]=0;
        x[28] = 9000;
        x[56] = 14000;
        x[84] = 23500;
        x[112] = 30500;

        SsfData sx = new SsfData(x, null);

        SarimaSpecification spec=new SarimaSpecification(1);
        spec.setD(2);
        DisturbanceSmoother smoother = new DisturbanceSmoother();
        smoother.setSsf(new SsfArima(new SarimaModel(spec)));
        smoother.process(sx);
        SmoothingResults sstates = smoother.calcSmoothedStates();
        
        double[] c0 = sstates.component(0);
    }
}