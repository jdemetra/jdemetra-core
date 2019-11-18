/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices;

import demetra.math.Constants;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.data.LogSign;
import jdplus.math.matrices.lapack.TRMV;
import jdplus.math.matrices.lapack.TRSV;
import jdplus.random.RandomNumberGenerator;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class LowerTriangularMatrix {

    public void randomize(Matrix M, RandomNumberGenerator rng) {
        M.set((r, c) -> (c > r) ? 0 : rng.nextDouble());
    }

    public void lsolve(final Matrix L, final Matrix B) throws MatrixException {
        int nc = B.getColumnsCount();
        if (nc != L.getRowsCount()) {
            throw new MatrixException(MatrixException.DIM);
        }
        DataBlockIterator rows = B.rowsIterator();
        while (rows.hasNext()) {
            DataBlock c = rows.next();
            TRSV.Ltsolve(L, c.getStorage(), c.getStartPosition(), c.getIncrement());
        }
    }

    public void rsolve(final Matrix L, final Matrix B) throws MatrixException {
        int nr = B.getRowsCount(), nc = B.getColumnsCount();
        if (nr != L.getRowsCount()) {
            throw new MatrixException(MatrixException.DIM);
        }
        double[] pb = B.getStorage();
        int start = B.getStartPosition(), lda = B.getColumnIncrement();
        int bmax = start + nc * lda;
        for (int b = start; b < bmax; b += lda) {
            TRSV.Lsolve(L, pb, b, 1);
        }
    }

    /**
     * Computes r = L*r The method right-multiplies the matrix by a vector.The
     * Length of the vector must be equal or less than the number of rows of the
     * matrix. The multiplier is modified in place. Column version
     *
     * @param L The lower triangular matrix
     * @param r An array of double
     */
    public void rmul(Matrix L, DataBlock r) {
        TRMV.Lx(L, r.getStorage(), r.getStartPosition(), r.getIncrement());
    }

    /**
     * Computes l = l*L The method left-multiplies the matrix by a vector.The
     * Length of the vector must be equal to the number of rows of the matrix.
     * The multiplier is modified in place. Column version
     *
     * @param L The lower triangular matrix
     * @param l An array of double
     */
    public void lmul(Matrix L, DataBlock l) {
        // l = l *L <=> L'l' = l'
        TRMV.Ltx(L, l.getStorage(), l.getStartPosition(), l.getIncrement());
    }

    /**
     * B=L*B
     *
     * @param L
     * @param B
     */
    public void rmul(final Matrix L, final Matrix B) {
        int nr = B.getRowsCount(), nc = B.getColumnsCount();
        if (nr != L.getRowsCount()) {
            throw new MatrixException(MatrixException.DIM);
        }
        double[] pb = B.getStorage();
        int start = B.getStartPosition(), lda = B.getColumnIncrement();
        int bmax = start + nc * lda;
        for (int b = start; b < bmax; b += lda) {
            TRMV.Lx(L, pb, b, 1);
        }
    }

    /**
     * B=B*L
     *
     * @param L
     * @param B
     */
    public void lmul(final Matrix L, final Matrix B) {
        // B=B*L <=> L'B' = B'
        int nc = B.getColumnsCount();
        if (nc != L.getRowsCount()) {
            throw new MatrixException(MatrixException.DIM);
        }
        DataBlockIterator rows = B.rowsIterator();
        while (rows.hasNext()) {
            DataBlock c = rows.next();
            TRMV.Ltx(L, c.getStorage(), c.getStartPosition(), c.getIncrement());
        }
    }

    public void solve(Matrix L, final DataBlock b, double zero) throws MatrixException {
        double[] data = L.getStorage();
        double[] x = b.getStorage();
        int xbeg = b.getStartPosition();
        int xinc = b.getIncrement();
        int xend = b.getEndPosition();
        int nr = L.getRowsCount();
        int dinc = nr + 1;

        for (int i = 0, xi = xbeg; xi != xend; i += dinc, xi += xinc) {
            double t = x[xi];
            if (Math.abs(t) > zero) {
                double d = data[i];
                if (d == 0) {
                    for (int xj = xi + xinc, j = i + 1; xj != xend; xj += xinc, ++j) {
                        if (Math.abs(data[j]) > zero) {
                            throw new MatrixException(MatrixException.SINGULAR);
                        }
                    }
                    x[xi] = 0;
                } else {
                    double c = t / d;
                    x[xi] = c;
                    for (int xj = xi + xinc, j = i + 1; xj != xend; xj += xinc, ++j) {
                        x[xj] -= c * data[j];
                    }
                }
            } else {
                x[xi] = 0;
            }
        }
    }

    /**
     * Solves L*x=b
     *
     * @param L
     * @param b
     * @throws MatrixException
     */
    public void rsolve(Matrix L, final DataBlock b) throws MatrixException {
        TRSV.Lsolve(L, b.getStorage(), b.getStartPosition(), b.getIncrement());
    }

    /**
     * solves xL=b (or L'x = b)
     *
     * @param L
     * @param x
     * @throws MatrixException
     */
    public void lsolve(Matrix L, DataBlock x) throws MatrixException {
        TRSV.Ltsolve(L, x.getStorage(), x.getStartPosition(), x.getIncrement());
    }

    public void toLower(Matrix M) {
        int m = M.getRowsCount(), n = M.getColumnsCount();
        if (m != n) {
            throw new MatrixException(MatrixException.SQUARE);
        }
        if (n == 1) {
            return;
        }
        double[] x = M.getStorage();
        int lda = M.getColumnIncrement(), start = M.getStartPosition();
        for (int c = 0, ic = start; c < n; ic += lda, ++c) {
            int id = ic + c;
            for (int iu = ic; iu < id; ++iu) {
                x[iu] = 0;
            }
        }
    }

    /**
     * Computes the inverse of a triangular matrix R = L^-1
     *
     * @param L The triangular matrix being inverted
     * @return The inverse
     * @throws MatrixException when the matrix is non invertible (some elements
     * of the diagonal are 0).
     */
    public Matrix inverse(final Matrix L) throws MatrixException {
        if (L.diagonal().anyMatch(x -> x == 0)) {
            throw new MatrixException(MatrixException.SINGULAR);
        }
        int n = L.getRowsCount();
        if (n != L.getColumnsCount()) {
            throw new MatrixException(MatrixException.SQUARE);
        }

        Matrix IL = Matrix.identity(n);
        rsolve(L, IL);
        return IL;
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
