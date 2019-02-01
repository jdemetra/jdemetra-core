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
package demetra.x11plus;

import demetra.design.Development;
import demetra.maths.linearfilters.FiniteFilter;
import demetra.maths.linearfilters.HendersonFilters;
import demetra.maths.linearfilters.IFiniteFilter;
import demetra.maths.polynomials.Polynomial;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory for the creation of the asymmetric Musgrave filters
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class MusgraveFilterFactory {

    @lombok.Value
    private static class Key {

        int length;
        double D;
    }

    private static final Map<Key, FiniteFilter[]> FILTERSTORE = new HashMap<>();
    private static final Polynomial X11_H1 = Polynomial.valueOf(-0.073, 0.294, 0.522, 0.257),
            X11_H0 = Polynomial.valueOf(-0.073, 0.403, 0.670);

    private static Polynomial computeCoefficients(final double[] h,
            final double D, final int M) {
        double[] c = new double[M];
        int N = h.length;
        for (int i = 0; i < M; i++) {
            c[i] = h[i];
            double p1 = 0.0, p2 = 0.0;
            for (int j = M; j < N; j++) {
                p1 += h[j];
                p2 += h[j] * ((j + 1) - (M + 1) / 2.0);
            }
            p1 /= M;
            p2 *= (((i + 1) - (M + 1) / 2.0) * D / (1 + (M * (M - 1) * (M + 1)
                    * D * (1.0 / 12.0))));
            c[i] += (p1 + p2);
        }
        return Polynomial.of(c);
    }

    /**
     * Returns the ratio for Musgrave filters corresponding to Henderson
     * filters, based on the length of the filter and on the annual frequency of
     * the series.
     *
     * @param length Length of the filter
     * @param frequency Annual frequency of the series
     * @return Ratio for Musgrave filters
     */
    public static double findR(final int length, final int frequency) {
        if (frequency == 4) {
            return (length <= 5) ? 0.001 : 4.5;
        } else if (length <= 9) {
            return 1;
        } else if (length <= 13) {
            return 3.5;
        } else {
            return 4.5;
        }
    }

    /**
     *
     * @param filter
     * @param ic
     * @return
     */
    public static FiniteFilter[] makeFilters(IFiniteFilter filter, double ic) {
        double D = 4.0 / (Math.PI * ic * ic);

        int len = filter.getUpperBound();
        if (len <= 0) {
            return null;
        }
        FiniteFilter[] filters = new FiniteFilter[len];
        double[] h = filter.weightsToArray();
        for (int i = 1; i <= len; ++i) {
            Polynomial w = computeCoefficients(h, D, h.length - i);
            filters[i - 1] = new FiniteFilter(w, filter.getLowerBound());
        }
        return filters;
    }

    /**
     * Gets the Musgrave filters corresponding to the Henderson filters
     *
     * @param len Length of the Henderson filter.
     * @param freq Annual frequency of the series
     * @return
     */
    public static synchronized IFiniteFilter[] makeFiltersForHenderson(int len,
            int freq) {
        double r = findR(len, freq);
        double D = 4.0 / (Math.PI * r * r);
        Key key = new Key(len, D);
        FiniteFilter[] filters = FILTERSTORE.get(key);
        if (filters == null) {
            filters = new FiniteFilter[len / 2];
            double[] h = HendersonFilters.ofLength(len).weightsToArray();
            int l2 = len / 2;
            for (int i = 1; i <= l2; ++i) {
                Polynomial w = computeCoefficients(h, D, len - i);
                filters[i - 1] = new FiniteFilter(w, -l2);
            }
            FILTERSTORE.put(key, filters);
        }
        return filters.clone();
    }

    /**
     *
     * @return
     */
    public static FiniteFilter[] makeX11Filters() {
        return new FiniteFilter[]{new FiniteFilter(X11_H1, -2),
            new FiniteFilter(X11_H0, -2)};
    }
}
