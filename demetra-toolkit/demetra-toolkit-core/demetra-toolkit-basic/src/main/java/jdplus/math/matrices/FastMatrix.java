/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices;

import demetra.math.matrices.MatrixType;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;

/**
 *
 * @author palatej
 */
public interface FastMatrix extends MatrixType.Mutable {

    double[] getStorage();

    int getStartPosition();

    int getColumnIncrement();

    default void copy(FastMatrix B) {
        int ncols = getColumnsCount(), nrows = getRowsCount();
        if (nrows != B.getRowsCount() || ncols != B.getColumnsCount()) {
            throw new MatrixException(MatrixException.DIM);
        }
        DataBlockIterator cols = columnsIterator();
        DataBlockIterator bcols = B.columnsIterator();
        while (cols.hasNext()) {
            cols.next().copy(bcols.next());
        }
    }

    default void copyTranspose(FastMatrix B) {
        int ncols = getColumnsCount(), nrows = getRowsCount();
        if (ncols != B.getRowsCount() || nrows != B.getColumnsCount()) {
            throw new MatrixException(MatrixException.DIM);
        }
        DataBlockIterator cols = columnsIterator();
        DataBlockIterator brows = B.rowsIterator();
        while (cols.hasNext()) {
            cols.next().copy(brows.next());
        }
    }

    @Override
    default DataBlock diagonal() {
        int start = getStartPosition();
        int n = Math.min(getRowsCount(), getColumnsCount()), inc = 1 + getColumnIncrement();
        return DataBlock.of(getStorage(), start, start + inc * n, inc);
    }

    /**
     *
     * @param pos
     * @return
     */
    @Override
    default DataBlock subDiagonal(int pos) {
        int ncols = getColumnsCount(), colInc = getColumnIncrement(),
                nrows = getRowsCount(), start = getStartPosition();
        if (pos >= ncols) {
            return DataBlock.EMPTY;
        }
        if (-pos >= nrows) {
            return DataBlock.EMPTY;
        }
        int beg = start, inc = 1 + colInc;
        int n;
        if (pos > 0) {
            beg += pos * colInc;
            n = Math.min(nrows, ncols - pos);
        } else if (pos < 0) {
            beg -= pos;
            n = Math.min(nrows + pos, ncols);
        } else {
            n = Math.min(nrows, ncols);
        }
        return DataBlock.of(getStorage(), beg, beg + inc * n, inc);
    }

    @Override
    default DataBlock row(int r) {
        int inc = getColumnIncrement();
        int beg = getStartPosition() + r, end = beg + getColumnsCount() * inc;
        return DataBlock.of(getStorage(), beg, end, inc);
    }

    @Override
    default DataBlock column(int c) {
        int beg = getStartPosition() + c * getColumnIncrement(), end = beg + getRowsCount();
        return DataBlock.of(getStorage(), beg, end, 1);
    }

    default DataBlockIterator rowsIterator() {
        return new RCIterator(topOutside(), getRowsCount(), 1);
    }

    default DataBlockIterator reverseRowsIterator() {
        return new RCIterator(bottomOutside(), getRowsCount(), -1);
    }

    default DataBlockIterator columnsIterator() {
        return new RCIterator(leftOutside(), getColumnsCount(), getColumnIncrement());
    }

    default DataBlockIterator reverseColumnsIterator() {
        return new RCIterator(rightOutside(), getColumnsCount(), -getColumnIncrement());
    }

    default DataBlock topOutside() {
        int beg = getStartPosition() - 1,
                inc = getColumnIncrement();
        return DataBlock.of(getStorage(), beg, beg + getColumnsCount() * inc, inc);
    }

    default DataBlock leftOutside() {
        int beg = getStartPosition() - getColumnIncrement();
        return DataBlock.of(getStorage(), beg, beg + getRowsCount(), 1);
    }

    default DataBlock bottomOutside() {
        int beg = getStartPosition() + getRowsCount(), inc = getColumnIncrement();

        return DataBlock.of(getStorage(), beg, beg + getColumnsCount() * inc, inc);
    }

    default DataBlock rightOutside() {
        int beg = getStartPosition() + getColumnIncrement() * getColumnsCount();
        return DataBlock.of(getStorage(), beg, beg + getRowsCount(), 1);
    }
}

class RCIterator extends DataBlockIterator {

    RCIterator(final DataBlock start, int niter, int inc) {
        super(start, niter, inc);
    }
}
