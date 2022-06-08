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
package jdplus.regarima.outlier;

import demetra.arima.SarimaOrders;
import demetra.data.Data;
import demetra.data.DoubleSeq;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.DayClustering;
import jdplus.math.matrices.FastMatrix;
import jdplus.modelling.regression.AdditiveOutlierFactory;
import jdplus.modelling.regression.GenericTradingDaysFactory;
import jdplus.modelling.regression.IOutlierFactory;
import jdplus.modelling.regression.LevelShiftFactory;
import jdplus.modelling.regression.PeriodicOutlierFactory;
import jdplus.regarima.RegArimaModel;
import jdplus.sarima.SarimaModel;

/**
 *
 * @author PALATEJ
 */
public class ExactSingleOutlierDetectorTest {
    
    public ExactSingleOutlierDetectorTest() {
    }

    public static void main(String[] args) {
        stressTest();
    }

    public static void stressTest() {
        int K = 1000;
        double[] A = Data.PROD.clone();
        A[14] *= 1.3;
        A[55] *= .7;
        DoubleSeq Y = DoubleSeq.of(A);
        FastMatrix days = FastMatrix.make(A.length, 7);
        GenericTradingDaysFactory.fillTdMatrix(TsPeriod.monthly(1967, 1), days);
        FastMatrix td = GenericTradingDaysFactory.generateContrasts(DayClustering.TD3, days);

        int[] length = new int[]{40, 60, 120, 180, 240, 300, 336};
        for (int l = 0; l < length.length; ++l) {
            long t0 = System.currentTimeMillis();
            for (int k = 0; k < K; ++k) {
                SarimaOrders spec=SarimaOrders.airline(12);
                SarimaModel model = SarimaModel.builder(spec)
                        .setDefault().build();
                forwardstep(model, Y.log().range(0, length[l]), td.extract(0, length[l], 0, td.getColumnsCount()));
//                OutliersDetection od = OutliersDetection.builder()
//                        .bsm(spec)
//                        .maxIter(1)
//                        .build();
//                od.process(Y.log().range(0, length[l]), td.extract(0, length[l], 0, td.getColumnsCount()), 12);
            }
            long t1 = System.currentTimeMillis();
            System.out.println(t1 - t0);
        }
    }
    
    private static boolean forwardstep(SarimaModel model, DoubleSeq y, FastMatrix W) {
        
        ExactSingleOutlierDetector sod=new ExactSingleOutlierDetector(null, null, null);
        IOutlierFactory[] factories=new IOutlierFactory[]{AdditiveOutlierFactory.FACTORY,LevelShiftFactory.FACTORY_ZEROENDED, new PeriodicOutlierFactory(12, true)};
        sod.setOutlierFactories(factories);
        sod.prepare(y.length());
        RegArimaModel<SarimaModel> regarima=RegArimaModel.builder()
                .arima(model)
                .y(y)
//                .addX(W)
                .build();
        sod.process(regarima);
        return true;
    }
    
}
