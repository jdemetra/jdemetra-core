/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices;

import jdplus.math.matrices.lapack.LAIC1;
import demetra.data.DoubleSeqCursor;
import demetra.math.Constants;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.data.LogSign;
import demetra.dstats.RandomNumberGenerator;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class UpperTriangularMatrix {

    public void randomize(FastMatrix M, RandomNumberGenerator rng) {
        M.set((r, c) -> (r < c) ? 0 : rng.nextDouble());
    }

    public int rank(FastMatrix U, double rcond) {
        return fastRank(U, rcond);
    }

    public int fastRank(FastMatrix U, double rcond) {
        return U.diagonal().count(x -> Math.abs(x) > rcond);
    }

    public int robustRank(FastMatrix U, double rcond) {
        DoubleSeqCursor.OnMutable cursor = U.diagonal().cursor();
        double smax = Math.abs(cursor.getAndNext()), smin = smax;
        if (smax == 0) {
            return 0;
        }
        int rank = 1;
        int n = U.getRowsCount();
        double[] xmin = new double[n], xmax = new double[n];
        xmin[0] = 1;
        xmax[0] = 1;
        LAIC1 cmax = new LAIC1(), cmin = new LAIC1();
        CPointer pxmax = new CPointer(xmax, 0), pxmin = new CPointer(xmin, 0);
        CPointer pw = new CPointer(U.getStorage(), U.getStartPosition());
        while (rank < n) {
            pw.move(U.getColumnIncrement());
            double urr = cursor.getAndNext();
            cmin.minSingularValue(rank, pxmin, smin, pw, urr);
            cmax.maxSingularValue(rank, pxmax, smax, pw, urr);
            double sminpr = cmin.getSestpr();
            double smaxpr = cmax.getSestpr();
            if (smaxpr * rcond > sminpr) {
                break;
            }
            for (int i = 0; i < rank - 1; ++i) {
                xmin[i] *= cmin.getS();
                xmax[i] *= cmax.getS();
            }
            xmin[rank] = cmin.getC();
            xmax[rank] = cmax.getC();
            smin = sminpr;
            smax = smaxpr;
            ++rank;
        }
        return rank;
    }

    /**
     * y := U*x or x = iU*y
     *
     * @param U
     * @param x
     * @param zero
     */
    public void solveUx(FastMatrix U, DataBlock x, double zero) {
        solveUx(U, x.getStorage(), x.getStartPosition(), x.getIncrement(), zero);
    }

    public void solveUx(FastMatrix U, DataBlock x) {
        solveUx(U, x.getStorage(), x.getStartPosition(), x.getIncrement(), 0);
    }

    /**
     * y := x*U or y' = U'*x or x = iUt*y
     *
     * @param U
     * @param x
     */
    public void solvexU(FastMatrix U, DataBlock x) {
        solveUtx(U, x.getStorage(), x.getStartPosition(), x.getIncrement(), 0);
    }

    /**
     * y := x*U or y' = U'*x or x = iUt*y
     *
     * @param U
     * @param x
     * @param zero
     */
    public void solvexU(FastMatrix U, DataBlock x, double zero) {
        solveUtx(U, x.getStorage(), x.getStartPosition(), x.getIncrement(), zero);
    }

    /**
     * Solves the system X*U=B
     *
     * @param U
     * @param B
     * @throws MatrixException
     */
    public void solveXU(final FastMatrix U, final FastMatrix B) throws MatrixException {
        solveXU(U, B, 0);
    }

    /**
     * Solves the system X*U=B for (quasi) singular matrices X*U=M or X = M*iU
     * (iU = U^-1) or Z = iUt*N or Ut*Z=N
     *
     * @param U
     * @param B
     * @param zero
     * @throws MatrixException
     */
    public void solveXU(final FastMatrix U, final FastMatrix B, double zero) throws MatrixException {
        int nc = B.getColumnsCount();
        if (nc != U.getRowsCount()) {
            throw new MatrixException(MatrixException.DIM);
        }
        DataBlockIterator rows = B.rowsIterator();
        while (rows.hasNext()) {
            DataBlock r = rows.next();
            solveUtx(U, r.getStorage(), r.getStartPosition(), r.getIncrement(), zero);
        }
    }

    public void solveUX(final FastMatrix U, final FastMatrix M) throws MatrixException {
        solveUX(U, M, 0);
    }

    public void solveUX(final FastMatrix U, final FastMatrix M, double zero) throws MatrixException {
        int nr = M.getRowsCount(), nc = M.getColumnsCount();
        if (nr != U.getRowsCount()) {
            throw new MatrixException(MatrixException.DIM);
        }
        double[] pb = M.getStorage();
        int start = M.getStartPosition(), lda = M.getColumnIncrement();
        int bmax = start + nc * lda;
        for (int b = start; b < bmax; b += lda) {
            solveUx(U, pb, b, 1, zero);
        }
    }

    /**
     * X*U'=M or U*X'=M' or X' = iU*M' or Z = iU*N
     *
     * @param U
     * @param M
     * @param zero
     * @throws MatrixException
     */
    public void solveXUt(final FastMatrix U, final FastMatrix M, double zero) throws MatrixException {
        int nr = M.getColumnsCount();
        if (nr != U.getRowsCount()) {
            throw new MatrixException(MatrixException.DIM);
        }
        DataBlockIterator rows = M.rowsIterator();
        while (rows.hasNext()) {
            DataBlock r = rows.next();
            solveUx(U, r.getStorage(), r.getStartPosition(), r.getIncrement(), zero);
        }
    }

    /**
     *
     * @param U
     * @param M
     * @throws MatrixException
     */
    public void solveXUt(final FastMatrix U, final FastMatrix M) throws MatrixException {
        solveXUt(U, M, 0);
    }

    /**
     * X'U=M' or U'X=M or X = iUtM
     *
     * @param U
     * @param M
     * @param zero
     * @throws MatrixException
     */
    public void solveUtX(final FastMatrix U, final FastMatrix M, double zero) throws MatrixException {
        int nr = M.getRowsCount();
        if (nr != U.getRowsCount()) {
            throw new MatrixException(MatrixException.DIM);
        }
        DataBlockIterator cols = M.columnsIterator();
        while (cols.hasNext()) {
            DataBlock c = cols.next();
            solveUtx(U, c.getStorage(), c.getStartPosition(), 1, zero);
        }
    }

    public void solveUtX(final FastMatrix U, final FastMatrix M) throws MatrixException {
        solveUtX(U, M, 0);
    }

    /**
     * x := U*x
     *
     * @param U
     * @param x
     */
    public void Ux(FastMatrix U, DataBlock x) {
        int incx = x.getIncrement();
        if (incx == 1) {
            Ux(U, x.getStorage(), x.getStartPosition());
        } else {
            Ux(U, x.getStorage(), x.getStartPosition(), x.getIncrement());
        }
    }

    /**
     * x := x*U or z := U'z with z=x'
     *
     * @param U
     * @param x
     */
    public void xU(FastMatrix U, DataBlock x) {
        int incx = x.getIncrement();
        if (incx == 1) {
            Utx(U, x.getStorage(), x.getStartPosition());
        } else {
            Utx(U, x.getStorage(), x.getStartPosition(), x.getIncrement());
        }
    }

    /**
     * M := U*M
     *
     * @param U
     * @param M
     */
    public void UM(FastMatrix U, FastMatrix M) {
        int mstart = M.getStartPosition(), mlda = M.getColumnIncrement(), n = M.getColumnsCount();
        double[] pm = M.getStorage();
        int cmax = mstart + mlda * n;
        for (int c = mstart; c < cmax; c += mlda) {
            Ux(U, pm, c);
        }
    }

    /**
     * M := M*U or U'N = N (N = M')
     *
     * @param U
     * @param M
     */
    public void MU(FastMatrix U, FastMatrix M) {
        int mstart = M.getStartPosition(), mlda = M.getColumnIncrement(), m = M.getRowsCount();
        double[] pm = M.getStorage();
        int cmax = mstart + m;
        for (int c = mstart; c < cmax; ++c) {
            Utx(U, pm, c, mlda);
        }
    }

    /**
     * M := U'*M
     *
     * @param U
     * @param M
     */
    public void UtM(FastMatrix U, FastMatrix M) {
        DataBlockIterator cols = M.columnsIterator();
        while (cols.hasNext()) {
            DataBlock c = cols.next();
            Utx(U, c.getStorage(), c.getStartPosition());
        }
    }

    /**
     * M := M*U' or UN = N (N = M')
     *
     * @param U
     * @param M
     */
    public void MUt(FastMatrix U, FastMatrix M) {
        DataBlockIterator rows = M.rowsIterator();
        while (rows.hasNext()) {
            DataBlock r = rows.next();
            Ux(U, r.getStorage(), r.getStartPosition(), r.getIncrement());
        }
    }

    private void Ux(FastMatrix U, double[] px, int startx) {
        int n = U.getColumnsCount(), lda = U.getColumnIncrement(), start = U.getStartPosition();
        double[] pu = U.getStorage();
        int xend = startx + n;
        for (int ixj = startx, u0 = start; ixj < xend; ++ixj, u0 += lda) {
            double xcur = px[ixj];
            if (xcur != 0) {
                int iu = u0;
                for (int ix = startx; ix < ixj; ++ix) {
                    px[ix] += xcur * pu[iu++];
                }
                px[ixj] = xcur * pu[iu];
            }
        }
    }

    private void Ux(FastMatrix U, double[] px, int startx, int incx) {
        int n = U.getColumnsCount(), lda = U.getColumnIncrement(), start = U.getStartPosition();
        double[] pu = U.getStorage();
        int xend = startx + n * incx;
        for (int ixj = startx, u0 = start; ixj != xend; ixj += incx, u0 += lda) {
            double xcur = px[ixj];
            if (xcur != 0) {
                int iu = u0;
                for (int ix = startx; ix != ixj; ix += incx) {
                    px[ix] += xcur * pu[iu++];
                }
                px[ixj] = xcur * pu[iu];
            }
        }
    }

    private void Utx(FastMatrix U, double[] px, int startx) {
        int n = U.getColumnsCount(), lda = U.getColumnIncrement(), start = U.getStartPosition();
        double[] pu = U.getStorage();
        int xend = startx + n - 1, uend = start + (n - 1) * (lda + 1);
        for (int ixj = xend, u0 = uend; ixj >= startx; u0 -= lda, --ixj) {
            double tmp = px[ixj] * pu[u0--];
            for (int ix = ixj - 1, iu = u0; ix >= startx; --ix, --iu) {
                tmp += px[ix] * pu[iu];
            }
            px[ixj] = tmp;
        }
    }

    private void Utx(FastMatrix U, double[] px, int startx, int incx) {
        int n = U.getColumnsCount(), lda = U.getColumnIncrement(), start = U.getStartPosition();
        double[] pu = U.getStorage();
        int xend = startx + n * incx, uend = start + (n - 1) * (lda + 1);
        for (int ixj = xend, u0 = uend; ixj != startx; u0 -= lda) {
            ixj -= incx;
            double tmp = px[ixj] * pu[u0--];
            int iu = u0;
            int ix = ixj;
            while (ix != startx) {
                ix -= incx;
                tmp += px[ix] * pu[iu--];
            }
            px[ixj] = tmp;
        }
    }

    public void solveUtx(FastMatrix U, double[] px, int startx, int incx, double zero) {
        int n = U.getColumnsCount(), lda = U.getColumnIncrement(), start = U.getStartPosition();
        double[] pu = U.getStorage();
        if (incx == 1) {
            int xend = startx + n;
            for (int ix = startx, il = start; ix < xend; ++ix, il += lda) {
                double t = px[ix];
                int jl = il;
                for (int jx = startx; jx < ix; ++jx, ++jl) {
                    t -= px[jx] * pu[jl];
                }
                double d = pu[jl];
                if (Math.abs(d) <= zero) {
                    if (Math.abs(t) >= zero) { // if zero=0, an exception is always thrown
                        throw new MatrixException(MatrixException.SINGULAR);
                    } else {
                        px[ix] = 0;
                    }
                } else {
                    px[ix] = t / d;
                }
            }
        } else {
            int xend = startx + n * incx;
            for (int ix = startx, il = start; ix < xend; ix += incx, il += lda) {
                double t = px[ix];
                int jl = il;
                for (int jx = startx; jx < ix; jx += incx, ++jl) {
                    t -= px[jx] * pu[jl];
                }
                double d = pu[jl];
                if (Math.abs(d) <= zero) {
                    if (Math.abs(t) >= zero) { // if zero=0, an exception is always thrown
                        throw new MatrixException(MatrixException.SINGULAR);
                    } else {
                        px[ix] = 0;
                    }
                } else {
                    px[ix] = t / d;
                }
            }
        }
    }

    public void solveUx(FastMatrix U, double[] px, int startx, int incx, double zero) {
        int n = U.getColumnsCount(), lda = U.getColumnIncrement(), start = U.getStartPosition();
        double[] pu = U.getStorage();
        if (incx == 1) {
            int xend = startx + n - 1;
            for (int jx = xend, ju = start + (n - 1) * (lda + 1); jx >= startx; --jx, ju -= lda + 1) {
                double t = px[jx];
                double d = pu[ju];
                if (Math.abs(d) <= zero) {
                    if (Math.abs(t) >= zero) {
                        throw new MatrixException(MatrixException.SINGULAR);
                    } else {
                        px[jx] = 0;
                    }
                } else {
                    double c = t / d;
                    px[jx] = c;
                    for (int ix = jx - 1, il = ju - 1; ix >= startx; --ix, --il) {
                        px[ix] -= c * pu[il];
                    }
                }
            }
        } else {
            int xend = startx + n * incx;
            for (int jx = xend, ju = start + (n - 1) * (lda + 1); jx != startx; ju -= lda + 1) {
                jx -= incx;
                double t = px[jx];
                double d = pu[ju];
                if (Math.abs(d) <= zero) {
                    if (Math.abs(t) >= zero) {
                        throw new MatrixException(MatrixException.SINGULAR);
                    } else {
                        px[jx] = 0;
                    }
                } else {
                    double c = t / d;
                    px[jx] = c;
                    for (int ix = jx, il = ju - 1; ix != startx; --il) {
                        ix -= incx;
                        px[ix] -= c * pu[il];
                    }
                }
            }
        }
    }

    /**
     * Set 0 to the lower part of a matrix
     *
     * @param M
     */
    public void toUpper(FastMatrix M) {
        int m = M.getRowsCount(), n = M.getColumnsCount();
        if (m != n) {
            throw new MatrixException(MatrixException.SQUARE);
        }
        if (n == 1) {
            return;
        }
        double[] x = M.getStorage();
        int lda = M.getColumnIncrement(), start = M.getStartPosition();
        for (int c = n - 1, id = start + 1; c > 0; id += lda + 1, --c) {
            int imax = id + c;
            for (int iu = id; iu < imax; ++iu) {
                x[iu] = 0;
            }
        }
    }

    /**
     * Computes the inverse of a triangular matrix R = U^-1
     *
     * @param U The triangular matrix being inverted
     * @return The inverse
     * @throws MatrixException when the matrix is non invertible (some elements
     * of the diagonal are 0).
     */
    public FastMatrix inverse(final FastMatrix U) throws MatrixException {
        if (U.diagonal().anyMatch(x -> x == 0)) {
            throw new MatrixException(MatrixException.SINGULAR);
        }
        int n = U.getRowsCount();
        if (n != U.getColumnsCount()) {
            throw new MatrixException(MatrixException.SQUARE);
        }

        FastMatrix IU = FastMatrix.identity(n);
        solveUX(U, IU);
        return IU;
    }

    public LogSign logDeterminant(FastMatrix U) {
        return LogSign.of(U.diagonal());
    }

    public double determinant(FastMatrix U) {
        LogSign ls = logDeterminant(U);
        if (ls == null) {
            return 0;
        }
        double val = Math.exp(ls.getValue());
        return ls.isPositive() ? val : -val;
    }

}
