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
public class Daxpy {
    /**
     * 
     * @param n
     * @param da
     * @param dx
     * @param ix
     * @param incx
     * @param dy
     * @param iy
     * @param incy
     */
    public static void fn(int n, double da, double[] dx, int ix,
	    int incx, double[] dy, int iy, int incy) {
	//
	// constant times a vector plus a vector.
	// uses unrolled loops for increments equal to one.
	// jack dongarra, linpack, 3/11/78.
	//
	if (n <= 0) {
	    return;
	}
	if (da == 0.0) {
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
		dy[iy] += da * dx[ix];
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
	    int m = n % 4;
	    if (m != 0) {
		for (int i = 0; i < m; ++i) {
		    dy[iy++] += da * dx[ix++];
		}
	    }
	    if (n >= 4) {
		for (int i = m; i < n; i += 4) {
		    dy[iy++] += da * dx[ix++];
		    dy[iy++] += da * dx[ix++];
		    dy[iy++] += da * dx[ix++];
		    dy[iy++] += da * dx[ix++];
		}
	    }
	}
    }

}
