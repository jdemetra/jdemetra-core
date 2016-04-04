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
public class Dgemv {
    private final static double ZERO = 0.0, ONE = 1.0;

    /**
     * 
     * @param trans
     * @param m
     * @param n
     * @param alpha
     * @param a
     * @param ia
     * @param lda
     * @param x
     * @param ix
     * @param incx
     * @param beta
     * @param y
     * @param iy
     * @param incy
     * @throws Xerbla
     */
    public static void fn(OP trans, int m, int n, double alpha, double[] a,
	    int ia, int lda, double[] x, int ix, int incx, double beta,
	    double[] y, int iy, int incy) throws Xerbla {
	// Purpose
	// =======
	//
	// DGEMV performs one of the matrix-vector operations
	// y := alpha*A*x + beta*y, or y := alpha*A'*x + beta*y,
	// where alpha and beta are scalars, x and y are vectors and A is an
	// m by n matrix.
	// Parameters
	// ==========
	// TRANS - CHARACTER*1.
	// On entry, TRANS specifies the operation to be performed as
	// follows:
	// TRANS = 'N' or 'n' y := alpha*A*x + beta*y.
	// TRANS = 'T' or 't' y := alpha*A'*x + beta*y.
	// TRANS = 'C' or 'c' y := alpha*A'*x + beta*y.
	// Unchanged on exit.
	// M - INTEGER.
	// On entry, M specifies the number of rows of the matrix A.
	// M must be at least zero.
	// Unchanged on exit.
	// N - INTEGER.
	// On entry, N specifies the number of columns of the matrix A.
	// N must be at least zero.
	// Unchanged on exit.
	// ALPHA - DOUBLE PRECISION.
	// On entry, ALPHA specifies the scalar alpha.
	// Unchanged on exit.
	// A - DOUBLE PRECISION array of DIMENSION ( LDA, n ).
	// Before entry, the leading m by n part of the array A must
	// contain the matrix of coefficients.
	// Unchanged on exit.
	// LDA - INTEGER.
	// On entry, LDA specifies the first dimension of A as declared
	// in the calling (sub) program. LDA must be at least
	// max( 1, m ).
	// Unchanged on exit.
	// X - DOUBLE PRECISION array of DIMENSION at least
	// ( 1 + ( n - 1 )*abs( INCX ) ) when TRANS = 'N' or 'n'
	// and at least
	// ( 1 + ( m - 1 )*abs( INCX ) ) otherwise.
	// Before entry, the incremented array X must contain the
	// vector x.
	// Unchanged on exit.
	// INCX - INTEGER.
	// On entry, INCX specifies the increment for the elements of
	// X. INCX must not be zero.
	// Unchanged on exit.
	// BETA - DOUBLE PRECISION.
	// On entry, BETA specifies the scalar beta. When BETA is
	// supplied as zero then Y need not be set on input.
	// Unchanged on exit.
	// Y - DOUBLE PRECISION array of DIMENSION at least
	// ( 1 + ( m - 1 )*abs( INCY ) ) when TRANS = 'N' or 'n'
	// and at least
	// ( 1 + ( n - 1 )*abs( INCY ) ) otherwise.
	// Before entry with BETA non-zero, the incremented array Y
	// must contain the vector y. On exit, Y is overwritten by the
	// updated vector y.
	// INCY - INTEGER.
	// On entry, INCY specifies the increment for the elements of
	// Y. INCY must not be zero.
	// Unchanged on exit.
	// Level 2 Blas routine.
	// -- Written on 22-October-1986.
	// Jack Dongarra, Argonne National Lab.
	// Jeremy Du Croz, Nag Central Office.
	// Sven Hammarling, Nag Central Office.
	// Richard Hanson, Sandia National Labs.
	// Test the input parameters.

	int info = 0;
	if (m < 0)
	    info = 2;
	else if (n < 0)
	    info = 3;
	else if (lda < Math.max(1, m))
	    info = 6;
	else if (incx == 0)
	    info = 8;
	else if (incy == 0)
	    info = 11;
	if (info != 0)
	    throw new Xerbla("Dgemv", info);
	// Quick return if possible.
	if (m == 0 || n == 0 || (alpha == ZERO && beta == ONE))
	    return;
	int leny;// , kx, ky;

	if (trans == OP.None)
	    leny = m;
	else
	    leny = n;

	// if (incx > 0)
	// kx = ix;
	// else
	// kx = ix - (lenx - 1) * incx;
	// if (incy > 0)
	// ky = iy;
	// else
	// ky = iy - (leny - 1) * incy;
	// Start the operations. In this version the elements of A are
	// accessed sequentially with one pass through A.
	// First form y := beta*y.
	if (beta != ONE)
	    if (incy == 1)
		if (beta == ZERO)
		    for (int i = 0; i < leny; ++i)
			y[iy + i] = ZERO;
		else
		    for (int i = 0; i < leny; ++i)
			y[iy + i] *= beta;
	    else if (beta == ZERO)
		for (int i = 0, j = iy; i < leny; ++i, j += incy)
		    y[j] = ZERO;
	    else
		for (int i = 0, j = iy; i < leny; ++i, j += incy)
		    y[j] *= beta;
	if (alpha == ZERO)
	    return;
	if (trans == OP.None) {
	    // Form y := alpha*A*x + y.
	    if (incy == 1) {
		for (int j = 0, jx = ix, iv = ia; j < n; ++j, iv += lda, jx += incx)
		    if (x[jx] != ZERO) {
			double temp = alpha * x[jx];
			for (int i = 0, iw = iv; i < m; ++i, ++iw)
			    y[iy + i] += temp * a[iw];
		    }
	    } else {
		for (int j = 0, jx = ix, iv = ia; j < n; ++j, iv += lda, jx += incx)
		    if (x[jx] != ZERO) {
			double temp = alpha * x[jx];
			for (int i = 0, icur = iy, iw = iv; i < m; ++i, icur += incy, ++iw)
			    y[icur] += temp * a[iw];
		    }
	    }
	} else
	// Form y := alpha*A'*x + y.
	if (incx == 1) {
	    for (int j = 0, jy = iy, iv = ia; j < n; ++j, iv += lda, jy += incy) {
		double temp = ZERO;
		for (int i = 0, iw = iv; i < m; ++i, ++iw)
		    temp += a[iw] * x[ix + i];
		y[jy] += alpha * temp;
	    }
	} else {
	    for (int j = 0, jy = iy; j < n; ++j, jy += incy) {
		double temp = ZERO;
		for (int i = 0, icur = ix; i < m; ++i, icur += incx)
		    temp += a[ia + i + j * lda] * x[icur];
		y[jy] += alpha * temp;
	    }
	}
    }
}
