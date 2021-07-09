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
package internal.jdplus.maths.polynomials;

import java.util.Arrays;
import lombok.NonNull;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class Coefficients {

    public final double EPSILON = 1e-9;

    private final double[] C_ZERO = {0}, C_ONE = {1},
            C_POSINF = {Double.POSITIVE_INFINITY},
            C_NEGINF = {Double.NEGATIVE_INFINITY},
            C_NAN = {Double.NaN};

    /**
     * Return the coefficients of a polynomial of getDegree n as an array of
     * doubles. All coefficients are set to zero, except the highest which
     * is set to 1.
     *
     * @param degree
     * @return
     */
    public double[] fromDegree(int degree) {
        // all coefficuents are set to 0, except the highest.
        double[] c = new double[degree + 1];
        c[degree] = 1;
        return c;
    }

    /**
     * Simplify the given coefficients for trailing (quasi-)zeros if need be.
     * 
     * @param c The original coefficients
     * @return Either the given array (not a copy) or a shorter one after simplification
     */
    public double[] ofInternal(double[] c) {
        int nd = getUsedCoefficients(c, EPSILON);
        if (nd == c.length) {
            return c;
        } else if (nd == 0) {
            return C_ZERO;
        } else {
            return Arrays.copyOf(c, nd);
        }
    }

    
    /**
     * Simplify the given coefficients for trailing (quasi-)zeros if need be.
     * 
     * @param c The original coefficients
     * @return Either a copy of the given array or a shorter one after simplification
     */
    public double[] of(double[] c) {
        int nd = getUsedCoefficients(c, EPSILON);
        if (nd == c.length) {
            return c.clone();
        } else if (nd == 0) {
            return C_ZERO;
        } else {
            return Arrays.copyOf(c, nd);
        }
    }

    public double[] of(double c0, @NonNull double[] c) {
        int nd = getUsedCoefficients(c, EPSILON);
        if (nd == 0) {
            if (Math.abs(c0) <= EPSILON) {
                return C_ZERO;
            } else if (c0 == 1) {
                return C_ONE;
            } else {
                return new double[]{c0};
            }
        } else {
            double[] nc = new double[nd + 1];
            nc[0] = c0;
            for (int i = 0; i < nd; ++i) {
                nc[i + 1] = c[i];
            }
            return nc;
        }
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
}
