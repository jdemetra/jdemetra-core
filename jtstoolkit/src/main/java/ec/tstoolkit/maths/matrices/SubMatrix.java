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

import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.NewObject;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SubMatrix implements Cloneable {

    final double[] m_data;
    final int m_row_inc, m_col_inc;
    int m_start, m_nrows, m_ncols;

    /**
     * Creates a new instance of SubMatrix
     *
     * @param data
     * @param nrows
     * @param ncols
     */
    public SubMatrix(final double[] data, final int nrows, final int ncols) {
        m_data = data;
        m_nrows = nrows;
        m_ncols = ncols;
        m_row_inc = 1;
        m_col_inc = nrows;
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
    public SubMatrix(final double[] data, final int start, final int nrows,
            final int ncols, final int rowinc, final int colinc) {
        m_data = data;
        m_start = start;
        m_nrows = nrows;
        m_ncols = ncols;
        m_row_inc = rowinc;
        m_col_inc = colinc;
    }

    /**
     *
     * @param m
     */
    public SubMatrix(final SubMatrix m) {
        m_data = m.m_data;
        m_start = m.m_start;
        m_nrows = m.m_nrows;
        m_ncols = m.m_ncols;
        m_row_inc = m.m_row_inc;
        m_col_inc = m.m_col_inc;
    }

    /**
     *
     * @param val
     */
    public void add(final double val) {
        if (val == 0) {
            return;
        }
        if (m_row_inc == 1) {
            for (int c = 0, ic = m_start; c < m_ncols; ++c, ic += m_col_inc) {
                for (int r = 0, ir = ic; r < m_nrows; ++r, ++ir) {
                    m_data[ir] += val;
                }
            }
        } else if (m_col_inc == 1) {
            for (int r = 0, ir = m_start; r < m_nrows; ++r, ir += m_row_inc) {
                for (int c = 0, ic = ir; c < m_ncols; ++c, ++ic) {
                    m_data[ic] += val;
                }
            }
        } else {
            for (int c = 0, ic = m_start; c < m_ncols; ++c, ic += m_col_inc) {
                for (int r = 0, ir = ic; r < m_nrows; ++r, ir += m_row_inc) {
                    m_data[ir] += val;
                }
            }
        }
    }

    /**
     *
     * @param row
     * @param col
     * @param val
     */
    public void add(final int row, final int col, final double val) {
        m_data[m_start + row * m_row_inc + col * m_col_inc] += val;
    }

    /**
     *
     * @param m
     */
    public void add(final SubMatrix m) {
        // special handling of full matrices
        if (isFull() && m.isFull()) {
            for (int i = 0; i < m_data.length; ++i) {
                m_data[i] += m.m_data[i];
            }
            return;
        }

        // if (m_nrows != m.m_nrows || m_ncols != m.m_ncols)
        // throw new MatrixException(MatrixException.IncompatibleDimensions);
        DataBlockIterator iter, siter;
        if (m.m_row_inc == 1 && m_row_inc == 1) {
            iter = columns();
            siter = m.columns();
        } else {
            iter = rows();
            siter = m.rows();
        }

        DataBlock icur = iter.getData(), sicur = siter.getData();
        do {
            icur.add(sicur);
        } while (iter.next() && siter.next());
    }

    /**
     * Adds the current submatrix to the submatrix given by its top/left position (i,j) in another submatrix
     * @param i The row index
     * @param j TheColumn index
     * @param m The target submatrix
     */
    public void addTo(SubMatrix m, int i, int j) {
        int scur=m_start, tcur=m.m_start + i * m.m_row_inc + j * m.m_col_inc;
        for (int c=0; c<m_ncols; ++c, scur+=m_col_inc, tcur+=m.m_col_inc){
            for (int r=0, sidx=scur, tidx=tcur; r<m_nrows; ++r, sidx+=m_row_inc, tidx+=m.m_row_inc){
                m.m_data[tidx]+=m_data[sidx];
            }
        }
    }

    /**
     * Adds the current submatrix to the submatrix given by its top/left position (i,j) in another submatrix
     * @param i The row index
     * @param j TheColumn index
     * @param m The target submatrix
     */
    public void subTo(SubMatrix m, int i, int j) {
        int scur=m_start, tcur=m.m_start + i * m.m_row_inc + j * m.m_col_inc;
        for (int c=0; c<m_ncols; ++c, scur+=m_col_inc, tcur+=m.m_col_inc){
            for (int r=0, sidx=scur, tidx=tcur; r<m_nrows; ++r, sidx+=m_row_inc, tidx+=m.m_row_inc){
                m.m_data[tidx]-=m_data[sidx];
            }
        }
    }
    /**
     *
     */
    public void chs() {
        if (m_row_inc == 1) {
            for (int c = 0, ic = m_start; c < m_ncols; ++c, ic += m_col_inc) {
                for (int r = 0, ir = ic; r < m_nrows; ++r, ++ir) {
                    m_data[ir] = -m_data[ir];
                }
            }
        } else if (m_col_inc == 1) {
            for (int r = 0, ir = m_start; r < m_nrows; ++r, ir += m_row_inc) {
                for (int c = 0, ic = ir; c < m_ncols; ++c, ++ic) {
                    m_data[ic] = -m_data[ic];
                }
            }
        } else {
            for (int c = 0, ic = m_start; c < m_ncols; ++c, ic += m_col_inc) {
                for (int r = 0, ir = ic; r < m_nrows; ++r, ir += m_row_inc) {
                    m_data[ir] = -m_data[ir];
                }
            }
        }
    }

    /**
     * x(i,j) = fn(i,j)
     * @param fn The given function
     * @since 2.2
     */
    public void set(final Matrix.MatrixFunction fn) {
        if (m_row_inc == 1) {
            for (int c = 0, ic = m_start; c < m_ncols; ++c, ic += m_col_inc) {
                for (int r = 0, ir = ic; r < m_nrows; ++r, ++ir) {
                    m_data[ir] = fn.apply(r, c);
                }
            }
        } else if (m_col_inc == 1) {
            for (int r = 0, ir = m_start; r < m_nrows; ++r, ir += m_row_inc) {
                for (int c = 0, ic = ir; c < m_ncols; ++c, ++ic) {
                    m_data[ic] = fn.apply(r, c);
                }
            }
        } else {
            for (int c = 0, ic = m_start; c < m_ncols; ++c, ic += m_col_inc) {
                for (int r = 0, ir = ic; r < m_nrows; ++r, ir += m_row_inc) {
                    m_data[ir] = fn.apply(r, c);
                }
            }
        }
    }
    
    /**
     * x(i,j) = x(i,j) + fn(i,j)
     * @param fn The given function
     * @since 2.2
     */
    public void add(final Matrix.MatrixFunction fn) {
        if (m_row_inc == 1) {
            for (int c = 0, ic = m_start; c < m_ncols; ++c, ic += m_col_inc) {
                for (int r = 0, ir = ic; r < m_nrows; ++r, ++ir) {
                    m_data[ir] += fn.apply(r, c);
                }
            }
        } else if (m_col_inc == 1) {
            for (int r = 0, ir = m_start; r < m_nrows; ++r, ir += m_row_inc) {
                for (int c = 0, ic = ir; c < m_ncols; ++c, ++ic) {
                    m_data[ic] += fn.apply(r, c);
                }
            }
        } else {
            for (int c = 0, ic = m_start; c < m_ncols; ++c, ic += m_col_inc) {
                for (int r = 0, ir = ic; r < m_nrows; ++r, ir += m_row_inc) {
                    m_data[ir] += fn.apply(r, c);
                }
            }
        }
    }

    /**
     * x(i,j) = fn(i,j,x(i,j))
     * @param fn The given function
     * @since 2.2
     */
    public void set(final Matrix.MatrixRelativeFunction fn) {
        if (m_row_inc == 1) {
            for (int c = 0, ic = m_start; c < m_ncols; ++c, ic += m_col_inc) {
                for (int r = 0, ir = ic; r < m_nrows; ++r, ++ir) {
                    m_data[ir] = fn.apply(r, c, m_data[ir]);
                }
            }
        } else if (m_col_inc == 1) {
            for (int r = 0, ir = m_start; r < m_nrows; ++r, ir += m_row_inc) {
                for (int c = 0, ic = ir; c < m_ncols; ++c, ++ic) {
                    m_data[ic] = fn.apply(r, c, m_data[ic]);
                }
            }
        } else {
            for (int c = 0, ic = m_start; c < m_ncols; ++c, ic += m_col_inc) {
                for (int r = 0, ir = ic; r < m_nrows; ++r, ir += m_row_inc) {
                    m_data[ir] = fn.apply(r, c, m_data[ir]);
                }
            }
        }
    }
    
    /**
     * x(i,j) = x(i,j) + fn(i,j,x(i,j))
     * @param fn The given function
     * @since 2.2
     */
    public void add(final Matrix.MatrixRelativeFunction fn) {
        if (m_row_inc == 1) {
            for (int c = 0, ic = m_start; c < m_ncols; ++c, ic += m_col_inc) {
                for (int r = 0, ir = ic; r < m_nrows; ++r, ++ir) {
                    m_data[ir] += fn.apply(r, c, m_data[ir]);
                }
            }
        } else if (m_col_inc == 1) {
            for (int r = 0, ir = m_start; r < m_nrows; ++r, ir += m_row_inc) {
                for (int c = 0, ic = ir; c < m_ncols; ++c, ++ic) {
                    m_data[ic] += fn.apply(r, c, m_data[ic]);
                }
            }
        } else {
            for (int c = 0, ic = m_start; c < m_ncols; ++c, ic += m_col_inc) {
                for (int r = 0, ir = ic; r < m_nrows; ++r, ir += m_row_inc) {
                    m_data[ir] += fn.apply(r, c, m_data[ir]);
                }
            }
        }
    }

    @Override
    public SubMatrix clone() {
        try {
            return (SubMatrix) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    /**
     *
     * @param c
     * @return
     */
    public DataBlock column(final int c) {
        int beg = m_start + c * m_col_inc, end = beg + m_row_inc * m_nrows;
        return new DataBlock(m_data, beg, end, m_row_inc);
    }

    /**
     *
     * @return
     */
    public DataBlockIterator columns() {
        return new DataBlockIterator(m_data, m_start, m_ncols, m_nrows,
                m_col_inc, m_row_inc);
    }

    /**
     *
     * @param m
     */
    public void copy(final SubMatrix m) {
        // special handling of full matrices
        if (isFull() && m.isFull()) {
            System.arraycopy(m.m_data, 0, m_data, 0, m_data.length);
            return;
        }

        int scur=m.m_start, tcur=m_start;
        for (int c=0; c<m_ncols; ++c, scur+=m.m_col_inc, tcur+=m_col_inc){
            for (int r=0, sidx=scur, tidx=tcur; r<m_nrows; ++r, sidx+=m.m_row_inc, tidx+=m_row_inc){
                m_data[tidx]=m.m_data[sidx];
            }
        }
    }

    /**
     * Copies all the rows of src to rows of this object, following a given
     * selection array The number of rows in src should correspond to the number
     * of "trues" in the selection and the number of columns in src and in this
     * object should be the same
     *
     * @param brows Selected rows. The length of the array must be smaller or
     * equal to the number of rows in this object The copy stops when the end of
     * the selection array is reached or if the last row of src has been copied.
     * @param src The sub-matrix being copied
     */
    public void copyRows(final SubMatrix src, final boolean[] brows) {
        DataBlockIterator iter = rows(), siter = src.rows();

        DataBlock icur = iter.getData(), sicur = siter.getData();
        int i = 0;
        do {
            if (brows[i]) {
                icur.copy(sicur);
                if (!siter.next()) {
                    return;
                }
            }
        } while (++i < brows.length && iter.next());
    }

    /**
     *
     * @param irows Destination indexes
     * @param src
     */
    public void copyRows(final SubMatrix src, final int[] irows) {
        for (int i = 0; i < irows.length && i < src.getRowsCount(); ++i) {
            row(irows[i]).copy(src.row(i));
        }
    }

    /**
     * Copies all the columns of src to columns of this object, following a
     * given selection array The number of columns in src should correspond to
     * the number of "trues" in the selection and the number of rows in src and
     * in this object should be the same
     *
     * @param bcols Selected columns. The length of the array must be smaller or
     * equal to the number of columns in this object The copy stops when the end
     * of the selection array is reached or if the last column of src has been
     * copied.
     * @param src The sub-matrix being copied
     */
    public void copyColumns(final SubMatrix src, final boolean[] bcols) {
        DataBlockIterator iter = columns(), siter = src.columns();

        DataBlock icur = iter.getData(), sicur = siter.getData();
        int i = 0;
        do {
            if (bcols[i]) {
                icur.copy(sicur);
                if (!siter.next()) {
                    return;
                }
            }
        } while (++i < bcols.length && iter.next());
    }

    /**
     *
     * @param icols Destination indexes
     * @param src
     */
    public void copyColumns(final SubMatrix src, final int[] icols) {
        for (int i = 0; i < icols.length && i < src.getColumnsCount(); ++i) {
            column(icols[i]).copy(src.column(i));
        }
    }

    public void copy(final SubMatrix src, final boolean[] brows, final boolean[] bcols) {
        int rmax = Math.max(brows.length, this.m_nrows);
        int cmax = Math.max(bcols.length, this.m_ncols);
        for (int c = 0, sc = 0; c < cmax; ++c) {
            if (bcols[c]) {
                for (int r = 0, sr = 0; r < rmax; ++r) {
                    if (brows[r]) {
                        set(r, c, src.get(sr, sc));
                        ++sr;
                    }
                }
                ++sc;
            }
        }
    }

    /**
     *
     * @param irows Destination rows
     * @param icols Destination columns
     * @param src sub-matrix being copied
     */
    public void copy(final SubMatrix src, final int[] irows, final int[] icols) {
        int rmax = Math.max(irows.length, src.m_nrows);
        int cmax = Math.max(icols.length, src.m_ncols);
        for (int c = 0; c < cmax; ++c) {
            for (int r = 0; r < rmax; ++r) {
                set(irows[r], icols[c], src.get(r, c));
            }
        }
    }

    /**
     * Copies the current submatrix at a given position (i,j) in another matrix
     * @param i The row index
     * @param j TheColumn index
     * @param m The target submatrix
     */
    public void copyTo(SubMatrix m, int i, int j) {
        int scur=m_start, tcur=m.m_start + i * m.m_row_inc + j * m.m_col_inc;
        for (int c=0; c<m_ncols; ++c, scur+=m_col_inc, tcur+=m.m_col_inc){
            for (int r=0, sidx=scur, tidx=tcur; r<m_nrows; ++r, sidx+=m_row_inc, tidx+=m.m_row_inc){
                m.m_data[tidx]=m_data[sidx];
            }
        }
    }
    /**
     * this = a * Y
     *
     * @param a
     * @param Y
     */
    public void setAY(final double a, final SubMatrix Y) {
        // if (m_nrows != Y.m_nrows || m_ncols != Y.m_ncols)
        // throw new MatrixException(MatrixException.IncompatibleDimensions);
        DataBlockIterator iter, siter;
        if (m_row_inc == 1) {
            iter = columns();
            siter = Y.columns();
        } else {
            iter = rows();
            siter = Y.rows();
        }

        DataBlock icur = iter.getData(), sicur = siter.getData();
        do {
            icur.setAY(a, sicur);
        } while (iter.next() && siter.next());
    }

    /**
     * this = this + a * Y
     *
     * @param a
     * @param Y
     */
    public void addAY(final double a, final SubMatrix Y) {
        if (a == 0) {
            return;
        }
        // if (m_nrows != Y.m_nrows || m_ncols != Y.m_ncols)
        // throw new MatrixException(MatrixException.IncompatibleDimensions);
        DataBlockIterator iter, siter;
        if (m_row_inc == 1) {
            iter = columns();
            siter = Y.columns();
        } else {
            iter = rows();
            siter = Y.rows();
        }

        DataBlock icur = iter.getData(), sicur = siter.getData();
        do {
            icur.addAY(a, sicur);
        } while (iter.next() && siter.next());
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
    /**
     *
     * @return
     */
    public DataBlock diagonal() {
        int n = Math.min(m_nrows, m_ncols), inc = m_row_inc + m_col_inc;
        return new DataBlock(m_data, m_start, m_start + inc * n, inc);
    }

    /**
     *
     * @param ls
     * @param rs
     */
    public void difference(final SubMatrix ls, final SubMatrix rs) {
        // if (m_nrows != ls.m_nrows || m_ncols != ls.m_ncols || m_nrows !=
        // rs.m_nrows || m_ncols != rs.m_ncols)
        // throw new MatrixException(MatrixException.IncompatibleDimensions);
        DataBlockIterator iter, liter, riter;
        if (ls.m_row_inc == 1 && rs.m_row_inc == 1 && m_row_inc == 1) {
            iter = columns();
            liter = ls.columns();
            riter = rs.columns();
        } else {
            iter = rows();
            liter = ls.rows();
            riter = rs.rows();
        }

        DataBlock icur = iter.getData(), lcur = liter.getData(), rcur = riter.getData();
        do {
            icur.difference(lcur, rcur);
        } while (iter.next() && liter.next() && riter.next());
    }

    /**
     *
     * @param r0
     * @param r1
     * @param c0
     * @param c1
     * @return
     */
    public SubMatrix extract(final int r0, final int r1, final int c0,
            final int c1) {
        return new SubMatrix(m_data, m_start + r0 * m_row_inc + c0 * m_col_inc,
                r1 - r0, c1 - c0, m_row_inc, m_col_inc);
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
    public SubMatrix extract(final int r0, final int c0, final int nrows,
            final int ncols, final int rowinc, final int colinc) {
        return new SubMatrix(m_data, m_start + r0 * m_row_inc + c0 * m_col_inc,
                nrows, ncols, m_row_inc * rowinc, m_col_inc * colinc);
    }

    /**
     *
     * @param row
     * @param col
     * @return
     */
    public double get(final int row, final int col) {
        return m_data[m_start + row * m_row_inc + col * m_col_inc];
    }

    /**
     *
     * @return
     */
    public int getColumnsCount() {
        return m_ncols;
    }

    /**
     *
     * @return
     */
    public int getRowsCount() {

        return m_nrows;
    }

    /**
     *
     * @param dr
     * @param dc
     */
    public void move(final int dr, final int dc) {
        m_start += dr * m_row_inc + dc * m_col_inc;
    }

    /**
     *
     * @param val
     */
    public void mul(final double val) {
        if (val == 0) {
            set(0);
        }
        if (val == 1) {
            return;
        }
        if (m_row_inc == 1) {
            for (int c = 0, ic = m_start; c < m_ncols; ++c, ic += m_col_inc) {
                for (int r = 0, ir = ic; r < m_nrows; ++r, ++ir) {
                    m_data[ir] *= val;
                }
            }
        } else if (m_col_inc == 1) {
            for (int r = 0, ir = m_start; r < m_nrows; ++r, ir += m_row_inc) {
                for (int c = 0, ic = ir; c < m_ncols; ++c, ++ic) {
                    m_data[ic] *= val;
                }
            }
        } else {
            for (int c = 0, ic = m_start; c < m_ncols; ++c, ic += m_col_inc) {
                for (int r = 0, ir = ic; r < m_nrows; ++r, ir += m_row_inc) {
                    m_data[ir] *= val;
                }
            }
        }
    }

    /**
     *
     * @param row
     * @param col
     * @param val
     */
    public void mul(final int row, final int col, final double val) {
        m_data[m_start + row * m_row_inc + col * m_col_inc] *= val;
    }

    /**
     * Computes sum(this(i,j**m(i,j))
     *
     * @param m The right operand of the dot product
     * @return
     */
    public double dot(final SubMatrix m) {
        double p = 0;
        DataBlockIterator cols = columns(), mcols = m.columns();
        DataBlock col = cols.getData(), mcol = mcols.getData();
        do {
            p += col.dot(mcol);
        } while (cols.next() && mcols.next());
        return p;
    }

    /**
     *
     * @param m
     * @param n
     */
    public void product(final SubMatrix m, final SubMatrix n) {
        // if (m_nrows != m.m_nrows || m_ncols != n.m_ncols || m.m_ncols !=
        // n.m_nrows)
        // throw new MatrixException(MatrixException.IncompatibleDimensions);
        DataBlockIterator iter, riter, citer;
        iter = columns();
        riter = m.rows();
        citer = n.columns();

        DataBlock cur = iter.getData(), col = citer.getData(), row = riter.getData();
        do {
            int pos = 0;
            riter.begin();
            do {
                cur.set(pos++, row.dot(col));
            } while (riter.next());
        } while (iter.next() && citer.next());
    }

    /**
     * Computes the kronecker product of two matrix. This object will contain
     * the results. The dimensions of this object must be equal to the product
     * of the dimensions of the operands. For optimisation purpose, the code
     * consider that the resulting sub-matrix is set to 0 at the entry of the
     * code
     *
     * @param m The left operand
     * @param n The right operand
     */
    public void kronecker(final SubMatrix m, final SubMatrix n) {
        int rm = m.getRowsCount(), cm = m.getColumnsCount();
        int rn = n.getRowsCount(), cn = n.getColumnsCount();
        for (int r = 0, i = 0; r < rm; ++r, i += rn) {
            for (int c = 0, j = 0; c < cm; ++c, j += cn) {
                SubMatrix cur = extract(i, i + rn, j, j + cn);
                double e = m.get(r, c);
                if (e != 0) {
                    cur.setAY(e, n);
                }
            }
        }
    }

    /**
     *
     * @param r
     * @return
     */
    public DataBlock row(final int r) {
        int beg = m_start + r * m_row_inc, end = beg + m_col_inc * m_ncols;
        return new DataBlock(m_data, beg, end, m_col_inc);
    }

    /**
     *
     * @return
     */
    public DataBlockIterator rows() {
        return new DataBlockIterator(m_data, m_start, m_nrows, m_ncols,
                m_row_inc, m_col_inc);
    }

    /**
     *
     * @param val
     */
    public void set(final double val) {
        if (m_row_inc == 1) {
            for (int c = 0, ic = m_start; c < m_ncols; ++c, ic += m_col_inc) {
                for (int r = 0, ir = ic; r < m_nrows; ++r, ++ir) {
                    m_data[ir] = val;
                }
            }
        } else if (m_col_inc == 1) {
            for (int r = 0, ir = m_start; r < m_nrows; ++r, ir += m_row_inc) {
                for (int c = 0, ic = ir; c < m_ncols; ++c, ++ic) {
                    m_data[ic] = val;
                }
            }
        } else {
            for (int c = 0, ic = m_start; c < m_ncols; ++c, ic += m_col_inc) {
                for (int r = 0, ir = ic; r < m_nrows; ++r, ir += m_row_inc) {
                    m_data[ir] = val;
                }
            }
        }
    }

    /**
     *
     * @param row
     * @param col
     * @param value
     */
    public void set(final int row, final int col, final double value) {
        m_data[m_start + row * m_row_inc + col * m_col_inc] = value;
    }

    /**
     *
     * @param n
     */
    public void shift(final int n) {
        if (n < 0) {
            int del = (m_row_inc + m_col_inc) * n;

            for (int c = 0, i = m_start; c < m_ncols + n; ++c, i += m_col_inc) {
                for (int r = 0, j = i; r < m_nrows + n; ++r, j += m_row_inc) {
                    m_data[j] = m_data[j - del];
                }
            }
        } else if (n > 0) {
            /*
             * SubMatrix t = Extract(n, m_nrows, n, m_ncols); SubMatrix s =
             * Extract(0, m_nrows - n, 0, m_ncols - n); RCIterator tc =
             * t.Columns(), sc = s.Columns(); tc.End(); sc.End(); do
             * tc.Current.Copy(sc.Current); while (tc.Previous() &&
             * sc.Previous()); if (bzero) { for (int c = 0; c < n; ++c)
             * Column(c).Set(0); for (int r = 0; r < n; ++r) Row(r).Set(0); }
             */

            int del = (m_row_inc + m_col_inc) * n;

            for (int c = n, i = m_start + (m_nrows - 1) * m_row_inc
                    + (m_ncols - 1) * m_col_inc; c < m_ncols; ++c, i -= m_col_inc) {
                for (int r = n, j = i; r < m_nrows; ++r, j -= m_row_inc) {
                    m_data[j] = m_data[j - del];
                }
            }
        }
    }

    /**
     *
     * @param m
     */
    public void sub(final SubMatrix m) {
        // special handling of full matrices
        if (isFull() && m.isFull()) {
            for (int i = 0; i < m_data.length; ++i) {
                m_data[i] -= m.m_data[i];
            }
            return;
        }
        // if (m_nrows != m.m_nrows || m_ncols != m.m_ncols)
        // throw new MatrixException(MatrixException.IncompatibleDimensions);
        DataBlockIterator iter, siter;
        if (m.m_row_inc == 1 && m_row_inc == 1) {
            iter = columns();
            siter = m.columns();
        } else {
            iter = rows();
            siter = m.rows();
        }

        DataBlock icur = iter.getData(), sicur = siter.getData();
        do {
            icur.sub(sicur);
        } while (iter.next() && siter.next());
    }

    /**
     *
     * @param pos
     * @return
     */
    public DataBlock subDiagonal(int pos) {
        if (pos >= m_ncols) {
            return DataBlock.EMPTY;
        }
        if (-pos >= m_nrows) {
            return DataBlock.EMPTY;
        }
        int beg = m_start, inc = m_row_inc + m_col_inc;
        int n;
        if (pos > 0) {
            beg += pos * m_col_inc;
            n = Math.min(m_nrows, m_ncols - pos);
        } else if (pos < 0) {
            beg -= pos * m_row_inc;
            n = Math.min(m_nrows + pos, m_ncols);
        } else {
            n = Math.min(m_nrows, m_ncols);
        }
        return new DataBlock(m_data, beg, beg + inc * n, inc);
    }

    /**
     *
     * @return
     */
    public double sum() {
        double s = 0;
        if (m_row_inc == 1) {
            for (int c = 0, ic = m_start; c < m_ncols; ++c, ic += m_col_inc) {
                for (int r = 0, ir = ic; r < m_nrows; ++r, ++ir) {
                    s += m_data[ir];
                }
            }
        } else if (m_col_inc == 1) {
            for (int r = 0, ir = m_start; r < m_nrows; ++r, ir += m_row_inc) {
                for (int c = 0, ic = ir; c < m_ncols; ++c, ++ic) {
                    s += m_data[ic];
                }
            }
        } else {
            for (int c = 0, ic = m_start; c < m_ncols; ++c, ic += m_col_inc) {
                for (int r = 0, ir = ic; r < m_nrows; ++r, ir += m_row_inc) {
                    s += m_data[ir];
                }
            }
        }
        return s;
    }

    /**
     *
     * @param ls
     * @param rs
     */
    public void sum(final SubMatrix ls, final SubMatrix rs) {
        // if (m_nrows != ls.m_nrows || m_ncols != ls.m_ncols || m_nrows !=
        // rs.m_nrows || m_ncols != rs.m_ncols)
        // throw new MatrixException(MatrixException.IncompatibleDimensions);
        DataBlockIterator iter, liter, riter;
        if (ls.m_row_inc == 1 && rs.m_row_inc == 1 && m_row_inc == 1) {
            iter = columns();
            liter = ls.columns();
            riter = rs.columns();
        } else {
            iter = rows();
            liter = ls.rows();
            riter = rs.rows();
        }

        DataBlock icur = iter.getData(), lcur = liter.getData(), rcur = riter.getData();
        do {
            icur.sum(lcur, rcur);
        } while (iter.next() && liter.next() && riter.next());
    }

    /**
     *
     * @return
     */
    public SubMatrix transpose() {
        return new SubMatrix(m_data, m_start, m_ncols, m_nrows, m_col_inc,
                m_row_inc);
    }

    public boolean isEmpty() {
        return getColumnsCount() <= 0 || getRowsCount() <= 0;
    }

    public boolean isZero(double zero) {
        if (isFull()) {
            return new DataBlock(m_data).isZero(zero);
        } else {
            DataBlockIterator cols = columns();
            DataBlock col = cols.getData();
            do {
                if (!col.isZero(zero)) {
                    return false;
                }
            } while (cols.next());
            return true;
        }
    }

    @Deprecated
    public boolean isNull(double zero) {
        return isZero(zero);
    }

    public boolean isDiagonal() {
        return isLower() && isUpper();
    }

    public boolean isIdentity() {
        return isDiagonal() && diagonal().isConstant(1);
    }

    public boolean isLower() {
        for (int i = 1; i < m_ncols; ++i) {
            if (!subDiagonal(i).isZero()) {
                return false;
            }
        }
        return true;
    }

    public boolean isUpper() {
        for (int i = 1; i < m_nrows; ++i) {
            if (!subDiagonal(-i).isZero()) {
                return false;
            }
        }
        return true;
    }

    private boolean isFull() {
        return m_nrows * m_ncols == m_data.length;
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        DataBlockIterator rows = this.rows();
        do {
            builder.append(rows.getData()).append("\r\n");
        } while (rows.next());
        return builder.toString();
    }

    public String toString(String fmt) {
        if (isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        DataBlockIterator rows = this.rows();
        do {
            if (rows.getPosition() > 0) {
                builder.append("\r\n");
            }
            builder.append(rows.getData().toString(fmt));
        } while (rows.next());
        return builder.toString();
    }
    
    /**
     * Computes the sum of the rows
     *
     * @return A new dataBlock is returned ([0, ncols[)
     */
    @NewObject
    public DataBlock rowSum() {
        return rows().sum();
    }

    /**
     * Computes the sum of the columns
     *
     * @return A new dataBlock is returned ([0, nrows[)
     */
    @NewObject
    public DataBlock columnSum() {
        return columns().sum();
    }

    /**
     * Computes in place src*this*y. The result is stored in this object. src,y and
 this object must be square matrices with the same dimensions
     *
     * @param x
     * @param y
     */
    public void xmy(SubMatrix x, SubMatrix y) {
        if (m_nrows != m_ncols || x.m_nrows != x.m_ncols || y.m_nrows != y.m_ncols
                || m_nrows != x.m_nrows || m_nrows != y.m_nrows) {
            throw new MatrixException(MatrixException.IncompatibleDimensions);
        }
        // computes my.
        // rows mi of m are replaced by mi * src
        DataBlock tmp = new DataBlock(m_nrows);
        DataBlockIterator mrows = rows();
        DataBlock mrow = mrows.getData();
        do {
            tmp.copy(mrow);
            mrow.product(tmp, y.columns());
        } while (mrows.next());

        // computes src m. 
        DataBlockIterator mcols = columns();
        DataBlock mcol = mcols.getData();
        do {
            tmp.copy(mcol);
            mcol.product(x.rows(), tmp);
        } while (mcols.next());

    }

    /**
     * The euclidian (frobenius) norm of the matrix
     *
     * @return sqrt(sum(src(i,j)*src(i,j))) is returned
     */
    public double nrm2() {
        if (isFull()) {
            return new DataBlock(m_data).nrm2();
        } else {
            DataBlockIterator columns = columns();
            DataBlock column = columns.getData();
            double n = column.nrm2();
            while (columns.next()) {
                DataBlock.hypot(n, column.nrm2());
            }
            return n;
        }

    }

//<editor-fold defaultstate="collapsed" desc="iterator method">
    
    /**
     * The following methods can be used to create fast iterations.
     * They avoid the creation of unnecessary objects
     * 
     * example:
     * 
     * (Sub)Matrix data=...
     * SubMatrix cur=data.topLeft();
     * while (cur){
     *    cur.next(r,c);
     * }
     */
    
    
    /**
     * Takes the bottom-right of the current submatrix as the new starting position
     * and updates the number of rows/columns
     * @param nrows The number of rows in the submatrix
     * @param ncols The number of columns in the submatrix
     */
    public void next(int nrows, int ncols){
        m_start+=m_nrows*m_row_inc+m_ncols*m_col_inc;
        m_nrows=nrows;
        m_ncols=ncols;
    }

    /**
     * Takes the bottom-right of the current submatrix as the new starting position
      */
    public void next(){
        m_start+=m_nrows*m_row_inc+m_ncols*m_col_inc;
    }

    /**
     * Takes the top-right of the current submatrix as the new starting position
     * and updates the number of columns
     * @param ncols The number of columns in the submatrix
     */
    public void hnext(int ncols){
        m_start+=m_ncols*m_col_inc;
        m_ncols=ncols;
    }

    /**
     * Takes the top-right of the current submatrix as the new starting position
     */
    public void hnext(){
        m_start+=m_ncols*m_col_inc;
    }
    
    /**
     * Takes the bottom-left of the current submatrix as the new starting position
     * and updates the number of rows
     * @param nrows The number of rows in the submatrix
     */
    public void vnext(int nrows){
        m_start+=m_nrows*m_row_inc;
        m_nrows=nrows;
    }

    /**
     * Takes the bottom-left of the current submatrix as the new starting position
     */
    public void vnext(){
        m_start+=m_nrows*m_row_inc;
    }
    
    /**
     * Takes the top-left of the current submatrix as the new ending position
     * and updates the number of rows/columns
     * @param nrows The number of rows in the submatrix
     * @param ncols The number of columns in the submatrix
     */
    public void previous(int nrows, int ncols){
        m_start-=nrows*m_row_inc+ncols*m_col_inc;
        m_nrows=nrows;
        m_ncols=ncols;
    }

    /**
     * Takes the top-left of the current submatrix as the new ending position
     */
    public void previous(){
        m_start-=m_nrows*m_row_inc+m_ncols*m_col_inc;
    }

    /**
     * Takes the bottom-left of the current submatrix as the new ending position
     * and updates the number of columns
     * @param ncols The number of columns in the submatrix
     */
    public void hprevious(int ncols){
        m_start-=ncols*m_col_inc;
        m_ncols=ncols;
    }

    /**
     * Takes the bottom-left of the current submatrix as the new ending position
     */
    public void hprevious(){
        m_start-=m_ncols*m_col_inc;
    }
    
    /**
     * Takes the top-right of the current submatrix as the new ending position
     * and updates the number of rows
     * @param nrows The number of rows in the submatrix
     */
    public void vprevious(int nrows){
        m_start-=nrows*m_row_inc;
        m_nrows=nrows;
    }

    /**
     * Takes the top-right of the current submatrix as the new ending position
     */
    public void vprevious(){
        m_start-=m_nrows*m_row_inc;
    }
    
    /**
     * Top-left empty sub-matrix. To be used with next(a,b)
     * @return An empty sub-matrix
     */
    public SubMatrix topLeft(){
        return new SubMatrix(m_data, m_start, 0, 0, m_row_inc, m_col_inc);
    }
    
    /**
     * Top-left sub-matrix
     * @param nr Number of rows. Could be 0.
     * @param nc Number of columns. Could be 0. 
     * @return A nr src nc sub-matrix
     */
    public SubMatrix topLeft(int nr, int nc){
        return new SubMatrix(m_data, m_start, nr, nc, m_row_inc, m_col_inc);
    }
    
    /**
     * Top-left sub-matrix
     * @param nr Number of rows. Could be 0.
     * @return A nr src nc sub-matrix
     */
    public SubMatrix top(int nr){
        return new SubMatrix(m_data, m_start, nr, m_ncols, m_row_inc, m_col_inc);
    }

    /**
     * Top-left sub-matrix
     * @param nc Number of columns. Could be 0.
     * @return A nr src nc sub-matrix
     */
    public SubMatrix left(int nc){
        return new SubMatrix(m_data, m_start, m_nrows, nc, m_row_inc, m_col_inc);
    }
    
    /**
     * bottom-right  sub-matrix. 
     * @return An empty sub-matrix
     */
    public SubMatrix bottomRight(){
        int start=m_nrows*m_row_inc+m_ncols*m_col_inc;
        return new SubMatrix(m_data, start, 0, 0, m_row_inc, m_col_inc);
    }

    /**
     * Bottom-right sub-matrix
     * @param nr Number of rows. Could be 0.
     * @param nc Number of columns. Could be 0. 
     * @return A nr src nc sub-matrix
     */
    public SubMatrix bottomRight(int nr, int nc){
        int start=(m_nrows-nr)*m_row_inc+(m_ncols-nc)*m_col_inc;
        return new SubMatrix(m_data, start, nr, nc, m_row_inc, m_col_inc);
    }
    
    /**
     * Bottom sub-matrix
     *
     * @param nr Number of rows. Could be 0.
     * @return The last n rows
     */
    public SubMatrix bottom(int nr) {
        return new SubMatrix(m_data, m_start + m_nrows - nr, nr, m_ncols, m_row_inc, m_col_inc);
    }


    /**
     * right sub-matrix
     *
     * @param nc Number of columns. Could be 0.
     * @return The nc right columns
     */
    public SubMatrix right(int nc) {
        return new SubMatrix(m_data, m_start + (m_ncols - nc) * m_col_inc, m_nrows, nc, m_row_inc, m_col_inc);
    }
//</editor-fold>

}
