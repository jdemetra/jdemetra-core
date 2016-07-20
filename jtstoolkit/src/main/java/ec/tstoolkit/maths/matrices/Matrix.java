/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package ec.tstoolkit.maths.matrices;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.polynomials.UnitRoots;
import ec.tstoolkit.random.IRandomNumberGenerator;
import ec.tstoolkit.random.JdkRNG;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.IntToDoubleFunction;

/**
 * Represents matrices of doubles. Data are arranged by columns, following the
 * FORTRAN convention.
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class Matrix implements Cloneable {

    /**
     * @since 2.2
     */
    @FunctionalInterface
    public static interface MatrixFunction {

        /**
         * Applies this function to the given arguments.
         *
         * @param row
         * @param column
         * @return the function result
         */
        double apply(int row, int column);
    }

    /**
     * @since 2.2
     */
    @FunctionalInterface
    public static interface MatrixRelativeFunction {

        /**
         * Applies this function to the given arguments.
         *
         * @param row The row position
         * @param column The column position
         * @param cur The current value
         * @return the function result
         */
        double apply(int row, int column, double cur);
    }
    /**
     * Creates a diagonal matrix
     *
     * @param d The diagonal items (length = n)
     * @return The n src n diagonal matrix
     */
    public static Matrix diagonal(final double[] d) {
        Matrix M = new Matrix(d.length, d.length);
        for (int i = 0, j = 0; i < d.length; ++i, j += d.length + 1) {
            M.data_[j] = d[i];
        }
        return M;
    }

    public static Matrix diagonal(final IReadDataBlock d) {
        Matrix M = new Matrix(d.getLength(), d.getLength());
        M.diagonal().copy(d);
        return M;
    }

    // we don't have the "diff" function. So, we should define a similar one
    public static Matrix diff(int n, int lag, int d) {
        Polynomial D = UnitRoots.D(lag, d);
        int deg = lag * d;
        Matrix diff = new Matrix(n - deg, n);
        for (int i = 0; i <= deg; ++i) {
            diff.subDiagonal(i).set(D.get(deg - i));
        }
        return diff;
    }

    public static Matrix diff(int n, Polynomial D) {
        int deg = D.getDegree();
        Matrix diff = new Matrix(n - deg, n);
        for (int i = 0; i <= deg; ++i) {
            diff.subDiagonal(i).set(D.get(deg - i));
        }
        return diff;
    }

    /**
     * Creates the n src n identity matrix
     *
     * @param n The size of the matrix
     * @return The identity matrix (n src n)
     */
    public static Matrix identity(final int n) {
        Matrix M = new Matrix(n, n);
        for (int i = 0, j = 0; i < n; ++i, j += n + 1) {
            M.data_[j] = 1;
        }
        return M;
    }

    public static Matrix square(final int n) {
        return new Matrix(n, n);
    }

    public static Matrix select(SubMatrix m, boolean[] rsel, boolean[] csel) {
        int nr = 0, nc = 0;
        int rmax = Math.min(rsel.length, m.m_nrows);
        for (int i = 0; i < rmax; ++i) {
            if (rsel[i]) {
                ++nr;
            }
        }
        int cmax = Math.min(csel.length, m.m_ncols);
        for (int i = 0; i < cmax; ++i) {
            if (csel[i]) {
                ++nc;
            }
        }
        Matrix M = new Matrix(nr, nc);
        if (M.isEmpty()) {
            return M;
        }
        for (int c = 0, j = 0; c < cmax; ++c) {
            if (csel[c]) {
                DataBlock mc = m.column(c);
                for (int r = 0; r < rmax; ++r) {
                    if (rsel[r]) {
                        M.data_[j++] = mc.get(r);
                    }
                }
            }
        }
        return M;
    }

    public static Matrix select(SubMatrix m, int[] rsel, int[] csel) {
        int nr = rsel.length, nc = csel.length;
        Matrix M = new Matrix(nr, nc);
        if (M.isEmpty()) {
            return M;
        }
        for (int c = 0, j = 0; c < csel.length; ++c) {
            DataBlock mc = m.column(csel[c]);
            for (int r = 0; r < rsel.length; ++r) {
                M.data_[j++] = mc.get(rsel[r]);
            }
        }
        return M;
    }

    public static Matrix selectRows(SubMatrix m, boolean[] rsel) {
        int nr = 0;
        int rmax = Math.min(rsel.length, m.m_nrows);
        for (int i = 0; i < rmax; ++i) {
            if (rsel[i]) {
                ++nr;
            }
        }
        Matrix M = new Matrix(nr, m.getColumnsCount());
        if (M.isEmpty()) {
            return M;
        }
        DataBlockIterator rows = M.rows();
        DataBlock row = rows.getData();
        for (int r = 0; r < rmax; ++r) {
            if (rsel[r]) {
                row.copy(m.row(r));
                rows.next();
            }
        }
        return M;
    }

    public static Matrix selectRows(SubMatrix m, int[] rsel) {
        int nr = rsel.length;
        Matrix M = new Matrix(nr, m.getColumnsCount());
        if (M.isEmpty()) {
            return M;
        }
        DataBlockIterator rows = M.rows();
        DataBlock row = rows.getData();
        for (int r = 0; r < rsel.length; ++r) {
            row.copy(m.row(rsel[r]));
            rows.next();
        }
        return M;
    }

    public static Matrix selectColumns(SubMatrix m, boolean[] csel) {
        int nc = 0;
        int cmax = Math.min(csel.length, m.m_ncols);
        for (int i = 0; i < cmax; ++i) {
            if (csel[i]) {
                ++nc;
            }
        }
        Matrix M = new Matrix(m.getRowsCount(), nc);
        if (M.isEmpty()) {
            return M;
        }
        DataBlockIterator columns = M.columns();
        DataBlock column = columns.getData();
        for (int c = 0; c < cmax; ++c) {
            if (csel[c]) {
                column.copy(m.column(c));
                columns.next();
            }
        }
        return M;
    }

    public static Matrix selectColumns(SubMatrix m, int[] csel) {
        int nc = csel.length;
        Matrix M = new Matrix(m.getRowsCount(), nc);
        if (M.isEmpty()) {
            return M;
        }
        DataBlockIterator cols = M.columns();
        DataBlock col = cols.getData();
        for (int c = 0; c < csel.length; ++c) {
            col.copy(m.column(csel[c]));
            cols.next();
        }
        return M;
    }

    double[] data_;
    int nrows_, ncols_;
    private static final IRandomNumberGenerator RNG = JdkRNG.newRandom(0);

    /**
     * Creates a matrix from a given array of src. The new object is a wrapper
     * around the src. The parameters must be coherent: src.length = nrows src
     * ncols(not checked)
     *
     * @param data The src
     * @param nrows The number of rows
     * @param ncols The number of columns
     */
    public Matrix(final double[] data, final int nrows, final int ncols) {
        data_ = data;
        nrows_ = nrows;
        ncols_ = ncols;
    }

    /**
     * Creates a new matrix
     *
     * @param nrows The number of rows
     * @param ncols The number of columns
     */
    public Matrix(final int nrows, final int ncols) {
        data_ = new double[nrows * ncols];
        nrows_ = nrows;
        ncols_ = ncols;
    }

    /**
     * Creates a new matrix from a given sub-matrix. The src are independent of
     * the given sub-matrix
     *
     * @param sm The sub-matrix
     */
    public Matrix(final SubMatrix sm) {
        // if (sm == null)
        // throw new ArgumentNullException("sm");
        int nrows = sm.getRowsCount(), ncols = sm.getColumnsCount();
        data_ = new double[nrows * ncols];
        nrows_ = nrows;
        ncols_ = ncols;
        for (int isel = sm.m_start, c = 0, j = 0; c < sm.m_ncols; ++c, isel += sm.m_col_inc) {
            for (int sel = isel, r = 0; r < sm.m_nrows; sel += sm.m_row_inc, ++j, ++r) {
                data_[j] = sm.m_data[sel];
            }
        }
    }

    /**
     * X = X + r
     *
     * @param r The right operand
     */
    public void add(final double r) {
        int n = data_.length;
        for (int i = 0; i < n; ++i) {
            data_[i] += r;
        }
    }

    /**
     * X(row,col) = X(row,col) + val
     *
     * @param row The row index (in [0, rowsCount[)
     * @param col The column index (in [0, columnsCount[)
     * @param val The value being added
     */
    public void add(final int row, final int col, final double val) {
        data_[row + col * nrows_] += val;
    }

    /**
     * X = X + M
     * The matrices must have the same dimensions (not checked)
     *
     * @param M The right matrix.
     */
    public void add(final Matrix M) {
        /*
         * if (M == null) throw new ArgumentNullException("M"); if (ncols_ !=
         * M.ncols_ || nrows_ != M.nrows_) throw new
         * MatrixException(MatrixException.IncompatibleDimensions);
         */
        int n = data_.length;
        for (int i = 0; i < n; ++i) {
            data_[i] += M.data_[i];
        }
    }

    /**
     * X = -X Changes the sign of all elements
     */
    public void chs() {
        for (int i = 0; i < data_.length; ++i) {
            data_[i] = -data_[i];
        }
    }

    /**
     * Sets small values to 0.
     *
     * @param epsilon The threshold for small values
     */
    public void clean(double epsilon) {
        int n = data_.length;
        for (int i = 0; i < n; ++i) {
            double d = data_[i];
            if (-epsilon < d && d < epsilon) {
                data_[i] = 0;
            }
        }
    }

    /**
     * Sets all elements to 0.
     */
    public void clear() {
        int n = data_.length;
        for (int i = 0; i < n; ++i) {
            data_[i] = 0;
        }
    }

    @Override
    public Matrix clone() {
        try {
            Matrix m = (Matrix) super.clone();
            m.data_ = data_.clone();
            return m;
        } catch (CloneNotSupportedException err) {
            throw new AssertionError();
        }
    }

    /**
     * Gets a given column of the matrix
     *
     * @param col The index of the column (in [0, columnsCount[)
     * @return The src blocks representing the column. Refers to the actual src
     * (changing the src block modifies the underlying matrix).
     */
    public DataBlock column(final int col) {
        int beg = col * nrows_, end = beg + nrows_;
        return new DataBlock(data_, beg, end, 1);
    }

    /**
     * Gets the columns of the matrix as a list of src block
     *
     * @return The list of all the columns.
     */
    public List<DataBlock> columnList() {
        ArrayList<DataBlock> rc = new ArrayList<>();
        for (int i = 0; i < data_.length; i += nrows_) {
            rc.add(new DataBlock(data_, i, i + nrows_, 1));
        }
        return rc;
    }

    /**
     * Gets an iterator on all the columns of the matrix
     *
     * @return An iterator on all the columns
     */
    public DataBlockIterator columns() {
        return new DataBlockIterator(data_, 0, ncols_, nrows_, nrows_, 1);
    }

    /**
     * Copies the src of the matrix in a buffer. The src are copied column by
     * column.
     *
     * @param buffer The buffer
     * @param start The position in the buffer of the first cell of the matrix.
     */
    public void copyTo(final double[] buffer, final int start) {
        System.arraycopy(data_, 0, buffer, start, data_.length);
    }

    /**
     * Gets the diagonal of the matrix
     *
     * @return The src blocks representing the diagonal. Refers to the actual
     * src (changing the src block modifies the underlying matrix).
     */
    public DataBlock diagonal() {
        int n = Math.min(nrows_, ncols_), inc = nrows_ + 1;
        return new DataBlock(data_, 0, inc * n, inc);
    }

    /**
     * Gets a given sub-diagonal of the matrix
     *
     * @param pos The index of the sub-diagonal (in [-rowsCount, columnsCount[).
     * Positive values indicate diagonals above the main diagonal; negative
     * values indicate diagonals under the main diagonal.
     * @return The src blocks representing the sub-diagonal. Refers to the
     * actual src (changing the src block modifies the underlying matrix).
     */
    public DataBlock subDiagonal(int pos) {
        if (pos >= ncols_) {
            return DataBlock.EMPTY;
        }
        if (-pos >= nrows_) {
            return DataBlock.EMPTY;
        }
        int beg = 0, inc = 1 + nrows_;
        int n;
        if (pos > 0) {
            beg += pos * nrows_;
            n = Math.min(nrows_, ncols_ - pos);
        } else if (pos < 0) {
            beg -= pos;
            n = Math.min(nrows_ + pos, ncols_);
        } else {
            n = Math.min(nrows_, ncols_);
        }
        return new DataBlock(data_, beg, beg + inc * n, inc);
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
        int nmax = Math.max(nrows_, ncols_);
        if (pos >= nmax) {
            return null;
        }

        int beg, inc = nrows_ - 1;
        int n;
        if (pos < nrows_) {
            beg = pos;
            n = Math.min(pos + 1, ncols_);
        } else {
            int rlast = nrows_ - 1;
            int col = pos - rlast;
            beg = rlast + nrows_ * (col); // cell (nrows-1, pos-(nrows-1)) 
            n = Math.min(nrows_, ncols_ - col);
        }
        return new DataBlock(data_, beg, beg + inc * n, inc);
    }

    /**
     * Gets an element of the matrix
     *
     * @param row The 0-based row index
     * @param col The 0-based column index
     * @return X(row, col)
     */
    public double get(final int row, final int col) {
        return data_[row + col * nrows_];
    }

    /**
     * Gets the number of columns
     *
     * @return The number of columns (&gt 0).
     */
    public int getColumnsCount() {
        return ncols_;
    }

    /**
     * Gets the number of rows
     *
     * @return The number of rows (&gt 0)
     */
    public int getRowsCount() {
        return nrows_;
    }

    /**
     * Gets the underlying memory block
     *
     * @return The memory block that contains the src of the matrix (arranged by
     * columns). The direct use of the memory block should be reserved to
     * critical algorithms. Accessing the src using the different accessors
     * provided by the Matrix class is usually sufficient and much safer.
     */
    public double[] internalStorage() {
        return data_;
    }

    /**
     * Checks that all the elements of the matrix are nearly 0
     *
     * @param eps A small value.
     * @return True if all the elements are strictly smaller (in absolute value)
     * than the given eps.
     */
    public boolean isZero(double eps) {
        int n = data_.length;
        for (int i = 0; i < n; ++i) {
            double d = data_[i];
            if (d < -eps || d > eps) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks that the matrix is (quasi-)diagonal
     *
     * @param eps A small value.
     * @return True if all the elements are strictly smaller (in absolute value)
     * than the given eps.
     */
    public boolean isDiagonal(double eps) {
        if (ncols_ != nrows_) {
            return false;
        }
        int n = data_.length;
        int idx = 1;
        while (idx < n) {
            int end = idx + nrows_;

            for (int i = idx; i < end; ++i) {
                double d = data_[i];
                if (d < -eps || d > eps) {
                    return false;
                }
            }
            idx = end + 1;
        }
        return true;
    }

    public boolean isZero() {
        int n = data_.length;
        for (int i = 0; i < n; ++i) {
            if (data_[i] != 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isDiagonal() {
        if (ncols_ != nrows_) {
            return false;
        }
        int n = data_.length;
        int idx = 1;
        while (idx < n) {
            int end = idx + nrows_;
            for (int i = idx; i < end; ++i) {
                if (data_[i] != 0) {
                    return false;
                }
            }
            idx = end + 1;
        }
        return true;
    }

    public boolean isSquare() {
        return ncols_ == nrows_;
    }

    /**
     * Y = X - r
     *
     * @param r The right operand
     * @return A new matrix is returned
     */
    public Matrix minus(final double r) {
        // if (l == null)
        // throw new ArgumentNullException("l");
        Matrix s = new Matrix(getRowsCount(), getColumnsCount());
        int n = data_.length;
        for (int i = 0; i < n; ++i) {
            s.data_[i] = data_[i] - r;
        }
        return s;
    }

    /**
     * Z = X - Y
     *
     * @param Y The right operand. This matrix should have the same dimensions
     * as this matrix. However, that condition is not checked
     * @return A new matrix is returned
     */
    public Matrix minus(final Matrix Y) {
        Matrix s = new Matrix(getRowsCount(), getColumnsCount());
        int n = data_.length;
        for (int i = 0; i < n; ++i) {
            s.data_[i] = data_[i] - Y.data_[i];
        }
        return s;
    }

    /**
     * X = X * r
     *
     * @param r The right operand.
     */
    public void mul(final double r) {
        if (r == 1) {
            return;
        }
        if (r == 0) {
            clear();
        }
        int n = data_.length;
        for (int i = 0; i < n; ++i) {
            data_[i] *= r;
        }
    }

    /**
     * Multiplies a cell of the matrix by a given value X(row, col) = X(row,
     * col) * val
     *
     * @param row The 0-based row index
     * @param col The 0-based column index
     * @param val The multiplier
     */
    public void mul(final int row, final int col, final double val) {
        if (val == 1) {
            return;
        }
        data_[row + col * nrows_] *= val;
    }

    /**
     * The euclidian (frobenius) norm of the matrix
     *
     * @return sqrt(sum(src(i,j)*src(i,j))) is returned
     */
    public double nrm2() {
        return new DataBlock(data_).nrm2();
    }

    /**
     * Y = X + r
     *
     * @param r The right operand
     * @return A new matrix is returned
     */
    public Matrix plus(final double r) {
        // if (l == null)
        // throw new ArgumentNullException("l");
        Matrix s = new Matrix(getRowsCount(), getColumnsCount());
        int n = data_.length;
        for (int i = 0; i < n; ++i) {
            s.data_[i] = data_[i] + r;
        }
        return s;
    }

    /**
     * Z = X + Y
     *
     * @param Y The right operand. This matrix should have the same dimensions
     * as this matrix. However, that condition is not checked
     * @return A new matrix is returned
     */
    public Matrix plus(final Matrix Y) {
        /*
         * if (l == null) throw new ArgumentNullException("l"); if (r == null)
         * throw new ArgumentNullException("r"); if (l.ncols_ != r.ncols_ ||
         * l.nrows_ != r.nrows_) throw new
         * MatrixException(MatrixException.IncompatibleDimensions);
         */
        Matrix s = new Matrix(getRowsCount(), getColumnsCount());
        int n = data_.length;
        for (int i = 0; i < n; ++i) {
            s.data_[i] = data_[i] + Y.data_[i];
        }
        return s;
    }

    /**
     * Fills the matrix with random numbers (in [0, 1[)
     */
    public void randomize() {
        for (int i = 0; i < data_.length; ++i) {
            data_[i] = RNG.nextDouble();
        }
    }

    /**
     * Fills the matrix with random numbers (in [0, 1[)
     *
     * @param seed
     */
    public void randomize(int seed) {
        Random rnd = new Random(seed);
        for (int i = 0; i < data_.length; ++i) {
            data_[i] = rnd.nextDouble();
        }
    }

    /**
     * Returns a given row (as a src block)
     *
     * @param row The 0-based index of the row.
     * @return A src block giving access to the actual src of the matrix.
     */
    public DataBlock row(final int row) {
        return new DataBlock(data_, row, row + data_.length, nrows_);
    }

    /**
     * Returns all the rows of the matrix in a list of src blocks.
     *
     * @return The list containing all the rows.
     */
    public List<DataBlock> rowList() {
        ArrayList<DataBlock> rc = new ArrayList<>();
        for (int i = 0; i < nrows_; ++i) {
            rc.add(new DataBlock(data_, i, i + data_.length, nrows_));
        }
        return rc;
    }

    /**
     * Gets an iterator on all the rows of the matrix.
     *
     * @return The iterator on the rows. On return, the iterator points on the
     * first row of the matrix.
     */
    public DataBlockIterator rows() {
        return new DataBlockIterator(data_, 0, nrows_, ncols_, 1, nrows_);
    }

    /**
     * Sets all the cells of a matrix to a given value X(i,j) = value
     *
     * @param value The value
     */
    public void set(double value) {
        for (int i = 0; i < data_.length; ++i) {
            data_[i] = value;
        }
    }

    /**
     * Sets a specific cell of the matrix to a given values. Using intensively
     * that method can be expensive. User should prefer modifying the matrix
     * through src blocks. X(row, col) = value
     *
     * @param row The 0-based row index of the modified cell
     * @param col The 0-based column index of the modified cell
     * @param value The new value
     */
    public void set(final int row, final int col, final double value) {
        data_[row + col * nrows_] = value;
    }

    /**
     * x(i,j) = fn(i,j)
     * @param fn The given function
     * @since 2.2
     */
    public void set(MatrixFunction fn) {
        for (int c = 0, i = 0; c < ncols_; ++c) {
            for (int r = 0; r < nrows_; ++r, ++i) {
                data_[i] = fn.apply(r, c);
            }
        }
    }

    /**
     * x(i,j) = fn(i,j,x(i,j))
     * @param fn The given function
     * @since 2.2
     */
    public void set(MatrixRelativeFunction fn) {
        for (int c = 0, i = 0; c < ncols_; ++c) {
            for (int r = 0; r < nrows_; ++r, ++i) {
                data_[i] = fn.apply(r, c, data_[i]);
            }
        }
    }
    
    /**
     * x(i,j) = x(i,j) + fn(i,j)
     * @param fn The given function
     * @since 2.2
     */
    public void add(MatrixFunction fn) {
        for (int c = 0, i = 0; c < ncols_; ++c) {
            for (int r = 0; r < nrows_; ++r, ++i) {
                data_[i] += fn.apply(r, c);
            }
        }
    }

    /**
     * x(i,j) = x(i,j) + fn(i,j,x(i,j))
     * @param fn The given function
     * @since 2.2
     */
    public void add(MatrixRelativeFunction fn) {
        for (int c = 0, i = 0; c < ncols_; ++c) {
            for (int r = 0; r < nrows_; ++r, ++i) {
                data_[i] += fn.apply(r, c, data_[i]);
            }
        }
    }

    /**
     * Computes the sum of all the squared cells
     *
     * @return sum( X(i,j)*X(i,j) )
     */

    public double ssq() {
        double s = 0;
        for (int i = 0; i < data_.length; ++i) {
            double d = data_[i];
            s += d * d;
        }
        return s;
    }

    /**
     * X = X - r
     *
     * @param r The right operand.
     */
    public void sub(final double r) {
        int n = data_.length;
        for (int i = 0; i < n; ++i) {
            data_[i] -= r;
        }
    }

    /**
     * X = X - M
     * The matrices must have the same dimensions (not checked)
     *
     * @param M The right matrix.
     */
    public void sub(final Matrix M) {
        /*
         * if (m == null) throw new ArgumentNullException("m"); if (ncols_ !=
         * m.ncols_ || nrows_ != m.nrows_) throw new
         * MatrixException(MatrixException.IncompatibleDimensions);
         */
        int n = data_.length;
        for (int i = 0; i < n; ++i) {
            data_[i] -= M.data_[i];
        }
    }

    /**
     * Creates a sub-matrix from this matrix. That method should be used when
     * sub-matrices are needed for some processing.
     *
     * @return A sub-matrix containing all the cell is returned.
     */
    public SubMatrix all() {
        return new SubMatrix(data_, 0, nrows_, ncols_, 1, nrows_);
    }

    @Deprecated
    public SubMatrix subMatrix() {
        return new SubMatrix(data_, 0, nrows_, ncols_, 1, nrows_);
    }

    /**
     * Creates a sub-matrix from this matrix
     *
     * @param r0 First row (included). 0-based.
     * @param r1 Last row (excluded). 0-based.
     * @param c0 First column (included). 0-based.
     * @param c1 Last column (excluded). 0-based.
     * @return
     */
    public SubMatrix subMatrix(final int r0, final int r1, final int c0,
            final int c1) {
        int nr, nc;
        if (r1 < 0) {
            nr = nrows_ - r0;
        } else {
            nr = r1 - r0;
        }
        if (c1 < 0) {
            nc = ncols_ - c0;
        } else {
            nc = c1 - c0;
        }
        return new SubMatrix(data_, r0 + c0 * nrows_, nr, nc, 1,
                nrows_);
    }

    /**
     * Computes the sum off all the cells
     *
     * @return The sum off all the cells
     */
    public double sum() {
        double s = 0;
        for (int i = 0; i < data_.length; ++i) {
            s += data_[i];
        }
        return s;
    }

    /**
     * Y = X * r
     *
     * @param r The right operand
     * @return A new matrix is returned
     */
    public Matrix times(final double r) {
        // if (l == null)
        // throw new ArgumentNullException("l");
        if (r == 1) {
            return clone();
        }
        Matrix s = new Matrix(getRowsCount(), getColumnsCount());
        if (r == 0) {
            return s;
        }
        int n = data_.length;
        for (int i = 0; i < n; ++i) {
            s.data_[i] = data_[i] * r;
        }
        return s;
    }

    /**
     * Z = X * Y
     *
     * @param Y The right operand. This matrix should have the same dimensions
     * as this matrix. However, that condition is not checked
     * @return A new matrix is returned
     */
    public Matrix times(final Matrix Y) {
        /*
         * if (l == null) throw new ArgumentNullException("l"); if (r == null)
         * throw new ArgumentNullException("r"); if (l.ncols_ != r.nrows_ )
         * throw new MatrixException(MatrixException.IncompatibleDimensions);
         */

        int nr = nrows_, nc = Y.ncols_, nk = ncols_;
        Matrix s = new Matrix(nr, nc);
        double[] tmp = new double[nk];
        for (int i = 0; i < nr; ++i) {
            for (int j = 0, k = i; j < nk; ++j, k += nr) {
                tmp[j] = data_[k];
            }
            int ir = 0, idx = i;
            while (ir < Y.data_.length) {
                double x = 0;
                for (int k = 0; k < nk; ++k, ++ir) {
                    x += Y.data_[ir] * tmp[k];
                }
                s.data_[idx] = x;
                idx += nr;
            }
        }
        return s;
    }

    /**
     * Computes sum(this(i,j**m(i,j))
     *
     * @param m The right operand of the dot product
     * @return
     */
    public double dot(Matrix m) {
        double p = 0;
        for (int i = 0; i < data_.length; ++i) {
            p += data_[i] * m.data_[i];
        }
        return p;
    }

    /**
     * Solves SX=B.
     *
     * @param S The Initial matrix
     * @param B In parameter. The right-hand side of the equation.
     * @return Returns the solution of the system
     */
    public static Matrix rsolve(SubMatrix S, SubMatrix B) {
        if (S.getRowsCount() != B.getRowsCount()) {
            throw new MatrixException(MatrixException.IncompatibleDimensions);
        }
        HouseholderR qr = new HouseholderR(true);
        qr.decompose(S);
        Matrix X = new Matrix(S.getColumnsCount(), B.getColumnsCount());
        DataBlockIterator bc = B.columns(), xc = X.columns();
        DataBlock b = bc.getData(), x = xc.getData();
        do {
            qr.solve(b, x);
        } while (bc.next() && xc.next());
        return X;
    }

    /**
     * Solves XS=B.
     *
     * @param S The Initial matrix
     * @param B In parameter. The right-hand side of the equation.
     * @return Returns the solution of the system
     */
    public static Matrix lsolve(SubMatrix S, SubMatrix B) {
        if (S.getColumnsCount() != B.getColumnsCount()) {
            throw new MatrixException(MatrixException.IncompatibleDimensions);
        }
        Householder qr = new Householder(true);
        qr.decompose(S.transpose());
        Matrix X = new Matrix(B.getRowsCount(), S.getRowsCount());
        DataBlockIterator bc = B.rows(), xc = X.rows();
        DataBlock b = bc.getData(), x = xc.getData();
        do {
            qr.solve(b, x);
        } while (bc.next() && xc.next());
        return X;
    }

    /**
     * Converts this matrix to a lower triangular matrix
     */
    public void toLower() {
        for (int c = 1, id = nrows_; c < ncols_; ++c) // id == index of the
        // diagonal cell
        {
            int nr = Math.min(nrows_, c);
            for (int r = 0; r < nr; ++r) {
                data_[id++] = 0;
            }
            id += nrows_ - nr;
        }
    }

    /**
     * Converts this matrix to an upper triangular matrix.
     */
    public void toUpper() {
        int nc = Math.min(nrows_ - 1, ncols_);
        for (int c = 0, id = 0, rmax = nrows_; c < nc; ++c, id += nrows_ + 1, rmax += nrows_) {
            for (int ir = id + 1; ir < rmax; ++ir) {
                data_[ir] = 0;
            }
        }
    }

    /**
     * Transposes this matrix
     *
     * @return A new matrix is returned. The transposing of the sub-matrix is
     * usually a better option.
     */
    public Matrix transpose() {
        Matrix T = new Matrix(ncols_, nrows_);
        int tmax = data_.length;
        for (int j = 0, s = 0; j < ncols_; ++j) {
            for (int t = j; t < tmax; t += ncols_, ++s) {
                T.data_[t] = data_[s];
            }
        }
        return T;
    }

    /**
     * this = this + a * Y
     *
     * @param a
     * @param Y
     */
    public void addAY(double a, Matrix Y) {
        DataBlock t = new DataBlock(data_);
        DataBlock s = new DataBlock(Y.data_);
        t.addAY(a, s);
    }

    /**
     * M = M + X * a * X'
     *
     * @param a
     * @param x
     */
    public void addXaXt(double a, DataBlock x) {
        DataBlockIterator cols = columns();
        DataBlock col = cols.getData();
        do {
            double z = a * x.get(cols.getPosition());
            col.addAY(z, x);
        } while (cols.next());
    }

    /**
     * M = M + X * a * Y'
     *
     * @param a
     * @param x
     * @param y
     */
    public void addXaYt(double a, DataBlock x, DataBlock y) {
        DataBlockIterator cols = columns();
        DataBlock col = cols.getData();
        do {
            double z = a * y.get(cols.getPosition());
            col.addAY(z, x);
        } while (cols.next());
    }

    public boolean isEmpty() {
        return data_.length == 0;
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "";
        }
        return all().toString();
    }

    public String toString(String fmt) {
        if (isEmpty()) {
            return "";
        }
        return all().toString(fmt);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof Matrix && equals((Matrix) obj));
    }

    public boolean equals(Matrix other) {
        return this.ncols_ == other.ncols_ && this.nrows_ == other.nrows_
                && Arrays.equals(data_, other.data_);
    }

    public boolean equals(Matrix other, double eps) {
        if (this.ncols_ != other.ncols_ || this.nrows_ != other.nrows_) {
            return false;
        }
        for (int i = 0; i < data_.length; ++i) {
            if (Math.abs(data_[i] - other.data_[i]) > eps) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Arrays.hashCode(this.data_);
        hash = 97 * hash + this.nrows_;
        return hash;
    }

    public int rank() {
        Householder hous = new Householder(true);
        hous.setEpsilon(1e-12);
        if (this.nrows_ >= this.ncols_) {
            hous.decompose(this);
        } else {
            hous.decompose(this.all().transpose());
        }
        return hous.getRank();
    }

    public void permuteColumns(final int i, final int j) {
        if (i == j) {
            return;
        }
        for (int k = nrows_ * i, kend = k + nrows_, l = nrows_ * j; k < kend; ++k, ++l) {
            double tmp = data_[k];
            data_[k] = data_[l];
            data_[l] = tmp;
        }
    }

    public void permuteRows(final int i, final int j) {
        if (i == j) {
            return;
        }
        for (int k = i, l = j; k < data_.length; k += nrows_, l += nrows_) {
            double tmp = data_[k];
            data_[k] = data_[l];
            data_[l] = tmp;
        }
    }

    /**
     * Copies a given matrix. In most cases, the two matrices should have the
     * same dimensions. However, the function only copies the src (and doesn't
     * modify the current dimensions). The copied matrix could be larger than
     * the current matrix. In such a case, only the first cells will be copied.
     *
     * @param C The copied matrix
     */
    public void copy(Matrix C) {
        System.arraycopy(C.data_, 0, data_, 0, data_.length);
    }

    /**
     * Computes ||this-m||
     *
     * @param m The second matrix
     * @return
     */
    public double distance(Matrix m) {
        return this.minus(m).nrm2();
    }

    public void smooth(double eps) {
        double scale = 1;
        double q = Math.sqrt(eps);

        while (q < 1) {
            scale *= 10;
            q *= 10;
        }

        for (int i = 0; i < data_.length; ++i) {
            double c = data_[i];
            double d = Math.round(c * scale) / scale;
            if (Math.abs(d - c) < eps) {
                data_[i] = d;
            }
        }
    }

    /**
     * Top-left empty sub-matrix. To be used with next(a,b)
     *
     * @return An empty sub-matrix
     */
    public SubMatrix topLeft() {
        return new SubMatrix(data_, 0, 0, 0, 1, nrows_);
    }

    /**
     * Top-left sub-matrix
     *
     * @param nr Number of rows. Could be 0.
     * @param nc Number of columns. Could be 0.
     * @return A nr src nc sub-matrix
     */
    public SubMatrix topLeft(int nr, int nc) {
        return new SubMatrix(data_, 0, nr, nc, 1, nrows_);
    }

    /**
     * bottom-right sub-matrix (outside).
     *
     * @return An empty sub-matrix
     */
    public SubMatrix bottomRight() {
        return new SubMatrix(data_, data_.length, 0, 0, 1, nrows_);
    }

    /**
     * Bottom-right sub-matrix
     *
     * @param nr Number of rows. Could be 0.
     * @param nc Number of columns. Could be 0.
     * @return A nr src nc sub-matrix
     */
    public SubMatrix bottomRight(int nr, int nc) {
        int start = data_.length - nr - nc * nrows_;
        return new SubMatrix(data_, start, nr, nc, 1, nrows_);
    }
}
