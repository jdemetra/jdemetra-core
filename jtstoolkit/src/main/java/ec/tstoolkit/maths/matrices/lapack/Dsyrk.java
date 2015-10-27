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
public class Dsyrk {

    private final static double ZERO = 0.0, ONE = 1.0;

    private static void fn_zeroalpha(UPLO uplo, int n, double beta, double[] c,
	    int ic, int ldc) {
	if (uplo == UPLO.Upper)
	    if (beta == ZERO)
		for (int j = 0; j < n; ++j)
		    for (int i = 0; i <= j; ++i)
			c[ic + i + j * ldc] = ZERO;
	    else
		for (int j = 0; j < n; ++j)
		    for (int i = 0; i <= j; ++i)
			c[ic + i + j * ldc] *= beta;
	else if (beta == ZERO)
	    for (int j = 0; j < n; ++j)
		for (int i = j; i < n; ++i)
		    c[ic + i + j * ldc] = ZERO;
	else
	    for (int j = 0; j < n; ++j)
		for (int i = j; i < n; ++i)
		    c[ic + i + j * ldc] *= beta;
    }

    /**
     * 
     * @param uplo
     * @param trans
     * @param n
     * @param k
     * @param alpha
     * @param a
     * @param ia
     * @param lda
     * @param beta
     * @param c
     * @param ic
     * @param ldc
     * @throws Xerbla
     */
    public void fn(UPLO uplo, OP trans, int n, int k, double alpha, double[] a,
	    int ia, int lda, double beta, double[] c, int ic, int ldc)
	    throws Xerbla {
	// Purpose
	// =======
	// DSYRK performs one of the symmetric rank k operations
	// C := alpha*A*A' + beta*C,
	// or
	// C := alpha*A'*A + beta*C,
	// where alpha and beta are scalars, C is an n by n symmetric matrix
	// and A is an n by k matrix in the first case and a k by n matrix
	// in the second case.
	// Parameters
	// ==========
	// UPLO - CHARACTER*1.
	// On entry, UPLO specifies whether the upper or lower
	// triangular part of the array C is to be referenced as
	// follows:
	// UPLO = 'U' or 'u' Only the upper triangular part of C
	// is to be referenced.
	// UPLO = 'L' or 'l' Only the lower triangular part of C
	// is to be referenced.
	// Unchanged on exit.
	// TRANS - CHARACTER*1.
	// On entry, TRANS specifies the operation to be performed as
	// follows:
	// TRANS = 'N' or 'n' C := alpha*A*A' + beta*C.
	// TRANS = 'T' or 't' C := alpha*A'*A + beta*C.
	// TRANS = 'C' or 'c' C := alpha*A'*A + beta*C.
	// Unchanged on exit.
	// N - INTEGER.
	// On entry, N specifies the order of the matrix C. N must be
	// at least zero.
	// Unchanged on exit.
	// K - INTEGER.
	// On entry with TRANS = 'N' or 'n', K specifies the number
	// of columns of the matrix A, and on entry with
	// TRANS = 'T' or 't' or 'C' or 'c', K specifies the number
	// of rows of the matrix A. K must be at least zero.
	// Unchanged on exit.
	// ALPHA - DOUBLE PRECISION.
	// On entry, ALPHA specifies the scalar alpha.
	// Unchanged on exit.
	// A - DOUBLE PRECISION array of DIMENSION ( LDA, ka ), where ka is
	// k when TRANS = 'N' or 'n', and is n otherwise.
	// Before entry with TRANS = 'N' or 'n', the leading n by k
	// part of the array A must contain the matrix A, otherwise
	// the leading k by n part of the array A must contain the
	// matrix A.
	// Unchanged on exit.
	// LDA - INTEGER.
	// On entry, LDA specifies the first dimension of A as declared
	// in the calling (sub) program. When TRANS = 'N' or 'n'
	// then LDA must be at least max( 1, n ), otherwise LDA must
	// be at least max( 1, k ).
	// Unchanged on exit.
	// BETA - DOUBLE PRECISION.
	// On entry, BETA specifies the scalar beta.
	// Unchanged on exit.
	// C - DOUBLE PRECISION array of DIMENSION ( LDC, n ).
	// Before entry with UPLO = 'U' or 'u', the leading n by n
	// upper triangular part of the array C must contain the upper
	// triangular part of the symmetric matrix and the strictly
	// lower triangular part of C is not referenced. On exit, the
	// upper triangular part of the array C is overwritten by the
	// upper triangular part of the updated matrix.
	// Before entry with UPLO = 'L' or 'l', the leading n by n
	// lower triangular part of the array C must contain the lower
	// triangular part of the symmetric matrix and the strictly
	// upper triangular part of C is not referenced. On exit, the
	// lower triangular part of the array C is overwritten by the
	// lower triangular part of the updated matrix.
	// LDC - INTEGER.
	// On entry, LDC specifies the first dimension of C as declared
	// in the calling (sub) program. LDC must be at least
	// max( 1, n ).
	// Unchanged on exit.
	// Level 3 Blas routine.
	// -- Written on 8-February-1989.
	// Jack Dongarra, Argonne National Laboratory.
	// Iain Duff, AERE Harwell.
	// Jeremy Du Croz, Numerical Algorithms Group Ltd.
	// Sven Hammarling, Numerical Algorithms Group Ltd.
	// Test the input parameters.
	int nrowa;
	if (trans == OP.None)
	    nrowa = n;
	else
	    nrowa = k;
	int info = 0;
	if (n < 0)
	    info = 3;
	else if (k < 0)
	    info = 4;
	else if (lda < Math.max(1, nrowa))
	    info = 7;
	else if (ldc < Math.max(1, n))
	    info = 10;
	if (info != 0)
	    throw new Xerbla("Dsyrk", info);

	// Quick return if possible.
	if (n == 0 || ((alpha == ZERO || k == 0) && (beta == ONE)))
	    return;
	if (alpha == ZERO)
	    fn_zeroalpha(uplo, n, beta, c, ic, ldc);
	else
	// Form C := alpha*A*A' + beta*C.
	if (trans == OP.None)
	    if (uplo == UPLO.Upper)
		fn_notrans_upper(n, k, alpha, a, ia, lda, beta, c, ic, ldc);
	    else
		fn_notrans_lower(n, k, alpha, a, ia, lda, beta, c, ic, ldc);
	else
	// Form C := alpha*A'*A + beta*C.
	if (uplo == UPLO.Upper)
	    fn_trans_upper(n, k, alpha, a, ia, lda, beta, c, ic, ldc);
	else
	    fn_trans_lower(n, k, alpha, a, ia, lda, beta, c, ic, ldc);
    }

    private void fn_notrans_lower(int n, int k, double alpha, double[] a,
	    int ia, int lda, double beta, double[] c, int ic, int ldc) {
	for (int j = 0; j < n; ++j) {
	    if (beta == ZERO)
		for (int i = j; i < n; ++i)
		    c[ic + i + j * ldc] = ZERO;
	    else if (beta != ONE)
		for (int i = j; i < n; ++i)
		    c[ic + i + j * ldc] *= beta;
	    for (int l = 0; l < k; ++l)
		if (a[ia + j + l * lda] != ZERO) {
		    double temp = alpha * a[ia + j + l * lda];
		    for (int i = j; i < n; ++i)
			c[ic + i + j * ldc] += temp * a[ia + i + l * lda];
		}
	}
    }

    private void fn_notrans_upper(int n, int k, double alpha, double[] a,
	    int ia, int lda, double beta, double[] c, int ic, int ldc) {
	for (int j = 0; j < n; ++j) {
	    if (beta == ZERO)
		for (int i = 0; i <= j; ++i)
		    c[ic + i + j * ldc] = ZERO;
	    else if (beta != ONE)
		for (int i = 0; i <= j; ++i)
		    c[ic + i + j * ldc] *= beta;
	    for (int l = 0; l < k; ++l)
		if (a[ia + j + l * lda] != ZERO) {
		    double temp = alpha * a[ia + j + l * lda];
		    for (int i = 0; i <= j; ++i)
			c[ic + i + j * ldc] += temp * a[ia + i + l * lda];
		}
	}
    }

    private void fn_trans_lower(int n, int k, double alpha, double[] a, int ia,
	    int lda, double beta, double[] c, int ic, int ldc) {
	for (int j = 0; j < n; ++j)
	    for (int i = j; i < n; ++i) {
		double temp = ZERO;
		for (int l = 0; l < k; ++l)
		    temp += a[ia + l + i * lda] * a[ia + l + j * lda];
		if (beta == ZERO)
		    c[ic + i + j * ldc] = alpha * temp;
		else
		    c[ic + i + j * ldc] = alpha * temp + beta
			    * c[ic + i + j * ldc];
	    }
    }

    private void fn_trans_upper(int n, int k, double alpha, double[] a, int ia,
	    int lda, double beta, double[] c, int ic, int ldc) {
	for (int j = 0; j < n; ++j)
	    for (int i = 0; i <= j; ++i) {
		double temp = ZERO;
		for (int l = 0; l < k; ++l)
		    temp += a[ia + l + i * lda] * a[ia + l + j * lda];
		if (beta == ZERO)
		    c[ic + i + j * ldc] = alpha * temp;
		else
		    c[ic + i + j * ldc] = alpha * temp + beta
			    * c[ic + i + j * ldc];
	    }
    }
}
