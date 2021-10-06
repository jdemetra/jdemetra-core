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
package jdplus.math.linearfilters;

import jdplus.math.polynomials.Polynomial;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class SymmetricFiltersFactory {

    /**
     * Creates a m x n symmetric filter. The sum of m and n should be even. The
     * length of the final filter is (m+n-1).
     *
     * @param m The length of the first filter.
     * @param n The length of the second filter
     *
     * @return The corresponding symmetric filter
     */
    public SymmetricFilter makeSymmetricFilter(final int m, final int n) {
        if ((m + n) % 2 == 1) {
            throw new IllegalArgumentException();
        }
        Polynomial M = simpleFilter(m);
        Polynomial N = simpleFilter(n);
        return SymmetricFilter.of(M.times(N).coefficients());

    }

    private Polynomial simpleFilter(int len) {
        double[] c = new double[len];
        double w = 1.0 / len;
        for (int i = 0; i < len; i++) {
            c[i] = w;
        }
        return Polynomial.of(c);
    }

    public SymmetricFilter makeSymmetricFilter(final int length) {
        int ilen = (length + 1) / 2;
        if (length % 2 == 1) {
            double[] c = new double[ilen];
            double w = 1.0 / length;
            for (int i = 0; i < c.length; i++) {
                c[i] = w;
            }
            return SymmetricFilter.ofInternal(c);
        } else {
            double[] c = new double[ilen + 1];
            double w = 1.0 / length;
            for (int i = 0; i < ilen; i++) {
                c[i] = w;
            }
            c[ilen] = w / 2;
            return SymmetricFilter.ofInternal(c);
        }
    }
}
