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
package demetra.arima.ssf;

import demetra.arima.regarima.ConcentratedLikelihoodComputer;
import demetra.arima.regarima.ConcentratedLikelihoodEstimation;
import demetra.arima.regarima.RegArimaModel;
import demetra.data.Data;
import demetra.data.DoubleSequence;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import demetra.ssf.akf.AkfToolkit;
import demetra.ssf.akf.DiffuseLikelihood;
import demetra.ssf.dk.DkLikelihood;
import demetra.ssf.dk.DkToolkit;
import demetra.ssf.implementations.TimeInvariantSsf;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.SsfData;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class SsfArimaTest {

    private static final int N = 100000, M = 10000;
    static final demetra.sarima.SarimaModel arima;
    static final double[] data;
    
    static{
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.airline();
        arima = SarimaModel.builder(spec).theta(1, -.6).btheta(1, -.8).build();
        data = Data.PROD.clone();
    }

    public SsfArimaTest() {
    }

    @Test
    public void testArima() {
        SsfArima ssf = SsfArima.of(arima);
        SsfData sdata = new SsfData(data);
        SsfData ssfData = new SsfData(data);
        DkLikelihood ll1 = DkToolkit.likelihoodComputer(false, true).compute(ssf, ssfData);
       // square root form
        DkLikelihood ll2 = DkToolkit.likelihoodComputer(true, true).compute(ssf, ssfData);
//        System.out.println(ll1);
//        System.out.println(ll2);
        assertEquals(ll1.logLikelihood(), ll2.logLikelihood(), 1e-6);
        DiffuseLikelihood ll3 = AkfToolkit.likelihoodComputer().compute(ssf, ssfData);
//        System.out.println(ll3);
        assertEquals(ll1.logLikelihood(), ll3.logLikelihood(), 1e-6);
        RegArimaModel<SarimaModel> model = RegArimaModel.builder(DoubleSequence.of(data), arima)
                .build();
        ConcentratedLikelihoodEstimation cll = ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(model);
        assertEquals(ll1.logLikelihood(), cll.getLikelihood().logLikelihood(), 1e-6);
    }

    @Test
    public void testArimaWithMissing() {
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.airline();
        SarimaModel arima = SarimaModel.builder(spec).theta(1, -.6).btheta(1, -.8).build();
        SsfArima ssf = SsfArima.of(arima);
        double[] mdata=data.clone();
        mdata[2] = Double.NaN;
        mdata[11] = Double.NaN;
        mdata[119] = Double.NaN;

        SsfData sdata = new SsfData(mdata);
        DkLikelihood ll1 = DkToolkit.likelihoodComputer(false, true).compute(ssf, sdata);
        // square root form
        DkLikelihood ll2 = DkToolkit.likelihoodComputer(true, true).compute(ssf, sdata);
        assertEquals(ll1.logLikelihood(), ll2.logLikelihood(), 1e-6);
        DiffuseLikelihood ll3 = AkfToolkit.likelihoodComputer().compute(ssf, sdata);
        assertEquals(ll1.logLikelihood(), ll3.logLikelihood(), 1e-6);
        RegArimaModel<SarimaModel> model = RegArimaModel.builder(DoubleSequence.of(data), arima)
                .missing(2, 11, 119)
                .build();
        ConcentratedLikelihoodEstimation cll = ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(model);
        assertEquals(ll1.logLikelihood(), cll.getLikelihood().logLikelihood(), 1e-6);
    }
    
    @Ignore
    @Test
    public void testStressLikelihood() {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < M; ++i) {
            SsfArima ssf = SsfArima.of(arima);
            DkLikelihood ll = DkToolkit.likelihoodComputer(false).compute(ssf, new SsfData(data));
        }
        long t1 = System.currentTimeMillis();
        System.out.println("DK (normal)");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < M; ++i) {
            SsfArima ssf = SsfArima.of(arima);
            DkLikelihood ll = DkToolkit.likelihoodComputer(true).compute(ssf, new SsfData(data));
        }
        t1 = System.currentTimeMillis();
        System.out.println("DK (square root form)");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < M; ++i) {
            SsfArima ssf = SsfArima.of(arima);
            ISsf tssf = TimeInvariantSsf.of(ssf);
            DkLikelihood ll = DkToolkit.likelihoodComputer(true).compute(tssf, new SsfData(data));
        }

        t1 = System.currentTimeMillis();
        System.out.println("DK Filter. Matrix");
        System.out.println(t1 - t0);
    }
    
}
