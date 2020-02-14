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

import demetra.arima.SarimaOrders;
import jdplus.math.polynomials.Polynomial;
import jdplus.sarima.SarimaModel;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class DefaultModelValidatorTest {

    public DefaultModelValidatorTest() {
    }

    @Test
    public void testValidation1() {
        SarimaOrders orders = new SarimaOrders(12);
        orders.setRegular(1, 1, 1);
        orders.setSeasonal(0, 1, 1);
        SarimaModel arima = SarimaModel.builder(orders)
                .setDefault()
                .phi(1e-8)
                .theta(.99)
                .btheta(-.99)
                .build();
        DefaultModelValidator validator = DefaultModelValidator.builder()
                .xl(.95)
                .build();
        assertFalse(validator.validate(arima));
        SarimaModel newModel = validator.getNewModel();
        DefaultModelValidator validator2 = DefaultModelValidator.builder()
                .xl(.95 + 1e-6)
                .build();
//        System.out.println(newModel);
        assertTrue(validator2.validate(newModel));
    }

    @Test
    public void testValidation2() {
        SarimaOrders orders = new SarimaOrders(12);
        orders.setRegular(3, 1, 3);
        orders.setSeasonal(0, 1, 1);
        SarimaModel arima = SarimaModel.builder(orders)
                .setDefault()
                .phi(.5, .3, 1e-8)
                .theta(p(.96, -.98, .2))
                .btheta(-.99)
                .build();
        DefaultModelValidator validator = DefaultModelValidator.builder()
                .xl(.95)
                .build();
        assertFalse(validator.validate(arima));
        SarimaModel newModel = validator.getNewModel();
        DefaultModelValidator validator2 = DefaultModelValidator.builder()
                .xl(.95 + 1e-6)
                .build();
//        System.out.println(newModel);
        assertTrue(validator2.validate(newModel));
    }

    private double[] p(double... r) {
        Polynomial P = Polynomial.ONE;
        for (int i = 0; i < r.length; ++i) {
            Polynomial Q = Polynomial.of(1, -r[i]);
            P = P.times(Q);
        }
        return P.coefficients().drop(1, 0).toArray();
    }
}
