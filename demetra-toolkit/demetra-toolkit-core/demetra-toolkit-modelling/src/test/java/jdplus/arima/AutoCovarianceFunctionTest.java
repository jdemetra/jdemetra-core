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
package jdplus.arima;

import demetra.arima.SarimaOrders;
import demetra.data.DoubleSeq;
import jdplus.math.linearfilters.BackFilter;
import jdplus.sarima.SarimaModel;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class AutoCovarianceFunctionTest {

    public AutoCovarianceFunctionTest() {
    }

    @Test
    public void testSarima() {
        SarimaOrders orders = SarimaOrders.airline(12);
        orders.setQ(0);
        SarimaModel arima = SarimaModel.builder(orders)
                .btheta(-.9)
                .build();
        SarimaModel arma = arima.stationaryTransformation().getStationaryModel();
        BackFilter ma = arma.getMa();
        assertTrue(ma.getDegree() == 12);
        AutoCovarianceFunction acf = arma.getAutoCovarianceFunction();
        double[] ac = acf.values(15);
        System.out.println(DoubleSeq.of(ac));
    }

    @Test
    public void testAR() {
        SarimaOrders orders = new SarimaOrders(1);
        orders.setP(3);
        SarimaModel arima = SarimaModel.builder(orders)
                .phi(-.2,-.20000001, -.2)
                .build();
        AutoCovarianceFunction acf = arima.getAutoCovarianceFunction();
        double[] ac = acf.values(15);
        System.out.println(DoubleSeq.of(ac));
        ec.tstoolkit.maths.polynomials.Polynomial AR=ec.tstoolkit.maths.polynomials.Polynomial.of(new double[]{1,-.2,-.20000001,-.2});
        ec.tstoolkit.maths.polynomials.Polynomial MA=ec.tstoolkit.maths.polynomials.Polynomial.ONE;
        ec.tstoolkit.arima.AutoCovarianceFunction oacf=new ec.tstoolkit.arima.AutoCovarianceFunction(MA, AR, 1);
        System.out.println(DoubleSeq.of(oacf.values(15)));
        
    }
}
