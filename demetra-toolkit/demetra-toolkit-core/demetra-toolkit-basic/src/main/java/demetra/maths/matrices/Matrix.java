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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.DoublePredicate;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;
import javax.annotation.Nonnegative;
import demetra.data.DoubleSeqCursor;
import demetra.data.LogSign;
import java.util.ArrayList;
import java.util.List;
import demetra.maths.matrices.internal.Householder;
import javax.annotation.Nonnull;
import demetra.data.DoubleSeq;
import java.util.Iterator;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface Matrix extends MatrixType.Mutable {
    
    public static final CanonicalMatrix EMPTY = new CanonicalMatrix(new double[0], 0, 0);
    
    /**
     * This version try to return the current object if its type is Matrix. To
     * be used with caution, only when the returned object is a temporary object
     * that is not modified. For optimization only.
     *
     * @param matrix
     * @return
     */
    public static Matrix ofInternal(MatrixType matrix) {
        if (matrix == null) {
            return null;
        }
        if (matrix instanceof Matrix) {
            return (Matrix) matrix;
        } else {
            return new CanonicalMatrix(matrix.toArray(), matrix.getRowsCount(), matrix.getColumnsCount());
        }
    }
    
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
    
    double[] getStorage();
    
    int getRowIncrement();
    
    int getColumnIncrement();
    
    int getStartPosition();
    
    int getPosition(int row, int col);
    
    int getLastPosition();
    
    void apply(int row, int col, DoubleUnaryOperator fn);
    
    String toString(String FMT);
 
    @Override
    DataBlock row(@Nonnull int irow);
    
    @Override
    DataBlock diagonal();
    
    @Override
    DataBlock subDiagonal(int pos);
    
    @Override
    DataBlock column(@Nonnull int icolumn);
    
    CanonicalMatrix asCanonical();
    
    SubMatrix asSubMatrix();
    
    default void set(final double d) {
        DataBlockIterator cols = columnsIterator();
        while (cols.hasNext()) {
            cols.next().set(d);
        }
    }
    
    default void set(final DoubleSupplier fn) {
        DataBlockIterator cols = columnsIterator();
        while (cols.hasNext()) {
            cols.next().set(fn);
        }
    }
    
    default void set(final MatrixFunction fn) {
        int c = 0;
        DataBlockIterator cols = columnsIterator();
        while (cols.hasNext()) {
            final int cur = c++;
            cols.next().set(r -> fn.apply(r, cur));
        }
    }
    
    default void add(int row, int col, double d) {
        if (d != 0) {
            apply(row, col, x -> x + d);
        }
    }
    
    default void sub(int row, int col, double d) {
        if (d != 0) {
            apply(row, col, x -> x - d);
        }
    }
    
    default void mul(int row, int col, double d) {
        if (d != 1) {
            apply(row, col, x -> x * d);
        }
    }
    
    default void div(int row, int col, double d) {
        if (d != 1) {
            apply(row, col, x -> x / d);
        }
    }
    
    default void apply(final DoubleUnaryOperator fn) {
        if (getColumnIncrement() == 1) {
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
    
    default void applyByRows(final Consumer<DataBlock> fn) {
        DataBlockIterator rows = rowsIterator();
        while (rows.hasNext()) {
            fn.accept(rows.next());
        }
    }
    
    default void applyByColumns(final Consumer<DataBlock> fn) {
        DataBlockIterator cols = columnsIterator();
        while (cols.hasNext()) {
            fn.accept(cols.next());
        }
    }
    
    default void applyByRows(final Matrix M, final BiConsumer<DataBlock, DataBlock> fn) {
        DataBlockIterator rows = rowsIterator();
        DataBlockIterator mrows = M.rowsIterator();
        while (rows.hasNext()) {
            fn.accept(rows.next(), mrows.next());
        }
    }
    
    default void applyByColumns(final Matrix M, final BiConsumer<DataBlock, DataBlock> fn) {
        DataBlockIterator cols = columnsIterator();
        DataBlockIterator mcols = M.columnsIterator();
        while (cols.hasNext()) {
            fn.accept(cols.next(), mcols.next());
        }
    }
    
    default double sum() {
        if (isEmpty()) {
            return 0;
        }
        if (getColumnIncrement() == 1) {
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
    
    default double ssq() {
        if (isEmpty()) {
            return 0;
        }
        if (getColumnIncrement() == 1) {
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
    
    default double frobeniusNorm() {
        double scale = 0;
        double ssq = 1;
        
        if (getColumnIncrement() == 1) {
            int ncols = getColumnsCount();
            DataBlockIterator rows = rowsIterator();
            while (rows.hasNext()) {
                DoubleSeqCursor cell = rows.next().cursor();
                for (int i = 0; i < ncols; ++i) {
                    double cur = cell.getAndNext();
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
            int nrows = getRowsCount();
            DataBlockIterator columns = columnsIterator();
            while (columns.hasNext()) {
                DoubleSeqCursor cell = columns.next().cursor();
                for (int i = 0; i < nrows; ++i) {
                    double cur = cell.getAndNext();
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
    
    default void add(final double d) {
        if (d != 0) {
            apply(x -> x + d);
        }
    }
    
    default void sub(final double d) {
        if (d != 0) {
            apply(x -> x - d);
        }
    }
    
    default void mul(final double d) {
        if (d == 0) {
            set(0);
        } else if (d != 1) {
            apply(x -> x * d);
        }
    }
    
    default void div(final double d) {
        if (d == 0) {
            set(Double.NaN);
        } else if (d != 1) {
            apply(x -> x / d);
        }
    }
    
    default boolean test(final DoublePredicate pred) {
        if (isEmpty()) {
            return true;
        }
        if (getColumnIncrement() == 1) {
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
    
    default CanonicalMatrix deepClone() {
        return new CanonicalMatrix(toArray(), getRowsCount(), getColumnsCount());
    }
    
    @Override
    default void copyTo(double[] data, final int start) {
        int pos = start;
        int nrows = getRowsCount();
        DataBlockIterator cols = columnsIterator();
        while (cols.hasNext()) {
            cols.next().copyTo(data, pos);
            pos += nrows;
        }
    }
    
    default void copy(Matrix M) {
        DataBlockIterator cols = columnsIterator();
        DataBlockIterator mcols = M.columnsIterator();
        while (cols.hasNext()) {
            cols.next().copy(mcols.next());
        }
    }

    /**
     *
     * @return
     */
    default Matrix transpose() {
        return new SubMatrix(getStorage(), getStartPosition(),
                getColumnsCount(), getRowsCount(),
                getColumnIncrement(), getRowIncrement());
    }
    
    boolean isCanonical();
    
    default boolean isDiagonal(double zero) {
        int nrows = getRowsCount(), ncols = getColumnsCount();
        if (ncols != nrows) {
            return false;
        }
        if (nrows == 1) {
            return true;
        }
        
        int rowInc = getRowIncrement(), colInc = getColumnIncrement();
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
    
    default boolean isIdentity() {
        int nrows = getRowsCount(), ncols = getColumnsCount();
        if (ncols != nrows) {
            return false;
        }
        if (nrows == 1) {
            return get(0, 0) == 1;
        }
        if (diagonal().anyMatch(x -> x != 1)) {
            return false;
        }
        
        int rowInc = getRowIncrement(), colInc = getColumnIncrement();
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
    
    default boolean isDiagonal() {
        return isDiagonal(0);
    }
    
    default boolean isZero(double eps) {
        return test(x -> Math.abs(x) <= eps);
    }
    
    default boolean isSymmetric(double eps) {
        int nrows = getRowsCount(), ncols = getColumnsCount();
        if (nrows != ncols) {
            return false;
        }
        if (nrows == 1) {
            return true;
        }
        DataBlock diag = diagonal();
        DataWindow ldiag = diag.window();
        DataWindow udiag = diag.window();
        int rowInc = getRowIncrement(), colInc = getColumnIncrement();
        for (int i = 1; i < nrows; ++i) {
            if (!ldiag.slideAndShrink(rowInc).allMatch(udiag.slideAndShrink(colInc),
                    (x, y) -> Math.abs(x - y) <= eps)) {
                return false;
            }
        }
        return true;
    }
    
    default boolean isSymmetric() {
        return isSymmetric(0);
    }
    
    default MatrixType unmodifiable() {
        return MatrixType.ofInternal(toArray(), getRowsCount(), getColumnsCount());
    }

    /**
     *
     * @param r0
     * @param nrows
     * @param ncols
     * @param c0
     * @return
     */
    @Override
    default SubMatrix extract(final int r0, final int nrows, final int c0, final int ncols) {
        double[] storage = getStorage();
        int start = getStartPosition(),
                rowInc = getRowIncrement(), colInc = getColumnIncrement();
        return new SubMatrix(storage, start + r0 * rowInc + c0 * colInc,
                nrows, ncols, rowInc, colInc);
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
    default SubMatrix extract(final int r0, final int c0, final int nrows,
            final int ncols, final int rowinc, final int colinc) {
        double[] storage = getStorage();
        int start = getStartPosition(),
                rowInc = getRowIncrement(), colInc = getColumnIncrement();
        return new SubMatrix(storage, start + r0 * rowInc + c0 * colInc,
                nrows, ncols, rowInc * rowinc, colInc * colinc);
    }
    
    default SubMatrix dropTopLeft(int nr, int nc) {
        double[] storage = getStorage();
        int start = getStartPosition(), nrows = getRowsCount(), ncols = getColumnsCount(),
                rowInc = getRowIncrement(), colInc = getColumnIncrement();
        return new SubMatrix(storage, start + nr * rowInc + nc * colInc,
                nrows - nr, ncols - nc, rowInc, colInc);
        
    }
    
    default SubMatrix dropBottomRight(int nr, int nc) {
        double[] storage = getStorage();
        int start = getStartPosition(), nrows = getRowsCount(), ncols = getColumnsCount(),
                rowInc = getRowIncrement(), colInc = getColumnIncrement();
        return new SubMatrix(storage, start,
                nrows - nr, ncols - nc, rowInc, colInc);
        
    }

    //<editor-fold defaultstate="collapsed" desc="In place operations">
    default void setAY(double a, Matrix Y) {
        if (a != 0) {
            if (getColumnIncrement() == 1 && Y.getColumnIncrement() == 1) {
                applyByRows(Y, (x, y) -> x.setAY(a, y));
            } else {
                applyByColumns(Y, (x, y) -> x.setAY(a, y));
            }
        }
    }
    
    default void addAY(double a, Matrix Y) {
        if (a != 0) {
            if (getColumnIncrement() == 1 && Y.getColumnIncrement() == 1) {
                applyByRows(Y, (x, y) -> x.addAY(a, y));
            } else {
                applyByColumns(Y, (x, y) -> x.addAY(a, y));
            }
        }
    }
    
    default void add(Matrix X) {
        if (getColumnIncrement() == 1 && X.getColumnIncrement() == 1) {
            applyByRows(X, (x, y) -> x.add(y));
        } else {
            applyByColumns(X, (x, y) -> x.add(y));
        }
    }
    
    default void sub(Matrix X) {
        if (getColumnIncrement() == 1 && X.getColumnIncrement() == 1) {
            applyByRows(X, (x, y) -> x.sub(y));
        } else {
            applyByColumns(X, (x, y) -> x.sub(y));
        }
    }
    
    static final int PROD_THRESHOLD = 6;

    /**
     *
     * @param lm
     * @param rm
     */
    default void product(final Matrix lm, final Matrix rm) {
        if (lm.getColumnsCount() < PROD_THRESHOLD * (lm.getRowsCount())) {
            DataBlockIterator cols = columnsIterator();
            DataBlockIterator rcols = rm.columnsIterator();
            DataBlockIterator lcols = lm.columnsIterator();
            double[] rx = rm.getStorage();
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
    }
    
    default void robustProduct(final Matrix lm, final Matrix rm, DoubleAccumulator acc) {
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
    
    default void addXaXt(final double a, final DataBlock x) {
        if (a == 0) {
            return;
        }
        double[] storage = getStorage();
        int start = getStartPosition(),
                rowInc = getRowIncrement(), colInc = getColumnIncrement();
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
    
    default void addXaYt(final double a, final DataBlock x, final DataBlock y) {
        if (a == 0) {
            return;
        }
        DataBlockIterator cols = columnsIterator();
        DoubleSeqCursor.OnMutable cursor = y.cursor();
        while (cols.hasNext()) {
            cols.next().addAY(a * cursor.getAndNext(), x);
        }
    }
    
    default void addXY(final Matrix X, final Matrix Y) {
        // Raw gaxpy implementation
        DataBlockIterator cols = X.columnsIterator();
        DataBlockIterator rows = Y.rowsIterator();
        while (cols.hasNext()) {
            addXaYt(1, cols.next(), rows.next());
        }
    }
    
    default void chs() {
        apply(x -> -x);
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Operations">
    default CanonicalMatrix times(Matrix B) {
        CanonicalMatrix AB = new CanonicalMatrix(getRowsCount(), B.getColumnsCount());
        if (isCanonical() && B.isCanonical()) {
            AB.addXY(asCanonical(), B.asCanonical());
        } else {
            AB.product(this, B);
        }
        return AB;
    }
    
    default CanonicalMatrix times(double d) {
        if (d == 0) {
            return new CanonicalMatrix(getRowsCount(), getColumnsCount());
        } else if (d == 1) {
            return deepClone();
        } else {
            CanonicalMatrix r = deepClone();
            r.mul(d);
            return r;
        }
    }
    
    default CanonicalMatrix plus(double d) {
        CanonicalMatrix r = deepClone();
        r.add(d);
        return r;
    }
    
    default CanonicalMatrix plus(Matrix B) {
        CanonicalMatrix AB = deepClone();
        if (B.isCanonical()) {
            AB.add(B.asCanonical());
        } else {
            AB.add(B);
        }
        return AB;
    }
    
    default CanonicalMatrix minus(double d) {
        CanonicalMatrix r = deepClone();
        r.sub(d);
        return r;
    }
    
    default CanonicalMatrix minus(Matrix B) {
        CanonicalMatrix AB = deepClone();
        if (B.isCanonical()) {
            AB.sub(B.asCanonical());
        } else {
            AB.sub(B);
        }
        return AB;
    }
    
    default CanonicalMatrix minus() {
        CanonicalMatrix r = deepClone();
        r.apply(x -> -x);
        return r;
    }

    //</editor-fold>
    DataBlockIterator rowsIterator();
    
    DataBlockIterator reverseRowsIterator();
    
    DataBlockIterator columnsIterator();
    
    DataBlockIterator reverseColumnsIterator();
    
    default Iterable<DataBlock> rows() {
        return () -> new Rows(this);
    }
    
    default Iterable<DataBlock> columns() {
        return () -> new Columns(this);
    }

    /**
     * Gets the columns of the matrix as a list of src block
     *
     * @return The list of all the columns.
     */
    default List<DataBlock> columnList() {
        ArrayList<DataBlock> rc = new ArrayList<>();
        int ncols = getColumnsCount();
        for (int i = 0; i < ncols; ++i) {
            rc.add(column(i));
        }
        return rc;
    }

    /**
     * Gets the columns of the matrix as a list of src block
     *
     * @return The list of all the columns.
     */
    default List<DataBlock> rowList() {
        ArrayList<DataBlock> rc = new ArrayList<>();
        int nrows = getRowsCount();
        for (int i = 0; i < nrows; ++i) {
            rc.add(row(i));
        }
        return rc;
    }

    /**
     * Shifts the matrix to the top-left corner. a(i,j) = a(i+n, j+n) for i in
     * [0, nrows-n[ and j in [0, ncols-n[ The cells that are not moved are not
     * modified
     *
     * @param n The displacement (n cells left and n cells up)
     */
    default void upLeftShift(@Nonnegative final int n) {
        double[] storage = getStorage();
        int start = getStartPosition(), nrows = getRowsCount(), ncols = getColumnsCount(),
                rowInc = getRowIncrement(), colInc = getColumnIncrement();
        int del = (rowInc + colInc) * n;
        for (int c = 0, i = start; c < ncols - n; ++c, i += colInc) {
            for (int r = 0, j = i; r < nrows - n; ++r, j += rowInc) {
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
    default void downRightShift(@Nonnegative final int n) {
        double[] storage = getStorage();
        int start = getStartPosition(), nrows = getRowsCount(), ncols = getColumnsCount(),
                rowInc = getRowIncrement(), colInc = getColumnIncrement();
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
    default MatrixWindow topLeft() {
        double[] storage = getStorage();
        int start = getStartPosition(),
                rowInc = getRowIncrement(), colInc = getColumnIncrement();
        return new MatrixWindow(storage, start, 0, 0, rowInc, colInc);
    }

    /**
     * Top-reader sub-matrix
     *
     * @param nr Number of rows. Could be 0.
     * @param nc Number of columns. Could be 0.
     * @return A nr src nc sub-matrix
     */
    default MatrixWindow topLeft(int nr, int nc) {
        double[] storage = getStorage();
        int start = getStartPosition(),
                rowInc = getRowIncrement(), colInc = getColumnIncrement();
        return new MatrixWindow(storage, start, nr, nc, rowInc, colInc);
    }

    /**
     * Top-reader sub-matrix
     *
     * @param nr Number of rows. Could be 0.
     * @return A nr src nc sub-matrix
     */
    default MatrixWindow top(int nr) {
        double[] storage = getStorage();
        int start = getStartPosition(), ncols = getColumnsCount(),
                rowInc = getRowIncrement(), colInc = getColumnIncrement();
        return new MatrixWindow(storage, start, nr, ncols, rowInc, colInc);
    }

    /**
     * Top-reader sub-matrix
     *
     * @param nc Number of columns. Could be 0.
     * @return A nr src nc sub-matrix
     */
    default MatrixWindow left(int nc) {
        double[] storage = getStorage();
        int start = getStartPosition(), nrows = getRowsCount(),
                rowInc = getRowIncrement(), colInc = getColumnIncrement();
        return new MatrixWindow(storage, start, nrows, nc, rowInc, colInc);
    }

    /**
     * bottom-right sub-matrix.
     *
     * @return An empty sub-matrix
     */
    default MatrixWindow bottomRight() {
        double[] storage = getStorage();
        int start = getStartPosition(), nrows = getRowsCount(), ncols = getColumnsCount(),
                rowInc = getRowIncrement(), colInc = getColumnIncrement();
        int nstart = start + nrows * rowInc + ncols * colInc;
        return new MatrixWindow(storage, nstart, 0, 0, rowInc, colInc);
    }

    /**
     * Bottom-right sub-matrix
     *
     * @param nr Number of rows. Could be 0.
     * @param nc Number of columns. Could be 0.
     * @return A nr src nc sub-matrix
     */
    default MatrixWindow bottomRight(int nr, int nc) {
        double[] storage = getStorage();
        int start = getStartPosition(), nrows = getRowsCount(), ncols = getColumnsCount(),
                rowInc = getRowIncrement(), colInc = getColumnIncrement();
        int nstart = start + (nrows - nr) * rowInc + (ncols - nc) * colInc;
        return new MatrixWindow(storage, nstart, nr, nc, rowInc, colInc);
    }

    /**
     * Bottom sub-matrix
     *
     * @param nr Number of rows. Could be 0.
     * @return The last n rows
     */
    default MatrixWindow bottom(int nr) {
        double[] storage = getStorage();
        int start = getStartPosition(), nrows = getRowsCount(), ncols = getColumnsCount(),
                rowInc = getRowIncrement(), colInc = getColumnIncrement();
        return new MatrixWindow(storage, start + nrows - nr, nr, ncols, rowInc, colInc);
    }

    /**
     * right sub-matrix
     *
     * @param nc Number of columns. Could be 0.
     * @return The nc right columns
     */
    default MatrixWindow right(int nc) {
        double[] storage = getStorage();
        int start = getStartPosition(), nrows = getRowsCount(), ncols = getColumnsCount(),
                rowInc = getRowIncrement(), colInc = getColumnIncrement();
        return new MatrixWindow(storage, start + (ncols - nc) * colInc, nrows, nc, rowInc, colInc);
    }

    //</editor-fold>    
    //<editor-fold defaultstate="collapsed" desc="toArray range">
    default DataWindow top() {
        double[] storage = getStorage();
        int start = getStartPosition(), ncols = getColumnsCount(),
                colInc = getColumnIncrement();
        return DataWindow.windowOf(storage, start, start + ncols * colInc, colInc);
    }
    
    default DataWindow left() {
        double[] storage = getStorage();
        int start = getStartPosition(), nrows = getRowsCount(),
                rowInc = getRowIncrement();
        return DataWindow.windowOf(storage, start, start + nrows * rowInc, rowInc);
    }
    
    default DataWindow bottom() {
        double[] storage = getStorage();
        int start = getStartPosition(), nrows = getRowsCount(), ncols = getColumnsCount(),
                rowInc = getRowIncrement(), colInc = getColumnIncrement();
        int beg = start + (nrows - 1) * rowInc;
        return DataWindow.windowOf(storage, beg, beg + ncols * colInc, colInc);
    }
    
    default DataWindow right() {
        double[] storage = getStorage();
        int start = getStartPosition(), nrows = getRowsCount(), ncols = getColumnsCount(),
                rowInc = getRowIncrement(), colInc = getColumnIncrement();
        int beg = start + (ncols - 1) * colInc;
        return DataWindow.windowOf(storage, beg, beg + nrows * rowInc, rowInc);
    }
    
    public static LogSign logDeterminant(Matrix X) {
        if (!X.isSquare()) {
            throw new IllegalArgumentException();
        }
        Householder hous = new Householder(true);
        hous.decompose(X);
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
