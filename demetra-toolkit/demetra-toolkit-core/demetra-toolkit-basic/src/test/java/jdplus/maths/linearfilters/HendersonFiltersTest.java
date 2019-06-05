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

import jdplus.maths.linearfilters.HendersonFilters;
import jdplus.maths.linearfilters.SymmetricFilter;
import java.util.function.DoubleUnaryOperator;
import jdplus.data.DataBlock;
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
    
}

@lombok.experimental.UtilityClass
class GenericHendersonFilters{
    /**
     * Criterion
     * min sum(D^q(w(k))^2 under sum(w(k))=1, sum(k*w(k))=0
     * @param nlags
     * @param nleads
     * @return 
     */
    FiniteFilter make(int nlags, int nleads){
        int n=nlags+nleads+1;
        CanonicalMatrix J=CanonicalMatrix.square(n+2);
        S(J.extract(0, n, 0, n));
        SubMatrix C = J.extract(n, 2, 0, n);
        C.row(0).set(1);
        C.row(1).set(k->k-nlags);
        J.extract(0, n, n, 2).copy(C.transpose());
        DataBlock z=DataBlock.make(n+2);
        z.set(n, 1);
        Householder hous=new Householder(false);
        return null;
    }
    
    private double[] W=new double[]{20, -15, 6, -1};
    
    private void S(FastMatrix s){
        s.diagonal().set(W[0]);
        for (int i=1; i<W.length; ++i){
            s.subDiagonal(i).set(W[i]);
            s.subDiagonal(-i).set(W[i]);
        }
    }
}
