/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths.matrices;

import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.data.LogSign;
import demetra.random.RandomNumberGenerator;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class UpperTriangularMatrix {

    public void toUpper(Matrix S) {
        int m = S.getColumnsCount(), n = S.getRowsCount();
        if (n == 1) {
            return;
        }
        int rinc = S.getRowIncrement(), cinc = S.getColumnIncrement(), start = S.getStartPosition();
        double[] x = S.getStorage();
        int q = Math.min(m, n);
        for (int c = 0, id = start; c < q; ++c, id += rinc + cinc) {
            for (int r = c + 1, il = id; r < n; ++r) {
                il += rinc;
                x[il] = 0;
            }
        }
    }

    public void randomize(Matrix M, RandomNumberGenerator rng) {
        M.set((r, c) -> (r < c) ? 0 : rng.nextDouble());
    }

    public void rsolve(Matrix M, DataBlock x, double zero) throws MatrixException {
        LowerTriangularMatrix.lsolve(M.transpose(), x, zero);
    }

    public void lsolve(Matrix M, DataBlock x, double zero) throws MatrixException {
        LowerTriangularMatrix.rsolve(M.transpose(), x, zero);
    }

    public void rmul(Matrix M, DataBlock x) {
        LowerTriangularMatrix.lmul(M.transpose(), x);
    }

    public void lmul(Matrix M, DataBlock x) {
        LowerTriangularMatrix.rmul(M.transpose(), x);
    }

    public void rsolve(Matrix M, DataBlock x) throws MatrixException {
        rsolve(M, x, 0);
    }

    public void lsolve(Matrix M, DataBlock x) throws MatrixException {
        lsolve(M, x, 0);
    }

    // Matrix versions
    /**
     * Solves the set of equations X*L = B
     *
     * @param L The lower triangular matrix
     * @param B On entry the left hand side of the equation. Contains the
     * solution x on returning.
     * @param zero Small positive value identifying 0. Can be 0.
     * @throws MatrixException Thrown when the Length of b is larger than the
     * number of rows of the matrix.
     */
    public void lsolve(final Matrix L, final Matrix B, double zero) throws MatrixException {
        DataBlockIterator rows = B.rowsIterator();
        while (rows.hasNext()) {
            lsolve(L, rows.next(), zero);
        }
    }

    public void lsolve(final Matrix L, final Matrix B) throws MatrixException {
        lsolve(L, B, 0);
    }

    /**
     * Solves the set of equations LX = B where X and B are matrices with a
     * number of rows less than or equal to the number of columns of L. The
     * solution is returned in place i.e. the solution X replaces the right hand
     * side B. Column version
     *
     * @param L L. Lower triangular matrix
     * @param B On entry the right hand side of the equation. Contains the
     * solution X on returning.
     * @param zero Small positive value identifying 0. Can be 0.
     * @throws MatrixException Thrown when the system cannot be solved.
     */
    public void rsolve(final Matrix L, final Matrix B, final double zero) throws MatrixException {
        DataBlockIterator columns = B.columnsIterator();
        while (columns.hasNext()) {
            rsolve(L, columns.next(), zero);
        }
    }

    public void rsolve(final Matrix L, final Matrix B) throws MatrixException {
        rsolve(L, B, 0);
    }

    /**
     * Computes B = L * B
     *
     * @param L
     * @param B
     */
    public void rmul(final Matrix L, final Matrix B) {
        DataBlockIterator columns = B.columnsIterator();
        while (columns.hasNext()) {
            rmul(L, columns.next());
        }
    }

    /**
     * Computes B = B * L
     *
     * @param L
     * @param B
     * @throws MatrixException
     */
    public void lmul(final Matrix L, final Matrix B) {
        DataBlockIterator rows = B.rowsIterator();
        while (rows.hasNext()) {
            lmul(L, rows.next());
        }
    }

    /**
     * Computes the inverse of a triangular matrix R = U^-1
     *
     * @param U The upper matrix being inverted
     * @return The inverse
     * @throws MatrixException when the matrix is non invertible (some elements
     * of the diagonal are 0).
     */
    public CanonicalMatrix inverse(final Matrix U) throws MatrixException {
        int n = U.getRowsCount();
        CanonicalMatrix IU = CanonicalMatrix.identity(n);
        rsolve(U, IU);
        return IU;
    }
    
    public LogSign logDeterminant(Matrix L) {
        return LogSign.of(L.diagonal());
    }

    public double determinant(Matrix L) {
        LogSign ls = logDeterminant(L);
        if (ls == null) {
            return 0;
        }
        double val = Math.exp(ls.getValue());
        return ls.isPositive() ? val : -val;
    }

}
