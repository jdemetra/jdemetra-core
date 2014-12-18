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
package ec.tstoolkit.maths;

import ec.tstoolkit.design.Algorithm;
import ec.tstoolkit.design.Development;

/**
 * Generic specification for simplifying operations.
 * Given two objects L, R, the simplifying tool computes l, r, c such 
 * that l*c = L, r*c = R, for some binary operator "*"
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Algorithm(entryPoint = "simplify")
public abstract class Simplifying<T> {
    /**
     * Left, right and common items
     */
    protected T m_left, m_right, m_common;

    /**
     * Clear the simplifying tool
     */
    protected void clear() {
	m_left = null;
	m_right = null;
	m_common = null;
    }

    /**
     * Gets the common "factor"
     * @return The common factor. Should not be null. 
     */
    public T getCommon() {
	return m_common;
    }

    /**
     * Gets the simplified left operand
     * @return The left operand. Could be the same as the initial operand
     */
    public T getLeft() {
	return m_left;
    }

    /**
     * Gets the simplified right operand
     * @return The right operand. Could be the same as the initial operand
     */
    public T getRight() {
	return m_right;
    }

    /**
     * Simplifies two elements T
     * @param left The left operand
     * @param right The right operand
     * @return True if the items have been simplified, false otherwise
     */
    public abstract boolean simplify(T left, T right);

}
