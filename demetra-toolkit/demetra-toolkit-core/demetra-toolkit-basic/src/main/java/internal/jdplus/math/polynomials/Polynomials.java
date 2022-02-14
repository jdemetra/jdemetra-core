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
package internal.jdplus.math.polynomials;

import java.util.Arrays;
import java.util.function.IntToDoubleFunction;
import jdplus.math.polynomials.Polynomial;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class Polynomials {

    public double[] divide(final double[] num, final double[] denom) {
        int n = num.length - 1, nv = denom.length - 1;
        while (n >= 0) {
            if (Math.abs(num[n]) > Coefficients.EPSILON) {
                break;
            } else {
                --n;
            }
        }
        if (n < 0) {
            return Coefficients.zero();
        }
        while (nv >= 0) {
            if (Math.abs(denom[nv]) > Coefficients.EPSILON) {
                break;
            } else {
                --nv;
            }
        }
        if (nv < 0) {
            if (num[n] > 0) {
                return Coefficients.positiveInfinity();
            } else if (num[n] < 0) {
                return Coefficients.negativeInfinity();
            } else {
                return Coefficients.nan();
            }
        }

        if (nv > n) {
            return Coefficients.zero();
        }
        double[] r = num.clone();
        double[] q = new double[n + 1];

        for (int k = n - nv; k >= 0; --k) {
            q[k] = r[nv + k] / denom[nv];
            for (int j = nv + k - 1; j >= k; j--) {
                r[j] -= q[k] * denom[j - k];
            }
        }
        return Arrays.copyOf(q, n - nv + 1);
    }

    /**
     * Evaluates a polynomial defined by given coefficients at a given point.
     * The coefficients are stored in normal order (the first coefficient
     * corresponds to the constant and the last one to the highest power)
     *
     * @param c The coefficients. Should contain at least one element (not
     * checked)
     * @param x The evaluation point;
     * @return the value ofFunction p(x)
     */
    public double evaluate(final double[] c, final double x) {
        int p = c.length - 1;
        double y = c[p--];
        for (; p >= 0; --p) {
            y = c[p] + (y * x);
        }
        return y;
    }

    /**
     * Evaluates a polynomial with coefficients defined by a give function at a
     * given point.
     *
     * @param degree The getDegree ofFunction the polynomial
     * @param fn The function defining the coefficients. fn(i) is the
     * coefficient corresponding to the power i
     * @param x The evaluation point;
     * @return the value ofFunction p(x)
     */
    public double evaluate(final int degree, IntToDoubleFunction fn, final double x) {
        int p = degree;
        double y = fn.applyAsDouble(p--);
        for (; p >= 0; --p) {
            y = fn.applyAsDouble(p) + (y * x);
        }
        return y;
    }

    public double[] plus(double[] l, double[] r) {
        // swap l and r if l.Degree < r.Degree
        if (l.length < r.length) {
            return plus(r, l);
        }
        double[] result = l.clone();
        for (int i = 0; i < r.length; ++i) {
            result[i] += r[i];
        }
        return result;
    }

    public double[] minus(double[] l, double[] r) {
        // swap l and r if l.Degree < r.Degree
        if (l.length >= r.length) {
            double[] result = l.clone();
            for (int i = 0; i < r.length; ++i) {
                result[i] -= r[i];
            }
            return result;
        } else {
            double[] result = new double[r.length];
            for (int i = 0; i < l.length; i++) {
                result[i] = l[i] - r[i];
            }
            for (int i = l.length; i < r.length; ++i) {
                result[i] = -r[i];
            }
            return result;
        }
    }

    public double[] times(double[] l, double[] r) {
        int d = l.length + r.length - 1;
        double[] result = new double[d];
        for (int u = 0; u < l.length; ++u) {
            if (l[u] != 0) {
                for (int v = 0; v < r.length; ++v) {
                    if (r[v] != 0) {
                        result[u + v] += l[u] * r[v];
                    }
                }
            }
        }
        return result;
    }
}
