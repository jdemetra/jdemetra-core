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
package ec.tstoolkit.ssf;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.SubMatrix;

/**
 * Data for an uni-variate state space model
 * y = X b + e
 * e ~ ssf
 * b may contain diffuse elements and/or fixed unknown elements
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SsfModelData {

    private final ISsfData m_data;

    private final SubMatrix m_x;

    private final int[] m_idiffuse;

     /**
     * Creates a new model, with observations, regression variables and information
     * on diffuse coefficients.
     * @param data The observations
     * @param X The regression variables, stored in the columns of the matrix. May be null
     * @param DiffuseX The 0-based positions of the diffuse regression coefficients.
     * May be null.
     */
    public SsfModelData(final ISsfData data, final SubMatrix X,
	    final int[] DiffuseX) {
	m_data = data;
	m_x = X;
	m_idiffuse = DiffuseX;
    }

    /**
     * Gets the observations
     * @return The internal object containing the observations
     */
    public ISsfData getData()
    {
	return m_data;
    }

    /**
     * Gets the 0-based positions of the diffuse coefficients.
     * @return The internal object containing the positions. May be null.
     */
    public int[] getDiffuseX()
    {
	return m_idiffuse;
    }

    /**
     * Gets the matrix of the regression variables
     * @return The internal object containing the regression variables. May be null.
     */
    public SubMatrix getX()
    {
	return m_x;
    }
}
