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

import demetra.data.DoubleSeq;
import java.util.function.DoubleUnaryOperator;
import jdplus.data.DataBlock;
import jdplus.data.analysis.DiscreteKernel;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.FastMatrix;
import jdplus.maths.matrices.SubMatrix;
import jdplus.maths.matrices.decomposition.Householder;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class HendersonFiltersTest {

    public HendersonFiltersTest() {
    }

    @Test
    public void testWeights() {
        for (int i = 3; i < 99; i += 2) {
            SymmetricFilter f = HendersonFilters.ofLength(i);
            double[] w = f.weightsToArray();
            double s = 0;
            for (int j = 0; j < w.length; ++j) {
                s += w[j];
            }
            assertEquals(s, 1, 1e-9);
        }
    }

    @Test
    public void testGain() {
        DoubleUnaryOperator gain = HendersonFilters.ofLength(23).squaredGainFunction();
        for (int i = 0; i <= 100; ++i) {
            double g = gain.applyAsDouble(i * Math.PI / 100);
//            System.out.println(gain.apply(i * Math.PI / 100));
        }
//        System.out.println("");
//        System.out.println(DataBlock.ofInternal(HendersonFilters.instance.create(13).weightsToArray()));
    }

    public static void main(String[] arg) {
        int K = 1000, n = 13;
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            SymmetricFilter f2 = LocalPolynomialFilters.ofDefault(n / 2, 3, DiscreteKernel.henderson(n / 2));
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1-t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            SymmetricFilter f3 = LocalPolynomialFilters.ofDefault2(n / 2, 3, DiscreteKernel.henderson(n / 2));
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1-t0);
    }
}

