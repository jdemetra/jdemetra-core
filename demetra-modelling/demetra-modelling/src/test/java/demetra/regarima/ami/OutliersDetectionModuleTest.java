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
package demetra.regarima.ami;

import demetra.data.Data;
import demetra.data.DoubleSequence;
import demetra.regarima.RegArimaModel;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import demetra.sarima.GlsSarimaProcessor;
import demetra.sarima.internal.HannanRissanenInitializer;
import demetra.timeseries.TsPeriod;
import java.util.function.Consumer;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class OutliersDetectionModuleTest {

    public OutliersDetectionModuleTest() {
    }

    @Test
    public void testMonthly() {
        TsPeriod start = TsPeriod.monthly(1967, 1);
        HannanRissanenInitializer hr = HannanRissanenInitializer.builder().build();
        OutliersDetectionModule<SarimaModel> od = OutliersDetectionModule.build(SarimaModel.class)
                .processor(GlsSarimaProcessor.builder().initializer(hr).build())
                .setAll()
                .build();
        od.setCriticalValue(3.0);
        SarimaSpecification spec = new SarimaSpecification();
        spec.airline(12);
        SarimaModel sarima = SarimaModel.builder(spec).setDefault().build();

        Consumer<int[]> hook = a -> System.out.println("Add outlier: " + od.getFactory(a[1]).getCode() + '-' + start.plus(a[0]).display());
        Consumer<int[]> rhook = a -> System.out.println("Remove outlier: " + od.getFactory(a[1]).getCode() + '-' + start.plus(a[0]).display());
        RegArimaModel<SarimaModel> regarima = RegArimaModel.builder(SarimaModel.class).y(DoubleSequence.of(Data.PROD)).arima(sarima).build();
        od.setAddHook(hook);
        od.setRemoveHook(rhook);
//        long t0 = System.currentTimeMillis();
//        for (int i = 0; i < 100; ++i) {
        od.process(regarima);
//        }
//        long t1 = System.currentTimeMillis();
//        System.out.println(t1 - t0);
        assertTrue(od.getOutliers().length == 8);
    }

}
