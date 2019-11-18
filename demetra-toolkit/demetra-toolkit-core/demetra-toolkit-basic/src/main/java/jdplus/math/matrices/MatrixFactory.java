/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices;

import demetra.math.matrices.MatrixType;
import jdplus.data.DataBlockIterator;
import java.util.Arrays;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class MatrixFactory {

    public Matrix rowBind(@NonNull MatrixType... M) {
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
        Matrix all = new Matrix(nr, nc);
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

    public Matrix columnBind(@NonNull MatrixType... M) {
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
        Matrix all = new Matrix(nr, nc);
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


    public Matrix select(MatrixType M, final int[] selectedRows, final int[] selectedColumns) {
        // TODO optimization
        Matrix m = new Matrix(selectedRows.length, selectedColumns.length);
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
    public Matrix  exclude(Matrix M, final int[] excludedRows, final int[] excludedColumns) {
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

        Matrix m = new Matrix(nrows - nrx, ncols - ncx);
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
    public Matrix expand(Matrix M, final int nr, final int[] rowPos, final int nc, final int[] colPos) {
        if (rowPos.length != nr || colPos.length != nc) {
            throw new MatrixException(MatrixException.DIM);
        }
        Matrix m = new Matrix(nr, nc);
        int nrows=M.getRowsCount(), ncols=M.getColumnsCount();
        for (int c = 0; c < ncols; ++c) {
            for (int r = 0; r < nrows; ++r) {
                m.set(rowPos[r], colPos[c], M.get(r, c));
            }
        }
        return m;
    }

}
