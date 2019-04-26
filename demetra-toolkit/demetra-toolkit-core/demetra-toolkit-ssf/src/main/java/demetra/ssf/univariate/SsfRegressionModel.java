/*
* Copyright 2013 National Bank of Belgium
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
package demetra.ssf.univariate;

import demetra.design.Development;
import demetra.maths.matrices.FastMatrix;
import java.util.Arrays;

/**
 * Uni-variate state space regression model y = X b + e e ~ ssf b may contain
 * diffuse elements and/or fixed unknown elements
 *
 * @param <F> The class of the state space model
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SsfRegressionModel<F extends ISsf> {

    private final ISsfData y;

    private final FastMatrix X;

    private final int[] diffuseElements;

    /**
     *
     */
    private final F ssf;

    /**
     * Creates a new uni-variate ssf model
     *
     * @param ssf The state space form of the residuals
     * @param data The observations
     * @param X The regression variables. May be null.
     * @param DiffuseX The 0-based positions of the diffuse regression
     * coefficients. May be null.
     */
    public SsfRegressionModel(final F ssf, final ISsfData data, final FastMatrix X,
            final int[] DiffuseX) {
        this.ssf = ssf;
        y = data;
        this.X = X;
        diffuseElements = DiffuseX;
        if (diffuseElements != null) {
            Arrays.sort(diffuseElements);
        }
    }

    public SsfRegressionModel(final F ssf, final ISsfData data) {
        this(ssf, data, null, null);
    }

    /**
     * Gets the observations
     *
     * @return The internal object containing the observations
     */
    public ISsfData getY() {
        return y;
    }

    public F getSsf() {
        return ssf;
    }

    /**
     * Gets the 0-based positions of the diffuse coefficients.
     *
     * @return The internal object containing the positions. May be null.
     */
    public int[] getDiffuseElements() {
        return diffuseElements;
    }

    public boolean isDiffuse(int idx) {
        if (diffuseElements == null) {
            return false;
        } else {
            for (int i = 0; i < diffuseElements.length; ++i) {
                if (diffuseElements[i] == idx) {
                    return true;
                } else if (diffuseElements[i] > idx) {
                    return false;
                }
            }
            return false;
        }
    }

    /**
     * Gets the matrix of the regression variables
     *
     * @return The internal object containing the regression variables. May be
     * null.
     */
    public FastMatrix getX() {
        return X;
    }

    public boolean hasX() {
        return X != null;
    }

}
