/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.math.matrices;

import demetra.data.DoubleSeq;
import demetra.design.Development;
import java.util.function.DoubleUnaryOperator;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Development(status = Development.Status.Release)
class LightMutableMatrix implements MatrixType.Mutable{

    private final double[] storage;
    private final int nrows, ncolumns;

    LightMutableMatrix(final double[] storage, final int nrows, final int ncolumns) {
        this.storage = storage;
        this.nrows = nrows;
        this.ncolumns = ncolumns;
    }
    
    @Override
     public double[] toArray(){
        return storage.clone();
    }

    @Override
    public double get(int row, int column) throws IndexOutOfBoundsException {
        if (row < 0 || row >= nrows || column < 0 || column >= ncolumns) {
            throw new IndexOutOfBoundsException();
        }
        return storage[row + column * nrows];
    }

    @Override
    public void set(int row, int column, double value) throws IndexOutOfBoundsException {
        if (row < 0 || row >= nrows || column < 0 || column >= ncolumns) {
            throw new IndexOutOfBoundsException();
        }
        storage[row + column * nrows]=value;
    }

    @Override
    public void apply(int row, int column, DoubleUnaryOperator fn) throws IndexOutOfBoundsException {
        if (row < 0 || row >= nrows || column < 0 || column >= ncolumns) {
            throw new IndexOutOfBoundsException();
        }
        int idx=row + column * nrows;
        storage[idx]=fn.applyAsDouble(storage[idx]);
    }

    @Override
    public DoubleSeq.Mutable row(int irow) {
        if (irow < 0 || irow >= nrows) {
            throw new IndexOutOfBoundsException();
        }
         return DoubleSeq.Mutable.of(storage, irow, ncolumns, nrows);
    }

    @Override
    public DoubleSeq.Mutable column(int icolumn) {
        if (icolumn < 0 || icolumn >= ncolumns) {
            throw new IndexOutOfBoundsException();
        }
        return DoubleSeq.Mutable.of(storage, icolumn * nrows, nrows, 1);
    }

    @Override
    public DoubleSeq.Mutable subDiagonal(int pos) {
        if (pos >= ncolumns) {
            return DoubleSeq.Mutable.EMPTY;
        }
        if (-pos >= nrows) {
            return DoubleSeq.Mutable.EMPTY;
        }
        int beg = 0, inc = 1 + nrows;
        int n;
        if (pos > 0) {
            beg = pos * nrows;
            n = Math.min(nrows, ncolumns - pos);
        } else if (pos < 0) {
            beg = -pos;
            n = Math.min(nrows + pos, ncolumns);
        } else {
            n = Math.min(nrows, ncolumns);
        }
        return DoubleSeq.Mutable.of(storage, beg, n, inc);
    }

    @Override
    public DoubleSeq.Mutable diagonal() {
        int inc = 1 + nrows;
        int n = Math.min(nrows, ncolumns);
        return DoubleSeq.Mutable.of(storage, 0, n, inc);
    }

    @Override
    public int getColumnsCount() {
        return ncolumns;
    }

    @Override
    public int getRowsCount() {
        return nrows;
    }

    @Override
    public String toString(){
        return MatrixType.format(this);
    }
    
}
