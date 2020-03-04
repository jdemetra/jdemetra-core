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
package jdplus.math;

import demetra.design.Development;

/**
 * Generic specification for simplifying operations.
 * Given two objects L, R, the simplifying tool computes l, r, c such 
 * that l*c = L, r*c = R, for some binary operator "*"
 * @author Jean Palate
 * @param <T>
 */
@Development(status = Development.Status.Release)
public abstract class Simplifying<T> {
    /**
     * Left, right and common items
     */
    protected T simplifiedLeft, simplifiedRight, common;

    /**
     * Clear the simplifying tool
     */
    protected void clear() {
	simplifiedLeft = null;
	simplifiedRight = null;
	common = null;
    }

    /**
     * Gets the common "factor"
     * @return The common factor. Should not be null. 
     */
    public T getCommon() {
	return common;
    }

    /**
     * Gets the simplified simplifiedLeft operand
     * @return The simplifiedLeft operand. Could be the same as the initial operand
     */
    public T getLeft() {
	return simplifiedLeft;
    }

    /**
     * Gets the simplified right operand
     * @return The right operand. Could be the same as the initial operand
     */
    public T getRight() {
	return simplifiedRight;
    }

    /**
     * Simplifies two elements T
     * @param left The simplifiedLeft operand
     * @param right The right operand
     * @return True if the items have been simplified, false otherwise
     */
    public abstract boolean simplify(T left, T right);

}
