/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices;

import demetra.data.DoubleSeq;
import demetra.util.IntList;
import jdplus.data.DataBlockIterator;
import java.util.Arrays;
import java.util.function.DoublePredicate;
import jdplus.data.DataBlock;
import org.checkerframework.checker.nullness.qual.NonNull;
import demetra.math.matrices.Matrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class MatrixFactory {
    
    public FastMatrix rowMatrix(DoubleSeq data){
        return new FastMatrix(data.toArray(), 1, data.length());
    }

    public FastMatrix columnMatrix(DoubleSeq data){
        return new FastMatrix(data.toArray(), data.length(), 1);
    }

    public FastMatrix rowBind(@NonNull Matrix... M) {
        int nr = 0;
        int nc = 0;
        for (int i = 0; i < M.length; ++i) {
            if (M[i] != null) {
                nr += M[i].getRowsCount();
                if (nc == 0) {
                    nc = M[i].getColumnsCount();
                } else if (M[i].getColumnsCount() != nc) {
                    throw new MatrixException(MatrixException.DIM);
                }
            }
        }
        FastMatrix all = new FastMatrix(nr, nc);
        DataBlockIterator rows = all.rowsIterator();
        for (int i = 0; i < M.length; ++i) {
            if (M[i] != null) {
                int ncur = M[i].getRowsCount();
                for (int j = 0; j < ncur; ++j) {
                    rows.next().copy(M[i].row(j));
                }
            }
        }
        return all;
    }
    
    public FastMatrix columnBind(@NonNull Matrix... M) {
        int nr = 0;
        int nc = 0;
        for (int i = 0; i < M.length; ++i) {
            if (M[i] != null) {
                nc += M[i].getColumnsCount();
                if (nr == 0) {
                    nr = M[i].getRowsCount();
                } else if (M[i].getRowsCount() != nr) {
                    throw new MatrixException(MatrixException.DIM);
                }
            }
        }
        FastMatrix all = new FastMatrix(nr, nc);
        DataBlockIterator cols = all.columnsIterator();
        for (int i = 0; i < M.length; ++i) {
            if (M[i] != null) {
                int ncur = M[i].getColumnsCount();
                for (int j = 0; j < ncur; ++j) {
                    cols.next().copy(M[i].column(j));
                }
            }
        }
        return all;
    }

    public static FastMatrix select(FastMatrix m, IntList selectedRows, IntList selectedColumns) {
        if (m == null) {
            return null;
        }
        if (selectedRows == null) {
            return selectColumns(m, selectedColumns);
        }
        if (selectedColumns == null) {
            return selectRows(m, selectedRows);
        }

        FastMatrix s = FastMatrix.make(selectedRows.size(), selectedColumns.size());
        double[] ps = s.getStorage(), pm = m.getStorage();

        int scur = 0;

        for (int c = 0; c < s.ncols; ++c) {
            DataBlock mcol = m.column(selectedColumns.get(c));
            int mcur = mcol.getStartPosition();
            for (int r = 0; r < s.nrows; ++r) {
                ps[scur++] = pm[mcur + selectedRows.get(r)];
            }
        }
        return s;
    }

    public static FastMatrix selectRows(FastMatrix m, @NonNull IntList selectedRows) {
        if (m == null) {
            return null;
        }
        if (selectedRows == null) {
            return m;
        }
        FastMatrix s = FastMatrix.make(selectedRows.size(), m.ncols);
        double[] ps = s.getStorage(), pm = m.getStorage();

        int scur = 0;
        DataBlockIterator cols = m.columnsIterator();
        while (cols.hasNext()) {
            DataBlock mcol = cols.next();
            int mcur = mcol.getStartPosition();
            for (int r = 0; r < s.nrows; ++r) {
                ps[scur++] = pm[mcur + selectedRows.get(r)];
            }
        }
        return s;

    }

    public static FastMatrix selectColumns(FastMatrix m, @NonNull IntList selectedColumns) {
        if (m == null) {
            return null;
        }
        if (selectedColumns == null) {
            return m;
        }
        FastMatrix s = FastMatrix.make(m.nrows, selectedColumns.size());
        double[] ps = s.getStorage();

        int scur = 0;
        for (int c = 0; c < s.ncols; ++c) {
            DataBlock mcol = m.column(selectedColumns.get(c));
            mcol.copyTo(ps, scur);
            scur += s.nrows;
        }
        return s;
    }

    public static FastMatrix selectRows(FastMatrix m, @NonNull int[] selectedRows) {
        if (m == null) {
            return null;
        }
        if (selectedRows == null) {
            return m;
        }
        FastMatrix s = FastMatrix.make(selectedRows.length, m.ncols);
        double[] ps = s.getStorage(), pm = m.getStorage();

        int scur = 0;
        DataBlockIterator cols = m.columnsIterator();
        while (cols.hasNext()) {
            DataBlock mcol = cols.next();
            int mcur = mcol.getStartPosition();
            for (int r = 0; r < s.nrows; ++r) {
                ps[scur++] = pm[mcur + selectedRows[r]];
            }
        }
        return s;

    }

    public static FastMatrix selectColumns(FastMatrix m, @NonNull int[] selectedColumns) {
        if (m == null) {
            return null;
        }
        if (selectedColumns == null) {
            return m;
        }
        FastMatrix s = FastMatrix.make(m.nrows, selectedColumns.length);
        double[] ps = s.getStorage();

        int scur = 0;
        for (int c = 0; c < s.ncols; ++c) {
            DataBlock mcol = m.column(selectedColumns[c]);
            mcol.copyTo(ps, scur);
            scur += s.nrows;
        }
        return s;
    }

    public static FastMatrix embed(DoubleSeq x, int dim) {
        int n = x.length();
        if (dim > n) {
            throw new IllegalArgumentException();
        }
        int m = n - dim + 1;
        FastMatrix E = FastMatrix.make(m, dim);
        double[] pe = E.getStorage();
        for (int i = dim - 1, j = 0; i >= 0; --i, j += m) {
            x.range(i, i + m).copyTo(pe, j);
        }
        return E;
    }

    public static FastMatrix embed(FastMatrix X, int dim) {
        int n = X.nrows, q = X.getColumnsCount();
        if (dim > n) {
            throw new IllegalArgumentException();
        }
        int m = n - dim + 1;
        FastMatrix E = FastMatrix.make(m, q * dim);
        double[] pe = E.getStorage();
        for (int i = dim - 1, j = 0; i >= 0; --i) {
            for (int k = 0; k < q; ++k, j += m) {
                X.column(k).range(i, i + m).copyTo(pe, j);
            }
        }
        return E;
    }

    /**
     * Apply differencing on the columns of the matrix
     *
     * @param X The original matrix
     * @param lag The lag of the differences
     * @param pow The Power of the differencing
     * @return A smaller matrix is returned
     */
    public static FastMatrix delta(FastMatrix X, int lag, int pow) {
        if (X.isEmpty()) {
            return X;
        }
        if (pow <= 0 || lag <= 0) {
            throw new IllegalArgumentException();
        }
        if (pow > 1) {
            return delta(delta(X, lag, 1), lag, pow - 1);
        }
        int n = X.nrows, m = X.ncols;
        if (n < lag) {
            return FastMatrix.make(0, m);
        }
        FastMatrix D = FastMatrix.make(n - lag, m);
        double[] pd=D.getStorage(), px=X.getStorage();
        for (int i=0, j=0, k=X.start; i<m; ++i, k+=X.getColumnIncrement()){
            int rmax=k+D.nrows;
            for (int r=k; r<rmax; ++r, ++j){
                pd[j]=px[r+lag]-px[r];
            }
        }
        return D;
    }

    public FastMatrix select(FastMatrix M, final int[] selectedRows, final int[] selectedColumns) {
        // TODO optimization
        if (selectedRows == null) {
            return selectColumns(M, selectedColumns);
        }
        if (selectedColumns == null) {
            return selectRows(M, selectedRows);
        }
        FastMatrix m = new FastMatrix(selectedRows.length, selectedColumns.length);
        for (int c = 0; c < selectedRows.length; ++c) {
            for (int r = 0; r < selectedRows.length; ++r) {
                m.set(r, c, M.get(selectedRows[r], selectedColumns[c]));
            }
        }
        return m;
    }

    /**
     * Creates a new matrix which doesn't contain given rows/columns
     *
     * @param M
     * @param excludedRows
     * @param excludedColumns
     * @return A new matrix, based on another storage, is returned.
     */
    public FastMatrix  exclude(FastMatrix M, final int[] excludedRows, final int[] excludedColumns) {
        int[] srx = excludedRows.clone();
        Arrays.sort(srx);
        int[] scx = excludedColumns.clone();
        Arrays.sort(scx);
        int nrows=M.getRowsCount(), ncols=M.getColumnsCount();
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
            return M.deepClone();
        }

        FastMatrix m = new FastMatrix(nrows - nrx, ncols - ncx);
        for (int c = 0, nc = 0; c < ncols; ++c) {
            if (cx[c]) {
                for (int r = 0, nr = 0; r < nrows; ++r) {
                    if (rx[r]) {
                        m.set(nr, nc, M.get(r, c));
                        ++nr;
                    }
                }
                ++nc;
            }
        }
        return m;
    }

    /**
     * Creates a new matrix which contains the current matrix at given row/col
     * position
     *
     * @param M
     * @param nr
     * @param rowPos
     * @param nc
     * @param colPos
     * @return A new matrix, based on another storage, is returned.
     */
    public FastMatrix expand(FastMatrix M, final int nr, final int[] rowPos, final int nc, final int[] colPos) {
        if (rowPos.length != nr || colPos.length != nc) {
            throw new MatrixException(MatrixException.DIM);
        }
        FastMatrix m = new FastMatrix(nr, nc);
        int nrows=M.getRowsCount(), ncols=M.getColumnsCount();
        for (int c = 0; c < ncols; ++c) {
            for (int r = 0; r < nrows; ++r) {
                m.set(rowPos[r], colPos[c], M.get(r, c));
            }
        }
        return m;
    }

    /**
     * Removes the rows containing an item that doesn't match the predicate
     *
     * @param pred The condition that must be fulfilled by all the rows items to
     * select it
     * @param selection The index of the selected rows
     * @return The cleaned matrix is returned or this if the original matrix is
     * clean
     */
    public FastMatrix cleanRows(FastMatrix M, final @NonNull DoublePredicate pred, final @NonNull IntList selection) {
        selection.clear();
        DataBlockIterator rows = M.rowsIterator();
        int pos = 0;
        while (rows.hasNext()) {
            if (rows.next().allMatch(pred)) {
                selection.add(pos);
            }
            ++pos;
        }
        if (selection.size() == M.nrows) {
            return M;
        }
        return selectRows(M, selection);
    }
}
