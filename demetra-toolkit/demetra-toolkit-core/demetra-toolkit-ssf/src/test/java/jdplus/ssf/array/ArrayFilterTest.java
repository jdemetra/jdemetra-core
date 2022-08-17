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
package jdplus.ssf.array;

import demetra.arima.SarimaOrders;
import demetra.data.Data;
import jdplus.sarima.SarimaModel;
import jdplus.ssf.arima.SsfArima;
import jdplus.ssf.ckms.CkmsFilter;
import jdplus.ssf.univariate.OrdinaryFilter;
import jdplus.ssf.univariate.PredictionErrorDecomposition;
import jdplus.ssf.univariate.SsfData;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class ArrayFilterTest {

    private static final int N = 100000, M = 100000;
    static final jdplus.sarima.SarimaModel arima;
    static final double[] data;

    static {
        SarimaOrders spec = new SarimaOrders(12);
        spec.setRegular(0, 0, 1);
        spec.setSeasonal(0, 0, 1);
        arima = SarimaModel.builder(spec).theta(1, -.6).btheta(1, -.8).build();
        data = Data.TS_PROD.delta(1).delta(12).getValues().toArray();
    }

    public ArrayFilterTest() {
    }

    @Test
    public void testFilter() {
        PredictionErrorDecomposition pe1 = new PredictionErrorDecomposition(false);
        ArrayFilter af = new ArrayFilter();
        af.process(SsfArima.ssf(arima), new SsfData(data), pe1);
        PredictionErrorDecomposition pe2 = new PredictionErrorDecomposition(false);
        OrdinaryFilter of = new OrdinaryFilter();
        of.process(SsfArima.ssf(arima), new SsfData(data), pe2);
        assertEquals(pe1.likelihood(true).logLikelihood(), pe2.likelihood(true).logLikelihood(), 1e-9);
        PredictionErrorDecomposition pe3 = new PredictionErrorDecomposition(false);
        CkmsFilter cf = new CkmsFilter(SsfArima.fastInitializer(arima));
        cf.process(SsfArima.ssf(arima), new SsfData(data), pe3);
        System.out.println(pe1.likelihood(true));
        System.out.println(pe2.likelihood(true));
        System.out.println(pe3.likelihood(true));
        assertEquals(pe1.likelihood(true).logLikelihood(), pe3.likelihood(true).logLikelihood(), 1e-9);
    }

    public static void main(String[] args) {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < M; ++i) {
            PredictionErrorDecomposition pe = new PredictionErrorDecomposition(false);
            ArrayFilter af = new ArrayFilter();
            af.process(SsfArima.ssf(arima), new SsfData(data), pe);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(""
                + "Array");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < M; ++i) {
            PredictionErrorDecomposition pe = new PredictionErrorDecomposition(false);
            OrdinaryFilter of = new OrdinaryFilter();
            of.process(SsfArima.ssf(arima), new SsfData(data), pe);
        }
        t1 = System.currentTimeMillis();
        System.out.println("Normal");
        System.out.println(t1 - t0);

        t0 = System.currentTimeMillis();
        for (int i = 0; i < M; ++i) {
            PredictionErrorDecomposition pe = new PredictionErrorDecomposition(false);
            CkmsFilter cf = new CkmsFilter(SsfArima.fastInitializer(arima));
            cf.process(SsfArima.ssf(arima), new SsfData(data), pe);
        }
        t1 = System.currentTimeMillis();
        System.out.println("Normal");
        System.out.println(t1 - t0);
    }

}
