/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.data;

import java.util.Arrays;
import java.util.Formatter;
import jdplus.maths.polynomials.Polynomial;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class Coefficients {

    public final double EPSILON = 1e-9;

    public final double[] C_ZERO = {0}, C_ONE = {1},
            C_POSINF = {Double.POSITIVE_INFINITY},
            C_NEGINF = {Double.NEGATIVE_INFINITY},
            C_NAN = {Double.NaN};

    public double[] of(double[] c, double eps) {
        int nd = getUsedCoefficients(c, eps);
        if (nd == c.length) {
            return c;
        } else if (nd == 0) {
            return C_ZERO;
        } else {
            return Arrays.copyOf(c, nd);
        }
    }

    public double[] of(double[] c) {
        if (c.length == 1) {
            if (c[0] == 0) {
                return C_ZERO;
            }
            if (c[0] == 1) {
                return C_ONE;
            }
        }
        return c;
    }

    public double[] zero() {
        return C_ZERO;
    }

    public double[] one() {
        return C_ONE;
    }

    public double[] positiveInfinity() {
        return C_POSINF;
    }

    public double[] negativeInfinity() {
        return C_NEGINF;
    }

    public double[] nan() {
        return C_NAN;
    }

    public int getUsedCoefficients(double[] coefficients, double eps) {
        int n = coefficients.length;
        while ((n > 0) && (Math.abs(coefficients[n - 1]) <= eps)) {
            --n;
        }
        return n;
    }

    public double[] add(final double[] coefficients, final double d) {
        if (d == 0d) {
            return coefficients;
        }
        double[] result = coefficients.clone();
        result[0] += d;
        return of(result);
    }

    public double[] add(final double d, final double[] coefficients) {
        return add(coefficients, d);
    }

    public double[] chs(final double[] coefficients) {
        double[] result = new double[coefficients.length];
        for (int i = 0; i < result.length; ++i) {
            result[i] = -coefficients[i];
        }
        return of(result);
    }

    public double[] multiply(final double[] coefficients, final double d) {
        if (d == 0) {
            return C_ZERO;
        }
        if (d == 1) {
            return coefficients;
        }
        if (d == -1) {
            return chs(coefficients);
        }
        double[] result = new double[coefficients.length];
        for (int i = 0; i < result.length; ++i) {
            result[i] = coefficients[i] * d;
        }
        return of(result);
    }

    public double[] multiply(final double d, final double[] coefficients) {
        return multiply(coefficients, d);
    }

    public double[] add(final double[] l, final double[] r) {
        if (r == C_ZERO) {
            return l;
        }
        if (l == C_ZERO) {
            return r;
        }
        int n = Math.max(l.length, r.length);
        double[] result = new double[n];
        System.arraycopy(l, 0, result, 0, l.length);
        for (int i = 0; i < r.length; ++i) {
            result[i] += r[i];
        }
        return of(result);
    }

    public double[] subtract(final double[] l, final double[] r) {
        if (r == C_ZERO) {
            return l;
        }
        if (l == C_ZERO) {
            return r;
        }
        int n = Math.max(l.length, r.length);
        double[] result = new double[n];
        System.arraycopy(l, 0, result, 0, l.length);
        for (int i = 0; i < r.length; ++i) {
            result[i] -= r[i];
        }
        return of(result);
    }

    public double[] subtract(final double d, final double[] r) {
        double[] result = new double[r.length];
        result[0] = d - r[0];
        for (int i = 1; i < r.length; ++i) {
            result[i] -= r[i];
        }
        return of(result);
    }

    public double[] subtract(final double[] l, final double d) {
        return add(l, -d);
    }

    public double[] multiply(final double[] l, final double[] r) {
        if (r == C_ZERO || l == C_ZERO) {
            return C_ZERO;
        }
        if (r == C_ONE) {
            return l;
        }
        if (l == C_ONE) {
            return r;
        }
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
        return of(result);
    }

    public double[] divide(final double[] num, final double[] denom) {
        int n = num.length - 1, nv = denom.length - 1;
        while (n >= 0) {
            if (Math.abs(num[n]) > EPSILON) {
                break;
            } else {
                --n;
            }
        }
        if (n < 0) {
            return C_ZERO;
        }
        while (nv >= 0) {
            if (Math.abs(denom[nv]) > EPSILON) {
                break;
            } else {
                --nv;
            }
        }
        if (nv < 0) {
            if (num[n] > 0) {
                return C_POSINF;
            } else if (num[n] < 0) {
                return C_NEGINF;
            } else {
                return C_NAN;
            }
        }

        if (nv > n) {
            return C_ZERO;
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
     *
     * @param c
     * @param fmt
     * @param var
     * @param smooth
     * @return
     */
    public String toString(final double[] c, final String fmt, final char var,
            final boolean smooth) {
        // TODO: fmt
        StringBuilder sb = new StringBuilder(512);
        boolean sign = false;
        int n = c.length;
        if (n == 1) {
            sb.append(new Formatter().format(fmt, coef(c[0], smooth)));
        } else {
            for (int i = 0; i < n; ++i) {
                double v = coef(c[i], smooth);
                if (v != 0) {
                    if (v < 0) {
                        sb.append(" - ");
                    } else if (sign) {
                        sb.append(" + ");
                    }
                    if ((v != 1) || (i == 0)) {
                        sb.append(new Formatter().format(fmt, v).toString());
                    }
                    sign = true;
                    if (i > 0) {
                        sb.append(' ').append(var);
                    }
                    if (i > 1) {
                        sb.append('^').append(i);
                    }
                }
            }
        }

        return sb.toString();
    }

    private double coef(double c, boolean smooth) {
        if (smooth && Math.abs(c) < EPSILON) {
            return 0;
        } else {
            return c;
        }
    }

}
