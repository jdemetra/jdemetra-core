/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.maths.matrices;

import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import demetra.data.DoubleSeq;
import demetra.design.Development;
import demetra.design.Unsafe;
import demetra.math.matrices.MatrixType;
import java.util.function.DoubleUnaryOperator;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public final class Matrix implements FastMatrix {

    private final double[] storage;
    private final int nrows, ncols;

    public static Matrix square(int n) {
        double[] data = new double[n * n];
        return new Matrix(data, n, n);
    }
    
    public static Matrix make(int nrows, int ncols) {
        double[] data = new double[nrows * ncols];
        return new Matrix(data, nrows, ncols);
    }
    
    public static Matrix of(MatrixType matrix) {
        if (matrix == null) {
            return null;
        }
        return new Matrix(matrix.toArray(), matrix.getRowsCount(), matrix.getColumnsCount());
    }

    public static Matrix identity(int n) {
        Matrix i = square(n);
        i.diagonal().set(1);
        return i;
    }
    
    public static Matrix diagonal(DoubleSeq d) {
        Matrix i = square(d.length());
        i.diagonal().copy(d);
        return i;
    }
    
    public static Matrix rowOf(DataBlock x) {
        return new Matrix(x.toArray(), 1, x.length());
    }
    
    public static Matrix columnOf(DataBlock x) {
        return new Matrix(x.toArray(), x.length(), 1);
    }
    
    /**
     * Creates a new instance of SubMatrix
     *
     * @param data
     * @param nrows
     * @param ncols
     */
    public Matrix(final double[] data, final int nrows, final int ncols) {
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
    Matrix(final int nrows, final int ncols) {
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
    public Matrix asCanonical() {
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
     * Gets a given skew-diagonal of the matrix (as opposed to the main
     * diagonal)
     *
     * @param pos The index of the skew-diagonal (in [0, max(rowsCount,
     * columnsCount)[).
     * @return The src blocks representing the skew-diagonal. Refers to the
     * actual src (changing the src block modifies the underlying matrix).
     */
    public DataBlock skewDiagonal(int pos) {
        if (pos < 0) {
            return null;
        }
        int nmax = Math.max(nrows, ncols);
        if (pos >= nmax) {
            return null;
        }

        int beg, inc = nrows - 1;
        int n;
        if (pos < nrows) {
            beg = pos;
            n = Math.min(pos + 1, ncols);
        } else {
            int rlast = nrows - 1;
            int col = pos - rlast;
            beg = rlast + nrows * (col); // cell (nrows-1, pos-(nrows-1)) 
            n = Math.min(nrows, ncols - col);
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
    public void add(Matrix M) {
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

    public void sub(Matrix M) {
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
    public final void addXY(final Matrix X, final Matrix Y) {
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
    public Matrix times(Matrix B) {
        Matrix AB = new Matrix(nrows, B.getColumnsCount());
        AB.addXY(this, B);
        return AB;
    }

    public Matrix plus(Matrix B) {
        Matrix AB = deepClone();
        AB.add(B);
        return AB;
    }

    public Matrix minus(Matrix B) {
        Matrix AB = deepClone();
        AB.sub(B);
        return AB;
    }
    //</editor-fold>

    @Override
    public String toString(String fmt){
        return FastMatrix.format(this, fmt);
    }
    
    @Override
    public String toString(){
        return FastMatrix.format(this);
    }
    
}
