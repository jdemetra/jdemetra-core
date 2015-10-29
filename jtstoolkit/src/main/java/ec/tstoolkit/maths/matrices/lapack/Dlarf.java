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

import ec.tstoolkit.design.Development;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Exploratory)
public class Dlarf {

    private final static double ZERO = 0.0, ONE = 1.0;

    /**
     * 
     * @param side
     * @param m
     * @param n
     * @param v
     * @param iv
     * @param incv
     * @param tau
     * @param c
     * @param ic
     * @param ldc
     * @param work
     * @throws Xerbla
     */
    public static void fn(SIDE side, int m, int n, double[] v, int iv,
	    int incv, double tau, double[] c, int ic, int ldc, double[] work) {
	// Purpose
	// =======
	// DLARF applies a real elementary reflector H to a real m by n matrix
	// C, from either the left or the right. H is represented in the form
	// H = I - tau * v * v'
	// where tau is a real scalar and v is a real vector.
	// If tau = 0, then H is taken to be the unit matrix.
	// Arguments
	// =========
	// SIDE (input) CHARACTER*1
	// = 'L': form H * C
	// = 'R': form C * H
	// M (input) INTEGER
	// The number of rows of the matrix C.
	// N (input) INTEGER
	// The number of columns of the matrix C.
	// V (input) DOUBLE PRECISION array, dimension
	// (1 + (M-1)*abs(INCV)) if SIDE = 'L'
	// or (1 + (N-1)*abs(INCV)) if SIDE = 'R'
	// The vector v in the representation of H. V is not used if
	// TAU = 0.
	// INCV (input) INTEGER
	// The increment between elements of v. INCV <> 0.
	// TAU (input) DOUBLE PRECISION
	// The value tau in the representation of H.
	// C (input/output) DOUBLE PRECISION array, dimension (LDC,N)
	// On entry, the m by n matrix C.
	// On exit, C is overwritten by the matrix H * C if SIDE = 'L',
	// or C * H if SIDE = 'R'.
	// LDC (input) INTEGER
	// The leading dimension of the array C. LDC >= max(1,M).
	// WORK (workspace) DOUBLE PRECISION array, dimension
	// (N) if SIDE = 'L'
	// or (M) if SIDE = 'R'
	// =====================================================================
	if (side == SIDE.Left) {
	    // Form H * C
	    if (tau != ZERO) {
		// w := C' * v
		Dgemv.fn(OP.Transpose, m, n, ONE, c, ic, ldc, v, iv, incv,
			ZERO, work, 0, 1);
		// C := C - v * w'
		Dger.fn(m, n, -tau, v, iv, incv, work, 0, 1, c, ic, ldc);
	    }
	} else // Form C * H
	if (tau != ZERO) {
	    // w := C * v
	    Dgemv.fn(OP.None, m, n, ONE, c, ic, ldc, v, iv, incv, ZERO, work,
		    0, 1);
	    // C := C - w * v'
	    Dger.fn(m, n, -tau, work, 0, 1, v, iv, incv, c, ic, ldc);
	}
    }
}
