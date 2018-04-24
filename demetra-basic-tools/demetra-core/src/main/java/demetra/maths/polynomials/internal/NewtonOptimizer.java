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

import demetra.data.DoubleSequence;
import demetra.maths.Complex;
import demetra.maths.polynomials.Polynomial;

/**
 * This class search the actual root of a polynomial, starting from an
 * approximate solution
 *
 * @author Jean Palate
 */
public class NewtonOptimizer {

    /**
     * max. number ofInternal iterations
     */
    private final static int NITERMAX = 30;
    /**
     * calculate new dx, when change of x0 is smaller than FACTOR*(old change of
     * x0)
     */
    private final static double NFACTOR = 5;
    /**
     * initialisation of |P(xmin)|
     */
    private final static double NFVALUE = 1e36;
    /**
     * max. number of iterations with no better value
     */
    private final static int NNOISEMAX = 5;
    /**
     * smallest such that 1.0+DBL_EPSILON != 1.0
     */
    private final static double DBL_EPSILON = 2.2204460492503131e-016;
    /**
     * if the imaginary part of the root is smaller than BOUND5 => real root
     */
    private final static double NBOUND = 1e2 * Math.sqrt(DBL_EPSILON);

    private final static double DFBOUND = 1e-6;

    final PolynomialComputer fn;
    Complex r;
    private double err;
    private int multiplicity;
    final boolean mroots;

    public NewtonOptimizer(Polynomial p, boolean mroots) {
        // normalize the polynomial
        fn = new PolynomialComputer(p.divide(p.get(p.getDegree())));
        this.mroots = mroots;
    }

    public Complex root(final Complex ns) {

        double fabsmin = NFVALUE, eps = DBL_EPSILON;
        int noise = 0;
        int d = fn.getPolynomialDegree();

        Complex xcur = ns;
        Complex xmin = xcur;
        Complex dx = Complex.ONE;
        err = 1;

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
            double axcur = xcur.abs();
            if (mroots && axcur != 0) {
                if (fabsmin < DFBOUND && df.abs() / axcur < 1e3 * DFBOUND && d > 1) {
                    // could be a multiple root
                    // we compute the derivative
                    Polynomial D = fn.D(1);
                    // recursive call
                    NewtonOptimizer doptimizer = new NewtonOptimizer(D, true);
                    Complex rroot = doptimizer.root(xcur);
                    if (rroot != null) {
                        multiplicity = 1 + doptimizer.multiplicity;
                        err=doptimizer.getError();
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

            if (axcur != 0) {
                if ((err / axcur < eps) || (noise == NNOISEMAX)) {
                    xmin = checkRealRoot(xmin);
                    err /= axcur;
                    multiplicity = 1;
                    return xmin;
                }
            }
            xcur = xcur.minus(dx);
            ++noise;
        }
        if (fabsmin > NBOUND) {
            return null;
        }

        xmin = checkRealRoot(xmin);
        double axmin2 = xmin.abs();
        if (axmin2 != 0) {
            err /= axmin2;
        }
        multiplicity = 1;
        return xmin;
    }

    public int getMultiplicity() {
        return this.multiplicity;
    }

    private Complex checkRealRoot(Complex xmin) {
        double axim = Math.abs(xmin.getIm());
        if (axim < NBOUND) {
            return Complex.cart(xmin.getRe(), 0);
        } else {
            return xmin;
        }
    }

    /**
     * @return the err
     */
    public double getError() {
        return err;
    }
}
