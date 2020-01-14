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
package jdplus.regarima.outlier;

import jdplus.regarima.outlier.FastOutlierDetector;
import jdplus.data.DataBlock;
import jdplus.regarima.RegArimaModel;
import jdplus.sarima.SarimaModel;
import demetra.arima.SarmaSpecification;
import demetra.timeseries.regression.AdditiveOutlier;
import jdplus.modelling.regression.AdditiveOutlierFactory;
import demetra.timeseries.regression.LevelShift;
import jdplus.modelling.regression.LevelShiftFactory;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class FastOutlierDetectorTest {
    
   

    public FastOutlierDetectorTest() {
    }

    //@Test
    public void testNew() {
        DataBlock rnd = DataBlock.make(600);
        Random gen = new Random(0);
        rnd.set(gen::nextDouble);
        FastOutlierDetector sod = new FastOutlierDetector(null);
        sod.addOutlierFactory(AdditiveOutlierFactory.FACTORY);
        sod.addOutlierFactory(LevelShiftFactory.FACTORY_ZEROENDED);
        SarmaSpecification spec = new SarmaSpecification(12);
        spec.setBq(1);
        spec.setQ(1);
        SarimaModel model = SarimaModel.builder(spec)
                .setDefault()
                .build();
        RegArimaModel<SarimaModel> regarima = RegArimaModel.builder(SarimaModel.class)
                .y(rnd)
                .meanCorrection(true)
                .arima(model)
                .build();
            sod.process(regarima);
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            sod.process(regarima);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

    //@Test
    public void testLegacy() {
        ec.tstoolkit.data.DataBlock rnd = new ec.tstoolkit.data.DataBlock(600);
        Random gen = new Random(0);
        rnd.set(gen::nextDouble);
        ec.tstoolkit.modelling.arima.tramo.SingleOutlierDetector sod=new ec.tstoolkit.modelling.arima.tramo.SingleOutlierDetector();
        ec.tstoolkit.sarima.SarmaSpecification spec=new ec.tstoolkit.sarima.SarmaSpecification(12);
        sod.addOutlierFactory(new ec.tstoolkit.timeseries.regression.AdditiveOutlierFactory());
        sod.addOutlierFactory(new ec.tstoolkit.timeseries.regression.LevelShiftFactory());
        sod.prepare(new ec.tstoolkit.timeseries.simplets.TsDomain(TsFrequency.Monthly, 1980, 0, 600), null);
        spec.setQ(1);
        spec.setBQ(1);
        ec.tstoolkit.sarima.SarimaModel model=new ec.tstoolkit.sarima.SarimaModel(spec);
        model.setDefault();
        System.out.println("Legacy");
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            sod.process(model, rnd);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);

    }

}
