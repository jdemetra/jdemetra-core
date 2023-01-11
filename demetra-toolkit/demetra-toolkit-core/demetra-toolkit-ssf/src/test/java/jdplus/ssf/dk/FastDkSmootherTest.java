/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.ssf.dk;

import demetra.arima.SarimaOrders;
import demetra.data.Data;
import demetra.data.DoubleSeq;
import java.util.Random;
import jdplus.data.DataBlock;
import jdplus.sarima.SarimaModel;
import jdplus.ssf.arima.SsfArima;
import jdplus.ssf.univariate.DefaultSmoothingResults;
import jdplus.ssf.univariate.Ssf;
import jdplus.ssf.univariate.SsfData;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class FastDkSmootherTest {

    public FastDkSmootherTest() {
    }

    @Test
    public void testArima() {
        SarimaOrders spec = SarimaOrders.airline(12);
        SarimaModel arima = SarimaModel.builder(spec).theta(1, -.6).btheta(1, -.8).build();
        double[] data = Data.PROD.clone();
        Random rnd = new Random(0);
        for (int i = 0; i < 500; ++i) {
            data[rnd.nextInt(data.length)] = Double.NaN;
        }
        Ssf ssf = SsfArima.ssf(arima);
        DefaultDiffuseFilteringResults fr = DkToolkit.filter(ssf, new SsfData(data), true);
        FastDkSmoother smoother = new FastDkSmoother(ssf, fr);

        DoubleSeq z = DoubleSeq.of(Data.PROD);
        smoother.smooth(z);
        DataBlock sz = smoother.smoothedStates().item(0);
        
        DefaultSmoothingResults sr = DkToolkit.smooth(ssf, new SsfData(data), false, false);
        DoubleSeq c = sr.getComponent(0);
        assertTrue(c.distance(sz) <= 1e-9);
    }

    public static void main(String[] args) {
        stressTest();
    }

    public static void stressTest() {
        SarimaOrders spec = SarimaOrders.airline(12);
        SarimaModel arima = SarimaModel.builder(spec).theta(1, -.6).btheta(1, -.8).build();
        double[] data = Data.PROD.clone();
        Random rnd = new Random(0);
        for (int i = 0; i < 500; ++i) {
            data[rnd.nextInt(data.length)] = Double.NaN;
        }
        Ssf ssf = SsfArima.ssf(arima);
        DefaultDiffuseFilteringResults fr = DkToolkit.filter(ssf, new SsfData(data), true);
        FastDkSmoother smoother = new FastDkSmoother(ssf, fr);

        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            DefaultSmoothingResults sr = DkToolkit.smooth(ssf, new SsfData(data), false, false);
            DoubleSeq c = sr.getComponent(0);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("DK");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            DataBlock z = DataBlock.copyOf(Data.PROD);
            smoother.smooth(z);
        }
        t1 = System.currentTimeMillis();
        System.out.println("Fast DK");
        System.out.println(t1 - t0);
    }

}
