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
package jdplus.maths.functions.gsl.roots;

import java.util.function.DoubleUnaryOperator;

/**
 * The bisection algorithm is the simplest method of bracketing the roots of a
 * function.
 * It is the slowest algorithm provided by the library, with linear
 * convergence.
 * On each iteration, the interval is bisected and the value of the function at
 * the midpoint is calculated. The sign of this value is used to determine which
 * half of the interval does not contain a root. That half is discarded to give
 * a new, smaller interval containing the root. This procedure can be continued
 * indefinitely until the interval is sufficiently small.
 * At any time the current estimate of the root is taken as the midpoint of the
 * interval.
 *
 * @see
 * https://www.gnu.org/software/gsl/doc/html/roots.html#c.gsl_root_fsolver_bisection
 * @author Mats Maggi
 */
public class BisectionSolver extends FSolver {

    private double fLower;
    private double fUpper;

    public BisectionSolver(DoubleUnaryOperator fn, double lower, double upper) {
        this.function = fn;
        this.lower = lower;
        this.upper = upper;

        this.root = 0.5 * (lower + upper);
        fLower = fn.applyAsDouble(lower);
        fUpper = fn.applyAsDouble(upper);

        if ((fLower < 0.0 && fUpper < 0.0) || (fLower > 0.0 && fUpper > 0.0)) {
            throw new GslRootException("Endpoints do not straddle y=0");
        }
    }

    @Override
    public void iterate() {
        double xBisect, fBisect;
        final double xLeft = lower;
        final double xRight = upper;

        final double f_lower = fLower;
        final double f_upper = fUpper;

        if (f_lower == 0.0) {
            root = xLeft;
            upper = xLeft;
            return;
        }

        if (f_upper == 0.0) {
            root = xRight;
            lower = xRight;
            return;
        }

        xBisect = (xLeft + xRight) / 2.0;
        fBisect = function.applyAsDouble(xBisect);

        if (fBisect == 0.0) {
            root = xBisect;
            lower = xBisect;
            upper = xBisect;
            return;
        }

        /* Discard the half of the interval which doesn't contain the root. */
        if ((f_lower > 0.0 && fBisect < 0.0) || (f_lower < 0.0 && fBisect > 0.0)) {
            root = 0.5 * (xLeft + xBisect);
            upper = xBisect;
            fUpper = fBisect;
        } else {
            root = 0.5 * (xBisect + xRight);
            lower = xBisect;
            fLower = fBisect;
        }
    }
}
