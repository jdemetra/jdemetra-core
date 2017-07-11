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
public class Dpotf2 {

    private final static double ZERO = 0.0, ONE = 1.0;

    private static int dpotf2_lower(int n, double[] a, int ia, int lda)
	    throws Xerbla {
	// Compute the Cholesky factorization A = L*L'.
	for (int j = 0, ijj = ia, ij = ia; j < n; ++j, ijj += lda + 1, ++ij) {
	    double ajj = a[ijj] - Ddot.fn(j, a, ij, lda, a, ij, lda);
	    if (ajj < ZERO) {
		a[ijj] = ajj;
		return j + 1;
	    }
	    ajj = Math.sqrt(ajj);
	    a[ijj] = ajj;
	    // Compute elements J+1:N of column J.
	    if (j < n - 1) {
		Dgemv.fn(OP.None, n - j - 1, j, -ONE, a, ij + 1, lda, a, ij,
			lda, ONE, a, ijj + 1, 1);
		Dscal.fn(n - j - 1, ONE / ajj, a, ijj + 1, 1);
	    }
	}
	return 0;
    }

    private static int dpotf2_upper(int n, double[] a, int ia, int lda)
	    throws Xerbla {
	// Compute the Cholesky factorization A = U'*U.
	for (int j = 0, ijj = ia, ij = ia; j < n; ++j, ij += lda, ijj += lda + 1) {
	    // Compute U(J,J) and test for non-positive-definiteness.
	    double ajj = a[ijj] - Ddot.fn(j, a, ij, 1, a, ij, 1);
	    if (ajj < ZERO) {
		a[ijj] = ajj;
		return j + 1;
	    }
	    ajj = Math.sqrt(ajj);
	    a[ijj] = ajj;
	    // Compute elements J+1:N of row J.
	    if (j < n - 1) {
		Dgemv.fn(OP.Transpose, j, n - j - 1, -ONE, a, ij + lda, lda, a,
			ij, 1, ONE, a, ijj + lda, lda);
		Dscal.fn(n - j - 1, ONE / ajj, a, ijj + lda, lda);
	    }
	}
	return 0;
    }

    /**
     * 
     * @param uplo
     * @param n
     * @param a
     * @param ia
     * @param lda
     * @return
     * @throws Xerbla
     */
    public static int fn(UPLO uplo, int n, double[] a, int ia, int lda) {
	//
	// -- LAPACK routine (version 3.0) --
	// Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,
	// Courant Institute, Argonne National Lab, and Rice University
	// February 29, 1992
	// ..
	//
	// Purpose
	// =======
	//
	// DPOTF2 computes the Cholesky factorization of a real symmetric
	// positive definite matrix A.
	//
	// The factorization has the form
	// A = U' * U , if UPLO = 'U', or
	// A = L * L', if UPLO = 'L',
	// where U is an upper triangular matrix and L is lower triangular.
	//
	// This is the unblocked version of the algorithm, calling Level 2 BLAS.
	//
	// Arguments
	// =========
	//
	// UPLO (input) CHARACTER*1
	// Specifies whether the upper or lower triangular part of the
	// symmetric matrix A is stored.
	// = 'U': Upper triangular
	// = 'L': Lower triangular
	//
	// N (input) INTEGER
	// The order of the matrix A. N >= 0.
	//
	// A (input/output) DOUBLE PRECISION array, dimension (LDA,N)
	// On entry, the symmetric matrix A. If UPLO = 'U', the leading
	// n by n upper triangular part of A contains the upper
	// triangular part of the matrix A, and the strictly lower
	// triangular part of A is not referenced. If UPLO = 'L', the
	// leading n by n lower triangular part of A contains the lower
	// triangular part of the matrix A, and the strictly upper
	// triangular part of A is not referenced.
	//
	// On exit, if INFO = 0, the factor U or L from the Cholesky
	// factorization A = U'*U or A = L*L'.
	//
	// LDA (input) INTEGER
	// The leading dimension of the array A. LDA >= max(1,N).
	//
	// INFO (output) INTEGER
	// = 0: successful exit
	// < 0: if INFO = -k, the k-th argument had an illegal value
	// > 0: if INFO = k, the leading minor of order k is not
	// positive definite, and the factorization could not be
	// completed.
	//
	// =====================================================================
	int info = 0;
	// Test the input parameters.
	if (n < 0)
	    info = -2;
	if (lda < Math.max(1, n))
	    info = -4;
	if (info != 0)
	    throw new Xerbla("Dpotf2", -info);
	if (n == 0)
	    return 0;
	if (uplo == UPLO.Upper)
	    return dpotf2_upper(n, a, ia, lda);
	else
	    return dpotf2_lower(n, a, ia, lda);
    }
}
