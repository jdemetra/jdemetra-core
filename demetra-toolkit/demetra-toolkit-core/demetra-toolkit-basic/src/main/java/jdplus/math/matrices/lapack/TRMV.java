/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

import jdplus.math.matrices.Matrix;

/**
 * Some parts have been modified to take into account performance issues in case
 * of decreasing indexes
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class TRMV {

    public void Ux(Matrix U, double[] px, int startx, int incx) {
        int n = U.getColumnsCount(), lda = U.getColumnIncrement(), start = U.getStartPosition();
        double[] pu = U.getStorage();
        if (incx == 1) {
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
        } else {
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
    }

    public void Lx(Matrix L, double[] px, int startx, int incx) {
        int n = L.getColumnsCount(), lda = L.getColumnIncrement(), start = L.getStartPosition();
        double[] pl = L.getStorage();
        if (incx == 1) {
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
        } else {
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
    }

    public void xU(Matrix U, double[] px, int startx, int incx) {
        Utx(U, px, startx, incx);
    }

    public void xL(Matrix L, double[] px, int startx, int incx) {
        Ltx(L, px, startx, incx);
    }

    public void Utx(Matrix U, double[] px, int startx, int incx) {
        int n = U.getColumnsCount(), lda = U.getColumnIncrement(), start = U.getStartPosition();
        double[] pu = U.getStorage();
        if (incx == 1) {
            int xend = startx + n - 1, uend = start + (n - 1) * (lda + 1);
            for (int ixj = xend, u0 = uend; ixj >= startx; u0 -= lda, --ixj) {
                double tmp = px[ixj] * pu[u0--];
                for (int ix = ixj - 1, iu=u0; ix >= startx; --ix, --iu) {
                    tmp += px[ix] * pu[iu];
                }
                px[ixj] = tmp;
            }
        } else {
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
    }

    public void Ltx(Matrix L, double[] px, int startx, int incx) {
        int n = L.getColumnsCount(), lda = L.getColumnIncrement(), start = L.getStartPosition();
        double[] pl = L.getStorage();
        if (incx == 1) {
            int xend = startx + n;
            for (int ixj = startx, u0 = start; ixj < xend; ++ixj, u0 += lda + 1) {
                double tmp = px[ixj] * pl[u0];
                for (int ix = ixj + 1, iu = u0 + 1; ix < xend; ++ix, ++iu) {
                    tmp += px[ix] * pl[iu];
                }
                px[ixj] = tmp;
            }
        } else {
            int xend = startx + n * incx;
            for (int ixj = startx, u0 = start; ixj != xend; ixj += incx, u0 += lda + 1) {
                double tmp = px[ixj] * pl[u0];
                for (int ix = ixj + incx, iu = u0 + 1; ix != xend; ix += incx, ++iu) {
                    tmp += px[ix] * pl[iu];
                }
                px[ixj] = tmp;
            }
        }
    }

}
