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
package demetra.x12;

import demetra.data.Data;
import ec.tstoolkit.modelling.DefaultTransformationType;
import org.junit.Test;
import static org.junit.Assert.*;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
public class LogLevelModuleTest {

    public LogLevelModuleTest() {
    }

    @Test
    public void testProd() {

//        long t0 = System.currentTimeMillis();
//        System.out.println("New");
//        for (int i = 0; i < 1000; ++i) {
//        LogLevelModule ll = LogLevelModule.builder()
//                .estimationPrecision(1e-7)
//                .build();
//        ll.process(DoubleSequence.ofInternal(Data.PROD), 12, true);
//        assertTrue(ll.isChoosingLog());
//        System.out.println(ll.getAICcLevel());
//        System.out.println(ll.getAICcLog());
//        System.out.println(ll.isChoosingLog());
//        }
//        long t1 = System.currentTimeMillis();
//        System.out.println(t1 - t0);
    }

//    @Test
    public void testProdLegacy() {

//        long t0 = System.currentTimeMillis();
        System.out.println("Legacy");
//        for (int i = 0; i < 1000; ++i) {
        ec.tstoolkit.modelling.arima.x13.LogLevelTest ll = new ec.tstoolkit.modelling.arima.x13.LogLevelTest();
        ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, Data.PROD, true);
        ec.tstoolkit.modelling.arima.ModelDescription desc = new ec.tstoolkit.modelling.arima.ModelDescription(s, null);
        ec.tstoolkit.modelling.arima.ModellingContext context = new ec.tstoolkit.modelling.arima.ModellingContext();
        desc.setTransformation(DefaultTransformationType.Auto);
        desc.setAirline(true);
        context.description = desc;
        context.hasseas = true;
        ll.process(context);
        System.out.println(ll.getLevel().getStatistics().AICC);
        System.out.println(ll.getLog().getStatistics().AICC);
        System.out.println(ll.isChoosingLog());
//        }
//        long t1 = System.currentTimeMillis();
//        System.out.println(t1 - t0);
    }

}
