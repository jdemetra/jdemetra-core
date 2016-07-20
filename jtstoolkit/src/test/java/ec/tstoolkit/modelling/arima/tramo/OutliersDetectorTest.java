/*
 * Copyright 2016 National Bank of Belgium
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
package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.arima.ArimaModelBuilder;
import ec.tstoolkit.modelling.arima.ModelDescription;
import ec.tstoolkit.modelling.arima.ModelEstimation;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.RegArimaEstimator;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaModelBuilder;
import ec.tstoolkit.sarima.estimation.SarimaMapping;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class OutliersDetectorTest {
    
    public OutliersDetectorTest() {
    }

    @Ignore
    @Test
    public void testLongSeries() {
        SarimaModelBuilder builder=new SarimaModelBuilder();
        SarimaModel air = builder.createAirlineModel(12, -.6, -.8);
        ArimaModelBuilder rnd=new ArimaModelBuilder();
        double[] data = rnd.generate(air, 10000);
        Random rg=new Random();
        for (int i=0; i<10; ++i){
            data[rg.nextInt(data.length)]+=10;
        }
        TsData s=new TsData(TsFrequency.Monthly, 1900,0,data, false);
        OutliersDetector outliers=new OutliersDetector();
        outliers.setDefault();
        ModelDescription desc=new ModelDescription(s, null);
        desc.setAirline(true);
        ModelEstimation est=new ModelEstimation(desc.buildRegArima());
        est.compute(new RegArimaEstimator(new SarimaMapping(air.getSpecification(), true)), 2);
        ModellingContext context=new ModellingContext();
        context.description=desc;
        context.estimation=est;
        outliers.process(context);
    }
    
}
