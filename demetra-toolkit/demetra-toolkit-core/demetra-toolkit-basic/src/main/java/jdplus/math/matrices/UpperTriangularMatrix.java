/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices;

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
public class UpperTriangularMatrix {


    public void randomize(Matrix M, RandomNumberGenerator rng) {
        M.set((r, c) -> (r < c) ? 0 : rng.nextDouble());
    }

    public void rsolve(final Matrix U, final Matrix B) throws MatrixException {
        int nr = B.getRowsCount(), nc = B.getColumnsCount();
        if (nr != U.getRowsCount()) {
            throw new MatrixException(MatrixException.DIM);
        }
        double[] pb = B.getStorage();
        int start = B.getStartPosition(), lda = B.getColumnIncrement();
        int bmax = start + nc * lda;
        for (int b = start; b < bmax; b += lda) {
            TRSV.Usolve(U, pb, b, 1);
        }
    }

    public void lsolve(final Matrix U, final Matrix B) throws MatrixException {
        int nc = B.getColumnsCount();
        if (nc != U.getRowsCount()) {
            throw new MatrixException(MatrixException.DIM);
        }
        DataBlockIterator rows = B.rowsIterator();
        while (rows.hasNext()) {
            DataBlock c = rows.next();
            TRSV.Utsolve(U, c.getStorage(), c.getStartPosition(), c.getIncrement());
        }
    }

    /**
     * Computes r = U*r The method right-multiplies the matrix by a vector.The
     * Length of the vector must be equal or less than the number of rows of the
     * matrix. The multiplier is modified in place. Column version
     *
     * @param U The upper triangular matrix
     * @param r An array of double
     */
    public void rmul(Matrix U, DataBlock r) {
        TRMV.Ux(U, r.getStorage(), r.getStartPosition(), r.getIncrement());
    }

    /**
     * Computes l = l*U The method left-multiplies the matrix by a vector.The
     * Length of the vector must be equal to the number of rows of the matrix.
     * The multiplier is modified in place. Column version
     *
     * @param U The upper triangular matrix
     * @param l An array of double
     */
    public void lmul(Matrix U, DataBlock l) {
        // l = l *U <=> U'l' = l'
        TRMV.Utx(U, l.getStorage(), l.getStartPosition(), l.getIncrement());
    }

    /**
     * B=U*B
     *
     * @param U
     * @param B
     */
    public void rmul(final Matrix U, final Matrix B) {
        int nr = B.getRowsCount(), nc = B.getColumnsCount();
        if (nr != U.getRowsCount()) {
            throw new MatrixException(MatrixException.DIM);
        }
        double[] pb = B.getStorage();
        int start = B.getStartPosition(), lda = B.getColumnIncrement();
        int bmax = start + nc * lda;
        for (int b = start; b < bmax; b += lda) {
            TRMV.Ux(U, pb, b, 1);
        }
    }

    /**
     * B=B*U
     *
     * @param U
     * @param B
     */
     public void lmul(final Matrix U, final Matrix B) {
        // B=B*U <=> U'B' = B'
        int nc = B.getColumnsCount();
        if (nc != U.getRowsCount()) {
            throw new MatrixException(MatrixException.DIM);
        }
        DataBlockIterator rows = B.rowsIterator();
        while (rows.hasNext()) {
            DataBlock c = rows.next();
            TRMV.Utx(U, c.getStorage(), c.getStartPosition(), c.getIncrement());
        }
    }

    public void rsolve(Matrix U, DataBlock x) throws MatrixException {
        TRSV.Usolve(U, x.getStorage(), x.getStartPosition(), x.getIncrement());
    }

    public void lsolve(Matrix U, DataBlock x) throws MatrixException {
        TRSV.Utsolve(U, x.getStorage(), x.getStartPosition(), x.getIncrement());
    }

    /**
     * Computes the inverse of a triangular matrix R = U^-1
     *
     * @param U The upper matrix being inverted
     * @return The inverse
     * @throws MatrixException when the matrix is non invertible (some elements
     * of the diagonal are 0).
     */
    public Matrix inverse(final Matrix U) throws MatrixException {
        int n = U.getRowsCount();
        Matrix IU = Matrix.identity(n);
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

    public void toUpper(Matrix M) {
        int m = M.getRowsCount(), n = M.getColumnsCount();
        if (m != n) {
            throw new MatrixException(MatrixException.SQUARE);
        }
        if (n == 1) {
            return;
        }
        double[] x = M.getStorage();
        int lda = M.getColumnIncrement(), start = M.getStartPosition();
        for (int c = n-1, id = start+1; c >0; id += lda+1, --c) {
            int imax=id+c;
            for (int iu = id; iu < imax; ++iu) {
                x[iu] = 0;
            }
        }
    }
}
