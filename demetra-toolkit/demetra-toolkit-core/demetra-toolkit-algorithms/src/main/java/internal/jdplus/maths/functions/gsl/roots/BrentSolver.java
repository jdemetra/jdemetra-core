/*
 * Copyright 2019 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package internal.jdplus.maths.functions.gsl.roots;

import java.util.function.DoubleUnaryOperator;

/**
 * The Brent-Dekker method (referred to here as Brent’s method) combines an
 * interpolation strategy with the bisection algorithm. This produces a fast
 * algorithm which is still robust.
 *
 * On each iteration Brent’s method approximates the function using an
 * interpolating curve. On the first iteration this is a linear interpolation of
 * the two endpoints. For subsequent iterations the algorithm uses an inverse
 * quadratic fit to the last three points, for higher accuracy. The intercept of
 * the interpolating curve with the x-axis is taken as a guess for the root. If
 * it lies within the bounds of the current interval then the interpolating
 * point is accepted, and used to generate a smaller interval. If the
 * interpolating point is not accepted then the algorithm falls back to an
 * ordinary bisection step.
 *
 * The best estimate of the root is taken from the most recent interpolation or
 * bisection.
 *
 * @see
 * https://www.gnu.org/software/gsl/doc/html/roots.html#c.gsl_root_fsolver_brent
 * @author Mats Maggi
 */
public class BrentSolver extends FSolver {

    private double a, b, c, d, e;
    private double fa, fb, fc;

    public BrentSolver(DoubleUnaryOperator fn, double xLower, double xUpper) {
        this.function = fn;
        this.lower = xLower;
        this.upper = xUpper;

        double fLower, fUpper;

        this.root = 0.5 * (xLower + xUpper);

        fLower = fn.applyAsDouble(xLower);
        fUpper = fn.applyAsDouble(xUpper);

        a = xLower;
        fa = fLower;

        b = xUpper;
        fb = fUpper;

        c = xUpper;
        fc = fUpper;

        d = xUpper - xLower;
        e = xUpper - xLower;

        if ((fLower < 0.0 && fUpper < 0.0) || (fLower > 0.0 && fUpper > 0.0)) {
            throw new GslRootException("Endpoints do not straddle y=0");
        }
    }

    @Override
    public void iterate() {
        double tol, m;

        boolean ac_equal = false;

        if ((fb < 0 && fc < 0) || (fb > 0 && fc > 0)) {
            ac_equal = true;
            c = a;
            fc = fa;
            d = b - a;
            e = b - a;
        }

        if (Math.abs(fc) < Math.abs(fb)) {
            ac_equal = true;
            a = b;
            b = c;
            c = a;
            fa = fb;
            fb = fc;
            fc = fa;
        }

        tol = 0.5 * Math.ulp(1.0) * Math.abs(b);
        m = 0.5 * (c - b);

        if (fb == 0.0) {
            root = b;
            lower = b;
            upper = b;
            return;
        }

        if (Math.abs(m) <= tol) {
            root = b;
            if (b < c) {
                lower = b;
                upper = c;
            } else {
                lower = c;
                upper = b;
            }
            return;
        }

        if (Math.abs(e) < tol || Math.abs(fa) <= Math.abs(fb)) {
            // Use bisection
            d = m;
            e = m;
        } else {
            double p, q, r; // Use inverse cubic interpolation
            double s = fb / fa;

            if (ac_equal) {
                p = 2 * m * s;
                q = 1 - s;
            } else {
                q = fa / fc;
                r = fb / fc;
                p = s * (2 * m * q * (q - r) - (b - a) * (r - 1));
                q = (q - 1) * (r - 1) * (s - 1);
            }

            if (p > 0) {
                q = -q;
            } else {
                p = -p;
            }

            if (2 * p < Math.min(3 * m * q - Math.abs(tol * q), Math.abs(e * q))) {
                e = d;
                d = p / q;
            } else {
                // Interpolation failed, fall back to bisection
                d = m;
                e = m;
            }
        }

        a = b;
        fa = fb;

        if (Math.abs(d) > tol) {
            b += d;
        } else {
            b += (m > 0 ? +tol : -tol);
        }

        fb = function.applyAsDouble(b);

        // Update the best estimate of the root and bounds on each iteration
        root = b;
        if ((fb < 0 && fc < 0) || (fb > 0 && fc > 0)) {
            c = a;
        }

        if (b < c) {
            lower = b;
            upper = c;
        } else {
            lower = c;
            upper = b;
        }
    }
}
