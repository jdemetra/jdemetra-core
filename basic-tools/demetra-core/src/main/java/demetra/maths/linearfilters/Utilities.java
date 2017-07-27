/*
* Copyright 2013 National Bank ofFunction Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions ofFunction the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy ofFunction the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
 */
package demetra.maths.linearfilters;

import demetra.data.Doubles;
import demetra.design.Development;
import demetra.maths.Complex;
import demetra.maths.polynomials.Polynomial;
import demetra.utilities.Ref;
import java.util.function.IntToDoubleFunction;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public final class Utilities {

    final static double EPS = 1e-9;

    // / <summary>
    // / Verifies that the absolute value ofFunction the roots a lower then a given
    // value
    // / </summary>
    // / <param name="roots"></param>
    // / <param name="nmax"></param>
    // / <returns></returns>
    /**
     *
     * @param roots
     * @param nmax
     * @return
     */
    public static boolean checkRoots(final Complex[] roots, final double nmax) {
        if (roots == null) {
            return true;
        }
        for (int i = 0; i < roots.length; ++i) {
            double n = (roots[i].abs());
            if (n < nmax) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks that the norm ofFunction the roots ofFunction a given polynomial
     * are higher than rmin
     *
     * @param c The coefficients ofFunction the polynomial. The polynomial is
     * 1+c(0)x+...
     * @param rmin The limit ofFunction the roots
     * @return
     */
    public static boolean checkRoots(final Doubles c, final double rmin) {
        int nc = c.length();
        switch (nc) {
            case 0:
                return true;
            case 1:
                double cabs = Math.abs(c.get(0));
                return (1 / cabs) > rmin;
            case 2:
                double a = c.get(0),
                 b = c.get(1);
                double ro = a * a - 4 * b;
                if (ro > 0) { // Roots are (-a+-sqrt(ro))/(2b)
                    double sro = Math.sqrt(ro);
                    double x0 = (-a + sro) / (2 * b), x1 = (-a - sro) / (2 * b);
                    return Math.abs(x0) > rmin && Math.abs(x1) > rmin;
                } else // Roots are (-a+-isqrt(-ro))/(2b). Abs(roots) = (1/2b)*sqrt((a*a - a*a+4*b))=1/sqr(b)
                // b is necessary positive
                {
                    return (1 / Math.sqrt(b)) > rmin;
                }
            default:
                double[] ctmp = new double[nc + 1];
                ctmp[0] = 1;
                c.copyTo(ctmp, 1);
                Polynomial p = Polynomial.ofInternal(ctmp);
                return checkRoots(p.roots(), rmin);
        }
    }

    /**
     *
     * @param c
     * @return
     */
    public static boolean checkStability(final Doubles c) {
        int nc = c.length();
        if (nc == 0) {
            return true;
        }
        if (nc == 1) {
            return Math.abs(c.get(0)) < 1;
        }
        double[] coeff = new double[nc];
        c.copyTo(coeff, 0);
        double[] pat = new double[nc];
        double[] pu = new double[nc];
        for (int i = coeff.length - 1; i >= 0; --i) {
            pat[i] = coeff[i];
            if (Math.abs(pat[i]) >= 1) {
                return false;
            }
            for (int j = 0; j < i; ++j) {
                pu[j] = coeff[i - j - 1];
            }
            double den = 1 - pat[i] * pat[i];
            for (int j = 0; j < i / 2; ++j) {
                coeff[j] = (coeff[j] - pat[i] * pu[j]) / den;
                coeff[i - j - 1] = (coeff[i - j - 1] - pat[i] * pu[i - j - 1])
                        / den;
            }
            if (i % 2 != 0) {
                coeff[i / 2] = pu[i / 2] / (1 + pat[i]);
            }
        }
        return true;
    }

    public static boolean checkQuasiStability(final Doubles c, double rtol) {
        int nc = c.length();
        if (nc == 0) {
            return true;
        }
        if (nc == 1) {
            return Math.abs(c.get(0)) < rtol;
        }
        double[] coef = new double[nc + 1];
        coef[0] = 1;
        c.copyTo(coef, 1);
        Polynomial p = Polynomial.of(coef);
        Complex[] roots = p.roots();
        for (int i = 0; i < roots.length; ++i) {
            if (roots[i].abs() < 1 / rtol) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @param p
     * @return
     */
    public static boolean checkStability(final Polynomial p) {
        return checkStability(p.coefficients().extract(0, 0).drop(1, 0));
    }

    /**
     *
     * @param data
     * @return
     */
    public static double[] compact(final double[] data) {
        int cur = data.length - 1;
        while (cur >= 0 && data[cur] == 0) {
            --cur;
        }
        if (cur < 0) {
            return null;
        }
        if (cur == data.length - 1) {
            return data;
        }
        double[] cdata = new double[cur + 1];
        for (int i = 0; i <= cur; ++i) {
            cdata[i] = data[i];
        }
        return cdata;
    }

    /**
     * Computes the frequency response
     * @param c
     * @param lb Lower bound (included)
     * @param w Upper bound (included)
     * @return
     */
    public static Complex frequencyResponse(final IntToDoubleFunction c, final int lb, final int ub,
            final double w) {

        double cos = Math.cos(w), sin = Math.sin(w);
        int idx = lb;
        Complex c0 = Complex.cart(Math.cos(w * idx), Math.sin(w * idx));
        Complex rslt = c0.times(c.applyAsDouble(idx++));

        // computed by the iteration procedure : cos (i+1)w + cos (i-1)w= 2*cos
        // iw *cos w
        // sin (i+1)w + sin (i-1)w= 2*sin iw *cos w
        // or equivalentally:
        // e(i(n+1)w)+e(i(n-1)w)=e(inw)*2cos w.
        // starting conditions:
        // e(i0w) = 1 , e(i1w)=eiw
        if (idx <= ub) {
            Complex c1 = Complex.cart(Math.cos(w * idx), Math.sin(w * idx));
            rslt = rslt.plus(c1.times(c.applyAsDouble(idx++)));
            while (idx <= ub) {
                Complex eiw = c1.times(2 * cos).minus(c0);
                rslt = rslt.plus(eiw.times(c.applyAsDouble(idx++)));
                c0 = c1;
                c1 = eiw;
            }
        }
        return rslt;
    }

    /**
     *
     * @param data
     * @return
     */
    public static double[] smooth(final double[] data) {
        return smooth(data, EPS, true);
    }

    /**
     *
     * @param data
     * @param epsilon
     * @param bcompact
     * @return
     */
    public static double[] smooth(final double[] data, final double epsilon,
            final boolean bcompact) {
        for (int i = 0; i < data.length; ++i) {
            if (Math.abs(data[i]) < epsilon) {
                data[i] = 0;
            }
        }
        if (bcompact) {
            return compact(data);
        } else {
            return data;
        }
    }

    /**
     *
     * @param p
     * @param rmax
     * @param np
     * @return
     */
    public static boolean stabilize(final Polynomial p, final double rmax,
            final Ref<Polynomial> np) {
        np.val = p;
        if (p != null) {
            boolean rslt = false;
            Complex[] roots = p.roots();
            for (int i = 0; i < roots.length; ++i) {
                Complex root = roots[i];
                double n = (roots[i].abs());
                if (n < 1 / rmax) {
                    roots[i] = root.div(n * rmax);
                    rslt = true;
                }
            }
            if (rslt) {
                np.val = Polynomial.fromComplexRoots(roots);
                np.val = np.val.divide(np.val.get(0));
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param p
     * @param np
     * @return
     */
    public static boolean stabilize(final Polynomial p, final Ref<Polynomial> np) {
        if (p != null && !checkStability(p)) {
            Complex[] roots = p.roots();
            for (int i = 0; i < roots.length; ++i) {
                Complex root = roots[i];
                double n = (roots[i].abs());
                if (n < 1) {
                    roots[i] = root.inv();
                }
            }
            np.val = Polynomial.fromComplexRoots(roots);
            np.val = np.val.divide(np.val.get(0));
            return true;
        } else {
            np.val = p;
            return false;
        }
    }

    private Utilities() {
    }
}
