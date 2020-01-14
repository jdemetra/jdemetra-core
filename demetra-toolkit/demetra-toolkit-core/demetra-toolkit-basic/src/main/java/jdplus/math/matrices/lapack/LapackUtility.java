/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.math.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class LapackUtility {

    /**
     * Computes A=A+alpha*x*y'
     *
     * @param A
     * @param alpha
     * @param x
     * @param y
     */
    public void dger(Matrix A, double alpha, DataBlock x, DataBlock y) {
        // Quick return if possible
        if (A.isEmpty() || alpha == 0) {
            return;
        }
        // operate by columns
        DataBlockIterator columns = A.columnsIterator();
        DoubleSeqCursor.OnMutable cursor = y.cursor();
        while (columns.hasNext()) {
            double tmp = cursor.getAndNext();
            DataBlock col = columns.next();
            if (tmp != 0) {
                col.addAY(tmp * alpha, x);
            }
        }
    }

    public double dasum(int n, double[] dx, int startx, int incx) {
        double rslt = 0;
        if (n <= 0 || incx < 0) {
            return 0;
        }
        if (incx == 1) {
            int imax = startx + n;
            for (int i = startx; i < imax; ++i) {
                rslt += Math.abs(dx[i]);
            }
        } else {
            int i1 = startx + n * incx;
            for (int i = startx; i < i1; i += incx) {
                rslt += Math.abs(dx[i]);
            }
        }
        return rslt;
    }

    public double asum(DoubleSeq x) {
        int n = x.length();
        if (n <= 0) {
            return 0;
        }
        double rslt = 0;
        DoubleSeqCursor cursor = x.cursor();
        for (int i = 0; i < n; ++i) {
            rslt += Math.abs(cursor.getAndNext());
        }
        return rslt;
    }

    /**
     * Computes the norm 2 of an array.
     *
     * @param n
     * @param dx
     * @param startx
     * @param incx
     * @return
     */
    public double nrm2(int n, double[] dx, int startx, int incx) {
        if (n < 1) {
            return 0;
        } else if (n == 1) {
            return Math.abs(dx[startx]);
        } else {
            int imax = startx + n * incx;
            double scale = 0;
            double ssq = 1;
            for (int i = startx; i != imax; i += incx) {
                double xcur = dx[i];
                if (xcur != 0) {
                    double absxi = Math.abs(xcur);
                    if (scale < absxi) {
                        double tmp = scale / absxi;
                        ssq = 1 + ssq * tmp * tmp;
                        scale = absxi;
                    } else {
                        double tmp = absxi / scale;
                        ssq += tmp * tmp;
                    }
                }
            }
            return scale * Math.sqrt(ssq);
        }
    }

    public double nrm2(DoubleSeq seq) {
        int n = seq.length();

        if (n < 1) {
            return 0;
        } else if (n == 1) {
            return Math.abs(seq.get(0));
        } else {
            DoubleSeqCursor cursor = seq.cursor();
            double scale = 0;
            double ssq = 1;
            for (int i = 0; i < n; ++i) {
                double xcur = cursor.getAndNext();
                if (xcur != 0) {
                    double absxi = Math.abs(xcur);
                    if (scale < absxi) {
                        double tmp = scale / absxi;
                        ssq = 1 + ssq * tmp * tmp;
                        scale = absxi;
                    } else {
                        double tmp = absxi / scale;
                        ssq += tmp * tmp;
                    }
                }
            }
            return scale * Math.sqrt(ssq);
        }
    }

    /**
     * Computes sqrt(x, y);
     *
     * @param x
     * @param y
     * @return
     */
    public double lapy2(double x, double y) {
        if (Double.isNaN(x) || Double.isNaN(y)) {
            return Double.NaN;
        }
        double xabs = Math.abs(x), yabs = Math.abs(y);
        double w = Math.max(xabs, yabs);
        double z = Math.min(xabs, yabs);
        if (z == 0) {
            return w;
        } else {
            z /= w;
            return w * Math.sqrt(1 + z * z);
        }
    }

    /**
     * Computes
     *
     * A = A + alpha * x' * y
     *
     * @param A
     * @param alpha
     * @param px
     * @param startx
     * @param incx
     * @param py
     * @param starty
     * @param incy
     */
    public void dger(Matrix A, double alpha, double[] px, int startx, int incx, double[] py, int starty, int incy) {
        int m = A.getRowsCount(), n = A.getColumnsCount();
        if (m == 0 || n == 0 || alpha == 0) {
            return;
        }
        int start = A.getStartPosition(), lda = A.getColumnIncrement();
        double[] pa = A.getStorage();
        if (incx == 1) {
            for (int j = 0, jc = start, iy = starty; j < n; ++j, jc += lda, iy += incy) {
                double ycur = py[iy];
                if (ycur != 0) {
                    double tmp = alpha * ycur;
                    int imax = jc + m;
                    for (int ic = jc, ix = startx; ic < imax; ++ic, ++ix) {
                        pa[ic] += px[ix] * tmp;
                    }
                }
            }
        } else {
            for (int j = 0, jc = start, iy = starty; j < n; ++j, jc += lda, iy += incy) {
                double ycur = py[iy];
                if (ycur != 0) {
                    double tmp = alpha * ycur;
                    int imax = jc + m;
                    for (int ic = jc, ix = startx; ic < imax; ++ic, ix += incx) {
                        pa[ic] += px[ix] * tmp;
                    }
                }
            }
        }
    }

    /**
     * Computes
     *
     * A = A + alpha * x' * y
     *
     * @param A
     * @param alpha
     * @param x
     * @param y
     */
    public void ger(Matrix A, double alpha, DataBlock x, DataBlock y) {
        if (alpha == 0 || A.isEmpty()) {
            return;
        }
        DataBlockIterator cols = A.columnsIterator();
        DoubleSeqCursor.OnMutable cursor = y.cursor();
        while (cols.hasNext()) {
            double ycur = cursor.getAndNext();
            DataBlock c = cols.next();
            if (ycur != 0) {
                c.addAY(ycur, x);
            }
        }
    }

    public void axpy(double a, DoubleSeq X, DataBlock y) {
        int n = y.length();
        if (n == 0 || a == 0) {
            return;
        }
        DoubleSeqCursor xcur = X.cursor();
        double[] py = y.getStorage();
        int start = y.getStartPosition(), inc = y.getIncrement(), end = y.getEndPosition();
        if (inc == 1) {
            if (a == 1) {
                while (start != end) {
                    py[start++] += xcur.getAndNext();
                }
            } else if (a == -1) {
                while (start < end) {
                    py[start++] = xcur.getAndNext();
                }
            } else {
                while (start < end) {
                    py[start++] += a * xcur.getAndNext();
                }
            }
        } else {
            if (a == 1) {
                while (start != end) {
                    py[start] += xcur.getAndNext();
                    start += inc;
                }
            } else if (a == -1) {
                while (start != end) {
                    py[start] = xcur.getAndNext();
                    start += inc;
                }
            } else {
                while (start != end) {
                    py[start] += a * xcur.getAndNext();
                    start += inc;
                }
            }
        }
    }

}
