/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.seats;

import demetra.arima.SarimaSpec;
import demetra.data.Data;
import demetra.data.DoubleSeq;
import demetra.processing.ProcessingLog;
import demetra.seats.ComponentsSpec;
import demetra.seats.ModelSpec;
import demetra.seats.SeatsSpec;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class SeatsKernelTest {

    public SeatsKernelTest() {
    }

    @Test
    public void testProdBurman() {
        ModelSpec model = ModelSpec.builder()
                .log(true)
                .sarimaSpec(SarimaSpec.airline())
                .build();
        ComponentsSpec cmps = ComponentsSpec.builder()
                .backCastCount(0)
                .forecastCount(0)
                .build();

        SeatsSpec spec = SeatsSpec.builder()
                .componentsSpec(cmps)
                .modelSpec(model)
                .build();

        SeatsToolkit toolkit = SeatsToolkit.of(spec);
        SeatsKernel kernel = new SeatsKernel(toolkit);
        ProcessingLog log = new ProcessingLog();
        SeatsResults rslt = kernel.process(DoubleSeq.of(Data.PROD), 12, log);
        assertTrue(rslt != null);
//        log.all().forEach(v -> System.out.println(v));
//        System.out.println(rslt.getFinalComponents());
    }

    @Test
    public void testProdKF() {
        ModelSpec model = ModelSpec.builder()
                .log(true)
                .sarimaSpec(SarimaSpec.airline())
                .build();
        ComponentsSpec cmps = ComponentsSpec.builder()
                .backCastCount(-2)
                .forecastCount(-2)
                .method(ComponentsSpec.ComponentsEstimationMethod.KalmanSmoother)
                .build();

        SeatsSpec spec = SeatsSpec.builder()
                .componentsSpec(cmps)
                .modelSpec(model)
                .build();

        SeatsToolkit toolkit = SeatsToolkit.of(spec);
        SeatsKernel kernel = new SeatsKernel(toolkit);
        ProcessingLog log = new ProcessingLog();
        SeatsResults rslt = kernel.process(DoubleSeq.of(Data.PROD), 12, log);
        assertTrue(rslt != null);
//        log.all().forEach(v -> System.out.println(v));
//        System.out.println(rslt.getFinalComponents());
    }

    @Test
    public void testProdKF3() {
        SarimaSpec mspec = SarimaSpec.builder()
                .p(3).d(1).q(1).bp(0).bd(1).bq(1).build();
                
        ModelSpec model = ModelSpec.builder()
                .log(true)
                .sarimaSpec(mspec)
                .build();
        ComponentsSpec cmps = ComponentsSpec.builder()
                .backCastCount(-2)
                .forecastCount(-2)
                .method(ComponentsSpec.ComponentsEstimationMethod.KalmanSmoother)
                .build();

        SeatsSpec spec = SeatsSpec.builder()
                .componentsSpec(cmps)
                .modelSpec(model)
                .build();

        SeatsToolkit toolkit = SeatsToolkit.of(spec);
        SeatsKernel kernel = new SeatsKernel(toolkit);
        ProcessingLog log = new ProcessingLog();
        SeatsResults rslt = kernel.process(DoubleSeq.of(Data.PROD), 12, log);
        assertTrue(rslt != null);
//        log.all().forEach(v -> System.out.println(v));
//        System.out.println(rslt.getFinalComponents());
//        System.out.println(rslt.getUcarimaModel());
        
    }
}
