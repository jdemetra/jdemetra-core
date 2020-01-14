/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

import jdplus.data.DataBlock;
import jdplus.math.matrices.Matrix;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class TRSV {

    /**
     * DTRSV solves one of the systems of equations
     *
     * A*x = b, or A**T*x = b,
     *
     * where b and x are n element vectors and A is an n by n upper or lower
     * triangular matrix.
     *
     * No test for singularity or near-singularity is included in this routine.
     * Such tests must be performed before calling this routine.
     *
     * @param U
     * @param x
     */
    public void Usolve(Matrix U, double[] px, int startx, int incx) {
        int n = U.getColumnsCount(), lda = U.getColumnIncrement(), start = U.getStartPosition();
        double[] pu = U.getStorage();
        if (incx == 1) {
            int xend = startx + n - 1;
            for (int jx = xend, ju = start + (n - 1) * (lda + 1); jx >= startx; --jx, ju -= lda + 1) {
                double tmp = px[jx];
                if (tmp != 0) {
                    tmp /= pu[ju];
                    px[jx] = tmp;
                    for (int ix = jx - 1, il = ju - 1; ix >= startx; --ix, --il) {
                        px[ix] -= tmp * pu[il];
                    }
                }
            }
        } else {
            int xend = startx + n * incx;
            for (int jx = xend, ju = start + (n - 1) * (lda + 1); jx != startx; ju -= lda + 1) {
                jx -= incx;
                double tmp = px[jx];
                if (tmp != 0) {
                    tmp /= pu[ju];
                    px[jx] = tmp;
                    for (int ix = jx, il = ju - 1; ix != startx; --il) {
                        ix -= incx;
                        px[ix] -= tmp * pu[il];
                    }
                }
            }
        }
    }

    /**
     * L * x = b
     *
     * @param L Lower triangular matrix
     * @param px On entry, contains b. On exit, contains the solution of the
     * system
     * @param startx
     * @param incx
     */
    public void Lsolve(Matrix L, double[] px, int startx, int incx) {
        int n = L.getColumnsCount(), lda = L.getColumnIncrement(), start = L.getStartPosition();
        double[] pl = L.getStorage();
        if (incx == 1) {
            int xend = startx + n;
            for (int ix = startx, il = start; ix < xend; ++ix, il += lda + 1) {
                double tmp = px[ix];
                if (tmp != 0) {
                    tmp /= pl[il];
                    px[ix] = tmp;
                    for (int jx = ix + 1, jl = il + 1; jx < xend; ++jx, ++jl) {
                        px[jx] -= tmp * pl[jl];
                    }
                }
            }
        } else {
            int xend = startx + n * incx;
            for (int ix = startx, il = start; ix < xend; ix += incx, il += lda + 1) {
                double tmp = px[ix];
                if (tmp != 0) {
                    tmp /= pl[il];
                    px[ix] = tmp;
                    for (int jx = ix + incx, jl = il + 1; jx < xend; jx += incx, ++jl) {
                        px[jx] -= tmp * pl[jl];
                    }
                }
            }
        }
    }

    public void Utsolve(Matrix U, double[] px, int startx, int incx) {
        int n = U.getColumnsCount(), lda = U.getColumnIncrement(), start = U.getStartPosition();
        double[] pu = U.getStorage();
        if (incx == 1) {
            int xend = startx + n;
            for (int ix = startx, il = start; ix < xend; ++ix, il += lda) {
                double tmp = px[ix];
                int jl = il;
                for (int jx = startx; jx < ix; ++jx, ++jl) {
                    tmp -= px[jx] * pu[jl];
                }
                tmp /= pu[jl];
                px[ix] = tmp;
            }
        } else {
            int xend = startx + n * incx;
            for (int ix = startx, il = start; ix < xend; ix += incx, il += lda) {
                double tmp = px[ix];
                int jl = il;
                for (int jx = startx; jx < ix; jx += incx, ++jl) {
                    tmp -= px[jx] * pu[jl];
                }
                tmp /= pu[jl];
                px[ix] = tmp;
            }
        }
    }

    /**
     * L' * x = b or x' L = b'
     *
     * @param L Lower triangular matrix
     * @param px On entry, contains b. On exit, contains the solution of the
     * system
     * @param startx
     * @param incx
     */
    public void Ltsolve(Matrix L, double[] px, int startx, int incx) {
        int n = L.getColumnsCount(), lda = L.getColumnIncrement(), start = L.getStartPosition();
        double[] pl = L.getStorage();
        if (incx == 1) {
            int xend = startx + n;
            for (int jx = startx + n - 1, jl = start + (n - 1) * (lda + 1); jx >= startx; --jx, jl -= lda + 1) {
                double tmp = px[jx];
                for (int ix = jx + 1, il = jl + 1; ix < xend; ++ix, ++il) {
                    tmp -= pl[il] * px[ix];
                }
                tmp /= pl[jl];
                px[jx] = tmp;
            }
        } else {
            int xend = startx + n * incx;
            for (int jx = xend, jl = start + (n - 1) * (lda + 1); jx != startx; jl -= lda + 1) {
                jx -= incx;
                double tmp = px[jx];
                for (int ix = jx + incx, il = jl + 1; ix != xend; ix += incx, ++il) {
                    tmp -= pl[il] * px[ix];
                }
                tmp /= pl[jl];
                px[jx] = tmp;
            }
        }
    }
}
