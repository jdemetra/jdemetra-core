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

package ec.tstoolkit.data;

import ec.tstoolkit.arima.estimation.FastArimaForecasts;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.ssf.ExtendedSsfData;
import ec.tstoolkit.ssf.Filter;
import ec.tstoolkit.ssf.FilteringResults;
import ec.tstoolkit.ssf.Smoother;
import ec.tstoolkit.ssf.SmoothingResults;
import ec.tstoolkit.ssf.SsfData;
import ec.tstoolkit.ssf.SsfRefData;
import ec.tstoolkit.ssf.arima.SsfArima;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class SplineInterpolationTest {

    public SplineInterpolationTest() {
    }

    @Test
    public void testSplineCalendarization() {
        SplineInterpolation spline = new SplineInterpolation();
        spline.add(0, 0);
        spline.add(28, 9000);
        spline.add(56, 14000);
        spline.add(84, 23500);
        spline.add(112, 30500);

        double[] y = new double[113];
        for (int j = 0; j < 113; ++j) {
            y[j] = spline.evaluate(j);
        }
        
        double[] z=new double[134];
        for (int i=1; i<134; ++i)
            z[i]=Double.NaN;
        z[28]=9000;z[56]=14000; z[84]=23500; z[112]=30500;
        
        SarimaSpecification spec=new SarimaSpecification(1);
        spec.setD(2);
        SarimaModel model=new SarimaModel(spec);
        FastArimaForecasts ff=new FastArimaForecasts(model, false);
        double[] forecasts = ff.forecasts(y, 21);
        
        SmoothingResults sf=new SmoothingResults();
        Smoother sm=new Smoother();
        sm.setSsf(new SsfArima(model));
        sm.process(new SsfRefData(new ReadDataBlock(z), null), sf);
        double[] zc=sf.component(0);
    }
}