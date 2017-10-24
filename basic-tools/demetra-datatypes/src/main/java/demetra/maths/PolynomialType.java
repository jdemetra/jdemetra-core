/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths;

import java.util.Formatter;
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface PolynomialType {

    public static final PolynomialType ONE = new LightPolynomial(new double[]{1});
    public static final PolynomialType ZERO = new LightPolynomial(new double[]{0});

    public static PolynomialType of(@Nonnull double[] coefficients) {
        return new LightPolynomial(coefficients.clone());
    }

    public static PolynomialType of(double c0, double... coefficients) {
        if (coefficients == null) {
            return new LightPolynomial(new double[]{c0});
        } else {
            double[] p = new double[coefficients.length + 1];
            p[0] = c0;
            System.arraycopy(coefficients, 0, p, 1, coefficients.length);
            return new LightPolynomial(p);
        }
    }

    int getDegree();

    double get(int i);
    
    double[] toArray();

    public static boolean equals(PolynomialType p1, PolynomialType p2) {
        int d = p1.getDegree();
        if (d != p2.getDegree()) {
            return false;
        }
        for (int i = 0; i <= d; ++i) {
            if (p1.get(i) != p2.get(i)) {
                return false;
            }
        }
        return true;
    }

    public static String toString(PolynomialType p, final String fmt, final char var) {
        StringBuilder sb = new StringBuilder(512);
        boolean sign = false;
        int n = p.getDegree();
        if (n == 0) {
            sb.append(new Formatter().format(fmt, p.get(0)));
        } else {
            for (int i = 0; i <= n; ++i) {
                double v = Math.abs(p.get(i));
                if (v >= 1e-6) {
                    if (v > p.get(i)) {
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
}
