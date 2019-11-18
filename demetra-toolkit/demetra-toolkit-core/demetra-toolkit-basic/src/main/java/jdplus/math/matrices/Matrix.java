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
package jdplus.math.matrices;

import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import demetra.data.DoubleSeq;
import demetra.design.Development;
import demetra.design.Unsafe;
import demetra.math.matrices.MatrixType;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.DoublePredicate;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;
import jdplus.data.DataWindow;
import jdplus.data.LogSign;
import jdplus.math.matrices.decomposition.Householder;
import org.checkerframework.checker.index.qual.Positive;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Development(status = Development.Status.Release)
public final class Matrix implements FastMatrix {

    public static Builder builder(final double[] storage) {
        return new Builder(storage);
    }

    public static class Builder {

        private final double[] storage;
        private int start = 0, lda;
        private int nrows, ncols;

        public Builder(double[] s) {
            this.storage = s;
        }

        public Builder start(@Positive final int start) {
            this.start = start;
            return this;
        }

        public Builder nrows(@Positive final int nrows) {
            this.nrows = nrows;
            return this;
        }

        public Builder ncolumns(@Positive final int ncols) {
            this.ncols = ncols;
            return this;
        }

        public Builder columnIncrement(@Positive final int lda) {
            this.lda = lda;
            return this;
        }

        public Matrix build() {
            if (lda != 0 && lda < nrows) {
                throw new MatrixException(MatrixException.DIM);
            }
            return new Matrix(storage, start, lda == 0 ? nrows : lda, nrows, ncols);
        }
    }

    private final double[] storage;
    private final int start, lda;
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
        this.start = 0;
        this.lda = nrows;
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
        this.start = 0;
        this.lda = nrows;
    }

    Matrix(double[] x, int start, int lda, int nrows, int ncols) {
        this.storage = x;
        this.start = start;
        this.lda = lda;
        this.nrows = nrows;
        this.ncols = ncols;
    }

    public Matrix deepClone() {
        return new Matrix(toArray(), nrows, ncols);
    }

    @Override
    public Matrix extract(int r0, int nr, int c0, int nc) {
        return new Matrix(storage, start + r0 + c0 * lda, lda, nr, nc);
    }

    public boolean isFull() {
        return start == 0 && lda == nrows;
    }

    public boolean isDiagonal(double zero) {
        if (ncols != nrows) {
            return false;
        }
        if (nrows == 1) {
            return true;
        }

        DataBlock diag = diagonal();
        DataWindow ldiag = diag.window();
        DataWindow udiag = diag.window();
        for (int i = 1; i < nrows; ++i) {
            if (!ldiag.slideAndShrink(1).isZero(zero)) {
                return false;
            }
            if (!udiag.slideAndShrink(lda).isZero(zero)) {
                return false;
            }
        }
        return true;
    }

    public boolean isIdentity() {
        if (ncols != nrows) {
            return false;
        }
        if (nrows == 1) {
            return get(0, 0) == 1;
        }
        if (diagonal().anyMatch(x -> x != 1)) {
            return false;
        }

        DataBlock diag = diagonal();
        DataWindow ldiag = diag.window();
        DataWindow udiag = diag.window();
        for (int i = 1; i < nrows; ++i) {
            if (ldiag.slideAndShrink(1).anyMatch(x -> x != 0)) {
                return false;
            }
            if (udiag.slideAndShrink(lda).anyMatch(x -> x != 0)) {
                return false;
            }
        }
        return true;
    }

    public boolean isDiagonal() {
        return isDiagonal(0);
    }

    public boolean isZero(double eps) {
        return test(x -> Math.abs(x) <= eps);
    }

    public boolean test(final DoublePredicate pred) {
        if (isEmpty()) {
            return true;
        }
        DataBlockIterator cols = columnsIterator();
        while (cols.hasNext()) {
            if (!cols.next().allMatch(pred)) {
                return false;
            }
        }
        return true;
    }

    public boolean isSymmetric(double eps) {
        if (nrows != ncols) {
            return false;
        }
        if (nrows == 1) {
            return true;
        }
        DataBlock diag = diagonal();
        DataWindow ldiag = diag.window();
        DataWindow udiag = diag.window();
        for (int i = 1; i < nrows; ++i) {
            if (!ldiag.slideAndShrink(1).allMatch(udiag.slideAndShrink(lda),
                    (x, y) -> Math.abs(x - y) <= eps)) {
                return false;
            }
        }
        return true;
    }

    public boolean isSymmetric() {
        return isSymmetric(0);
    }

    @Override
    public void copyTo(double[] buffer, int pos) {
        if (isFull()) {
            System.arraycopy(storage, 0, buffer, pos, storage.length);
        }
        int lmax = pos + nrows * ncols;
        for (int l = pos, i0 = start, i1 = start + nrows; l < lmax; i0 += lda, i1 += lda) {
            for (int k = i0; k < i1; ++k, ++l) {
                buffer[l] = storage[k];
            }
        }
    }

    @Override
    public double[] toArray() {
        if (isFull()) {
            return storage.clone();
        }

        double[] z = new double[nrows * ncols];
        for (int l = 0, i0 = start, i1 = start + nrows; l < z.length; i0 += lda, i1 += lda) {
            for (int k = i0; k < i1; ++k, ++l) {
                z[l] = storage[k];
            }
        }
        return z;
    }

    public void set(MatrixFunction fn) {
        for (int c = 0, k = start; c < ncols; ++c, k += lda) {
            for (int r = 0, l = k; r < nrows; ++r, ++l) {
                storage[l] = fn.apply(r, c);
            }
        }
    }

    public void set(double value) {
        if (isFull()) {
            for (int i = 0; i < storage.length; ++i) {
                storage[i] = value;
            }
        } else {
            for (int c = 0, k = start; c < ncols; ++c, k += lda) {
                for (int r = 0, l = k; r < nrows; ++r, ++l) {
                    storage[l] = value;
                }
            }
        }
    }

    /**
     *
     * @param row
     * @param col
     * @return
     */
    @Override
    public double get(final int row, final int col) {
        return storage[start + row + col * lda];
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
     * Position of the top-left cell
     *
     * @return
     */
    @Override
    public int getStartPosition() {
        return start;
    }

    /**
     * Position of the bottom-right cell
     *
     * @return
     */
    public final int getLastPosition() {
        return start + (nrows - 1) + (ncols - 1) * lda;
    }

    /**
     * Increment between two adjacent cells on the same row
     *
     * @return
     */
    @Override
    public int getColumnIncrement() {
        return lda;
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

        int beg, inc = lda - 1;
        int n;
        if (pos < nrows) {
            beg = start + pos;
            n = Math.min(pos + 1, ncols);
        } else {
            int rlast = nrows - 1;
            int col = pos - rlast;
            beg = start + rlast + lda * (col); // cell (nrows-1, pos-(nrows-1)) 
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
        storage[start + row + col * lda] = value;
    }

    public void set(final int row, final int col, DoubleSupplier fn) {
        storage[start + row + col * lda] = fn.getAsDouble();
    }

    public void add(final int row, final int col, final double value) {
        storage[start + row + col * lda] += value;
    }

    public void mul(final int row, final int col, final double value) {
        storage[start + row + col * lda] *= value;
    }

    public void copy(Matrix B) {
        if (nrows != B.nrows || ncols != B.ncols) {
            throw new MatrixException(MatrixException.DIM);
        }
        if (isFull() && B.isFull()) {
            System.arraycopy(B.storage, 0, storage, 0, storage.length);
        } else {
            int end = start + ncols * lda;
            for (int i0 = start, i1 = start + nrows, j0 = B.start; i0 < end; i0 += lda, i1 += lda, j0 += B.lda) {
                for (int k = i0, l = j0; k < i1; ++k, ++l) {
                    storage[k] = B.storage[l];
                }
            }
        }
    }

    public void copyTranspose(Matrix B) {
        if (nrows != B.ncols || ncols != B.nrows) {
            throw new MatrixException(MatrixException.DIM);
        }
        int end = start + ncols * lda;
        for (int i0 = start, i1 = start + nrows, j0 = B.start; i0 < end; i0 += lda, i1 += lda, ++j0) {
            for (int k = i0, l = j0; k < i1; ++k, l += B.lda) {
                storage[k] = B.storage[l];
            }
        }
    }

    /**
     * Return the sum of all the cells of the matrix
     *
     * @return
     */
    public double sum() {
        if (isEmpty()) {
            return 0;
        }
        double s = 0;
        if (isFull()) {
            for (int i = 0; i < storage.length; ++i) {
                s += storage[i];
            }
        } else {
            DataBlockIterator cols = columnsIterator();
            while (cols.hasNext()) {
                s += cols.next().sum();
            }
        }
        return s;
    }

    /**
     *
     * @param row
     * @param col
     * @param fn
     */
    @Override
    public void apply(final int row, final int col, final DoubleUnaryOperator fn) {
        int idx = start + row + col * lda;
        storage[idx] = fn.applyAsDouble(storage[idx]);
    }

    public void applyByRows(final Consumer<DataBlock> fn) {
        DataBlockIterator rows = rowsIterator();
        while (rows.hasNext()) {
            fn.accept(rows.next());
        }
    }

    public void applyByColumns(final Consumer<DataBlock> fn) {
        DataBlockIterator cols = columnsIterator();
        while (cols.hasNext()) {
            fn.accept(cols.next());
        }
    }

    public void applyByRows(final Matrix M, final BiConsumer<DataBlock, DataBlock> fn) {
        DataBlockIterator rows = rowsIterator();
        DataBlockIterator mrows = M.rowsIterator();
        while (rows.hasNext()) {
            fn.accept(rows.next(), mrows.next());
        }
    }

    public void applyByColumns(final Matrix M, final BiConsumer<DataBlock, DataBlock> fn) {
        DataBlockIterator cols = columnsIterator();
        DataBlockIterator mcols = M.columnsIterator();
        while (cols.hasNext()) {
            fn.accept(cols.next(), mcols.next());
        }
    }

    public Iterable<DataBlock> rows() {
        return () -> new Rows(this);
    }

    public Iterable<DataBlock> columns() {
        return () -> new Columns(this);
    }

    /**
     *
     * @param M
     */
    public void add(Matrix M) {
        if (nrows != M.nrows || ncols != M.ncols) {
            throw new MatrixException(MatrixException.DIM);
        }
        if (isFull() && M.isFull()) {
            for (int i = 0; i < storage.length; ++i) {
                storage[i] += M.storage[i];
            }
        } else {
            int end = start + ncols * lda;
            for (int i0 = start, i1 = start + nrows, j0 = M.start; i0 < end; i0 += lda, i1 += lda, j0 += M.lda) {
                for (int k = i0, l = j0; k < i1; ++k, ++l) {
                    storage[k] += M.storage[l];
                }
            }
        }
    }

    public void add(double d) {
        if (d != 0) {
            if (isFull()) {
                for (int i = 0; i < storage.length; ++i) {
                    storage[i] += d;
                }
            } else {
                int end = start + ncols * lda;
                for (int i0 = start, i1 = start + nrows; i0 < end; i0 += lda, i1 += lda) {
                    for (int k = i0; k < i1; ++k) {
                        storage[k] += d;
                    }
                }
            }
        }
    }

    public void sub(Matrix M) {
        if (nrows != M.nrows || ncols != M.ncols) {
            throw new MatrixException(MatrixException.DIM);
        }
        if (isFull() && M.isFull()) {
            for (int i = 0; i < storage.length; ++i) {
                storage[i] -= M.storage[i];
            }
        } else {
            int end = start + ncols * lda;
            for (int i0 = start, i1 = start + nrows, j0 = M.start; i0 < end; i0 += lda, i1 += lda, j0 += M.lda) {
                for (int k = i0, l = j0; k < i1; ++k, ++l) {
                    storage[k] -= M.storage[l];
                }
            }
        }
    }

    public void sub(double d) {
        if (d != 0) {
            if (isFull()) {
                for (int i = 0; i < storage.length; ++i) {
                    storage[i] -= d;
                }
            } else {
                int end = start + ncols * lda;
                for (int i0 = start, i1 = start + nrows; i0 < end; i0 += lda, i1 += lda) {
                    for (int c = 0; c < ncols; ++c, i0 += lda, i1 += lda) {
                        for (int k = i0; k < i1; ++k) {
                            storage[k] += d;
                        }
                    }
                }
            }
        }
    }

    public void mul(double d) {
        if (d == 0) {
            if (isFull()) {
                for (int i = 0; i < storage.length; ++i) {
                    storage[i] = 0;
                }
            } else {
                int end = start + ncols * lda;
                for (int i0 = start, i1 = start + nrows; i0 < end; i0 += lda, i1 += lda) {
                    for (int k = i0; k < i1; ++k) {
                        storage[k] = 0;
                    }
                }
            }
        } else if (d != 1) {
            if (isFull()) {
                for (int i = 0; i < storage.length; ++i) {
                    storage[i] *= d;
                }
            } else {
                int end = start + ncols * lda;
                for (int i0 = start, i1 = start + nrows; i0 < end; i0 += lda, i1 += lda) {
                    for (int k = i0; k < i1; ++k) {
                        storage[k] *= d;
                    }
                }
            }
        }
    }

    public void div(double d) {
        if (d == 0) {
            if (isFull()) {
                for (int i = 0; i < storage.length; ++i) {
                    storage[i] = Double.NaN;
                }
            } else {
                int end = start + ncols * lda;
                for (int i0 = start, i1 = start + nrows; i0 < end; i0 += lda, i1 += lda) {
                    for (int k = i0; k < i1; ++k) {
                        storage[k] = Double.NaN;
                    }
                }
            }
        } else if (d != 1) {
            if (isFull()) {
                for (int i = 0; i < storage.length; ++i) {
                    storage[i] /= d;
                }
            } else {
                int end = start + ncols * lda;
                for (int i0 = start, i1 = start + nrows; i0 < end; i0 += lda, i1 += lda) {
                    for (int k = i0; k < i1; ++k) {
                        storage[k] /= d;
                    }
                }
            }
        }
    }

    /**
     * This = This + X*Y
     *
     * @param X
     * @param Y
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
    public void addXaXt(final double a, final DataBlock x) {
        if (a == 0) {
            return;
        }
        double[] px = x.getStorage();
        int xinc = x.getIncrement();
        int x0 = x.getStartPosition(), x1 = x.getEndPosition();
        int nmax = start+(lda+1)*(nrows-1);

        // Raw gaxpy implementation
        for (int pos = start, ypos = x0; pos < nmax; ypos += xinc, pos+=lda) {
            double yc = a * px[ypos];
            if (yc != 0) {
                if (xinc == 1) {
                    for (int xpos = x0, cpos=pos; xpos < x1; ++cpos, ++xpos) {
                        storage[cpos] += yc * px[xpos];
                    }
                } else {
                    for (int xpos = x0, cpos=pos; xpos != x1; ++cpos, xpos += xinc) {
                        storage[cpos] += yc * px[xpos];
                    }
                }
            } 
        }
    }
    
    public void addAY(double alpha, Matrix Y, boolean transposey) {
        if (alpha == 0) {
            return;
        }
        
        DataBlockIterator cols = this.columnsIterator();
        DataBlockIterator ycols= transposey ? Y.rowsIterator() : Y.columnsIterator();
        while (cols.hasNext()){
            cols.next().addAY(alpha, ycols.next());
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

    public Matrix inv() {
        if (!isSquare()) {
            throw new IllegalArgumentException();
        }
        Householder hous = new Householder(this);
        if (!hous.isFullRank()) {
            return null;
        }

        Matrix I = Matrix.identity(getRowsCount());
        DataBlockIterator cols = I.columnsIterator();
        while (cols.hasNext()) {
            hous.solve(cols.next());
        }
        return I;
    }

    public static LogSign logDeterminant(Matrix X) {
        if (!X.isSquare()) {
            throw new IllegalArgumentException();
        }
        Householder hous = new Householder(X);
        if (!hous.isFullRank()) {
            return null;
        }
        return LogSign.of(hous.rdiagonal(false));
    }

    public static double determinant(Matrix X) {
        LogSign ls = logDeterminant(X);
        if (ls == null) {
            return 0;
        }
        double val = Math.exp(ls.getValue());
        return ls.isPositive() ? val : -val;
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

    public Matrix plus(double d) {
        Matrix AB = deepClone();
        AB.add(d);
        return AB;
    }

    public Matrix minus(double d) {
        Matrix AB = deepClone();
        AB.sub(d);
        return AB;
    }

    public Matrix times(double d) {
        Matrix AB = deepClone();
        AB.mul(d);
        return AB;
    }

    public Matrix dividedBy(double d) {
        Matrix AB = deepClone();
        AB.div(d);
        return AB;
    }

    //</editor-fold>
    /**
     * Shifts the matrix to the top-left corner. a(i,j) = a(i+n, j+n) for i in
     * [0, nrows-n[ and j in [0, ncols-n[ The cells that are not moved are not
     * modified
     *
     * @param n The displacement (n cells left and n cells up)
     */
    public void upLeftShift(final int n) {
        int del = (1 + lda) * n;
        for (int c = 0, i = start; c < ncols - n; ++c, i += lda) {
            for (int r = 0, j = i; r < nrows - n; ++r, j++) {
                storage[j] = storage[j + del];
            }
        }
    }

    /**
     * Shifts the matrix to the bottom-right corner a(i,j) = a(i-n, j-n) for i
     * in [n, nrows[ and j in [n, ncols[ The cells that are not moved are not
     * modified.
     *
     * @param n The displacement (n cells right and n cells down)
     */
    public void downRightShift(final int n) {
        int del = (1 + lda) * n;
        for (int c = n, i = start + (nrows - 1)
                + (ncols - 1) * lda; c < ncols; ++c, i -= lda) {
            for (int r = n, j = i; r < nrows; ++r, j--) {
                storage[j] = storage[j - del];
            }
        }
    }

    //<editor-fold defaultstate="collapsed" desc="matrix windows">
    /**
     * @return
     */
    public MatrixWindow all() {
        return new MatrixWindow(storage, start, nrows, ncols, lda);
    }

    /**
     * Top-reader empty sub-matrix. To be used with next(a,b)
     *
     * @return An empty sub-matrix
     */
    public MatrixWindow topLeft() {
        return new MatrixWindow(storage, start, 0, 0, lda);
    }

    /**
     * Top-reader sub-matrix
     *
     * @param nr Number of rows. Could be 0.
     * @param nc Number of columns. Could be 0.
     * @return A nr src nc sub-matrix
     */
    public MatrixWindow topLeft(int nr, int nc) {
        return new MatrixWindow(storage, start, nr, nc, lda);
    }

    /**
     * Top-reader sub-matrix
     *
     * @param nr Number of rows. Could be 0.
     * @return A nr src nc sub-matrix
     */
    public MatrixWindow top(int nr) {
        return new MatrixWindow(storage, start, nr, ncols, lda);
    }

    /**
     * Top-reader sub-matrix
     *
     * @param nc Number of columns. Could be 0.
     * @return A nr src nc sub-matrix
     */
    public MatrixWindow left(int nc) {
        return new MatrixWindow(storage, start, nrows, nc, lda);
    }

    /**
     * bottom-right sub-matrix.
     *
     * @return An empty sub-matrix
     */
    public MatrixWindow bottomRight() {
        int nstart = start + nrows + ncols * lda;
        return new MatrixWindow(storage, nstart, 0, 0, lda);
    }

    /**
     * Bottom-right sub-matrix
     *
     * @param nr Number of rows. Could be 0.
     * @param nc Number of columns. Could be 0.
     * @return A nr src nc sub-matrix
     */
    public MatrixWindow bottomRight(int nr, int nc) {
        int nstart = start + (nrows - nr) + (ncols - nc) * lda;
        return new MatrixWindow(storage, nstart, nr, nc, lda);
    }

    /**
     * Bottom sub-matrix
     *
     * @param nr Number of rows. Could be 0.
     * @return The last n rows
     */
    public MatrixWindow bottom(int nr) {
        return new MatrixWindow(storage, start + nrows - nr, nr, ncols, lda);
    }

    /**
     * right sub-matrix
     *
     * @param nc Number of columns. Could be 0.
     * @return The nc right columns
     */
    public MatrixWindow right(int nc) {
        return new MatrixWindow(storage, start + (ncols - nc) * lda, nrows, nc, lda);
    }

    public DataWindow top() {
        return DataWindow.windowOf(storage, start, start + ncols * lda, lda);
    }

    public DataWindow left() {
        return DataWindow.windowOf(storage, start, start + nrows, 1);
    }

    public DataWindow bottom() {
        int beg = start + (nrows - 1);
        return DataWindow.windowOf(storage, beg, beg + ncols * lda, lda);
    }

    public DataWindow right() {
        int beg = start + (ncols - 1) * lda;
        return DataWindow.windowOf(storage, beg, beg + nrows, 1);
    }

    //</editor-fold>    
    public String toString(String fmt) {
        return MatrixType.format(this, fmt);
    }

    @Override
    public String toString() {
        return MatrixType.format(this);
    }

}

class Rows implements Iterator<DataBlock> {

    private int pos;
    private final Matrix M;

    Rows(Matrix M) {
        pos = 0;
        this.M = M;
    }

    @Override
    public boolean hasNext() {
        return pos < M.getRowsCount();
    }

    @Override
    public DataBlock next() {
        return M.row(pos++);
    }
}

class Columns implements Iterator<DataBlock> {

    private int pos;
    private final Matrix M;

    Columns(Matrix M) {
        pos = 0;
        this.M = M;
    }

    @Override
    public boolean hasNext() {
        return pos < M.getColumnsCount();
    }

    @Override
    public DataBlock next() {
        return M.column(pos++);
    }
}