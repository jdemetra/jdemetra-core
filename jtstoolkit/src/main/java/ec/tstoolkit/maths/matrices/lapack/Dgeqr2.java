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
public class Dgeqr2 {

    private final static double ONE = 1.0;

    /**
     * 
     * @param m
     * @param n
     * @param a
     * @param ia
     * @param lda
     * @param tau
     * @param work
     * @return
     * @throws Xerbla
     */
    public static int fn(int m, int n, double[] a, int ia, int lda,
	    double tau[], double[] work) {
	// Purpose
	// =======
	// DGEQR2 computes a QR factorization of a real m by n matrix A:
	// A = Q * R.
	// Arguments
	// =========
	// M (input) INTEGER
	// The number of rows of the matrix A. M >= 0.
	// N (input) INTEGER
	// The number of columns of the matrix A. N >= 0.
	// A (input/output) DOUBLE PRECISION array, dimension (LDA,N)
	// On entry, the m by n matrix A.
	// On exit, the elements on and above the diagonal of the array
	// contain the min(m,n) by n upper trapezoidal matrix R (R is
	// upper triangular if m >= n); the elements below the diagonal,
	// with the array TAU, represent the orthogonal matrix Q as a
	// product of elementary reflectors (see Further Details).
	// LDA (input) INTEGER
	// The leading dimension of the array A. LDA >= max(1,M).
	// TAU (output) DOUBLE PRECISION array, dimension (min(M,N))
	// The scalar factors of the elementary reflectors (see Further
	// Details).
	// WORK (workspace) DOUBLE PRECISION array, dimension (N)
	// INFO (output) INTEGER
	// = 0: successful exit
	// < 0: if INFO = -i, the i-th argument had an illegal value
	// Further Details
	// ===============
	// The matrix Q is represented as a product of elementary reflectors
	// Q = H(1) H(2) . . . H(k), where k = min(m,n).
	// Each H(i) has the form
	// H(i) = I - tau * v * v'
	// where tau is a real scalar, and v is a real vector with
	// v(1:i-1) = 0 and v(i) = 1; v(i+1:m) is stored on exit in A(i+1:m,i),
	// and tau in TAU(i).
	// =====================================================================

	int info = 0;
	// We skip the tests on the parameters (which should be always satisfied
	// if (m < 0)
	// info = -1;
	// else
	// if (n < 0)
	// info = -2;
	// else
	// if (lda < Math.max(1, m))
	// info = -4;
	// if (info != 0)
	// throw new Xerbla("Dgeqr2", info);

	int k = Math.min(m, n);
	Dlarfg dlarfg = new Dlarfg();
	for (int i = 0; i < k; ++i) {
	    // Generate elementary reflector H(i) to annihilate A(i+1:m,i)
	    dlarfg.fn(m - i, a[ia + i + i * lda], a, ia + i + 1 + i * lda, 1);
	    a[ia + i + i * lda] = dlarfg.beta();
	    tau[i] = dlarfg.tau();
	    if (i < n - 1) {
		// Apply H(i) to A(i:m,i+1:n) from the left
		double aii = a[ia + i + i * lda];
		a[ia + i + i * lda] = ONE;
		Dlarf.fn(SIDE.Left, m - i, n - i - 1, a, ia + i + i * lda, 1,
			tau[i], a, ia + i + (i + 1) * lda, lda, work);
		a[ia + i + i * lda] = aii;
	    }
	}
	return info;
    }
}
