/*
 * Copyright 2013-2014 National Bank of Belgium
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
package ec.tstoolkit.arima;

import ec.tstoolkit.dstats.T;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class ArimaModelBuilderTest {
    
    public ArimaModelBuilderTest() {
    }
    
    @Test
    public void testStationary() {
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.setP(3);
        spec.setBP(1);
        spec.setQ(1);
        spec.setBQ(1);
        SarimaModel arima=new SarimaModel(spec);
        arima.setBTheta(1, -.9);
        arima.setPhi(1, -.6);
        double[] x = new ArimaModelBuilder().generateStationary(arima, 200);
        for (int i = 0; i < x.length; ++i) {
            System.out.println(x[i]);
        }
    }
    
    @Test
    public void testNonStationary() {
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.airline();
//        spec.setP(3);
//        spec.setBP(1);
//        spec.setQ(1);
//        spec.setBQ(1);
        SarimaModel arima=new SarimaModel(spec);
        arima.setBTheta(1, -.9);
        arima.setTheta(1, -.6);
        ArimaModelBuilder builder=new ArimaModelBuilder();
        builder.setDistribution(new T());
        double[] x = builder.generate(arima, 300);
        for (int i = 0; i < x.length; ++i) {
            System.out.println(x[i]);
        }
   }
}
