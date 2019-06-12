/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths;

import demetra.design.Development;
import java.util.Formatter;
import javax.annotation.Nonnull;
import demetra.data.DoubleSeq;
import java.util.Arrays;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Development(status = Development.Status.Release)
public class RealPolynomial {

    public static final RealPolynomial ONE = new RealPolynomial(new double[]{1});
    public static final RealPolynomial ZERO = new RealPolynomial(new double[]{0});
    
    public static RealPolynomial of(@Nonnull double[] coefficients) {
        int n = coefficients.length;
        while (n > 0 && coefficients[n - 1] == 0) {
            --n;
        }
        if (n == 0) {
            return ZERO;
        } else if (n == 1 && coefficients[0] == 1) {
            return ONE;
        } else {
            return new RealPolynomial(Arrays.copyOf(coefficients, n));
        }
    }

    public static RealPolynomial of(double c0, double... coefficients) {
        if (coefficients == null) {
            if (c0 == 0) {
                return ZERO;
            } else if (c0 == 1) {
                return ONE;
            } else {
                return new RealPolynomial(new double[]{c0});
            }
        } else {
            double[] p = new double[coefficients.length + 1];
            p[0] = c0;
            System.arraycopy(coefficients, 0, p, 1, coefficients.length);
            return of(p);
        }
    }

    public static RealPolynomial ofInternal(@Nonnull double[] coefficients) {
        return new RealPolynomial(coefficients);
    }

    private static RealPolynomial ofCoefficients(@Nonnull double[] coefficients) {
        int n = coefficients.length;
        while (n > 0 && coefficients[n - 1] == 0) {
            --n;
        }
        if (n == 0) {
            return ZERO;
        } else if (n == 1 && coefficients[0] == 1) {
            return ONE;
        } else if (n == coefficients.length) {
            return new RealPolynomial(coefficients);
        } else {
            return new RealPolynomial(Arrays.copyOf(coefficients, n));
        }
    }

    private final double[] c;

    RealPolynomial(double[] c) {
        this.c = c;
    }

    /**
     * Gets the degree of the polynomial
     *
     * @return
     */
    public int degree() {
        return c.length - 1;
    }

    /**
     * Gets the coefficient corresponding to the given power
     *
     * @param i Position of the coefficient (corresponding to x^i). Should be in
     * [0, degree()]
     * @return
     */
    public double get(int i) {
        return c[i];
    }

    /**
     * Gets a copy of the coefficients (increasing power)
     *
     * @return
     */
    public double[] toArray() {
        return c.clone();
    }

    public void copyTo(double[] buffer, int startpos) {
        System.arraycopy(c, 0, buffer, startpos, c.length);
    }

    /**
     * Gets all the coefficients (increasing power)
     *
     * @return
     */
    public DoubleSeq coefficients() {
        return DoubleSeq.of(c);
    }

    public RealPolynomial times(RealPolynomial r) {
        if (this == ZERO || r == ZERO) {
            return ZERO;
        }
        if (this == ONE) {
            return r;
        }
        if (r == ONE) {
            return this;
        }
        int d = degree() + r.degree();
        double[] result = new double[d + 1];
        for (int u = 0; u < c.length; ++u) {
            if (c[u] != 0) {
                for (int v = 0; v < r.c.length; ++v) {
                    if (r.c[v] != 0) {
                        result[u + v] += c[u] * r.c[v];
                    }
                }
            }
        }
        return ofCoefficients(result);
    }

    public RealPolynomial negate() {
        if (this == ZERO) {
            return ZERO;
        }
        double[] result = new double[c.length];
        for (int i = 0; i < result.length; ++i) {
            result[i] = -c[i];
        }
        return ofCoefficients(result);
    }

    public RealPolynomial plus(RealPolynomial r) {
        if (this == ZERO) {
            return r;
        }
        if (r == ZERO) {
            return this;
        }

        if (c.length < r.c.length) {
            return r.plus(this);
        }
        double[] result = c.clone();
        for (int i = 0; i < r.c.length; ++i) {
            result[i] += r.get(i);
        }
        return ofCoefficients(result);
    }

    public RealPolynomial minus(RealPolynomial p) {
        return plus(p.negate());
    }

    public RealPolynomial plus(double d) {
        if (d == 0) {
            return this;
        }
        double[] result = c.clone();
        for (int i = 0; i < c.length; ++i) {
            result[i] += d;
        }
        return ofCoefficients(result);
    }

    public RealPolynomial times(double d) {
        if (d == 0) {
            return ZERO;
        }
        if (d == 1) {
            return this;
        }
        double[] result = c.clone();
        for (int i = 0; i < c.length; ++i) {
            result[i] *= d;
        }
        return ofCoefficients(result);
    }

    public String toString(final String fmt, final char var) {
        StringBuilder sb = new StringBuilder(512);
        boolean sign = false;
        int n = degree();
        if (n == 0) {
            sb.append(new Formatter().format(fmt, c[0]));
        } else {
            for (int i = 0; i <= n; ++i) {
                double v = Math.abs(c[i]);
                if (v >= 1e-6) {
                    if (v > c[i]) {
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

    @Override
    public String toString() {
        return toString("%6g", 'X');
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (other instanceof RealPolynomial) {
            RealPolynomial p = (RealPolynomial) other;
            return Arrays.equals(c, p.c);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + Arrays.hashCode(this.c);
        return hash;
    }

}
