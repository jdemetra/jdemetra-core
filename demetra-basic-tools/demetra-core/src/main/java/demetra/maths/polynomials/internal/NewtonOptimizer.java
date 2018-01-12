/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.maths.polynomials.internal;

import demetra.maths.Complex;

/**
 * This class search the actual root of a polynomial, starting from an
 * approximate solution
 *
 * @author Jean Palate
 */
class NewtonOptimizer {

    /**
     * max. number ofInternal iterations
     */
    private final static int NITERMAX = 20;
    /**
     * calculate new dx, when change of x0 is smaller than FACTOR*(old change of
     * x0)
     */
    private final static double NFACTOR = 5;
    /**
     * initialisation ofInternal |P(xmin)|
     */
    private final static double NFVALUE = 1e36;
    /**
     * max. number ofInternal iterations with no better value
     */
    private final static int NNOISEMAX = 5;
    /**
     * smallest such that 1.0+DBL_EPSILON != 1.0
     */
    private final static double DBL_EPSILON = 2.2204460492503131e-016;
    /**
     * if the imaginary part ofInternal the root is smaller than BOUND5 => real
     * root
     */
    private final static double NBOUND = Math.sqrt(DBL_EPSILON);

    private final static double DFBOUND = 1e-6;

    final PolynomialComputer fn;
    Complex r;
    double err;
    int multiplicity;
    final boolean mroots;

    NewtonOptimizer(double[] p, int idx, boolean mroots) {
        fn = new PolynomialComputer(p, idx);
        this.mroots=mroots;
    }

    Complex root(final Complex ns) {

        double fabsmin = NFVALUE, eps = DBL_EPSILON;
        int noise = 0;

        Complex xcur = ns;
        Complex xmin = xcur;
        Complex dx = Complex.ONE;
        err = dx.abs();

        for (int i = 0; i < NITERMAX; i++) {
            /* main loop */
            fn.computeAll(xcur);
            final Complex f = fn.f();
            final Complex df = fn.df();

            if (f.abs() < fabsmin) {
                xmin = xcur;
                fabsmin = f.abs();
                noise = 0;
            }
            double axmin = xmin.abs();
            if (mroots && axmin != 0) {
                if (df.abs() / axmin < DFBOUND && fn.p.length > 1) {
                    // should be a multiple root
                    // we compute the derivative
                    int d = fn.p.length-fn.i0- 1;
                    double[] np = new double[d];
                    for (int k=1, l = fn.i0+1; k <= d; ++k, ++l) {
                        np[k - 1] = k * fn.p[l];
                    }
                    // recursive call
                    NewtonOptimizer doptimizer = new NewtonOptimizer(np, 0, true);
                    Complex rroot = doptimizer.root(xcur);
                    if (rroot != null) {
                        multiplicity = 1+doptimizer.multiplicity;
                        return rroot;
                    }
                }
            }

            if (df.abs() > eps) {
                /* calculate new dx */
                final Complex dxh = f.div(df);
                if (dxh.abs() < err * NFACTOR) {
                    dx = dxh;
                    err = dx.abs();
                }
            }

            if (axmin != 0) {
                if ((err / axmin < eps) || (noise == NNOISEMAX)) {
                    if (Math.abs(xmin.getIm()) < NBOUND) {
                        xmin = Complex.cart(xmin.getRe(), 0);
                    }
                    err /= axmin;
                    multiplicity = 1;
                    return xmin;
                }
            }
            xcur = xcur.minus(dx);
            noise++;
        }

        if (Math.abs(xmin.getIm()) < NBOUND) {
            xmin = Complex.cart(xmin.getRe(), 0);
        }
        double axmin2 = xmin.abs();
        if (axmin2 != 0) {
            err /= axmin2;
        }
        multiplicity = 1;
        return xmin;
    }

}
