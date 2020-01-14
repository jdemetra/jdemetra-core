/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices;

import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.data.LogSign;
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

    /**
     * Solves the system X*L=B
     *
     * @param L
     * @param B
     * @throws MatrixException
     */
    public void solveXL(final Matrix L, final Matrix B) throws MatrixException {
        solveXL(L, B, 0);
    }

    /**
     * Solves the system X*L=B for (quasi) singular matrices X*L=M or X = M*iL
     * (iL = L^-1) or Z = iLt*N or Lt*Z=N
     *
     * @param L
     * @param B
     * @param zero
     * @throws MatrixException
     */
    public void solveXL(final Matrix L, final Matrix B, double zero) throws MatrixException {
        int nc = B.getColumnsCount();
        if (nc != L.getRowsCount()) {
            throw new MatrixException(MatrixException.DIM);
        }
        DataBlockIterator rows = B.rowsIterator();
        while (rows.hasNext()) {
            DataBlock r = rows.next();
            solveLtx(L, r.getStorage(), r.getStartPosition(), r.getIncrement(), zero);
        }
    }

    public void solveX1L(final Matrix L, final Matrix B) throws MatrixException {
        int nc = B.getColumnsCount();
        if (nc != L.getRowsCount()) {
            throw new MatrixException(MatrixException.DIM);
        }
        DataBlockIterator rows = B.rowsIterator();
        while (rows.hasNext()) {
            DataBlock r = rows.next();
            solveL1tx(L, r.getStorage(), r.getStartPosition(), r.getIncrement());
        }
    }

    /**
     * y := L*x or x = iL*y
     *
     * @param L
     * @param x
     * @param zero
     */
    public void solveLx(Matrix L, DataBlock x, double zero) {
        solveLx(L, x.getStorage(), x.getStartPosition(), x.getIncrement(), zero);
    }

    public void solveLx(Matrix L, DataBlock x) {
        solveLx(L, x.getStorage(), x.getStartPosition(), x.getIncrement(), 0);
    }

    public void solveL1x(Matrix L, DataBlock x) {
        solveL1x(L, x.getStorage(), x.getStartPosition(), x.getIncrement());
    }
    
    /**
     * y := x*L or y' = L'*x or x = iLt*y
     *
     * @param L
     * @param x
     */
    public void solvexL(Matrix L, DataBlock x) {
        solveLtx(L, x.getStorage(), x.getStartPosition(), x.getIncrement(), 0);
    }

    /**
     * y := x*L or y' = L'*x or x = iLt*y
     *
     * @param L L is a unitary lower triangular matrix (diag(L)=1)
     * @param x
     */
    public void solvexL1(Matrix L, DataBlock x) {
        solveL1tx(L, x.getStorage(), x.getStartPosition(), x.getIncrement());
    }
    /**
     * y := x*L or y' = L'*x or x = iLt*y
     *
     * @param L
     * @param x
     * @param zero
     */
    public void solvexL(Matrix L, DataBlock x, double zero) {
        solveLtx(L, x.getStorage(), x.getStartPosition(), x.getIncrement(), zero);
    }

    /**
     * X*L'=M or L*X'=M' or X' = iL*M' or Z = iL*N
     *
     * @param L
     * @param M
     * @param zero
     * @throws MatrixException
     */
    public void solveXLt(final Matrix L, final Matrix M, double zero) throws MatrixException {
        int nr = M.getColumnsCount();
        if (nr != L.getRowsCount()) {
            throw new MatrixException(MatrixException.DIM);
        }
        DataBlockIterator rows = M.rowsIterator();
        while (rows.hasNext()) {
            DataBlock r = rows.next();
            solveLx(L, r.getStorage(), r.getStartPosition(), r.getIncrement(), zero);
        }
    }

    /**
     *
     * @param L
     * @param M
     * @throws MatrixException
     */
    public void solveXLt(final Matrix L, final Matrix M) throws MatrixException {
        solveXLt(L, M, 0);
    }

    /**
     * X'L=M' or L'X=M or X = iLtM
     *
     * @param L
     * @param M
     * @param zero
     * @throws MatrixException
     */
    public void solveLtX(final Matrix L, final Matrix M, double zero) throws MatrixException {
        int nr = M.getRowsCount();
        if (nr != L.getRowsCount()) {
            throw new MatrixException(MatrixException.DIM);
        }
        DataBlockIterator cols = M.columnsIterator();
        while (cols.hasNext()) {
            DataBlock c = cols.next();
            solveLtx(L, c.getStorage(), c.getStartPosition(), 1, zero);
        }
    }

    public void solveLtX(final Matrix L, final Matrix M) throws MatrixException {
        solveLtX(L, M, 0);
    }

    public void solveLX(final Matrix L, final Matrix M) throws MatrixException {
        solveLX(L, M, 0);
    }

    public void solveLX(final Matrix L, final Matrix M, double zero) throws MatrixException {
        int nr = M.getRowsCount(), nc = M.getColumnsCount();
        if (nr != L.getRowsCount()) {
            throw new MatrixException(MatrixException.DIM);
        }
        double[] pb = M.getStorage();
        int start = M.getStartPosition(), lda = M.getColumnIncrement();
        int bmax = start + nc * lda;
        for (int b = start; b < bmax; b += lda) {
            LowerTriangularMatrix.solveLx(L, pb, b, 1, zero);
        }
    }

    public void solveL1X(final Matrix L, final Matrix M) throws MatrixException {
        int nr = M.getRowsCount(), nc = M.getColumnsCount();
        if (nr != L.getRowsCount()) {
            throw new MatrixException(MatrixException.DIM);
        }
        double[] pb = M.getStorage();
        int start = M.getStartPosition(), lda = M.getColumnIncrement();
        int bmax = start + nc * lda;
        for (int b = start; b < bmax; b += lda) {
            LowerTriangularMatrix.solveL1x(L, pb, b, 1);
        }
    }

    /**
     * x := L*x
     *
     * @param L
     * @param x
     */
    public void Lx(Matrix L, DataBlock x) {
        int incx = x.getIncrement();
        if (incx == 1) {
            Lx(L, x.getStorage(), x.getStartPosition());
        } else {
            Lx(L, x.getStorage(), x.getStartPosition(), x.getIncrement());
        }
    }

    /**
     * x := x*L' or z := L*z with z=x' This method is strictly the same as Lx
     *
     * @param L
     * @param x
     */
    /**
     * x := x*L or z := L'z with z=x'
     *
     * @param L
     * @param x
     */
    public void xL(Matrix L, DataBlock x) {
        int incx = x.getIncrement();
        if (incx == 1) {
            Ltx(L, x.getStorage(), x.getStartPosition());
        } else {
            Ltx(L, x.getStorage(), x.getStartPosition(), x.getIncrement());
        }
    }

    /**
     * M := L*M
     *
     * @param L
     * @param M
     */
    public void LM(Matrix L, Matrix M) {
        int mstart = M.getStartPosition(), mlda = M.getColumnIncrement(), n = M.getColumnsCount();
        double[] pm = M.getStorage();
        int cmax = mstart + mlda * n;
        for (int c = mstart; c < cmax; c += mlda) {
            Lx(L, pm, c);
        }
//        DataBlockIterator cols = M.columnsIterator();
//        while (cols.hasNext()) {
//            Lx(L, cols.next());
//        }
    }

    /**
     * M := M*L or L'N = N (N = M')
     *
     * @param L
     * @param M
     */
    public void ML(Matrix L, Matrix M) {
        int mstart = M.getStartPosition(), mlda = M.getColumnIncrement(), m = M.getRowsCount();
        double[] pm = M.getStorage();
        int cmax = mstart + m;
        for (int c = mstart; c < cmax; ++c) {
            Ltx(L, pm, c, mlda);
        }
    }

    /**
     * M := L'*M
     *
     * @param L
     * @param M
     */
    public void LtM(Matrix L, Matrix M) {
        DataBlockIterator cols = M.columnsIterator();
        while (cols.hasNext()) {
            DataBlock c = cols.next();
            Ltx(L, c.getStorage(), c.getStartPosition());
        }
    }

    /**
     * M := M*L' or LN = N (N = M')
     *
     * @param L
     * @param M
     */
    public void MLt(Matrix L, Matrix M) {
        DataBlockIterator rows = M.rowsIterator();
        while (rows.hasNext()) {
            DataBlock c = rows.next();
            Lx(L, c.getStorage(), c.getStartPosition(), c.getIncrement());
        }
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
        solveLX(L, IL);
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

    private void Lx(Matrix L, double[] px, int startx) {
        int n = L.getColumnsCount(), lda = L.getColumnIncrement(), start = L.getStartPosition();
        double[] pl = L.getStorage();
        int xend = startx + n;
        for (int li = start + (n - 1) * (lda + 1), xi = xend - 1; li >= start; li -= lda + 1, --xi) {
            double z = px[xi];
            if (z != 0) {
                px[xi] = pl[li] * z;
                for (int xj = xi + 1, lj = li + 1; xj < xend; ++xj, ++lj) {
                    px[xj] += pl[lj] * z;
                }
            }
        }
    }

    private void Lx(Matrix L, double[] px, int startx, int incx) {
        int n = L.getColumnsCount(), lda = L.getColumnIncrement(), start = L.getStartPosition();
        double[] pl = L.getStorage();
        int xend = startx + incx * n;
        for (int li = start + (n - 1) * (lda + 1), xi = xend - incx; li >= start; li -= lda + 1, xi -= incx) {
            double z = px[xi];
            if (z != 0) {
                px[xi] = pl[li] * z;
                for (int xj = xi + incx, idx = li + 1; xj != xend; xj += incx, ++idx) {
                    px[xj] += pl[idx] * z;
                }
            }
        }
    }

    private void Ltx(Matrix L, double[] px, int startx) {
        int n = L.getColumnsCount(), lda = L.getColumnIncrement(), start = L.getStartPosition();
        double[] pl = L.getStorage();
        int xend = startx + n;
        for (int ixj = startx, u0 = start; ixj < xend; ++ixj, u0 += lda + 1) {
            double tmp = px[ixj] * pl[u0];
            for (int ix = ixj + 1, iu = u0 + 1; ix < xend; ++ix, ++iu) {
                tmp += px[ix] * pl[iu];
            }
            px[ixj] = tmp;
        }
    }

    private void Ltx(Matrix L, double[] px, int startx, int incx) {
        int n = L.getColumnsCount(), lda = L.getColumnIncrement(), start = L.getStartPosition();
        double[] pl = L.getStorage();
        int xend = startx + n * incx, du = lda + 1;
        for (int ixj = startx, u0 = start; ixj != xend; ixj += incx, u0 += du) {
            double tmp = px[ixj] * pl[u0];
            for (int ix = ixj + incx, iu = u0 + 1; ix != xend; ix += incx, ++iu) {
                tmp += px[ix] * pl[iu];
            }
            px[ixj] = tmp;
        }
    }

    private void solveLx(Matrix L, double[] px, int startx, int incx, double zero) {
        int n = L.getColumnsCount(), lda = L.getColumnIncrement(), start = L.getStartPosition();
        double[] pl = L.getStorage();
        int dl = lda + 1;
        if (incx == 1) {
            int xend = startx + n;
            for (int ix = startx, il = start; ix < xend; ++ix, il += dl) {
                double t = px[ix];
                double d = pl[il];
                if (Math.abs(d) <= zero) {
                    if (Math.abs(t) >= zero) {
                        throw new MatrixException(MatrixException.SINGULAR);
                    } else {
                        px[ix] = 0;
                    }
                } else {
                    double c = t / d;
                    px[ix] = c;
                    for (int jx = ix + 1, jl = il + 1; jx < xend; ++jx, ++jl) {
                        px[jx] -= c * pl[jl];
                    }
                }
            }
        } else {
            int xend = startx + n * incx;
            for (int ix = startx, il = start; ix < xend; ix += incx, il += lda + 1) {
                double t = px[ix];
                double d = pl[il];
                if (Math.abs(d) <= zero) {
                    if (Math.abs(t) >= zero) {
                        throw new MatrixException(MatrixException.SINGULAR);
                    } else {
                        px[ix] = 0;
                    }
                } else {
                    double c = t / d;
                    px[ix] = c;
                    for (int jx = ix + incx, jl = il + 1; jx < xend; jx += incx, ++jl) {
                        px[jx] -= c * pl[jl];
                    }
                }
            }
        }
    }

    /**
     *
     * @param L L is a unit matrix (diag(L)=1)
     * @param px
     * @param startx
     * @param incx
     */
    private void solveL1x(Matrix L, double[] px, int startx, int incx) {
        int n = L.getColumnsCount(), lda = L.getColumnIncrement(), start = L.getStartPosition();
        double[] pl = L.getStorage();
        int dl = lda + 1;
        if (incx == 1) {
            int xend = startx + n;
            for (int ix = startx, il = start; ix < xend; ++ix, il += dl) {
                double t = px[ix];
                for (int jx = ix + 1, jl = il + 1; jx < xend; ++jx, ++jl) {
                    px[jx] -= t * pl[jl];
                }
            }
        } else {
            int xend = startx + n * incx;
            for (int ix = startx, il = start; ix < xend; ix += incx, il += lda + 1) {
                double t = px[ix];
                for (int jx = ix + incx, jl = il + 1; jx < xend; jx += incx, ++jl) {
                    px[jx] -= t * pl[jl];
                }
            }
        }
    }

    private void solveLtx(Matrix L, double[] px, int startx, int incx, double zero) {
        int n = L.getColumnsCount(), lda = L.getColumnIncrement(), start = L.getStartPosition();
        double[] pl = L.getStorage();
        if (incx == 1) {
            int xend = startx + n;
            for (int jx = startx + n - 1, jl = start + (n - 1) * (lda + 1); jx >= startx; --jx, jl -= lda + 1) {
                double t = px[jx];
                for (int ix = jx + 1, il = jl + 1; ix < xend; ++ix, ++il) {
                    t -= pl[il] * px[ix];
                }
                double d = pl[jl];
                if (Math.abs(d) <= zero) {
                    if (Math.abs(t) >= zero) { // if zero=0, an exception is always thrown
                        throw new MatrixException(MatrixException.SINGULAR);
                    } else {
                        px[jx] = 0;
                    }
                } else {
                    px[jx] = t / d;
                }
            }
        } else {
            int xend = startx + n * incx;
            for (int jx = xend, jl = start + (n - 1) * (lda + 1); jx != startx; jl -= lda + 1) {
                jx -= incx;
                double t = px[jx];
                for (int ix = jx + incx, il = jl + 1; ix != xend; ix += incx, ++il) {
                    t -= pl[il] * px[ix];
                }
                double d = pl[jl];
                if (Math.abs(d) <= zero) {
                    if (Math.abs(t) >= zero) { // if zero=0, an exception is always thrown
                        throw new MatrixException(MatrixException.SINGULAR);
                    } else {
                        px[jx] = 0;
                    }
                } else {
                    px[jx] = t / d;
                }
            }
        }
    }

    private void solveL1tx(Matrix L, double[] px, int startx, int incx) {
        int n = L.getColumnsCount(), lda = L.getColumnIncrement(), start = L.getStartPosition();
        double[] pl = L.getStorage();
        if (incx == 1) {
            int xend = startx + n;
            for (int jx = startx + n - 1, jl = start + (n - 1) * (lda + 1); jx >= startx; --jx, jl -= lda + 1) {
                double t = 0;
                for (int ix = jx + 1, il = jl + 1; ix < xend; ++ix, ++il) {
                    t += pl[il] * px[ix];
                }
                px[jx] -= t;
            }
        } else {
            int xend = startx + n * incx;
            for (int jx = xend, jl = start + (n - 1) * (lda + 1); jx != startx; jl -= lda + 1) {
                jx -= incx;
                double t = 0;
                for (int ix = jx + incx, il = jl + 1; ix != xend; ix += incx, ++il) {
                    t += pl[il] * px[ix];
                }
                px[jx] -= t;
            }
        }
    }
}
