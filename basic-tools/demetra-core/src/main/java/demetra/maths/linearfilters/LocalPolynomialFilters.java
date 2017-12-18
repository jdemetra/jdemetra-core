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

import java.util.function.IntToDoubleFunction;
import java.util.function.IntUnaryOperator;

/**
 * The local polynomial filter is defined as follows:
 * h is the number of lags (-> length of the filter is 2*h+1)
 * d is the order of the local polynomial
 * ki (local weight); we suppose that k(i) = k(-i) (symmetric filters; other filters could be considered)
 * 
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class LocalPolynomialFilters {
    
    /**
     *  
     * @param h the number of lags (-> length of the filter is 2*h+1)
     * @param d d is the order of the local polynomial
     * @param k weight of y(t+i); we suppose that k(i) = k(-i) (symmetric filters; other filters could be considered)
     * @return The corresponding filter 
     */
    public SymmetricFilter of(final int h, final int d, final IntToDoubleFunction k){
        return null;
    }

    public IntToDoubleFunction hendersonWeights(final int h){
        // TODO Optimze the computation...
        IntUnaryOperator fn = nonNormalizedHendersonWeights(h);
        int s=fn.applyAsInt(0);
        for (int i=1; i<=h; ++i)
            s+=2*fn.applyAsInt(i);
        final double d=s;
        return i->fn.applyAsInt(i)/d;
    }
    
    private IntUnaryOperator nonNormalizedHendersonWeights(final int h){
        return i->((h+1)*(h+1)-i*i)*((h+2)*(h+2)-i*i)*((h+3)*(h+3)-i*i);
    }
    
}
