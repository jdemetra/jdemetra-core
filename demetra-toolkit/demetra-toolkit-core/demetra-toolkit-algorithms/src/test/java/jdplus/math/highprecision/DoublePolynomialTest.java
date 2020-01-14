/*
 * Copyright 2019 National Bank of Belgium
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
package jdplus.math.highprecision;

import demetra.math.Complex;
import java.util.Random;
import jdplus.data.DataBlock;
import jdplus.math.polynomials.Polynomial;
import jdplus.math.polynomials.UnitRoots;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class DoublePolynomialTest {

    public DoublePolynomialTest() {
    }

    @Test
    public void testEvaluation() {
        DataBlock P = DataBlock.make(300);
        Random rnd = new Random(0);
        P.set(rnd::nextDouble);

        Polynomial sp = Polynomial.of(P.toArray());
        double z = sp.evaluateAt(0.999901);
//        System.out.println(z);

        DoublePolynomial dp = DoublePolynomial.of(P);
        DoubleDouble dz = dp.evaluatAt(0.999901);
//        System.out.println(dz);
//        System.out.println(z - dz.asDouble());
        assertEquals(z, dz.asDouble(), 1e-9);
    }

    @Test
    public void testDivision() {
        DataBlock N = DataBlock.make(30);
        N.set(i -> (1 + i));
        DataBlock D = DataBlock.make(15);
        D.set(i -> (1 + i * i));

        Polynomial sn = Polynomial.of(N.toArray());
        Polynomial sd = Polynomial.of(D.toArray());

        Polynomial.Division sq = Polynomial.divide(sn, sd);
//        System.out.println(sq.getQuotient());
//        System.out.println(sq.getRemainder());

        DoublePolynomial dn = DoublePolynomial.of(N);
        DoublePolynomial dd = DoublePolynomial.of(D);

        DoublePolynomial.Division dq = DoublePolynomial.divide(dn, dd);

        int nq = dq.getQuotient().degree();
        for (int i = 0; i <= nq; ++i) {
            assertEquals(sq.getQuotient().get(i), dq.getQuotient().get(i).asDouble(), 1e-9);
//            System.out.print(sq.getQuotient().get(i));
//            System.out.print('\t');
//            System.out.println(dq.getQuotient().get(i));
        }
        int nr = sq.getRemainder().degree();
        for (int i = 0; i <= nr; ++i) {
            assertEquals(sq.getRemainder().get(i), dq.getRemainder().get(i).asDouble(), 1e-9);

//            System.out.print(sq.getRemainder().get(i));
//            System.out.print('\t');
//            System.out.println(dq.getRemainder().get(i));
        }
    }
    
    @Test
    public void testProduct() {
        Complex[] roots = UnitRoots.D(100).roots();
        Polynomial p=Polynomial.fromComplexRoots(roots);
//        System.out.println(p.coefficients());
        DoublePolynomial dp=DoublePolynomial.of(roots, 1);
//        System.out.println(dp.toPolynomial().coefficients());
        DoublePolynomial dp2=DoublePolynomial.of2(roots, 1);
 //       System.out.println(dp2.toPolynomial().coefficients());
    }
    
    @Test
    public void testDerivate() {
        DataBlock N = DataBlock.make(30);
        N.set(i -> (1 + i));
        Polynomial p=Polynomial.of(N.toArray()).derivate();
        DoublePolynomial dp=DoublePolynomial.of(N).derivate();
        double z = p.evaluateAt(0.999901);
        DoubleDouble dz = dp.evaluatAt(0.999901);
        assertEquals(z, dz.asDouble(), 1e-9);
     }
    
}
