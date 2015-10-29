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
public class Ddot {

    /**
     * 
     * @param n
     * @param dx
     * @param ix
     * @param incx
     * @param dy
     * @param iy
     * @param incy
     * @return
     */
    public static double fn(int n, double[] dx, int ix, int incx, double[] dy,
	    int iy, int incy) {
	// forms the dot product of two vectors.
	// uses unrolled loops for increments equal to one.
	// jack dongarra, linpack, 3/11/78.
	if (n <= 0)
	    return 0;
	double dtemp = 0;
	if (incx != 1 || incy != 1) {
	    // code for unequal increments or equal increments
	    // not equal to 1
	    /*
	     * if (incx < 0) ix -= (n - 1) * incx; if (incy < 0) iy -= (n - 1) *
	     * incy;
	     */
	    for (int i = 0; i < n; ++i) {
		dtemp += dx[ix] * dy[iy];
		ix += incx;
		iy += incy;
	    }
	    return dtemp;
	} else {
	    // code for both increments equal to 1
	    // clean-up loop
	    int m = n % 5;
	    for (int i = 0; i < m; ++i)
		dtemp += dx[ix++] * dy[iy++];
	    for (int i = m; i < n; i += 5) {
		dtemp += dx[ix++] * dy[iy++];
		dtemp += dx[ix++] * dy[iy++];
		dtemp += dx[ix++] * dy[iy++];
		dtemp += dx[ix++] * dy[iy++];
		dtemp += dx[ix++] * dy[iy++];
	    }
	    return dtemp;
	}
    }
}
