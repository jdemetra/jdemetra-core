/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths.matrices;

import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.data.DoubleSeq;
import demetra.design.Unsafe;
import java.util.function.DoubleUnaryOperator;

/**
 *
 * @author Jean Palate
 */
public final class CanonicalMatrix implements FastMatrix {

    private final double[] storage;
    private final int nrows, ncols;

    public static CanonicalMatrix square(int n) {
        double[] data = new double[n * n];
        return new CanonicalMatrix(data, n, n);
    }
    
    public static CanonicalMatrix make(int nrows, int ncols) {
        double[] data = new double[nrows * ncols];
        return new CanonicalMatrix(data, nrows, ncols);
    }
    
    public static CanonicalMatrix of(Matrix matrix) {
        if (matrix == null) {
            return null;
        }
        return new CanonicalMatrix(matrix.toArray(), matrix.getRowsCount(), matrix.getColumnsCount());
    }

    public static CanonicalMatrix identity(int n) {
        CanonicalMatrix i = square(n);
        i.diagonal().set(1);
        return i;
    }
    
    public static CanonicalMatrix diagonal(DoubleSeq d) {
        CanonicalMatrix i = square(d.length());
        i.diagonal().copy(d);
        return i;
    }
    
    public static CanonicalMatrix rowOf(DataBlock x) {
        return new CanonicalMatrix(x.toArray(), 1, x.length());
    }
    
    public static CanonicalMatrix columnOf(DataBlock x) {
        return new CanonicalMatrix(x.toArray(), x.length(), 1);
    }
    
    /**
     * Creates a new instance of SubMatrix
     *
     * @param data
     * @param nrows
     * @param ncols
     */
    public CanonicalMatrix(final double[] data, final int nrows, final int ncols) {
        this.storage = data;
        this.nrows = nrows;
        this.ncols = ncols;

    }

    /**
     * Creates a new instance of SubMatrix
     *
     * @param nrows
     * @param ncols
     */
    CanonicalMatrix(final int nrows, final int ncols) {
        this.storage = new double[nrows * ncols];
        this.nrows = nrows;
        this.ncols = ncols;

    }

    @Override
    public boolean isCanonical() {
        return true;
    }

    @Override
    @Unsafe
    public CanonicalMatrix asCanonical() {
        return this;
    }

    @Override
    public SubMatrix asSubMatrix() {
        return new SubMatrix(storage, 0, nrows, ncols, 1, nrows);
    }

    /**
     *
     * @param row
     * @param col
     * @return
     */
    @Override
    public double get(final int row, final int col) {
        return storage[row + col * nrows];
    }

    /**
     *
     * @return
     */
    @Override
    public int getColumnsCount() {
        return ncols;
    }

    /**
     *
     * @return
     */
    @Override
    public int getRowsCount() {

        return nrows;
    }

    @Unsafe
    @Override
    public double[] getStorage() {
        return storage;
    }

    /**
     * Position of the top-reader cell
     *
     * @return
     */
    @Override
    public int getStartPosition() {
        return 0;
    }

    /**
     *
     * @param row
     * @param col
     * @return
     */
    @Override
    public int getPosition(int row, int col) {
        return row + col * nrows;
    }

    @Override
    public int getLastPosition() {
        return storage.length - 1;
    }

    @Override
    public int getRowIncrement() {
        return 1;
    }

    @Override
    public int getColumnIncrement() {
        return nrows;
    }

    /**
     *
     * @param c
     * @return
     */
    @Override
    public DataBlock column(final int c) {
        int beg = c * nrows, end = beg + nrows;
        return DataBlock.of(storage, beg, end, 1);
    }

    /**
     *
     * @param r
     * @return
     */
    @Override
    public DataBlock row(final int r) {
        return DataBlock.of(storage, r, r + nrows * ncols, nrows);
    }

    @Override
    public DataBlock diagonal() {
        int n = Math.min(nrows, ncols), inc = 1 + nrows;
        return DataBlock.of(storage, 0, inc * n, inc);
    }

    /**
     *
     * @param pos
     * @return
     */
    @Override
    public DataBlock subDiagonal(int pos) {
        if (pos >= ncols) {
            return DataBlock.EMPTY;
        }
        if (-pos >= nrows) {
            return DataBlock.EMPTY;
        }
        int beg = 0, inc = 1 + nrows;
        int n;
        if (pos > 0) {
            beg += pos * nrows;
            n = Math.min(nrows, ncols - pos);
        } else if (pos < 0) {
            beg -= pos;
            n = Math.min(nrows + pos, ncols);
        } else {
            n = Math.min(nrows, ncols);
        }
        return DataBlock.of(storage, beg, beg + inc * n, inc);
    }

    /**
     *
     * @param row
     * @param col
     * @param value
     */
    @Override
    public void set(final int row, final int col, final double value) {
        storage[row + col * nrows] = value;
    }

    /**
     *
     * @param row
     * @param col
     * @param fn
     */
    @Override
    public void apply(final int row, final int col, final DoubleUnaryOperator fn) {
        int idx = row + col * nrows;
        storage[idx] = fn.applyAsDouble(storage[idx]);
    }

    @Override
    public final DataBlockIterator rowsIterator() {
        return new RCIterator(topOutside(), nrows, 1);
    }

    @Override
    public final DataBlockIterator reverseRowsIterator() {
        return new RCIterator(bottomOutside(), nrows, -1);
    }

    @Override
    public final DataBlockIterator columnsIterator() {
        return new RCIterator(leftOutside(), ncols, nrows);
    }

    @Override
    public final DataBlockIterator reverseColumnsIterator() {
        return new RCIterator(rightOutside(), ncols, -nrows);
    }

    DataBlock topOutside() {
        return DataBlock.of(storage, -1, storage.length - 1, nrows);
    }

    DataBlock leftOutside() {
        return DataBlock.of(storage, -nrows, 0, 1);
    }

    DataBlock bottomOutside() {
        return DataBlock.of(storage, nrows, nrows + storage.length, nrows);
    }

    DataBlock rightOutside() {
        return DataBlock.of(storage, storage.length, storage.length + nrows, 1);
    }

    private static class RCIterator extends DataBlockIterator {

        private RCIterator(final DataBlock start, int niter, int inc) {
            super(start, niter, inc);
        }
    }

    /////////////////////////
    public void add(CanonicalMatrix M) {
        for (int i = 0; i < storage.length; ++i) {
            storage[i] += M.storage[i];
        }
    }

    @Override
    public void add(FastMatrix M) {
        if (M.isCanonical())
            add(M.asCanonical());
        else
            FastMatrix.super.add(M);
    }

    @Override
    public void add(double d) {
        if (d != 0) {
            for (int i = 0; i < storage.length; ++i) {
                storage[i] += d;
            }
        }
    }

    public void sub(CanonicalMatrix M) {
        for (int i = 0; i < storage.length; ++i) {
            storage[i] -= M.storage[i];
        }
    }

    @Override
    public void sub(FastMatrix M) {
        if (M.isCanonical())
            sub(M.asCanonical());
        else
            FastMatrix.super.sub(M);
    }

    @Override
    public void sub(double d) {
        if (d != 0) {
            for (int i = 0; i < storage.length; ++i) {
                storage[i] -= d;
            }
        }
    }

    @Override
    public void mul(double d) {
        if (d == 0) {
            for (int i = 0; i < storage.length; ++i) {
                storage[i] = 0;
            }
        } else if (d != 1) {
            for (int i = 0; i < storage.length; ++i) {
                storage[i] *= d;
            }
        }
    }

    @Override
    public void div(double d) {
        if (d == 0) {
            for (int i = 0; i < storage.length; ++i) {
                storage[i] = Double.NaN;
            }
        } else if (d != 1) {
            for (int i = 0; i < storage.length; ++i) {
                storage[i] /= d;
            }
        }
    }
    
    @Override
    public void product(final FastMatrix lm, final FastMatrix rm) {
        if (lm.isCanonical() && rm.isCanonical()){
            addXY(lm.asCanonical(), rm.asCanonical());
        }else
            FastMatrix.super.product(lm, rm);
    }
    /**
     * This = This + X*Y
     *
     * @param X
     * @param Y
     * @param M
     */
    public final void addXY(final CanonicalMatrix X, final CanonicalMatrix Y) {
        // Raw gaxpy implementation
        int cmax = X.storage.length, rnrows = Y.nrows, nmax = storage.length;

        for (int cpos = 0, cend = nrows, rpos = 0; cpos < cmax; ++rpos) {
            for (int pos = 0, mpos = rpos; pos < nmax; mpos += rnrows) {
                double rc = Y.storage[mpos];
                if (rc != 0) {
                    int lpos = cpos;
                    while (lpos < cend) {
                        storage[pos++] += X.storage[lpos++] * rc;
                    }
                } else {
                    pos += nrows;
                }
            }
            cpos = cend;
            cend += nrows;
        }
    }

    /**
     * This = This + a X*X'. This matrix must be a square matrix
     *
     * @param a Scalar
     * @param x Array. Length equal to the number of rows of this matrix
     */
    @Override
    public void addXaXt(final double a, final DataBlock x) {
        if (a == 0) {
            return;
        }
        double[] px = x.getStorage();
        int xinc = x.getIncrement();
        int x0 = x.getStartPosition(), x1 = x.getEndPosition();
        int nmax = storage.length;

        // Raw gaxpy implementation
        for (int pos = 0, ypos = x0; pos < nmax; ypos += xinc) {
            double yc = a * px[ypos];
            if (yc != 0) {
                if (xinc == 1) {
                    for (int xpos = x0; xpos < x1; ++pos, ++xpos) {
                        storage[pos] += yc * px[xpos];
                    }
                } else {
                    for (int xpos = x0; xpos != x1; ++pos, xpos += xinc) {
                        storage[pos] += yc * px[xpos];
                    }
                }
            } else {
                pos += nrows;
            }
        }
    }

    public void addXYt(final DataBlock x, final DataBlock y) {
        double[] px = x.getStorage(), py = y.getStorage();
        int xinc = x.getIncrement(), yinc = y.getIncrement();
        int x0 = x.getStartPosition(), x1 = x.getEndPosition(), y0 = y.getStartPosition();
        int nmax = storage.length;

        // Raw gaxpy implementation
        for (int pos = 0, ypos = y0; pos < nmax; ypos += yinc) {
            double yc = py[ypos];
            if (yc != 0) {
                if (xinc == 1) {
                    for (int xpos = x0; xpos < x1; ++pos, ++xpos) {
                        storage[pos] += yc * px[xpos];
                    }
                } else {
                    for (int xpos = x0; xpos != x1; ++pos, xpos += xinc) {
                        storage[pos] += yc * px[xpos];
                    }
                }
            } else {
                pos += nrows;
            }
        }
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Operations">
    public CanonicalMatrix times(CanonicalMatrix B) {
        CanonicalMatrix AB = new CanonicalMatrix(nrows, B.getColumnsCount());
        AB.addXY(this, B);
        return AB;
    }

    public CanonicalMatrix plus(CanonicalMatrix B) {
        CanonicalMatrix AB = deepClone();
        AB.add(B);
        return AB;
    }

    public CanonicalMatrix minus(CanonicalMatrix B) {
        CanonicalMatrix AB = deepClone();
        AB.sub(B);
        return AB;
    }
    //</editor-fold>

    @Override
    public String toString(String fmt){
        return Matrix.format(this, fmt);
    }
    
    @Override
    public String toString(){
        return Matrix.format(this);
    }
    
}
