/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.sarima;

import demetra.data.Data;
import demetra.data.DoubleSequence;
import demetra.regarima.RegArimaEstimation;
import demetra.regarima.RegArimaModel;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class SarimaFixedMappingTest {

    public SarimaFixedMappingTest() {
    }

    @Test
    public void testProd() {
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.airline(true);
        spec.setP(3);
        GlsSarimaProcessor processor = GlsSarimaProcessor.builder()
                .mapping(new SarimaFixedMapping(spec, DoubleSequence.of(-.6, .2, -.3, -.3, -.6), new boolean[]{false, false, true, true, false}))
                .precision(1e-9)
                .build();
        RegArimaModel<SarimaModel> model = RegArimaModel.builder(SarimaModel.class)
                .y(DoubleSequence.ofInternal(Data.PROD))
                .arima(SarimaModel.builder(spec).setDefault().build())
                .meanCorrection(true)
                .build();
//        System.out.println("GlsArima");
        RegArimaEstimation<SarimaModel> rslt = processor.process(model);
//        System.out.println(rslt.getModel().arima());
//        System.out.println(rslt.getConcentratedLikelihood().logLikelihood());
    }

    @Test
    public void testProd2() {
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.airline(true);
        spec.setP(3);
        RegSarimaProcessor processor = RegSarimaProcessor.builder()
                .mapping(new SarimaFixedMapping(spec, DoubleSequence.of(-.6, .2, -.3, -.3, -.6), new boolean[]{false, false, true, true, false}))
                .precision(1e-9)
                .startingPoint(RegSarimaProcessor.StartingPoint.Default)
                .build();
        RegArimaModel<SarimaModel> model = RegArimaModel.builder(SarimaModel.class)
                .y(DoubleSequence.ofInternal(Data.PROD))
                .arima(SarimaModel.builder(spec).setDefault().build())
                .meanCorrection(true)
                .build();
//        System.out.println("RegArima");
        RegArimaEstimation<SarimaModel> rslt = processor.process(model);
//        System.out.println(rslt.getModel().arima());
//        System.out.println(rslt.getConcentratedLikelihood().logLikelihood());
    }
}
