/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths.matrices;

import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.data.DataWindow;
import demetra.data.accumulator.DoubleAccumulator;
import demetra.design.IBuilder;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import demetra.design.Unsafe;
import java.util.function.DoublePredicate;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;
import javax.annotation.Nonnegative;
import demetra.data.DoubleReader;
import demetra.data.DoubleSequence;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import demetra.maths.MatrixType;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class Matrix implements MatrixType {

    public static final Matrix EMPTY = new Matrix(new double[0], 0, 0);

    public static Matrix square(int n) {
        double[] data = new double[n * n];
        return new Matrix(data, n, n);
    }

    public static Matrix make(int nrows, int ncols) {
        double[] data = new double[nrows * ncols];
        return new Matrix(data, nrows, ncols);
    }

    public static Matrix of(MatrixType matrix) {
        return new Matrix(matrix.toArray(), matrix.getRowsCount(), matrix.getColumnsCount());
    }

    public static Matrix identity(int n) {
        Matrix i = square(n);
        i.diagonal().set(1);
        return i;
    }

    public static Matrix diagonal(DoubleSequence d) {
        Matrix i = square(d.length());
        i.diagonal().copy(d);
        return i;
    }

    public static Matrix rowOf(DataBlock x) {
        return new Matrix(x.getStorage(), x.getStartPosition(), 1, x.length(), 1, x.getIncrement());
    }

    public static Matrix columnOf(DataBlock x) {
        return new Matrix(x.getStorage(), x.getStartPosition(), x.length(), 1, x.getIncrement(), 1);
    }

    public static class Builder implements IBuilder<Matrix> {

        private final double[] storage;
        private int row_inc = 1, col_inc = 0;
        private int start = 0, nrows = 1, ncols = 1;

        private Builder(double[] data) {
            this.storage = data;
        }

        public Builder start(final int start) {
            this.start = start;
            return this;
        }

        public Builder nrows(final int nrows) {
            this.nrows = nrows;
            return this;
        }

        public Builder ncolumns(final int ncols) {
            this.ncols = ncols;
            return this;
        }

        public Builder square(final int n) {
            this.nrows = n;
            this.ncols = n;
            return this;
        }

        public Builder rowIncrement(final int rowinc) {
            this.row_inc = rowinc;
            return this;
        }

        public Builder columnIncrement(final int colinc) {
            this.col_inc = colinc;
            return this;
        }

        @Override
        public Matrix build() {
            // TODO Add some controls on the state 
            return new Matrix(storage, start, nrows, ncols, row_inc, col_inc == 0 ? nrows : col_inc);
        }
    }

    public static Builder builder(double[] data) {
        return new Builder(data);
    }

    final double[] storage;
    final int rowInc, colInc;
    int start, nrows, ncols;

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
     * Creates a new instance of SubMatrix
     *
     * @param data
     * @param nrows
     * @param ncols
     */
    Matrix(final double[] data, final int nrows, final int ncols) {
        this.storage = data;
        this.nrows = nrows;
        this.ncols = ncols;
        rowInc = 1;
        colInc = nrows;
    }

    /**
     *
     * @param data
     * @param start
     * @param nrows
     * @param ncols
     * @param rowinc
     * @param colinc
     */
    Matrix(final double[] data, final int start, final int nrows,
            final int ncols, final int rowinc, final int colinc) {
        this.storage = data;
        this.start = start;
        this.nrows = nrows;
        this.ncols = ncols;
        rowInc = rowinc;
        colInc = colinc;
    }

    public final void set(final double d) {
        columns().forEach(r -> r.set(d));
    }

    public final void set(final DoubleSupplier fn) {
        columns().forEach(r -> r.set(fn));
    }

    public final void set(final MatrixFunction fn) {
        int c = 0;
        for (DataBlock col : columns()) {
            final int cur = c++;
            col.set(r -> fn.apply(r, cur));
        }
    }

    public void apply(int row, int col, DoubleUnaryOperator fn) {
        int idx = start + row * rowInc + col * colInc;
        storage[idx] = fn.applyAsDouble(storage[idx]);
    }

    public void add(int row, int col, double d) {
        if (d != 0) {
            storage[start + row * rowInc + col * colInc] += d;
        }
    }

    public void sub(int row, int col, double d) {
        if (d != 0) {
            storage[start + row * rowInc + col * colInc] -= d;
        }
    }

    public void mul(int row, int col, double d) {
        if (d != 1) {
            storage[start + row * rowInc + col * colInc] *= d;
        }
    }

    public void div(int row, int col, double d) {
        if (d != 1) {
            storage[start + row * rowInc + col * colInc] /= d;
        }
    }

    public final void apply(final DoubleUnaryOperator fn) {
        if (isEmpty()) {
            return;
        }
        if (isFull()) {
            for (int i = 0; i < storage.length; ++i) {
                storage[i] = fn.applyAsDouble(storage[i]);
            }
            return;
        }
        if (colInc == 1) {
            DataBlockIterator rows = rowsIterator();
            while (rows.hasNext()) {
                rows.next().apply(fn);
            }
        } else {
            DataBlockIterator cols = columnsIterator();
            while (cols.hasNext()) {
                cols.next().apply(fn);
            }
        }
    }

    public final void applyByRows(final Consumer<DataBlock> fn) {
        DataBlockIterator rows = rowsIterator();
        while (rows.hasNext()) {
            fn.accept(rows.next());
        }
    }

    public final void applyByColumns(final Consumer<DataBlock> fn) {
        DataBlockIterator cols = columnsIterator();
        while (cols.hasNext()) {
            fn.accept(cols.next());
        }
    }

    public final void applyByRows(final Matrix M, final BiConsumer<DataBlock, DataBlock> fn) {
        DataBlockIterator rows = rowsIterator();
        DataBlockIterator mrows = M.rowsIterator();
        while (rows.hasNext()) {
            fn.accept(rows.next(), mrows.next());
        }
    }

    public final void applyByColumns(final Matrix M, final BiConsumer<DataBlock, DataBlock> fn) {
        DataBlockIterator cols = columnsIterator();
        DataBlockIterator mcols = M.columnsIterator();
        while (cols.hasNext()) {
            fn.accept(cols.next(), mcols.next());
        }
    }

    public final double sum() {
        if (isEmpty()) {
            return 0;
        }
        if (isFull()) {
            double s = 0;
            for (int i = 0; i < storage.length; ++i) {
                s += storage[i];
            }
            return s;
        }
        if (colInc == 1) {
            double s = 0;
            DataBlockIterator rows = rowsIterator();
            while (rows.hasNext()) {
                s += rows.next().sum();
            }
            return s;
        } else {
            double s = 0;
            DataBlockIterator cols = columnsIterator();
            while (cols.hasNext()) {
                s += cols.next().sum();
            }
            return s;
        }
    }

    public final double ssq() {
        if (isEmpty()) {
            return 0;
        }
        if (isFull()) {
            double s = 0;
            for (int i = 0; i < storage.length; ++i) {
                double c=storage[i];
                s += c*c;
            }
            return s;
        }
        if (colInc == 1) {
            double s = 0;
            DataBlockIterator rows = rowsIterator();
            while (rows.hasNext()) {
                s += rows.next().ssq();
            }
            return s;
        } else {
            double s = 0;
            DataBlockIterator cols = columnsIterator();
            while (cols.hasNext()) {
                s += cols.next().ssq();
            }
            return s;
        }
    }

    public final double frobeniusNorm() {
        double scale = 0;
        double ssq = 1;
        if (colInc == 1) {
            DataBlockIterator rows = rowsIterator();
            while (rows.hasNext()) {
                DoubleReader cell = rows.next().reader();
                for (int i = 0; i < ncols; ++i) {
                    double cur = cell.next();
                    if (cur != 0) {
                        double absxi = Math.abs(cur);
                        if (scale < absxi) {
                            double s = scale / absxi;
                            ssq = 1 + ssq * s * s;
                            scale = absxi;
                        } else {
                            double s = absxi / scale;
                            ssq += s * s;
                        }
                    }
                }
            }
        } else {
            DataBlockIterator columns = columnsIterator();
            while (columns.hasNext()) {
                DoubleReader cell = columns.next().reader();
                for (int i = 0; i < nrows; ++i) {
                    double cur = cell.next();
                    if (cur != 0) {
                        double absxi = Math.abs(cur);
                        if (scale < absxi) {
                            double s = scale / absxi;
                            ssq = 1 + ssq * s * s;
                            scale = absxi;
                        } else {
                            double s = absxi / scale;
                            ssq += s * s;
                        }
                    }
                }
            }
        }
        return scale * Math.sqrt(ssq);

    }

    public final void add(final double d) {
        if (d != 0) {
            apply(x -> x + d);
        }
    }

    public final void sub(final double d) {
        if (d != 0) {
            apply(x -> x - d);
        }
    }

    public final void mul(final double d) {
        if (d == 0) {
            set(0);
        } else if (d != 1) {
            apply(x -> x * d);
        }
    }

    public final void div(final double d) {
        if (d == 0) {
            set(Double.NaN);
        } else if (d != 1) {
            apply(x -> x / d);
        }
    }

    public final boolean test(final DoublePredicate pred) {
        if (isEmpty()) {
            return true;
        }
        if (colInc == 1) {
            DataBlockIterator rows = rowsIterator();
            while (rows.hasNext()) {
                if (!rows.next().allMatch(pred)) {
                    return false;
                }
            }
            return true;
        } else {
            DataBlockIterator cols = columnsIterator();
            while (cols.hasNext()) {
                if (!cols.next().allMatch(pred)) {
                    return false;
                }
            }
            return true;
        }
    }

    public final void set(int row, int col, double value) {
        storage[start + row * rowInc + col * colInc] = value;
    }

    /**
     *
     * @return
     */
    @Override
    public final DataBlock diagonal() {
        int n = Math.min(nrows, ncols), inc = rowInc + colInc;
        return DataBlock.ofInternal(storage, start, start + inc * n, inc);
    }

    /**
     *
     * @param pos
     * @return
     */
    @Override
    public final DataBlock subDiagonal(int pos) {
        if (pos >= ncols) {
            return DataBlock.EMPTY;
        }
        if (-pos >= nrows) {
            return DataBlock.EMPTY;
        }
        int beg = start, inc = rowInc + colInc;
        int n;
        if (pos > 0) {
            beg += pos * colInc;
            n = Math.min(nrows, ncols - pos);
        } else if (pos < 0) {
            beg -= pos * rowInc;
            n = Math.min(nrows + pos, ncols);
        } else {
            n = Math.min(nrows, ncols);
        }
        return DataBlock.ofInternal(storage, beg, beg + inc * n, inc);
    }

    public final Matrix deepClone() {
        return new Matrix(data(), nrows, ncols);
    }

    public final double[] data() {
        if (isFull()) {
            return storage.clone();
        } else {
            double[] data = new double[nrows * ncols];
            copyTo(data, 0);
            return data;
        }
    }

    @Override
    public final void copyTo(double[] data, final int start) {
        int pos = start;
        DataBlockIterator cols = columnsIterator();
        while (cols.hasNext()) {
            cols.next().copyTo(data, pos);
            pos += nrows;
        }
    }

    public final void copy(Matrix M) {
        if (colInc == 1 && M.colInc == 1) {
            DataBlockIterator rows = rowsIterator();
            DataBlockIterator mrows = M.rowsIterator();
            while (rows.hasNext()) {
                rows.next().copy(mrows.next());
            }
        } else {
            DataBlockIterator cols = columnsIterator();
            DataBlockIterator mcols = M.columnsIterator();
            while (cols.hasNext()) {
                cols.next().copy(mcols.next());
            }
        }
    }

    /**
     *
     * @return
     */
    public Matrix transpose() {
        return new Matrix(storage, start, ncols, nrows, colInc, rowInc);
    }

    @Override
    public final boolean isEmpty() {
        return nrows <= 0 || ncols <= 0;
    }

    public final boolean isSquare() {
        return nrows == ncols;
    }

    public final boolean isRow() {
        return 1 == nrows;
    }

    public final boolean isColumn() {
        return 1 == ncols;
    }

    public final boolean isFull() {
        return start == 0 && rowInc == 1 && storage.length == nrows * ncols;
    }

    public boolean isDiagonal(double zero) {
        if (ncols != nrows) {
            return false;
        }
        if (nrows == 1) {
            return true;
        }

        int n = Math.min(nrows, ncols), inc = rowInc + colInc;
        DataBlock diag = diagonal();
        DataWindow ldiag = diag.window();
        DataWindow udiag = diag.window();
        for (int i = 1; i < nrows; ++i) {
            if (!ldiag.slideAndShrink(rowInc).isZero(zero)) {
                return false;
            }
            if (!udiag.slideAndShrink(colInc).isZero(zero)) {
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
            return storage[start] == 1;
        }
        if (diagonal().anyMatch(x -> x != 1)) {
            return false;
        }

        int n = Math.min(nrows, ncols), inc = rowInc + colInc;
        DataBlock diag = diagonal();
        DataWindow ldiag = diag.window();
        DataWindow udiag = diag.window();
        for (int i = 1; i < nrows; ++i) {
            if (ldiag.slideAndShrink(rowInc).anyMatch(x -> x != 0)) {
                return false;
            }
            if (udiag.slideAndShrink(colInc).anyMatch(x -> x != 0)) {
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
            if (!ldiag.slideAndShrink(rowInc).allMatch(udiag.slideAndShrink(colInc),
                    (x, y) -> Math.abs(x - y) <= eps)) {
                return false;
            }
        }
        return true;
    }

    public boolean isSymmetric() {
        return isSymmetric(0);
    }

    public MatrixType unmodifiable() {
        if (isFull()) {
            return MatrixType.ofInternal(storage, nrows, ncols);
        } else {
            return MatrixType.super.extract(0, nrows, 0, ncols);
        }
    }

    /**
     *
     * @param r0
     * @param nr
     * @param c0
     * @param nc
     * @return
     */
    @Override
    public Matrix extract(final int r0, final int nr, final int c0, final int nc) {
        return new Matrix(storage, start + r0 * rowInc + c0 * colInc,
                nr, nc, rowInc, colInc);
    }

    public Matrix dropTopLeft(int nr, int nc) {
        return new Matrix(storage, start + nr * rowInc + nc * colInc,
                nrows - nr, ncols - nc, rowInc, colInc);

    }

    public Matrix dropBottomRight(int nr, int nc) {
        return new Matrix(storage, start,
                nrows - nr, ncols - nc, rowInc, colInc);

    }

    /**
     *
     * @param r0
     * @param c0
     * @param nrows
     * @param ncols
     * @param rowinc
     * @param colinc
     * @return
     */
    public final Matrix extract(final int r0, final int c0, final int nrows,
            final int ncols, final int rowinc, final int colinc) {
        return new Matrix(storage, start + r0 * rowInc + c0 * colInc,
                nrows, ncols, rowInc * rowinc, colInc * colinc);
    }

    public final Matrix select(final int[] selectedRows, final int[] selectedColumns) {
        // TODO optimization
        Matrix m = Matrix.make(selectedRows.length, selectedColumns.length);
        for (int c = 0; c < selectedRows.length; ++c) {
            for (int r = 0; r < selectedRows.length; ++r) {
                m.set(r, c, get(selectedRows[r], selectedColumns[c]));
            }
        }
        return m;
    }

    /**
     * Creates a new matrix which doesn't contain given rows/columns
     *
     * @param excludedRows
     * @param excludedColumns
     * @return A new matrix, based on another storage, is returned.
     */
    public final Matrix exclude(final int[] excludedRows, final int[] excludedColumns) {
        int[] srx = excludedRows.clone();
        Arrays.sort(srx);
        int[] scx = excludedColumns.clone();
        Arrays.sort(scx);
        boolean[] rx = new boolean[nrows], cx = new boolean[ncols];
        int nrx = 0, ncx = 0;
        for (int i = 0; i < srx.length; ++i) {
            int cur = srx[i];
            if (!rx[cur]) {
                rx[cur] = true;
                nrx++;
            }
        }
        for (int i = 0; i < scx.length; ++i) {
            int cur = scx[i];
            if (!cx[cur]) {
                cx[cur] = true;
                ncx++;
            }
        }
        if (nrx == 0 && ncx == 0) {
            return deepClone();
        }

        Matrix m = Matrix.make(nrows - nrx, ncols - ncx);
        for (int c = 0, nc = 0; c < ncols; ++c) {
            if (cx[c]) {
                for (int r = 0, nr = 0; r < nrows; ++r) {
                    if (rx[r]) {
                        m.set(nr, nc, get(r, c));
                        ++nr;
                    }
                }
                ++nc;
            }
        }
        return m;
    }

    /**
     * Creates a new matrix which contains the current matrix at given row/col position
     *
     * @param nr
     * @param rowPos
     * @param nc
     * @param colPos
     * @return A new matrix, based on another storage, is returned.
     */
    public final Matrix expand(final int nr, final int[] rowPos, final int nc, final int[] colPos) {
        if (rowPos.length != nr || colPos.length != nc) {
            throw new MatrixException(MatrixException.DIM);
        }
        Matrix m = Matrix.make(nr, nc);
        for (int c = 0; c < ncols; ++c) {
            for (int r = 0; r < nrows; ++r) {
                m.set(rowPos[r], colPos[c], get(r, c));
            }
        }
        return m;
    }

    /**
     *
     * @param row
     * @param col
     * @return
     */
    @Override
    public final double get(final int row, final int col) {
        return storage[start + row * rowInc + col * colInc];
    }

    /**
     *
     * @return
     */
    @Override
    public final int getColumnsCount() {
        return ncols;
    }

    /**
     *
     * @return
     */
    @Override
    public final int getRowsCount() {

        return nrows;
    }

    @Unsafe
    public final double[] getStorage() {
        return storage;
    }

    /**
     * Position of the top-reader cell
     *
     * @return
     */
    public final int getStartPosition() {
        return start;
    }

    public final int getRowIncrement() {
        return rowInc;
    }

    /**
     * Position of the bottom-right cell
     *
     * @return
     */
    public final int getLastPosition() {
        return start + (nrows - 1) * rowInc + (ncols - 1) * colInc;
    }

    public final int getColumnIncrement() {
        return colInc;
    }

    //<editor-fold defaultstate="collapsed" desc="In place operations">
    public final void setAY(double a, Matrix Y) {
        if (a != 0) {
            if (colInc == 1 && Y.colInc == 1) {
                applyByRows(Y, (x, y) -> x.setAY(a, y));
            } else {
                applyByColumns(Y, (x, y) -> x.setAY(a, y));
            }
        }
    }

    public final void addAY(double a, Matrix Y) {
        if (a != 0) {
            if (colInc == 1 && Y.colInc == 1) {
                applyByRows(Y, (x, y) -> x.addAY(a, y));
            } else {
                applyByColumns(Y, (x, y) -> x.addAY(a, y));
            }
        }
    }

    public final void add(Matrix X) {
        if (isFull() && X.isFull()) {
            for (int i = 0; i < storage.length; ++i) {
                storage[i] += X.storage[i];
            }

        } else if (colInc == 1 && X.colInc == 1) {
            applyByRows(X, (x, y) -> x.add(y));
        } else {
            applyByColumns(X, (x, y) -> x.add(y));
        }
    }

    public final void sub(Matrix X) {
        if (isFull() && X.isFull()) {
            for (int i = 0; i < storage.length; ++i) {
                storage[i] -= X.storage[i];
            }
        } else if (colInc == 1 && X.colInc == 1) {
            applyByRows(X, (x, y) -> x.sub(y));
        } else {
            applyByColumns(X, (x, y) -> x.sub(y));
        }
    }

    private static final int PROD_THRESHOLD = 6;

    /**
     *
     * @param lm
     * @param rm
     */
    public final void product(final Matrix lm, final Matrix rm) {
//        if (lm.getColumnsCount() < PROD_THRESHOLD * (lm.getRowsCount())) {
//            // fast processing. The usual case
//            if (rowInc == 1 && lm.rowInc == 1) {
//                for (int c = 0, ix = reader, ir = rm.reader; c < ncols; ++c, ix += colInc, ir += rm.colInc) {
//                    int jr = ir;
//                    double a = rm.storage[jr];
//                    jr += rm.rowInc;
//                    for (int r = 0, jx = ix, jl = lm.reader; r < nrows; ++r, ++jx, ++jl) {
//                        storage[jx] = a * lm.storage[jl];
//                    }
//                    for (int lc = 1, jl = lm.reader + lm.colInc; lc < lm.ncols; ++lc, jl += lm.colInc) {
//                        a = rm.storage[jr];
//                        jr += rm.rowInc;
//                        if (a != 0) {
//                            for (int r = 0, kx = ix, kl = jl; r < nrows; ++r, ++kx, ++kl) {
//                                storage[kx] += a * lm.storage[kl];
//                            }
//                        }
//                    }
//                }
//            } else if (colInc == 1 && rm.colInc == 1) {
//                transpose().product(rm.transpose(), lm.transpose());
//            } else {
//                for (int c = 0, ix = reader, ir = rm.reader; c < ncols; ++c, ix += colInc, ir += rm.colInc) {
//                    int jr = ir;
//                    double a = rm.storage[jr];
//                    jr += rm.rowInc;
//                    for (int r = 0, jx = ix, jl = lm.reader; r < nrows; ++r, jx += rowInc, jl += lm.rowInc) {
//                        storage[jx] = a * lm.storage[jl];
//                    }
//                    for (int lc = 1, jl = lm.reader + lm.colInc; lc < lm.ncols; ++lc) {
//                        a = rm.storage[jr];
//                        jr += rm.rowInc;
//                        if (a != 0) {
//                            for (int r = 0, kx = ix, kl = jl; r < nrows; ++r, kx += rowInc, kl += lm.rowInc) {
//                                storage[kx] += a * lm.storage[kl];
//                            }
//                        }
//                    }
//                }
//            }
//        } else {
        if (lm.getColumnsCount() < PROD_THRESHOLD * (lm.getRowsCount())) {
            DataBlockIterator cols = columnsIterator();
            DataBlockIterator rcols = rm.columnsIterator();
            DataBlockIterator lcols = lm.columnsIterator();
            double[] rx = rm.storage;
            while (cols.hasNext()) {
                lcols.reset();
                DataBlock col = cols.next(), rcol = rcols.next();
                int pos = rcol.getStartPosition(), inc = rcol.getIncrement();
                col.setAY(rx[pos], lcols.next());
                while (lcols.hasNext()) {
                    pos += inc;
                    col.addAY(rx[pos], lcols.next());
                }
            }
        } else {
            DataBlockIterator iter = columnsIterator(), riter = lm.rowsIterator(), citer = rm.columnsIterator();
            while (iter.hasNext()) {
                riter.reset();
                DataBlock cur = iter.next(), col = citer.next();
                cur.set(riter, row -> col.dot(row));
            }
        }
//        }
    }

    public final void robustProduct(final Matrix lm, final Matrix rm, DoubleAccumulator acc) {
        DataBlockIterator iter = columnsIterator(), riter = lm.rowsIterator(), citer = rm.columnsIterator();
        while (iter.hasNext()) {
            riter.reset();
            DataBlock cur = iter.next(), col = citer.next();
            cur.set(riter, row -> {
                acc.reset();
                col.robustDot(row, acc);
                return acc.sum();
            });
        }
    }

    public void addXaXt(final double a, final DataBlock x) {
        if (a == 0) {
            return;
        }
        int nr = x.length(), xinc = x.getIncrement();
        double[] px = x.getStorage();
        if (xinc == 1) {
            for (int i = 0, ix = x.getStartPosition(), im = start; i < nr; ++i, ++ix, im += rowInc + colInc) {
                if (px[ix] != 0) {
                    for (int j = i, kx = ix, km = im, ks = im; j < nr; ++j, ++kx, km += rowInc, ks += colInc) {
                        double z = a * px[ix] * px[kx];
                        storage[km] += z;
                        if (ks != km) {
                            storage[ks] += z;
                        }
                    }
                }
            }
        } else {
            for (int i = 0, ix = x.getStartPosition(), im = start; i < nr; ++i, ix += xinc, im += rowInc + colInc) {
                if (px[ix] != 0) {
                    for (int j = i, kx = ix, km = im, ks = im; j < nr; ++j, kx += xinc, km += rowInc, ks += colInc) {
                        double z = a * px[ix] * px[kx];
                        storage[km] += z;
                        if (ks != km) {
                            storage[ks] += z;
                        }
                    }
                }
            }
        }
    }

    /**
     * Computes the kronecker product ofFunction two matrix. This object will contain
     * the results. The dimensions ofFunction this object must be equal to the product
     * ofFunction the dimensions ofFunction the operands. For optimisation purpose, the code
     * consider that the resulting sub-matrix is set to 0 at the entry ofFunction the
     * code
     *
     * @param m The left operand
     * @param n The right operand
     */
    public void kronecker(final Matrix m, final Matrix n) {
        int rm = m.getRowsCount(), cm = m.getColumnsCount();
        int rn = n.getRowsCount(), cn = n.getColumnsCount();
        for (int r = 0, i = 0; r < rm; ++r, i += rn) {
            for (int c = 0, j = 0; c < cm; ++c, j += cn) {
                Matrix cur = extract(i, rn, j, cn);
                double e = m.get(r, c);
                if (e != 0) {
                    cur.setAY(e, n);
                }
            }
        }
    }

    public void chs() {
        apply(x -> -x);
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Operations">
    public Matrix times(Matrix B) {
        Matrix AB = Matrix.make(nrows, B.getColumnsCount());
        AB.product(this, B);
        return AB;
    }

    public Matrix times(double d) {
        if (d == 0) {
            return Matrix.make(nrows, ncols);
        } else if (d == 1) {
            return deepClone();
        } else {
            Matrix r = deepClone();
            r.apply(x -> x * d);
            return r;
        }
    }

    public Matrix plus(double d) {
        if (d == 0) {
            return Matrix.make(nrows, ncols);
        } else {
            Matrix r = deepClone();
            r.apply(x -> x + d);
            return r;
        }
    }

    public Matrix plus(Matrix B) {
        Matrix AB = deepClone();
        AB.add(B);
        return AB;
    }

    public Matrix minus(double d) {
        if (d == 0) {
            return Matrix.make(nrows, ncols);
        } else {
            Matrix r = deepClone();
            r.apply(x -> x - d);
            return r;
        }
    }

    public Matrix minus(Matrix B) {
        Matrix AB = deepClone();
        AB.sub(B);
        return AB;
    }

    public Matrix minus() {
        Matrix r = deepClone();
        r.apply(x -> -x);
        return r;
    }

    //</editor-fold>
    /**
     *
     * @param c
     * @return
     */
    @Override
    public final DataBlock column(final int c) {
        int beg = start + c * colInc, end = beg + rowInc * nrows;
        return DataBlock.ofInternal(storage, beg, end, rowInc);
    }

    /**
     *
     * @param r
     * @return
     */
    @Override
    public final DataBlock row(final int r) {
        int beg = start + r * rowInc, end = beg + colInc * ncols;
        return DataBlock.ofInternal(storage, beg, end, colInc);
    }

    public final Iterable<DataBlock> rows() {
        return () -> new Rows();
    }

    public final Iterable<DataBlock> columns() {
        return () -> new Columns();
    }

    public final Iterable<DataBlock> fastRows() {
        return () -> rowsIterator();
    }

    public final Iterable<DataBlock> fastColumns() {
        return () -> columnsIterator();
    }

    public final DataBlockIterator rowsIterator() {
        return new RCIterator(topOutside(), nrows, rowInc);
    }

    public final DataBlockIterator reverseRowsIterator() {
        return new RCIterator(bottomOutside(), nrows, -rowInc);
    }

    public final DataBlockIterator columnsIterator() {
        return new RCIterator(leftOutside(), ncols, colInc);
    }

    public final DataBlockIterator reverseColumnsIterator() {
        return new RCIterator(rightOutside(), ncols, -colInc);
    }

    /**
     * Gets the columns of the matrix as a list of src block
     *
     * @return The list of all the columns.
     */
    public List<DataBlock> columnList() {
        ArrayList<DataBlock> rc = new ArrayList<>();
        columns().forEach(col -> rc.add(col));
        return rc;
    }

    /**
     * Gets the columns of the matrix as a list of src block
     *
     * @return The list of all the columns.
     */
    public List<DataBlock> rowList() {
        ArrayList<DataBlock> rc = new ArrayList<>();
        rows().forEach(row -> rc.add(row));
        return rc;
    }

    /**
     * Shifts the matrix to the top-left corner.
     * a(i,j) = a(i+n, j+n) for i in [0, nrows-n[ and j in [0, ncols-n[
     * The cells that are not moved are not modified
     *
     * @param n The displacement (n cells left and n cells up)
     */
    public void upLeftShift(@Nonnegative final int n) {
        int del = (rowInc + colInc) * n;
        for (int c = 0, i = start; c < ncols - n; ++c, i += colInc) {
            for (int r = 0, j = i; r < nrows - n; ++r, j += rowInc) {
                storage[j] = storage[j + del];
            }
        }
    }

    /**
     * Shifts the matrix to the bottom-right corner
     * a(i,j) = a(i-n, j-n) for i in [n, nrows[ and j in [n, ncols[
     * The cells that are not moved are not modified.
     *
     * @param n The displacement (n cells right and n cells down)
     */
    public void downRightShift(@Nonnegative final int n) {
        int del = (rowInc + colInc) * n;
        for (int c = n, i = start + (nrows - 1) * rowInc
                + (ncols - 1) * colInc; c < ncols; ++c, i -= colInc) {
            for (int r = n, j = i; r < nrows; ++r, j -= rowInc) {
                storage[j] = storage[j - del];
            }
        }
    }

    //<editor-fold defaultstate="collapsed" desc="matrix windows">
    /**
     * Top-reader empty sub-matrix. To be used with next(a,b)
     *
     * @return An empty sub-matrix
     */
    public MatrixWindow topLeft() {
        return new MatrixWindow(storage, start, 0, 0, rowInc, colInc);
    }

    /**
     * Top-reader sub-matrix
     *
     * @param nr Number of rows. Could be 0.
     * @param nc Number of columns. Could be 0.
     * @return A nr src nc sub-matrix
     */
    public MatrixWindow topLeft(int nr, int nc) {
        return new MatrixWindow(storage, start, nr, nc, rowInc, colInc);
    }

    /**
     * Top-reader sub-matrix
     *
     * @param nr Number of rows. Could be 0.
     * @return A nr src nc sub-matrix
     */
    public MatrixWindow top(int nr) {
        return new MatrixWindow(storage, start, nr, ncols, rowInc, colInc);
    }

    /**
     * Top-reader sub-matrix
     *
     * @param nc Number of columns. Could be 0.
     * @return A nr src nc sub-matrix
     */
    public MatrixWindow left(int nc) {
        return new MatrixWindow(storage, start, nrows, nc, rowInc, colInc);
    }

    /**
     * bottom-right sub-matrix.
     *
     * @return An empty sub-matrix
     */
    public MatrixWindow bottomRight() {
        int nstart = nrows * rowInc + ncols * colInc;
        return new MatrixWindow(storage, nstart, 0, 0, rowInc, colInc);
    }

    /**
     * Bottom-right sub-matrix
     *
     * @param nr Number of rows. Could be 0.
     * @param nc Number of columns. Could be 0.
     * @return A nr src nc sub-matrix
     */
    public MatrixWindow bottomRight(int nr, int nc) {
        int nstart = (nrows - nr) * rowInc + (ncols - nc) * colInc;
        return new MatrixWindow(storage, nstart, nr, nc, rowInc, colInc);
    }

    /**
     * Bottom sub-matrix
     *
     * @param nr Number of rows. Could be 0.
     * @return The last n rows
     */
    public MatrixWindow bottom(int nr) {
        return new MatrixWindow(storage, start + nrows - nr, nr, ncols, rowInc, colInc);
    }

    /**
     * right sub-matrix
     *
     * @param nc Number of columns. Could be 0.
     * @return The nc right columns
     */
    public MatrixWindow right(int nc) {
        return new MatrixWindow(storage, start + (ncols - nc) * colInc, nrows, nc, rowInc, colInc);
    }

    //</editor-fold>    
    //<editor-fold defaultstate="collapsed" desc="toArray range">
    public DataWindow top() {
        return DataWindow.windowOf(storage, start, start + ncols * colInc, colInc);
    }

    public DataWindow left() {
        return DataWindow.windowOf(storage, start, start + nrows * rowInc, rowInc);
    }

    public DataWindow bottom() {
        int beg = start + (nrows - 1) * rowInc;
        return DataWindow.windowOf(storage, beg, beg + ncols * colInc, colInc);
    }

    public DataWindow right() {
        int beg = start + (ncols - 1) * colInc;
        return DataWindow.windowOf(storage, beg, beg + nrows * rowInc, rowInc);
    }

    DataBlock topOutside() {
        int beg = start - rowInc;
        return DataBlock.ofInternal(storage, beg, beg + ncols * colInc, colInc);
    }

    DataBlock leftOutside() {
        int beg = start - colInc;
        return DataBlock.ofInternal(storage, beg, beg + nrows * rowInc, rowInc);
    }

    DataBlock bottomOutside() {
        int beg = start + rowInc * nrows;
        return DataBlock.ofInternal(storage, beg, beg + ncols * colInc, colInc);
    }

    DataBlock rightOutside() {
        int beg = start + colInc * ncols;
        return DataBlock.ofInternal(storage, beg, beg + nrows * rowInc, rowInc);
    }

    //</editor-fold>    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (!isEmpty()) {
            DataBlockIterator rows = this.rowsIterator();
            builder.append(rows.next());
            while (rows.hasNext()) {
                builder.append(System.lineSeparator());
                builder.append(rows.next());
            }
        }
        return builder.toString();
    }

    public String toString(String fmt) {
        StringBuilder builder = new StringBuilder();
        if (!isEmpty()) {
            DataBlockIterator rows = this.rowsIterator();
            builder.append(rows.next().toString(fmt));
            while (rows.hasNext()) {
                builder.append(System.lineSeparator());
                builder.append(rows.next().toString(fmt));
            }
        }
        return builder.toString();
    }

    private static class RCIterator extends DataBlockIterator {

        private RCIterator(final DataBlock start, int niter, int inc) {
            super(start, niter, inc);
        }
    }

    private class Rows implements Iterator<DataBlock> {

        private int start, pos;
        private final int len;

        Rows() {
            pos = 0;
            start = Matrix.this.start;
            len = Matrix.this.colInc * Matrix.this.ncols;
        }

        @Override
        public boolean hasNext() {
            return pos < Matrix.this.nrows;
        }

        @Override
        public DataBlock next() {
            if (pos++ > 0) {
                start += Matrix.this.rowInc;
            }
            return DataBlock.ofInternal(Matrix.this.storage, start, start + len, Matrix.this.colInc);
        }
    }

    private class Columns implements Iterator<DataBlock> {

        private int start, pos;
        private final int len;

        Columns() {
            pos = 0;
            len = Matrix.this.rowInc * Matrix.this.nrows;
            start = Matrix.this.start;
        }

        @Override
        public boolean hasNext() {
            return pos < Matrix.this.ncols;
        }

        @Override
        public DataBlock next() {
            if (pos++ > 0) {
                start += Matrix.this.colInc;
            }
            return DataBlock.ofInternal(Matrix.this.storage, start, start + len, Matrix.this.rowInc);
        }
    }
}
