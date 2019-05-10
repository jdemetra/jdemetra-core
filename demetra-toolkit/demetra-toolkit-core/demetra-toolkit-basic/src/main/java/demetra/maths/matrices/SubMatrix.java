/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths.matrices;

import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.design.Unsafe;
import java.util.function.DoubleUnaryOperator;

/**
 *
 * @author Jean Palate
 */
public class SubMatrix implements FastMatrix {

    protected final double[] storage;
    protected int start, nrows, ncols;
    protected final int rowInc, colInc;

    /**
     *
     * @param data
     * @param start
     * @param nrows
     * @param ncols
     * @param rowinc
     * @param colinc
     */
    SubMatrix(final double[] data, final int start, final int nrows,
            final int ncols, final int rowinc, final int colinc) {
        this.storage = data;
        this.nrows = nrows;
        this.ncols = ncols;
        this.start = start;
        this.rowInc = rowinc;
        this.colInc = colinc;
    }

    @Override
    public boolean isCanonical() {
        return start == 0 && rowInc == 1 && storage.length == nrows * ncols;
    }

    @Override
    @Unsafe
    public CanonicalMatrix asCanonical() {
        if (isCanonical()) {
            return new CanonicalMatrix(storage, nrows, ncols);
        } else {
            return new CanonicalMatrix(toArray(), nrows, ncols);
        }
    }

    @Override
    public SubMatrix asSubMatrix() {
        return this;
    }

    @Override
    public final DataBlock diagonal() {
        int n = Math.min(nrows, ncols), inc = rowInc + colInc;
        return DataBlock.of(storage, start, start + inc * n, inc);
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
        return DataBlock.of(storage, beg, beg + inc * n, inc);
    }

    @Override
    public DataBlock row(int r) {
        int beg = start + r * rowInc, end = beg + ncols * colInc;
        return DataBlock.of(storage, beg, end, colInc);
    }

    @Override
    public DataBlock column(int c) {
        int beg = start + c * colInc, end = beg + nrows * rowInc;
        return DataBlock.of(storage, beg, end, rowInc);
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
    @Override
    public final double[] getStorage() {
        return storage;
    }

    /**
     * Position of the top-reader cell
     *
     * @return
     */
    @Override
    public final int getStartPosition() {
        return start;
    }

    @Override
    public final int getPosition(int row, int col) {
        return start + row * rowInc + col * colInc;
    }

    /**
     * Position of the bottom-right cell
     *
     * @return
     */
    @Override
    public final int getLastPosition() {
        return start + (nrows - 1) * rowInc + (ncols - 1) * colInc;
    }

    @Override
    public final int getRowIncrement() {
        return rowInc;
    }

    @Override
    public final int getColumnIncrement() {
        return colInc;
    }

    /**
     *
     * @param row
     * @param col
     * @param value
     */
    @Override
    public void set(final int row, final int col, final double value) {
        storage[start + row * rowInc + col * colInc] = value;
    }

    /**
     *
     * @param row
     * @param col
     * @param fn
     */
    @Override
    public void apply(final int row, final int col, final DoubleUnaryOperator fn) {
        int idx = start + row * rowInc + col * colInc;
        storage[idx] = fn.applyAsDouble(storage[idx]);
    }

    @Override
    public final DataBlockIterator rowsIterator() {
        return new RCIterator(topOutside(), nrows, rowInc);
    }

    @Override
    public final DataBlockIterator reverseRowsIterator() {
        return new RCIterator(bottomOutside(), nrows, -rowInc);
    }

    @Override
    public final DataBlockIterator columnsIterator() {
        return new RCIterator(leftOutside(), ncols, colInc);
    }

    @Override
    public final DataBlockIterator reverseColumnsIterator() {
        return new RCIterator(rightOutside(), ncols, -colInc);
    }

    DataBlock topOutside() {
        int beg = start - rowInc;
        return DataBlock.of(storage, beg, beg + ncols * colInc, colInc);
    }

    DataBlock leftOutside() {
        int beg = start - colInc;
        return DataBlock.of(storage, beg, beg + nrows * rowInc, rowInc);
    }

    DataBlock bottomOutside() {
        int beg = start + rowInc * nrows;
        return DataBlock.of(storage, beg, beg + ncols * colInc, colInc);
    }

    DataBlock rightOutside() {
        int beg = start + colInc * ncols;
        return DataBlock.of(storage, beg, beg + nrows * rowInc, rowInc);
    }

    private static class RCIterator extends DataBlockIterator {

        private RCIterator(final DataBlock start, int niter, int inc) {
            super(start, niter, inc);
        }
    }

    @Override
    public String toString(String fmt) {
        return Matrix.format(this, fmt);
    }

    @Override
    public String toString() {
        return Matrix.format(this);
    }

}
