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
package jdplus.math.polynomials.spi;

import demetra.data.DoubleSeq;
import demetra.advanced.math.Polynomials;
import jdplus.math.polynomials.Polynomial;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author PALATEJ
 */
public class PolynomialsProcessorTest {

    public PolynomialsProcessorTest() {
    }

    @Test
    public void testEvaluation() {
        DoubleSeq seq = DoubleSeq.onMapping(10, i -> (i + 1));
        Polynomial P = Polynomial.of(seq.toArray());
        double z0 = P.evaluateAt(0.33);
        double z1 = Polynomials.evaluate(seq, 0.33);
        assertEquals(z0, z1, 1e-8);
    }

    public static void main(String[] arg) {
        DoubleSeq seq = DoubleSeq.onMapping(5, i -> (i + 1)).commit();
        Polynomial P = Polynomial.of(seq.toArray());
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 100000000; ++i) {
            double z = P.evaluateAt(1 / (Math.sqrt(i) + 1));
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < 100000000; ++i) {
            double z = Polynomials.evaluate(seq, 1 / (Math.sqrt(i) + 1));
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

}
