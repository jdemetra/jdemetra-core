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
public class Dcopy {
    /**
     * 
     * @param n
     * @param dx
     * @param ix
     * @param incx
     * @param dy
     * @param iy
     * @param incy
     */
    public static void fn(int n, double[] dx, int ix, int incx,
	    double[] dy, int iy, int incy) {
	//
	// copies a vector, x, to a vector, y.
	// uses unrolled loops for increments equal to one.
	// jack dongarra, linpack, 3/11/78.
	// modified 12/3/93, array(1) declarations changed to array(*)
	//
	//
	if (n <= 0) {
	    return;
	}
	if (incx != 1 || incy != 1) {
	    //
	    // code for unequal increments or equal increments
	    // not equal to 1
	    //
	    /*
	     * if (incx < 0) { ix -= (n - 1) * incx; } if (incy < 0) { iy -= (n
	     * - 1) * incy; }
	     */
	    for (int i = 0; i < n; ++i) {
		dy[iy] = dx[ix];
		ix += incx;
		iy += incy;
	    }
	} else {
	    //
	    // code for both increments equal to 1
	    //
	    //
	    // clean-up loop
	    //
	    int m = n % 7;
	    if (m != 0) {
		for (int i = 0; i < m; ++i) {
		    dy[iy++] = dx[ix++];
		}
	    }
	    if (n >= 7) {
		for (int i = m; i < n; i += 7) {
		    dy[iy++] = dx[ix++];
		    dy[iy++] = dx[ix++];
		    dy[iy++] = dx[ix++];
		    dy[iy++] = dx[ix++];
		    dy[iy++] = dx[ix++];
		    dy[iy++] = dx[ix++];
		    dy[iy++] = dx[ix++];
		}
	    }
	}
    }

}
