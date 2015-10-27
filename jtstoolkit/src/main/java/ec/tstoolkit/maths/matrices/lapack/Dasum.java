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
public class Dasum {
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
	// takes the sum of the absolute values.
	// jack dongarra, linpack, 3/11/78.
	//
	double dtemp = 0.0;
	if (n <= 0 || incx <= 0) {
	    return 0;
	}
	if (incx != 1) {
	    //
	    // code for increment not equal to 1
	    //
	    int nincx = ix + n * incx;
	    for (int i = ix; i < nincx; i += incx) {
		dtemp += Math.abs(dx[i]);
	    }
	    return dtemp;
	} else {
	    // code for increment equal to 1
	    //
	    //
	    // clean-up loop
	    //
	    int m = n % 6;
	    if (m != 0) {
		for (int i = ix; i < ix + m; ++i) {
		    dtemp += Math.abs(dx[i]);
		}
	    }
	    if (n >= 6) {
		for (int i = ix + m; i < n; i += 6) {
		    dtemp += Math.abs(dx[i]) + Math.abs(dx[i + 1])
			    + Math.abs(dx[i + 2]) + Math.abs(dx[i + 3])
			    + Math.abs(dx[i + 4]) + Math.abs(dx[i + 5]);
		}
	    }
	    return dtemp;
	}
    }

}
