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

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public final class SymmetricMatrix {

    /**
     *
     * @param m
     */
    public static void fromLower(final Matrix m) {
        double[] data = m.data_;
        int n = m.nrows_;
        for (int c = 0, id = 0; c < n; ++c, id += n + 1) {
            for (int r = c + 1, il = id, iu = id; r < n; ++r) {
                ++il;
                iu += n;
                data[iu] = data[il];
            }
        }
    }

    /**
     *
     * @param m
     * @throws MatrixException
     */
    public static void fromLower(final SubMatrix m) throws MatrixException {
        double[] data = m.m_data;
        int n = m.m_nrows;
        if (n != m.m_ncols) {
            throw new MatrixException(MatrixException.SquareOnly);
        }
        int ddel = m.m_row_inc + m.m_col_inc;
        for (int c = 0, id = m.m_start; c < n; ++c, id += ddel) {
            for (int r = c + 1, il = id, iu = id; r < n; ++r) {
                il += m.m_row_inc;
                iu += m.m_col_inc;
                data[iu] = data[il];
            }
        }
    }

    /**
     *
     * @param m
     */
    public static void fromUpper(final Matrix m) {
        double[] data = m.data_;
        int n = m.nrows_;
        // if (n != m.ncols_)
        // throw new MatrixException(MatrixException.SquareOnly);
        for (int c = 0, id = 0; c < n; ++c, id += n + 1) {
            for (int r = c + 1, il = id, iu = id; r < n; ++r) {
                ++il;
                iu += n;
                data[il] = data[iu];
            }
        }
    }

    /**
     *
     * @param m
     * @throws MatrixException
     */
    public static void fromUpper(final SubMatrix m) throws MatrixException {
        double[] data = m.m_data;
        int n = m.m_nrows;
        if (n != m.m_ncols) {
            throw new MatrixException(MatrixException.SquareOnly);
        }
        int ddel = m.m_row_inc + m.m_col_inc;
        for (int c = 0, id = m.m_start; c < n; ++c, id += ddel) {
            for (int r = c + 1, il = id, iu = id; r < n; ++r) {
                il += m.m_row_inc;
                iu += m.m_col_inc;
                data[il] = data[iu];
            }
        }
    }

    /**
     *
     * @param s
     * @return
     * @throws MatrixException
     */
    public static Matrix inverse(final Matrix s) throws MatrixException {
        // if (s == null)
        // throw new ArgumentNullException("s");
        try {
            Matrix lower = s.clone();
            lcholesky(lower);
            lower = LowerTriangularMatrix.inverse(lower);
            return XtX(lower);
        } catch (MatrixException e) {
            Householder householder = new Householder(true);
            householder.decompose(s);
            return householder.inverse();
        }
    }

    /**
     * Checks if a symmetric matrix is definite positive
     *
     * @param m Must be a symmetric matrix
     * @param useM
     * @return
     */
    public static boolean isDPos(Matrix m, boolean useM) {
        if (!useM) {
            m = m.clone();
        }
        double[] data = m.data_;
        int n = m.nrows_;
        for (int i = 0, idiag = 0; i < n; ++i, idiag += n + 1) {
            // compute aii;
            double aii = data[idiag];
            for (int j = i; j < idiag; j += n) {
                double x = data[j];
                aii -= x * x;
            }
            if (aii <= 0) {
                return false;
            }
            aii = Math.sqrt(aii);
            data[idiag] = aii;

            // compute elements i+1 : n of column i
            int ymax = (i + 1) * n;
            for (int jx = i; jx < idiag; jx += n) {
                double temp = data[jx];
                if (temp != 0) {
                    for (int ia = jx + 1, iy = idiag + 1; iy < ymax; ++ia, ++iy) {
                        data[iy] -= temp * data[ia];
                    }
                }
            }
            for (int iy = idiag + 1; iy < ymax; ++iy) {
                data[iy] /= aii;
            }
        }
        return true;
    }

    /**
     *
     * @param m
     * @param eps
     * @return
     */
    public static boolean isSymmetric(Matrix m, double eps) {
        double[] data = m.data_;
        int n = m.nrows_;
        for (int c = 0, id = 0; c < n; ++c, id += n + 1) {
            for (int r = c + 1, il = id, iu = id; r < n; ++r) {
                ++il;
                iu += n;
                if (Math.abs(data[il] - data[iu]) > eps) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     *
     * @param m
     * @throws MatrixException
     */
    public static void lcholesky(final Matrix m) {
        double[] data = m.data_;
        int n = m.nrows_;
        // if (n != m.ncols_)
        // throw new MatrixException(MatrixException.SquareOnly);

        for (int i = 0, idiag = 0; i < n; ++i, idiag += n + 1) {
            // compute aii;
            double aii = data[idiag];
            for (int j = i; j < idiag; j += n) {
                double x = data[j];
                aii -= x * x;
            }
            if (aii <= 0) {
                throw new MatrixException(MatrixException.CholeskyFailed);
            }
            aii = Math.sqrt(aii);
            data[idiag] = aii;

            // compute elements i+1 : n of column i
            int ymax = (i + 1) * n;
            for (int jx = i; jx < idiag; jx += n) {
                double temp = data[jx];
                if (temp != 0) {
                    for (int ia = jx + 1, iy = idiag + 1; iy < ymax; ++ia, ++iy) {
                        data[iy] -= temp * data[ia];
                    }
                }
            }
            for (int iy = idiag + 1; iy < ymax; ++iy) {
                data[iy] /= aii;
            }

        }
        m.toLower();
    }

    /**
     *
     * @param m
     * @param Zero
     * @throws MatrixException
     */
    public static void lcholesky(final Matrix m, double Zero) {
        double[] data = m.data_;
        int n = m.nrows_;
        // if (n != m.ncols_)
        // throw new MatrixException(MatrixException.SquareOnly);

        for (int i = 0, idiag = 0; i < n; ++i, idiag += n + 1) {
            // compute aii;
            double aii = data[idiag];
            for (int j = i; j < idiag; j += n) {
                double x = data[j];
                aii -= x * x;
            }
            if (aii < -Zero) {
                throw new MatrixException(MatrixException.CholeskyFailed);
            } else if (aii <= Zero) {
                data[idiag] = 0;
                // compute elements i+1 : n of column i
                int ymax = (i + 1) * n;
                for (int jx = i; jx < idiag; jx += n) {
                    double temp = data[jx];
                    if (temp != 0) {
                        for (int ia = jx + 1, iy = idiag + 1; iy < ymax; ++ia, ++iy) {
                            data[iy] -= temp * data[ia];
                        }
                    }
                }
                for (int iy = idiag + 1; iy < ymax; ++iy) {
                    if (Math.abs(data[iy]) > Zero) {
                        throw new MatrixException(
                                MatrixException.CholeskyFailed);
                    } else {
                        data[iy] = 0;
                    }
                }
            } else {
                aii = Math.sqrt(aii);
                data[idiag] = aii;
                // compute elements i+1 : n of column i
                int ymax = (i + 1) * n;
                for (int jx = i; jx < idiag; jx += n) {
                    double temp = data[jx];
                    if (temp != 0) {
                        for (int ia = jx + 1, iy = idiag + 1; iy < ymax; ++ia, ++iy) {
                            data[iy] -= temp * data[ia];
                        }
                    }
                }
                for (int iy = idiag + 1; iy < ymax; ++iy) {
                    data[iy] /= aii;
                }
            }
        }
        m.toLower();
    }

    /**
     *
     * @param lower
     * @return
     * @throws MatrixException
     */
    public static Matrix LLt(final Matrix lower) throws MatrixException {
        // if (lower == null)
        // throw new ArgumentNullException("lower");
        int n = lower.getRowsCount();
        double[] pl = lower.data_;
        Matrix O = new Matrix(n, n);
        double[] po = O.data_;
        for (int i = 0, idiag = 0, omax = n; i < n; ++i, idiag += n + 1, omax += n) {
            for (int l = i; l <= idiag; l += n) {
                double x = pl[l];
                for (int o = idiag, q = l; o < omax; ++o, ++q) {
                    po[o] += pl[q] * x;
                }
            }
        }
        fromLower(O);
        return O;
    }

    /**
     *
     * @param s
     * @param x
     * @return
     */
    public static double quadraticForm(final Matrix s, final DataBlock x) {
        /*
         * if (s == null) throw new ArgumentNullException("s"); if (x == null)
         * throw new ArgumentNullException("x");
         */
        // double n = s.RowsCount;
        // if (x.Length != n)
        // throw new MatrixException(MatrixException.IncompatibleDimensions);
        double w = 0;
        DataBlockIterator cols = s.columns();
        DataBlock col = cols.getData();
        int pos = 0;
        do {
            w += x.get(pos++) * x.dot(col);
        } while (cols.next());
        return w;
    }

    /**
     *
     * @param s
     * @param x
     * @return
     */
    public static double quadraticForm(final Matrix s, final double[] x) {
        /*
         * if (s == null) throw new ArgumentNullException("s"); if (x == null)
         * throw new ArgumentNullException("x");
         */
        double n = s.getRowsCount();
        // if (x.Length != n)
        // throw new MatrixException(MatrixException.IncompatibleDimensions);
        double w = 0;
        double[] data = s.data_;
        for (int c = 0, i = 0; c < n; ++c) {
            double z = 0;
            for (int r = 0; r < n; ++r, ++i) {
                z += data[i] * x[r];
            }
            w += z * x[c];
        }
        return w;
    }

    /**
     * Returns X'SX
     *
     * @param s
     * @param x
     * @return
     */
    public static Matrix quadraticForm(final Matrix s, final Matrix x) {
        int nc = x.getColumnsCount();
        Matrix SX = s.times(x);
        DataBlockIterator rows = x.columns(), cols = SX.columns();
        Matrix o = new Matrix(nc, nc);

        int idx = 0, c = 0;
        do {
            idx += c;
            rows.setPosition(c++);
            DataBlock col = cols.getData(), row = rows.getData();
            do {
                o.data_[idx++] = row.dot(col);
            } while (rows.next());
        } while (cols.next());
        fromLower(o);
        return o;
    }

    /**
     *
     * @param s
     * @param x
     * @return
     */
    public static double quadraticForm(final SubMatrix s, final DataBlock x) {
        /*
         * if (s == null) throw new ArgumentNullException("s"); if (x == null)
         * throw new ArgumentNullException("x");
         */
        // double n = s.RowsCount;
        // if (x.Length != n)
        // throw new MatrixException(MatrixException.IncompatibleDimensions);
        double w = 0;
        DataBlockIterator cols = s.columns();
        DataBlock col = cols.getData();
        int pos = 0;
        do {
            w += x.get(pos++) * x.dot(col);
        } while (cols.next());
        return w;
    }

    /**
     * x'SX
     *
     * @param s
     * @param x
     * @return
     * @throws MatrixException
     */
    public static Matrix quadraticForm(final SubMatrix s, final SubMatrix x)
            throws MatrixException {
        /*
         * if (s == null) throw new ArgumentNullException("s"); if (x == null)
         * throw new ArgumentNullException("x");
         */
        int n = s.getRowsCount();
        // if (x.RowsCount != n)
        // throw new MatrixException(MatrixException.IncompatibleDimensions);
        int nc = x.getColumnsCount();
        Matrix SX = new Matrix(n, nc);
        SX.subMatrix().product(s, x);
        DataBlockIterator rows = x.columns(), cols = SX.columns();
        Matrix o = new Matrix(nc, nc);

        int idx = 0, c = 0;
        DataBlock col = cols.getData(), row = rows.getData();
        do {
            idx += c;
            rows.setPosition(c++);
            do {
                o.data_[idx++] = row.dot(col);
            } while (rows.next());
        } while (cols.next());
        fromLower(o);
        return o;
    }

    /**
     *
     * @param s
     * @param x
     * @param xtsx
     * @throws MatrixException
     */
    public static void quadraticForm(final SubMatrix s, final SubMatrix x,
            final SubMatrix xtsx) throws MatrixException {
        int n = s.getRowsCount();
        int nc = x.getColumnsCount();
        Matrix SX = new Matrix(n, nc);
        SX.subMatrix().product(s, x);
        DataBlockIterator rows = x.columns(), cols = SX.columns(), xcols = xtsx
                .columns();
        DataBlock col = cols.getData(), xcol = xcols.getData(), cur = rows
                .getData();

        int c = 0;
        do {
            int idx = c;
            rows.setPosition(c++);
            do {
                xcol.set(idx++, cur.dot(col));
            } while (rows.next());
        } while (cols.next() && xcols.next());
        fromLower(xtsx);
    }

    /**
     * Computes XSX'
     *
     * @param s
     * @param x
     * @return
     * @throws MatrixException
     */
    public static Matrix quadraticFormT(final Matrix s, final Matrix x)
            throws MatrixException {
        int nr = x.getRowsCount();
        Matrix XS = x.times(s);
        DataBlockIterator rows = XS.rows(), cols = x.rows();
        Matrix o = new Matrix(nr, nr);

        int idx = 0, c = 0;
        DataBlock col = cols.getData(), row = rows.getData();
        do {
            idx += c;
            rows.setPosition(c++);
            do {
                o.data_[idx++] = row.dot(col);
            } while (rows.next());
        } while (cols.next());
        fromLower(o);
        return o;
    }

    /**
     * Computes XSX'
     *
     * @param s
     * @param x
     * @return
     * @throws MatrixException
     */
    public static Matrix quadraticFormT(final SubMatrix s, final SubMatrix x)
            throws MatrixException {
        /*
         * if (s == null) throw new ArgumentNullException("s"); if (x == null)
         * throw new ArgumentNullException("x");
         */
        int n = s.getRowsCount();
        // if (x.ColumnsCount != n)
        // throw new MatrixException(MatrixException.IncompatibleDimensions);
        int nr = x.getRowsCount();
        Matrix XS = new Matrix(nr, n);
        XS.subMatrix().product(x, s);
        DataBlockIterator rows = XS.rows(), cols = x.rows();
        Matrix o = new Matrix(nr, nr);

        int idx = 0, c = 0;
        DataBlock col = cols.getData(), row = rows.getData();
        do {
            idx += c;
            rows.setPosition(c++);
            do {
                o.data_[idx++] = row.dot(col);
            } while (rows.next());
        } while (cols.next());
        fromLower(o);
        return o;
    }

    /**
     * Computes X*S*X'
     *
     * @param s
     * @param x
     * @param xsxt
     * @throws MatrixException
     */
    public static void quadraticFormT(final SubMatrix s, final SubMatrix x,
            final SubMatrix xsxt) throws MatrixException {
        int n = s.getRowsCount();
        int nr = x.getRowsCount();
        Matrix XS = new Matrix(nr, n);
        XS.subMatrix().product(x, s);
        DataBlockIterator rows = XS.rows(), cols = x.rows(), xcols = xsxt
                .columns();
        DataBlock col = cols.getData(), xcol = xcols.getData(), cur = rows
                .getData();
        int c = 0;
        do {
            int idx = c;
            rows.setPosition(c++);
            do {
                xcol.set(idx++, cur.dot(col));
            } while (rows.next());
        } while (cols.next() && xcols.next());
        fromLower(xsxt);
    }

    /**
     *
     * @param m
     */
    public static void rcumul(Matrix m) {
        double[] data = m.data_;
        int n = m.nrows_;

        // RCumul columns
        int cur = n * n - 1, cmin = n * (n - 1);
        while (cmin >= 0) {
            while (--cur >= cmin) {
                data[cur] += data[cur + 1];
            }
            cmin -= n;
        }

        // RCumul rows. Only upper triangular matrix
        int cmax = n * n - 1, c = 0;
        while (++c < n) {
            --cmax;
            cmin = cmax - c * n;
            for (int i = cmax; i > cmin; i -= n) {
                data[i - n] += data[i];
            }
        }
        fromUpper(m);
    }

    /**
     *
     * @param m
     * @param delta
     */
    public static void rcumul(Matrix m, double delta) {
        double[] data = m.data_;
        int n = m.nrows_;

        // RCumul columns
        int cur = n * n - 1, cmin = n * (n - 1);
        while (cmin >= 0) {
            while (--cur >= cmin) {
                data[cur] += delta * data[cur + 1];
            }
            cmin -= n;
        }

        // RCumul rows. Only upper triangular matrix
        int cmax = n * n - 1, c = 0;
        while (++c < n) {
            --cmax;
            cmin = cmax - c * n;
            for (int i = cmax; i > cmin; i -= n) {
                data[i - n] += delta * data[i];
            }
        }

        fromUpper(m);
    }

    /**
     *
     * @param m
     * @param delta
     * @param lag
     */
    public static void rcumul(Matrix m, double delta, int lag) {
        double[] data = m.data_;
        int n = m.nrows_;

        // RCumul columns
        for (int imax = n * n - 1, jmax = n * (n - 1) + lag; imax > 0; imax -= n, jmax -= n) {
            for (int j = imax; j >= jmax; j -= lag) {
                data[j - lag] += delta * data[j];
            }
        }
        // RCumul rows. Only upper triangular matrix
        int cmax = n * n - 1, c = 0;
        int dlag = lag * n;
        while (++c < n) {
            --cmax;
            int cmin = cmax - c * n;
            for (int i = cmax - dlag; i >= cmin; i -= dlag) {
                data[i] += delta * data[i + dlag];
            }
        }

        fromUpper(m);
    }

    /**
     *
     * @param m
     * @param lag
     */
    public static void rcumul(Matrix m, int lag) {
        double[] data = m.data_;
        int n = m.nrows_;

        // RCumul columns
        for (int imax = n * n - 1, jmax = n * (n - 1) + lag; imax > 0; imax -= n, jmax -= n) {
            for (int j = imax; j >= jmax; j -= lag) {
                data[j - lag] += data[j];
            }
        }
        // RCumul rows. Only upper triangular matrix
        int cmax = n * n - 1, c = 0;
        int dlag = lag * n;
        while (++c < n) {
            --cmax;
            int cmin = cmax - c * n;
            for (int i = cmax - dlag; i >= cmin; i -= dlag) {
                data[i] += data[i + dlag];
            }
        }

        fromUpper(m);
    }

    /**
     *
     * @param m
     * @throws MatrixException
     */
    public static void reinforceSymmetry(final Matrix m) throws MatrixException {
        double[] data = m.data_;
        int n = m.nrows_;
        if (n != m.ncols_) {
            throw new MatrixException(MatrixException.SquareOnly);
        }
        for (int c = 0, id = 0; c < n; ++c, id += n + 1) {
            for (int r = c + 1, il = id, iu = id; r < n; ++r) {
                ++il;
                iu += n;
                double q = (data[iu] + data[il]) / 2;
                data[iu] = q;
                data[il] = q;
            }
        }
    }

    public static void reinforceSymmetry(final SubMatrix m) throws MatrixException {
        int n = m.m_nrows;
        if (n != m.m_ncols) {
            throw new MatrixException(MatrixException.SquareOnly);
        }
        for (int i = 0; i < n - 1; ++i) {
            DataBlock r = m.row(i).drop(i + 1, 0);
            DataBlock c = m.column(i).drop(i + 1, 0);
            c.add(r);
            c.mul(.5);
            r.copy(c);
        }
    }

    /**
     *
     * @param s
     * @throws MatrixException
     */
    public static void ucholesky(final Matrix s) throws MatrixException {
        // if (s == null)
        // throw new ArgumentNullException("s");
        double[] data = s.data_;
        int n = s.nrows_;
        // if (n != s.ncols_)
        // throw new MatrixException(MatrixException.SquareOnly);

        int ymax = data.length;
        for (int i = 0, idiag = 0; i < n; ++i, idiag += n + 1) {
            // compute aii;
            double aii = data[idiag];
            for (int j = i * n; j < idiag; ++j) {
                double x = data[j];
                aii -= x * x;
            }
            if (aii <= 0) {
                throw new MatrixException(MatrixException.CholeskyFailed);
            }
            aii = Math.sqrt(aii);
            data[idiag] = aii;

            // compute elemtents i+1 : n of column i
            for (int jx = i * n; jx < idiag; ++jx) {
                double temp = data[jx];
                if (temp != 0) {
                    for (int ia = jx + n, iy = idiag + n; iy < ymax; ia += n, iy += n) {
                        data[iy] -= temp * data[ia];
                    }
                }
            }
            for (int iy = idiag + n; iy < ymax; iy += n) {
                data[iy] /= aii;
            }

        }
        s.toUpper();
    }

    /*
     * [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming",
     * "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")] public
     * static Matrix LtL(Matrix lower) { //if (lower == null) // throw new
     * ArgumentNullException("lower"); int n = lower.RowsCount; double[] pl =
     * lower.data_; Matrix O = new Matrix(n, n); double[] po = O.data_; for
     * (int i = 0, idiag = 0, omax = n; i < n; ++i, idiag += n + 1, omax += n) {
     * for (int l = i; l <= idiag; l += n) { double x = pl[l]; for (int o =
     * idiag, q = l; o < omax; ++o, ++q) po[o] += pl[q] * x; } } FromLower(O);
     * return O; }
     */
    /**
     *
     * @param upper
     * @return
     */
    public static Matrix UtU(final Matrix upper) {
        // if (upper == null)
        // throw new ArgumentNullException("upper");
        int n = upper.getRowsCount();
        double[] pu = upper.data_;
        Matrix O = new Matrix(n, n);
        double[] po = O.data_;
        int umax = pu.length;
        for (int i = 0, j = 0; i < n; ++i, j += n) {
            int idiag = j + i;
            for (int u = j; u < umax; u += n) {
                double x = 0;
                for (int o = j, p = u; o <= idiag; ++o, ++p) {
                    x += pu[p] * pu[o];
                }
                po[u + i] = x;
            }
        }
        fromUpper(O);
        return O;
    }

    // X + X'
    /**
     *
     * @param m
     * @return
     */
    public static Matrix XpXt(final Matrix m) {
        Matrix o = m.clone();
        o.subMatrix().add(m.subMatrix().transpose());
        return o;
    }

    /**
     *
     * @param m
     * @param o
     */
    public static void XpXt(final SubMatrix m, final SubMatrix o) {
        o.sum(m, m.transpose());
    }

    /**
     *
     * @param x
     * @return
     * @throws MatrixException
     */
    public static Matrix XtX(final Matrix x) throws MatrixException {
        // if (x == null)
        // throw new ArgumentNullException("x");
        int n = x.getColumnsCount();
        DataBlockIterator rows = x.columns(), cols = x.columns();
        Matrix o = new Matrix(n, n);
        int idx = 0, c = 0;
        DataBlock col = cols.getData();
        DataBlock row = rows.getData();
        do {
            idx += c;
            rows.setPosition(c++);
            do {
                o.data_[idx++] = row.dot(col);
            } while (rows.next());
        } while (cols.next());
        fromLower(o);
        return o;
    }

    /**
     * Computes X'X and stores the results in m. The routines doesn't verify the
     * conditions on the dimensions of the sub-matrices.
     *
     * @param x r x c sub-matrix
     * @param m c x c sub-matrix.
     */
    public static void XtX(final SubMatrix x, final SubMatrix m) {
        DataBlockIterator rows = x.columns(), cols = x.columns(), rcols = m
                .columns();
        int c = 0;
        DataBlock col = cols.getData();
        DataBlock rcol = rcols.getData();
        DataBlock cur = rows.getData();
        do {
            int idx = c;
            rows.setPosition(c++);
            do {
                rcol.set(idx++, cur.dot(col));
            } while (rows.next());
        } while (cols.next() && rcols.next());
        fromLower(m);
    }

    /**
     *
     * @param x
     * @return
     * @throws MatrixException
     */
    public static Matrix XXt(final Matrix x) throws MatrixException {
        // if (x == null)
        // throw new ArgumentNullException("x");
        int n = x.getRowsCount();
        DataBlockIterator rows = x.rows(), cols = x.rows();
        Matrix o = new Matrix(n, n);
        int idx = 0, c = 0;
        DataBlock col = cols.getData(), row = rows.getData();
        do {
            idx += c;
            rows.setPosition(c++);
            do {
                o.data_[idx++] = row.dot(col);
            } while (rows.next());
        } while (cols.next());
        fromLower(o);
        return o;
    }

    /**
     *
     * @param x
     * @param pm
     * @throws MatrixException
     */
    public static void XXt(final SubMatrix x, final SubMatrix pm)
            throws MatrixException {
        DataBlockIterator rows = x.rows(), cols = x.rows(), rcols = pm
                .columns();
        int c = 0;
        DataBlock col = cols.getData();
        DataBlock rcol = rcols.getData();
        DataBlock cur = rows.getData();
        do {
            int idx = c;
            rows.setPosition(c++);
            do {
                rcol.set(idx++, cur.dot(col));
            } while (rows.next());
        } while (cols.next() && rcols.next());
        fromLower(pm);
    }

    /**
     * Computes S=S+x*a*x'
     *
     * @param S
     * @param a
     * @param x
     */
    public static void addXaXt(Matrix S, double a, DataBlock x) {
        if (a == 0) {
            return;
        }
        double[] s = S.data_;
        double[] z = x.getData();
        int n = S.ncols_, beg = x.getStartPosition(), inc = x.getIncrement();
        if (inc == 1) {
            for (int c = 0, k = 0, l = beg; c < n; ++c, k += c, ++l) {
                double x1 = z[l];
                if (x1 != 0) {
                    x1 *= a;
                    for (int r = c, m = l; r < n; ++r, ++k, ++m) {
                        s[k] += x1 * z[m];
                    }
                } else {
                    k += n - c;
                }
            }

        } else {
            for (int c = 0, k = 0, l = beg; c < n; ++c, k += c, l += inc) {
                double x1 = z[l];
                if (x1 != 0) {
                    x1 *= a;
                    for (int r = c, m = l; r < n; ++r, ++k, m += inc) {
                        s[k] += x1 * z[m];
                    }
                } else {
                    k += n - c;
                }
            }
        }
        fromLower(S);
    }

    /**
     * Solves SX=B, where S is a symmetric positive definite matrix.
     *
     * @param S The Symmetric matrix
     * @param B In-out parameter. The right-hand side of the equation. It will
     * contain the result at the end of the processing.
     * @param clone Indicates if the matrix S can be transformed (should be true
     * if the matrix S can't be modified). If S is transformed, it will contain
     * the lower Cholesky factor at the end of the processing.
     */
    public static void rsolve(Matrix S, SubMatrix B, boolean clone) {
        Matrix Q = S;
        if (clone) {
            Q = Q.clone();
        }
        lcholesky(Q);
        // LL'X = B
        // LY = B
        LowerTriangularMatrix.rsolve(Q, B);
        // L'X = Y
        // X'L = Y'
        LowerTriangularMatrix.lsolve(Q, B.transpose());
    }

    /**
     * Solves XS=B, where S is a symmetric positive definite matrix.
     *
     * @param S The Symmetric matrix
     * @param B In-out parameter. The right-hand side of the equation. It will
     * contain the result at the end of the processing.
     * @param clone Indicates if the matrix S can be transformed (should be true
     * if the matrix S can't be modified). If S is transformed, it will contain
     * the lower Cholesky factor at the end of the processing.
     */
    public static void lsolve(Matrix S, SubMatrix B, boolean clone) {
        Matrix Q = S;
        if (clone) {
            Q = Q.clone();
        }
        lcholesky(Q);
        // XLL' = B
        // YL' = B or LY'=B'
        LowerTriangularMatrix.rsolve(Q, B.transpose());
        // B contains Y'=(XL)' or Y = XL
        LowerTriangularMatrix.lsolve(Q, B);
    }

    /**
     * Solves xS=b or Sx=b, where S is a symmetric positive definite matrix.
     *
     * @param S The symmetric matrix
     * @param b In-Out parameter. The right-hand side of the equation. It will
     * contain the result at the end of the processing.
     * @param clone Indicates if the matrix S can be transformed (should be true
     * if the matrix S can't be modified). If S is transformed, it will contain
     * the lower Cholesky factor at the end of the processing.
     */
    public static void solve(Matrix S, DataBlock b, boolean clone) {
        Matrix Q = S;
        if (clone) {
            Q = Q.clone();
        }
        lcholesky(Q);
        // XLL' = B
        // YL' = B or LY'=B'
        LowerTriangularMatrix.rsolve(Q, b);
        // B contains Y'=(XL)' or Y = XL
        LowerTriangularMatrix.lsolve(Q, b);
    }

    /**
     * Solves Sx=b, where S is a symmetric positive definite matrix.
     *
     * @param S The Symmetric matrix
     * @param B In-out parameter. The right-hand side of the equation. It will
     * contain the result at the end of the processing.
     * @param clone Indicates if the matrix S can be transformed (should be true
     * if the matrix S can't be modified). If S is transformed, it will contain
     * the lower Cholesky factor at the end of the processing.
     */
    public static void rsolve(Matrix S, DataBlock x, boolean clone) {
        Matrix Q = S;
        if (clone) {
            Q = Q.clone();
        }
        lcholesky(Q);
        // LL'X = B
        // LY = B
        LowerTriangularMatrix.rsolve(Q, x);
        // L'X = Y
        // X'L = Y'
        LowerTriangularMatrix.lsolve(Q, x);
    }

    /**
     * Compute x * x'
     *
     * @param x column
     * @return
     */
    public static Matrix CCt(DataBlock x) {
        int n = x.getLength();
        Matrix m = new Matrix(n, n);
        DataBlockIterator cols = m.columns();
        DataBlock col = cols.getData();
        do {
            col.product(x, x.get(cols.getPosition()));
        } while (cols.next());
        return m;
    }

    private SymmetricMatrix() {
    }
}
