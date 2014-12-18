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
package ec.tstoolkit.maths.matrices;

import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public final class UpperTriangularMatrix {

    /**
     * 
     * @param upper
     * @return
     * @throws MatrixException
     */
    public static Matrix inverse(final Matrix upper) throws MatrixException {
	int n = upper.ncols_;
	Matrix rslt = Matrix.identity(n);
	DataBlockIterator cols = rslt.columns();
	DataBlock col = cols.getData();
	do
	    rsolve(upper, col);
	while (cols.next());
	return rslt;
    }

    /**
     * 
     * @param upper
     * @param left
     */
    public static void lmul(final Matrix upper, final DataBlock left) {
	/*
	 * if (upper == null) throw new ArgumentNullException("upper"); if (left
	 * == null) throw new ArgumentNullException("left");
	 */
	int nl = left.getLength();
	int n = upper.getRowsCount();
	// if (nl > n)
	// throw new MatrixException(MatrixException.IncompatibleDimensions);
	double[] data = upper.data_;
	for (int i = nl - 1, idx = i * n; i >= 0; --i, idx -= n) {
	    double t = 0.0;
	    for (int j = 0, idx2 = idx; j <= i; ++j, ++idx2)
		t += data[idx2] * left.get(j);
	    left.set(i, t);
	}
    }

    /**
     * The method left-multiplies the matrix with a vector. The Length of the
     * vector must be equal or less than the number of rows of the matrix. The
     * multiplier is modified in place <br>
     * <code>IncompatibleDimensionsException</code> Thrown when the Length of
     * the vector exceeds the number of rows of the matrix
     * 
     * @param upper
     * @param left
     *            An array of double
     */
    public static void lmul(final Matrix upper, final double[] left) {
	lmul(upper, new DataBlock(left));
    }

    /**
     * 
     * @param upper
     * @param left
     */
    public static void lmul(final Matrix upper, final SubMatrix left) {
	DataBlockIterator rows = left.rows();
	DataBlock row = rows.getData();
	do
	    lmul(upper, row);
	while (rows.next());
    }

    /**
     *
     * @param upper
     * @param b
     * @throws MatrixException
     */
    public static void lsolve(final Matrix upper, final DataBlock b)
	    throws MatrixException {
	// if (upper == null)
	// throw new ArgumentNullException("upper");
	// if (b == null)
	// throw new ArgumentNullException("b");
	int nb = b.getLength();
	int n = upper.getRowsCount();
	// if (nb > n)
	// throw new MatrixException(MatrixException.IncompatibleDimensions);
	double[] data = upper.data_;
	for (int i = 0, idx = 0; i < nb; ++i, idx += n) {
	    int idx2 = idx;
	    double t = b.get(i);
	    for (int j = 0; j < i; ++j, ++idx2)
		t -= b.get(j) * data[idx2];
	    double d = data[idx2];
	    if (d == 0)
		throw new MatrixException(MatrixException.Singular);
	    b.set(i, t / d);
	}
    }

    /**
     * Solves the set of equations x�A = right where x and right are vectors
     * with a Length less than or equal to the number of rows of the matrix. The
     * solution is returned in place i.e. the solution x replaces the right hand
     * side r.<br>
     * <code>IncompatibleDimensionsException</code> Thrown when the Length of
     * right is larger than the number of rows of the matrix.
     * 
     * @param upper
     *            On entry the leftt hand side of the equation. Contains the
     *            solution x on returning.
     * @param b
     * @throws MatrixException
     */
    public static void lsolve(final Matrix upper, final double[] b)
	    throws MatrixException {
	lsolve(upper, new DataBlock(b));
    }

    /**
     * 
     * @param upper
     * @param b
     * @throws MatrixException
     */
    public static void lsolve(final Matrix upper, final SubMatrix b)
	    throws MatrixException {
	DataBlockIterator rows = b.rows();
	DataBlock row = rows.getData();
	do
	    lsolve(upper, row);
	while (rows.next());
    }

    /**
     * 
     * @param upper
     * @param r
     */
    public static void rmul(final Matrix upper, final DataBlock r) {
	/*
	 * if (r == null) throw new ArgumentNullException("r"); if (upper ==
	 * null) throw new ArgumentNullException("upper");
	 */
	int nr = r.getLength();
	int n = upper.getColumnsCount();
	double[] data = upper.data_;

	// if (nr > n)
	// throw new MatrixException(MatrixException.IncompatibleDimensions);

	for (int i = 0, idx = 0; i < nr; ++i, idx += n + 1) {
	    double t = 0.0;
	    for (int j = i, idx2 = idx; j < nr; j++, idx2 += n)
		t += data[idx2] * r.get(j);
	    r.set(i, t);
	}
    }

    /**
     * The method right-multiplies the matrix with a vector. The Length of the
     * vector must be equal or less than the number of rows of the matrix. The
     * multiplier is modified in place<br>
     * <code>IncompatibleDimensionsException</code> Thrown when the Length of
     * the vector exceeds the number of rows of the matrix
     * 
     * @param upper
     * @param r
     *            An array of double
     */
    public static void rmul(final Matrix upper, final double[] r) {
	rmul(upper, new DataBlock(r));
    }

    /**
     * 
     * @param upper
     * @param rightmatrix
     */
    public static void rmul(final Matrix upper, final SubMatrix rightmatrix)
    {
	// if (rightmatrix == null)
	// throw new ArgumentNullException("rightmatrix");
	DataBlockIterator cols = rightmatrix.columns();
	DataBlock col = cols.getData();
	do
	    rmul(upper, col);
	while (cols.next());
    }

    /**
     * 
     * @param upper
     * @param b
     * @throws MatrixException
     */
    public static void rsolve(final Matrix upper, final DataBlock b)
	    throws MatrixException {
	// if (upper == null)
	// throw new ArgumentNullException("upper");
	// if (b == null)
	// throw new ArgumentNullException("b");
	int nb = b.getLength();
	int n = upper.getColumnsCount();
	// if (nb > n)
	// throw new MatrixException(MatrixException.IncompatibleDimensions);
	double[] data = upper.data_;
	// 
	for (int i = nb - 1, idx = (n + 1) * (nb - 1); i >= 0; --i, --idx) {
	    int idx2 = idx;
	    double t = b.get(i);
	    for (int j = nb - 1; j > i; --j, idx2 -= n)
		t -= b.get(j) * data[idx2];
	    double d = data[idx2];
	    if (d == 0)
		throw new MatrixException(MatrixException.Singular);
	    b.set(i, t / d);
	}
    }

    /**
     * Solves the set of equations A�x = b where x and b are vectors with a
     * Length less than or equal to the number of rows of the matrix. The
     * solution is returned in place i.e. the solution x replaces the right hand
     * side b. <br>
     * <code>IncompatibleDimensionsException</code> Thrown when the Length of b
     * is larger than the number of rows of the matrix.
     * 
     * @param upper
     * @param b
     *            On entry the right hand side of the equation. Contains the
     *            solution x on returning.
     * @throws MatrixException
     */
    public static void rsolve(final Matrix upper, final double[] b)
	    throws MatrixException {
	rsolve(upper, new DataBlock(b));
    }

    /**
     * 
     * @param upper
     * @param b
     * @throws MatrixException
     */
    public static void rsolve(final Matrix upper, final SubMatrix b)
	    throws MatrixException {
	// if (b == null)
	// throw new ArgumentNullException("b");
	DataBlockIterator cols = b.columns();
	DataBlock col = cols.getData();
	do
	    rsolve(upper, col);
	while (cols.next());
    }

    private UpperTriangularMatrix() {
    }

}
