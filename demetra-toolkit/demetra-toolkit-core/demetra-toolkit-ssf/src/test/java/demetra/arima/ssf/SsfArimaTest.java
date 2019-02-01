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

import demetra.arima.ssf.SsfArima;
import demetra.data.Data;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import demetra.ssf.likelihood.DiffuseLikelihood;
import demetra.ssf.dk.DkToolkit;
import demetra.ssf.implementations.TimeInvariantSsf;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.Ssf;
import demetra.ssf.univariate.SsfData;
import org.junit.Test;
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
        spec.airline(true);
        arima = SarimaModel.builder(spec).theta(1, -.6).btheta(1, -.8).build();
        data = Data.PROD.clone();
    }

    public SsfArimaTest() {
    }

    
    @Ignore
    @Test
    public void testStressLikelihood() {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < M; ++i) {
            Ssf ssf = SsfArima.of(arima);
            DiffuseLikelihood ll = DkToolkit.likelihoodComputer(false).compute(ssf, new SsfData(data));
        }
        long t1 = System.currentTimeMillis();
        System.out.println("DK (normal)");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < M; ++i) {
            Ssf ssf = SsfArima.of(arima);
            DiffuseLikelihood ll = DkToolkit.likelihoodComputer(true).compute(ssf, new SsfData(data));
        }
        t1 = System.currentTimeMillis();
        System.out.println("DK (square root form)");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < M; ++i) {
            Ssf ssf = SsfArima.of(arima);
            ISsf tssf = TimeInvariantSsf.of(ssf);
            DiffuseLikelihood ll = DkToolkit.likelihoodComputer(true).compute(tssf, new SsfData(data));
        }

        t1 = System.currentTimeMillis();
        System.out.println("DK Filter. Matrix");
        System.out.println(t1 - t0);
    }
    
}
