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
@Deprecated
public class Dnrm2 {
    /**
     * 
     * @param n
     * @param dx
     * @param ix
     * @param incx
     * @return
     */
    public static double fn(int n, double[] dx, int ix, int incx) {
	//
	// DNRM2 returns the euclidean norm of a vector via the function
	// name, so that
	//
	// DNRM2 := sqrt( x'*x )
	//
	//
	//
	// -- This version written on 25-October-1982.
	// Modified on 14-October-1993 to inline the call to DLASSQ.
	// Sven Hammarling, Nag Ltd.
	//
	//
	// .. Parameters ..
	final double ONE = 1.0, ZERO = 0.0;

	if (n < 1 || incx < 1) {
	    return 0;
	} else if (n == 1) {
	    return Math.abs(dx[ix]);
	} else {
	    double scale = ZERO;
	    double ssq = ONE;
	    int imax = ix + n * incx;
	    for (; ix < imax; ix += incx) {
		if (dx[ix] != ZERO) {
		    double absxi = Math.abs(dx[ix]);
		    if (scale < absxi) {
			double s = scale / absxi;
			ssq = ONE + ssq * s * s;
			scale = absxi;
		    } else {
			double s = absxi / scale;
			ssq += s * s;
		    }
		}
	    }
	    return scale * Math.sqrt(ssq);
	}
    }

}
