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
package demetra.maths.linearfilters;

import demetra.data.DoubleSequence;
import demetra.maths.matrices.Matrix;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class LocalPolynomialFiltersTest {

    public LocalPolynomialFiltersTest() {
    }

    @Test
    public void testSomeMethod() {
        for (int len = 1; len < 500; ++len) {
            SymmetricFilter lpf = LocalPolynomialFilters.of(len, 3, LocalPolynomialFilters.hendersonWeights(len));
            SymmetricFilter lpf2 = LocalPolynomialFilters.ofDefault(len, 3, LocalPolynomialFilters.hendersonWeights(len));
            SymmetricFilter hf = HendersonFilters.ofLength(2 * len + 1);
            assertTrue(lpf.coefficientsAsPolynomial().equals(hf.coefficientsAsPolynomial(), 1e-9));
            assertTrue(lpf.coefficientsAsPolynomial().equals(lpf2.coefficientsAsPolynomial(), 1e-5));
        }
    }

    @Test
    public void testHigh() {
        SymmetricFilter lpf = LocalPolynomialFilters.ofDefault(25, 3, LocalPolynomialFilters.fn3(25));
        SymmetricFilter lpf2 = LocalPolynomialFilters.ofDefault(25, 3, LocalPolynomialFilters.fn2(25));
    }

    //@Test
    public void testAsymmetric() {
        int h = 11;
        for (int i = 0; i <= h; ++i) {
            FiniteFilter f = LocalPolynomialFilters.directAsymmetricFilter(h, i, 3, LocalPolynomialFilters.hendersonWeights(h));
            System.out.println(DoubleSequence.ofInternal(f.weightsToArray()));
        }
        SymmetricFilter lp = LocalPolynomialFilters.ofDefault(h, 3, LocalPolynomialFilters.hendersonWeights(h));
        System.out.println(DoubleSequence.ofInternal(lp.weightsToArray()));
    }

    //@Test
    public void testAsymmetric2() {
        int h = 11;
        for (int i = 0; i <= h; ++i) {
            FiniteFilter f = LocalPolynomialFilters.directAsymmetricFilter(h, i, 1, LocalPolynomialFilters.fn3(h));
            System.out.println(DoubleSequence.ofInternal(f.weightsToArray()));
        }
        SymmetricFilter lp = LocalPolynomialFilters.ofDefault(h, 3, LocalPolynomialFilters.fn2(h));
        System.out.println(DoubleSequence.ofInternal(lp.weightsToArray()));
    }

    @Test
    public void testZ() {
        Matrix z = LocalPolynomialFilters.z(-12, 2, 0, 3);
        assertTrue(z.sum() != 0);
    }

    //@Test
    public void testAsymmetric3() {
        int h = 11;
        SymmetricFilter lp = LocalPolynomialFilters.ofDefault(h, 3, LocalPolynomialFilters.hendersonWeights(h));
        for (int i = 0; i < h; ++i) {
            FiniteFilter f = LocalPolynomialFilters.asymmetricFilter(lp, i, 0, new double[]{.4}, null);
            System.out.println(DoubleSequence.ofInternal(f.weightsToArray()));
        }
    }

}
