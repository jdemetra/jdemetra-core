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
package internal.jdplus.math.functions.gsl.roots;

import java.util.function.DoubleUnaryOperator;

/**
 * The false position algorithm is a method of finding roots based on linear
 * interpolation. Its convergence is linear, but it is usually faster than
 * bisection.
 *
 * On each iteration a line is drawn between the endpoints (a,f(a)) and (b,f(b))
 * and the point where this line crosses the x-axis taken as a “midpoint”. The
 * value of the function at this point is calculated and its sign is used to
 * determine which side of the interval does not contain a root. That side is
 * discarded to give a new, smaller interval containing the root. This procedure
 * can be continued indefinitely until the interval is sufficiently small.
 *
 * The best estimate of the root is taken from the linear interpolation of the
 * interval on the current iteration.
 *
 * @see
 * https://www.gnu.org/software/gsl/doc/html/roots.html#c.gsl_root_fsolver_falsepos
 * @author Mats Maggi
 */
public class FalsePosSolver extends FSolver {

    private double fLower;
    private double fUpper;

    public FalsePosSolver(DoubleUnaryOperator fn, double xLower, double xUpper) {
        this.function = fn;
        this.lower = xLower;
        this.upper = xUpper;

        this.root = 0.5 * (xLower + xUpper);
        fLower = fn.applyAsDouble(xLower);
        fUpper = fn.applyAsDouble(xUpper);

        if ((fLower < 0.0 && fUpper < 0.0) || (fLower > 0.0 && fUpper > 0.0)) {
            throw new GslRootException("Endpoints do not straddle y=0");
        }
    }

    @Override
    public void iterate() {
        double xLinear, fLinear;
        double xBisect, fBisect;
        final double xLeft = lower;
        final double xRight = upper;

        final double f_lower = fLower;
        final double f_upper = fUpper;

        double w;

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

        /* Draw a line between f(*lower_bound) and f(*upper_bound) and 
        note where it crosses the X axis; that's where we will
        split the interval. */
        xLinear = xRight - (f_upper * (xLeft - xRight) / (f_lower - f_upper));

        fLinear = function.applyAsDouble(xLinear);

        if (fLinear == 0.0) {
            root = xLinear;
            lower = xLinear;
            upper = xLinear;
            return;
        }

        /* Discard the half of the interval which doesn't contain the root. */
        if ((f_lower > 0.0 && fLinear < 0.0) || (f_lower < 0.0 && fLinear > 0.0)) {
            root = xLinear;
            upper = xLinear;
            fUpper = fLinear;
            w = xLinear - xLeft;
        } else {
            root = xLinear;
            lower = xLinear;
            fLower = fLinear;
            w = xRight - xLinear;
        }

        if (w < 0.5 * (xRight - xLeft)) {
            return;
        }

        xBisect = 0.5 * (xLeft + xRight);
        fBisect = function.applyAsDouble(xBisect);

        if ((f_lower > 0.0 && fBisect < 0.0) || (f_lower < 0.0 && fBisect > 0.0)) {
            upper = xBisect;
            fUpper = fBisect;
            if (root > xBisect) {
                root = 0.5 * (xLeft + xBisect);
            }
        } else {
            lower = xBisect;
            fLower = fBisect;
            if (root < xBisect) {
                root = 0.5 * (xBisect + xRight);
            }
        }
    }
}
