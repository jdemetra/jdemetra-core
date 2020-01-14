/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import jdplus.data.DataBlock;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class YPAX {

    public void apply(DataBlock y, double a, DoubleSeq x) {
        int inc = y.getIncrement();
        int n = y.length();
        if (inc == 1) {
            apply(y.getStorage(), y.getStartPosition(), n, a, x);
        } else {
            apply(y.getStorage(), y.getStartPosition(), n, inc, a, x);
        }
    }

    public void apply(DataBlock y, double a, DataBlock x) {
        if (a == 0) {
            return;
        }
//        int yinc = y.getIncrement(), xinc = x.getIncrement();
//        int ybeg = y.getStartPosition(), xbeg = x.getStartPosition(), yend = y.getEndPosition();
//        double[] y.getStorage() = y.getStorage(), px = x.getStorage();
        if (y.getIncrement() == 1) {
            if (x.getIncrement() == 1) {
                for (int i = y.getStartPosition(), j = x.getStartPosition(); i < y.getEndPosition(); ++i, ++j) {
                    y.getStorage()[i] += a * x.getStorage()[j];
                }
            } else {
                for (int i = y.getStartPosition(), j = x.getStartPosition(); i < y.getEndPosition(); ++i, j += x.getIncrement()) {
                    y.getStorage()[i] += a * x.getStorage()[j];
                }
            }
        } else {
            if (x.getIncrement() == 1) {
                for (int i = y.getStartPosition(), j = x.getStartPosition(); i != y.getEndPosition(); i += y.getIncrement(), ++j) {
                    y.getStorage()[i] += a * x.getStorage()[j];
                }
            } else {
                for (int i = y.getStartPosition(), j = x.getStartPosition(); i != y.getEndPosition(); i += y.getIncrement(), j += x.getIncrement()) {
                    y.getStorage()[i] += a * x.getStorage()[j];
                }
            }
        }
    }

    public void apply(double[] py, int start, int n, double a, DoubleSeq x) {
        if (n == 0 || a == 0) {
            return;
        }
        int end = start + n;
        DoubleSeqCursor xcur = x.cursor();
        if (a == 1) {
            for (int i = start; i < end; ++i) {
                py[i] += xcur.getAndNext();
            }
        } else if (a == -1) {
            for (int i = start; i < end; ++i) {
                py[i] = xcur.getAndNext();
            }
        } else {
            for (int i = start; i < end; ++i) {
                py[i] += a * xcur.getAndNext();
            }
        }
    }

    public void apply(double[] py, int start, int inc, int n, double a, DoubleSeq x) {
        if (n == 0 || a == 0) {
            return;
        }
        int end = start + n * inc;
        DoubleSeqCursor xcur = x.cursor();
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

    public void apply(double a, double[] px, int xstart, double[] py, int ystart, int n) {
        if (n == 0 || a == 0) {
            return;
        }
        int end = ystart + n;
        if (a == 1) {
            for (int i = ystart, j = xstart; i < end; ++i, ++j) {
                py[i] += px[j];
            }
        } else if (a == -1) {
            for (int i = ystart, j = xstart; i < end; ++i, ++j) {
                py[i] -= px[j];
            }
        } else {
            for (int i = ystart, j = xstart; i < end; ++i, ++j) {
                py[i] += a * px[j];
            }
        }

    }

    public void apply(double a, double[] px, int xstart, int xinc, double[] py, int ystart, int yinc, int n) {
        if (n == 0 || a == 0) {
            return;
        }
        int end = ystart + n * yinc;
        if (a == 1) {
            while (ystart != end) {
                py[ystart] += px[xstart];
                ystart += yinc;
                xstart += xinc;
            }
        } else if (a == -1) {
            while (ystart != end) {
                py[ystart] -= px[xstart];
                ystart += yinc;
                xstart += xinc;
            }
        } else {
            for (int i = ystart, j = xstart; i < end; i += yinc, j += xinc) {
                py[i] += a * px[j];
            }
        }
    }
}
