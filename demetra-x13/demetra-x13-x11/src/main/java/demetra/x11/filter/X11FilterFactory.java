/*
 * Copyright 2013 National Bank of Belgium
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
package demetra.x11.filter;

import demetra.design.Development;
import demetra.maths.linearfilters.SymmetricFilter;
import demetra.maths.polynomials.Polynomial;

/**
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.experimental.UtilityClass
public final class X11FilterFactory {

    /**
     * The method returns the appropriate symmetric filter for the given Month
     * for Cyclical Dominance value. The MCD corresponds to the first month
     * where the ratio I/C is smaller than 1.0.
     *
     * @param data An array of double values, the ratios I/C
     *
     * @return The corresponding symmetricFilter. Is never null.
     */
    public SymmetricFilter makeMCDFilter(final double[] data) {
        int idx = -1;
        for (int i = data.length - 1; i >= 0; i--) {
            if (data[i] >= 1.0) {
                idx = i + 2; // add one for zero-based index; add another to
                // include value itself
                break;
            }
        }

        if (idx == -1) {
            return makeSymmetricFilter(0);
        }
        if (idx > 6) {
            return makeSymmetricFilter(2, 6);
        } else if (idx % 2 == 0) {
            return makeSymmetricFilter(2, idx);
        } else {
            return makeSymmetricFilter(idx / 2);
        }
    }

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
        Polynomial M = simpleFilter(m);
        Polynomial N = simpleFilter(n);
        return SymmetricFilter.of(M.times(N).coefficients());

    }

    public Polynomial simpleFilter(int len) {
        double[] c = new double[len];
        double w = 1.0 / len;
        for (int i = 0; i < len; i++) {
            c[i] = w;
        }
        return Polynomial.of(c);
    }

    /**
     * Creates a simple symmetric filter with identical weights. ( 1/length,...,
     * 1/length).
     *
     * @param length Length of the filter. Should be odd
     *
     * @return The corresponding symmetric filter
     */
    public SymmetricFilter makeSymmetricFilter(final int length) {
        int len = length;
        double flen = len % 2 == 0 ? 1 : 0;

        int ilen = (len + 1) / 2;

        double[] c = new double[ilen + 1];
        double w = 1.0 / len;
        for (int i = 0; i < c.length - 1; i++) {
            c[i] = w;
        }
        double we = (flen / 2) / len;
        c[ilen] = we;
        return SymmetricFilter.ofInternal(c);
    }
}
