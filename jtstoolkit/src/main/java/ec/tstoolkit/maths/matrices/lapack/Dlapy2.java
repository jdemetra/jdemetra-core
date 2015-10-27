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

package ec.tstoolkit.maths.matrices.lapack;

/**
 * 
 * @author Jean Palate
 */
public class Dlapy2 {

    private final static double ZERO = 0.0, ONE = 1.0;

    /**
     * 
     * @param x
     * @param y
     * @return
     */
    public static double fn(double x, double y) {
	// Purpose
	// =======
	// DLAPY2 returns sqrt(x**2+y**2), taking care not to cause unnecessary
	// overflow.
	// Arguments
	// =========
	// X (input) DOUBLE PRECISION
	// Y (input) DOUBLE PRECISION
	// X and Y specify the values x and y.
	// =====================================================================
	double xabs = Math.abs(x);
	double yabs = Math.abs(y);
	double w = Math.max(xabs, yabs);
	double z = Math.min(xabs, yabs);
	if (z == ZERO)
	    return w;
	else {
	    double zw = z / w;
	    return w * Math.sqrt(ONE + zw * zw);
	}
    }
}
