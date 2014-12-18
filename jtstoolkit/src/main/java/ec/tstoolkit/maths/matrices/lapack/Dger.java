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
 * @author PCuser
 */
public class Dger {

    private final static double ZERO = 0.0;

    /**
     * 
     * @param m
     * @param n
     * @param alpha
     * @param x
     * @param ix
     * @param incx
     * @param y
     * @param iy
     * @param incy
     * @param a
     * @param ia
     * @param lda
     * @throws Xerbla
     */
    public static void fn(final int m, final int n, final double alpha,
	    final double[] x, final int ix, int incx, final double[] y,
	    final int iy, final int incy, final double[] a, final int ia,
	    final int lda) throws Xerbla {
	// Purpose
	// =======
	// DGER performs the rank 1 operation
	// A := alpha*x*y' + A,
	// where alpha is a scalar, x is an m element vector, y is an n element
	// vector and A is an m by n matrix.
	// Parameters
	// ==========
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
	// X - DOUBLE PRECISION array of dimension at least
	// ( 1 + ( m - 1 )*abs( INCX ) ).
	// Before entry, the incremented array X must contain the m
	// element vector x.
	// Unchanged on exit.
	// INCX - INTEGER.
	// On entry, INCX specifies the increment for the elements of
	// X. INCX must not be zero.
	// Unchanged on exit.
	// Y - DOUBLE PRECISION array of dimension at least
	// ( 1 + ( n - 1 )*abs( INCY ) ).
	// Before entry, the incremented array Y must contain the n
	// element vector y.
	// Unchanged on exit.
	// INCY - INTEGER.
	// On entry, INCY specifies the increment for the elements of
	// Y. INCY must not be zero.
	// Unchanged on exit.
	// A - DOUBLE PRECISION array of DIMENSION ( LDA, n ).
	// Before entry, the leading m by n part of the array A must
	// contain the matrix of coefficients. On exit, A is
	// overwritten by the updated matrix.
	// LDA - INTEGER.
	// On entry, LDA specifies the first dimension of A as declared
	// in the calling (sub) program. LDA must be at least
	// max( 1, m ).
	// Unchanged on exit.
	// Test the input parameters.

	int info = 0;
	if (m < 0)
	    info = 1;
	else if (n < 0)
	    info = 2;
	else if (incx == 0)
	    info = 5;
	else if (incx == 0)
	    info = 7;
	else if (lda < Math.max(1, m))
	    info = 9;
	if (info != 0)
	    throw new Xerbla("Dger", info);
	// Quick return if possible.
	if (m == 0 || n == 0 || alpha == ZERO)
	    return;
	// Start the operations. In this version the elements of A are
	// accessed sequentially with one pass through A.

	/*
	 * IF( INCY.GT.0 )THEN JY = 1 ELSE JY = 1 - ( N - 1 )*INCY END IF
	 */
	if (incx == 1) {
	    for (int j = 0, iv = iy; j < n; ++j, iv += incy)
		if (y[iv] != ZERO) {
		    double temp = alpha * y[iv];
		    for (int i = 0; i < m; ++i)
			a[ia + i + j * lda] += x[ix + i] * temp;
		}
	} else
	    for (int j = 0, iv = iy; j < n; ++j, iv += incy)
		if (y[iv] != ZERO) {
		    double temp = alpha * y[iv];
		    for (int i = 0, iw = ix; i < m; ++i, iw += incx)
			a[ia + i + j + lda] += x[iw] * temp;
		}
    }
}
