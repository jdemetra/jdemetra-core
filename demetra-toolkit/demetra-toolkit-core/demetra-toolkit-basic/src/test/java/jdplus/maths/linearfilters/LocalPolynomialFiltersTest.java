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
package jdplus.maths.linearfilters;

import jdplus.maths.linearfilters.LocalPolynomialFilters;
import jdplus.maths.linearfilters.HendersonFilters;
import jdplus.maths.linearfilters.SymmetricFilter;
import jdplus.maths.linearfilters.FiniteFilter;
import jdplus.data.analysis.DiscreteKernel;
import org.junit.Test;
import static org.junit.Assert.*;
import demetra.data.DoubleSeq;
import jdplus.maths.matrices.FastMatrix;

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
            SymmetricFilter lpf = LocalPolynomialFilters.of(len, 3, DiscreteKernel.henderson(len));
            SymmetricFilter hf = HendersonFilters.ofLength(2 * len + 1);
            assertTrue(lpf.coefficientsAsPolynomial().equals(hf.coefficientsAsPolynomial(), 1e-9));
        }
    }

    @Test
    public void testHigh() {
        SymmetricFilter lpf = LocalPolynomialFilters.ofDefault(25, 3, DiscreteKernel.triweight(25));
        SymmetricFilter lpf2 = LocalPolynomialFilters.ofDefault(25, 3, DiscreteKernel.biweight(25));
    }

    @Test
    public void testAsymmetric() {
        int h = 11;
        for (int i = 0; i <= h; ++i) {
            FiniteFilter f = LocalPolynomialFilters.directAsymmetricFilter(h, i, 3, DiscreteKernel.henderson(h));
            assertEquals(DoubleSeq.of(f.weightsToArray()).sum(), 1, 1e-9);
//            System.out.println(DoubleSequence.ofInternal(f.weightsToArray()));
        }
//        SymmetricFilter lp = LocalPolynomialFilters.ofDefault(h, 3, DiscreteKernels.henderson(h));
//        System.out.println(DoubleSequence.ofInternal(lp.weightsToArray()));
    }

    @Test
    public void testAsymmetric2() {
        int h = 11;
        for (int i = 0; i <= h; ++i) {
            FiniteFilter f = LocalPolynomialFilters.directAsymmetricFilter(h, i, 1, DiscreteKernel.tricube(h));
            assertEquals(DoubleSeq.of(f.weightsToArray()).sum(), 1, 1e-9);
//           System.out.println(DoubleSequence.ofInternal(f.weightsToArray()));
        }
//        SymmetricFilter lp = LocalPolynomialFilters.ofDefault(h, 3, DiscreteKernels.biweight(h));
//        System.out.println(DoubleSequence.ofInternal(lp.weightsToArray()));
    }

    @Test
    public void testZ() {
        FastMatrix z = LocalPolynomialFilters.z(-12, 2, 0, 3);
        assertTrue(z.sum() != 0);
    }

    @Test
    public void testAsymmetric3() {
        int h = 11;
        SymmetricFilter lp = LocalPolynomialFilters.ofDefault(h, 3, DiscreteKernel.henderson(h));
        for (int i = 0; i <= h; ++i) {
            FiniteFilter f = LocalPolynomialFilters.asymmetricFilter(lp, i, 0, new double[]{.4}, DiscreteKernel.triweight(h));
            assertEquals(DoubleSeq.of(f.weightsToArray()).sum(), 1, 1e-9);
//            System.out.println(DoubleSequence.ofInternal(f.weightsToArray()));
        }
    }

}
