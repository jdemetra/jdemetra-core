/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.maths.polynomials.internal;

import demetra.maths.Complex;
import jp.maths.polynomials.Polynomial;
import org.junit.Test;
import static org.junit.Assert.*;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
public class PolynomialComputerTest {

    public PolynomialComputerTest() {
    }

    @Test
    public void testDefault() {
        Polynomial P = Polynomial.ofInternal(DoubleSeq.onMapping(20, i -> 1.0 / (i + 1)).toArray());
        PolynomialComputer computer = new PolynomialComputer(P);
        Complex c = Complex.cart(.2, -.5);
        computer.computeAll(c);
        assertTrue(P.evaluateAt(c).equals(computer.f(), 1e-9));
        assertTrue(P.derivate().evaluateAt(c).equals(computer.df(), 1e-9));
        computer.computeAll(.33);
        assertEquals(P.evaluateAt(.33), computer.f().getRe(), 1e-9);
        assertEquals(P.derivate().evaluateAt(.33), computer.df().getRe(), 1e-9);
    }

}
