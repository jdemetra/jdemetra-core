/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.maths;

import demetra.design.Development;
import demetra.data.DoubleSeq;
import demetra.data.Doubles;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
class LightMatrix implements MatrixType {

    private final double[] storage;
    private final int nrows, ncolumns;

    LightMatrix(final double[] storage, final int nrows, final int ncolumns) {
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
    public DoubleSeq row(int irow) {
        if (irow < 0 || irow >= nrows) {
            throw new IndexOutOfBoundsException();
        }
        return DoubleSeq.of(storage, irow, ncolumns, nrows);
    }

    @Override
    public DoubleSeq column(int icolumn) {
        if (icolumn < 0 || icolumn >= ncolumns) {
            throw new IndexOutOfBoundsException();
        }
        return DoubleSeq.of(storage, icolumn * nrows, nrows);
    }

    @Override
    public DoubleSeq subDiagonal(int pos) {
        if (pos >= ncolumns) {
            return Doubles.EMPTY;
        }
        if (-pos >= nrows) {
            return Doubles.EMPTY;
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
        return DoubleSeq.of(storage, beg, n, inc);
    }

    @Override
    public DoubleSeq diagonal() {
        int inc = 1 + nrows;
        int n = Math.min(nrows, ncolumns);
        return DoubleSeq.of(storage, 0, n, inc);
    }

    @Override
    public int getColumnsCount() {
        return ncolumns;
    }

    @Override
    public int getRowsCount() {
        return nrows;
    }

}
