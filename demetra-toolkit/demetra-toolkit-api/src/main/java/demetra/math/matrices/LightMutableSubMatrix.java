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
package demetra.math.matrices;

import demetra.design.Development;
import demetra.data.DoubleSeq;
import java.util.function.DoubleUnaryOperator;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
class LightMutableSubMatrix implements MatrixType.Mutable {

    private final MatrixType.Mutable core;
    private final int r0, nr, c0, nc;

    LightMutableSubMatrix(final MatrixType.Mutable core, int r0, int nr, int c0, int nc) {
        this.core = core;
        this.r0 = r0;
        this.nr = nr;
        this.c0 = c0;
        this.nc = nc;
    }

    @Override
    public double get(int row, int column) throws IndexOutOfBoundsException {
        if (row < 0 || row >= nr || column < 0 || column >= nc) {
            throw new IndexOutOfBoundsException();
        }
        return core.get(row + r0, column + c0);
    }

    @Override
    public void set(int row, int column, double value) throws IndexOutOfBoundsException {
        if (row < 0 || row >= nr || column < 0 || column >= nc) {
            throw new IndexOutOfBoundsException();
        }
        core.set(row + r0, column + c0, value);
    }

    @Override
    public void apply(int row, int column, DoubleUnaryOperator fn) throws IndexOutOfBoundsException {
        if (row < 0 || row >= nr || column < 0 || column >= nc) {
            throw new IndexOutOfBoundsException();
        }
        core.apply(row + r0, column + c0, fn);
    }

    @Override
    public DoubleSeq.Mutable row(int irow) {
        if (irow < 0 || irow >= nr) {
            throw new IndexOutOfBoundsException();
        }
        return core.row(r0 + irow).extract(c0, nc);
    }

    @Override
    public DoubleSeq.Mutable column(int icolumn) {
        if (icolumn < 0 || icolumn >= nc) {
            throw new IndexOutOfBoundsException();
        }
        return core.column(c0 + icolumn).extract(r0, nr);
    }

    @Override
    public int getColumnsCount() {
        return nc;
    }

    @Override
    public int getRowsCount() {
        return nr;
    }

    @Override
    public DoubleSeq.Mutable diagonal() {
        DoubleSeq.Mutable d = core.subDiagonal(c0 - r0);
        int start = Math.min(r0, c0);
        int n = Math.min(nr, nc);
        return d.extract(start, n);
    }

    @Override
    public DoubleSeq.Mutable subDiagonal(int pos) {
        if (pos > nc || pos < -nr) {
            return DoubleSeq.Mutable.EMPTY;
        }
        int del = c0 - r0;
        int cpos=del+pos;
        DoubleSeq.Mutable d = core.subDiagonal(cpos);
        int start = 0;
        int n;
        if (del <= 0){
            if (pos <= 0)
                start=c0;
            else if (pos < -del)
                start=c0+pos;
            else
                start=r0;
        }
        else{ // del >O
            if (pos >= 0)
                start=r0;
            else if (pos > -del)
                start=r0-pos;
            else
                start=c0;
        } 
        if (pos < 0) {
            n = Math.min(nr + pos, nc);
        } else {
            n = Math.min(nr, nc-pos);
        }
        return d.extract(start, n);
    }

    @Override
    public String toString(){
        return MatrixType.format(this);
    }
}
