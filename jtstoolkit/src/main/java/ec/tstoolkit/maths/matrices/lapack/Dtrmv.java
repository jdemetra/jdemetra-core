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
public class Dtrmv {

    private static final double ZERO = 0.0;

    private static void dtrmv_notrans_lower(DIAG diag, int n, double[] a,
	    int ia, int lda, double[] x, int ix, int incx) {
	if (incx == 1) {
	    for (int j = n - 1; j >= 0; --j)
		if (x[ix + j] != ZERO) {
		    double temp = x[ix + j];
		    for (int i = n - 1; i > j; --i)
			x[ix + i] += temp * a[ia + i + j * lda];
		    if (diag != DIAG.Unit)
			x[ix + j] *= a[ia + j + j * lda];
		}
	} else
	    for (int j = n - 1, iv = ix + (n - 1) * incx; j >= 0; --j, iv -= incx)
		if (x[iv] != ZERO) {
		    double temp = x[iv];
		    for (int i = n - 1, iw = ix + (n - 1) * incx; i > j; --i, iw -= incx)
			x[iw] += temp * a[ia + i + j * lda];
		    if (diag != DIAG.Unit)
			x[iv] *= a[ia + j + j * lda];
		}
    }

    private static void dtrmv_notrans_upper(DIAG diag, int n, double[] a,
	    int ia, int lda, double[] x, int ix, int incx) {
	if (incx == 1) {
	    for (int j = 0; j < n; ++j)
		if (x[ix + j] != ZERO) {
		    double temp = x[ix + j];
		    for (int i = 0; i < j; ++i)
			x[ix + i] += temp * a[ia + i + j * lda];
		    if (diag != DIAG.Unit)
			x[ix + j] *= a[ia + j + j * lda];
		}
	} else
	    for (int j = 0, iv = ix; j < n; ++j, iv += incx)
		if (x[iv] != ZERO) {
		    double temp = x[iv];
		    for (int i = 0, iw = ix; i < j; ++i, iw += incx)
			x[iw] += temp * a[ia + i + j * lda];
		    if (diag != DIAG.Unit)
			x[iv] *= a[ia + j + j * lda];
		}
    }

    private static void dtrmv_trans_lower(DIAG diag, int n, double[] a, int ia,
	    int lda, double[] x, int ix, int incx) {
	if (incx == 1)
	    for (int j = 0; j < n; ++j) {
		double temp = x[ix + j];
		if (diag != DIAG.Unit)
		    temp *= a[ia + j + j * lda];
		for (int i = j + 1; i < n; ++i)
		    temp += a[ia + i + j * lda] * x[ix + i];
		x[ix + j] = temp;
	    }
	else
	    for (int j = 0, iv = ix; j < n; ++j, iv += incx) {
		double temp = x[iv];
		if (diag != DIAG.Unit)
		    temp *= a[ia + j + j * lda];
		for (int i = j + 1, iw = ix + incx; i < n; ++i)
		    temp += a[ia + i + j * lda] * x[iw];
		x[iv] = temp;
	    }
    }

    private static void dtrmv_trans_upper(DIAG diag, int n, double[] a, int ia,
	    int lda, double[] x, int ix, int incx) {
	if (incx == 1)
	    for (int j = n - 1; j >= 0; --j) {
		double temp = x[ix + j];
		if (diag != DIAG.Unit)
		    temp *= a[ia + j + j * lda];
		for (int i = j - 1; i >= 0; --i)
		    temp += a[ia + i + j * lda] * x[ix + i];
		x[ix + j] = temp;
	    }
	else
	    for (int j = n - 1, iv = ix + (n - 1) * incx; j >= 0; --j, iv -= incx) {
		double temp = x[iv];
		if (diag != DIAG.Unit)
		    temp *= a[ia + j + j * lda];
		for (int i = j - 1, iw = ix - incx; i >= 0; --i, iw -= incx)
		    temp += a[ia + i + j * lda] * x[iw];
		x[iv] = temp;
	    }
    }

    /**
     * 
     * @param uplo
     * @param trans
     * @param diag
     * @param n
     * @param a
     * @param ia
     * @param lda
     * @param x
     * @param ix
     * @param incx
     * @throws Xerbla
     */
    public static void fn(UPLO uplo, OP trans, DIAG diag, int n,
	    double[] a, int ia, int lda, double[] x, int ix, int incx)
	    throws Xerbla {
	// Purpose
	// =======
	// DTRMV performs one of the matrix-vector operations
	// x := A*x, or x := A'*x,
	// where x is an n element vector and A is an n by n unit, or non-unit,
	// upper or lower triangular matrix.
	// Parameters
	// ==========
	// UPLO - CHARACTER*1.
	// On entry, UPLO specifies whether the matrix is an upper or
	// lower triangular matrix as follows:
	// UPLO = 'U' or 'u' A is an upper triangular matrix.
	// UPLO = 'L' or 'l' A is a lower triangular matrix.
	// Unchanged on exit.
	// TRANS - CHARACTER*1.
	// On entry, TRANS specifies the operation to be performed as
	// follows:
	// TRANS = 'N' or 'n' x := A*x.
	// TRANS = 'T' or 't' x := A'*x.
	// TRANS = 'C' or 'c' x := A'*x.
	// Unchanged on exit.
	// DIAG - CHARACTER*1.
	// On entry, DIAG specifies whether or not A is unit
	// triangular as follows:
	// DIAG = 'U' or 'u' A is assumed to be unit triangular.
	// DIAG = 'N' or 'n' A is not assumed to be unit
	// triangular.
	// Unchanged on exit.
	// N - INTEGER.
	// On entry, N specifies the order of the matrix A.
	// N must be at least zero.
	// Unchanged on exit.
	// A - DOUBLE PRECISION array of DIMENSION ( LDA, n ).
	// Before entry with UPLO = 'U' or 'u', the leading n by n
	// upper triangular part of the array A must contain the upper
	// triangular matrix and the strictly lower triangular part of
	// A is not referenced.
	// Before entry with UPLO = 'L' or 'l', the leading n by n
	// lower triangular part of the array A must contain the lower
	// triangular matrix and the strictly upper triangular part of
	// A is not referenced.
	// Note that when DIAG = 'U' or 'u', the diagonal elements of
	// A are not referenced either, but are assumed to be unity.
	// Unchanged on exit.
	// LDA - INTEGER.
	// On entry, LDA specifies the first dimension of A as declared
	// in the calling (sub) program. LDA must be at least
	// max( 1, n ).
	// Unchanged on exit.
	// X - DOUBLE PRECISION array of dimension at least
	// ( 1 + ( n - 1 )*abs( INCX ) ).
	// Before entry, the incremented array X must contain the n
	// element vector x. On exit, X is overwritten with the
	// tranformed vector x.
	// INCX - INTEGER.
	// On entry, INCX specifies the increment for the elements of
	// X. INCX must not be zero.
	// Unchanged on exit.

	// .. Executable Statements ..
	// Test the input parameters.
	int info = 0;
	if (n < 0)
	    info = 4;
	else if (lda < Math.max(1, n))
	    info = 6;
	else if (incx == 0)
	    info = 8;
	if (info != 0)
	    throw new Xerbla("Dtrmv", info);
	// Quick return if possible.
	if (n == 0)
	    return;
	// Start the operations. In this version the elements of A are
	// accessed sequentially with one pass through A.
	if (trans == OP.None)
	    if (uplo == UPLO.Upper)
		dtrmv_notrans_upper(diag, n, a, ia, lda, x, ix, incx);
	    else
		dtrmv_notrans_lower(diag, n, a, ia, lda, x, ix, incx);
	else if (uplo == UPLO.Upper)
	    dtrmv_trans_upper(diag, n, a, ia, lda, x, ix, incx);
	else
	    dtrmv_trans_lower(diag, n, a, ia, lda, x, ix, incx);
    }
}