/*
 * Copyright 2013-2015 National Bank of Belgium
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
 * Collections of static methods to be applied on lower triangular matrices
 * It should be stressed that lower triangular matrices are represented by normal
 * matrices. The algorithms of this class never check that the considered matrices 
 * are actually lower triangular. In fact, they don't need to be lower triangular,
 * but just their lower part will be taken into account.
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public final class LowerTriangularMatrix {


    /**
     * Computes the inverse of a triangular matrix
     * R = L^-1
     * @param L The triangular matrix being inverted 
     * @return The inverse
     * @throws MatrixException when the matrix is non invertible (some elements 
     * of the diagonal are 0).
     */
    public static Matrix inverse(final Matrix L) throws MatrixException {
        int n = L.ncols_;
        Matrix rslt = Matrix.identity(n);
        DataBlockIterator cols = rslt.columns();
        DataBlock cur = cols.getData();
        do {
            rsolve(L, cur);
        } while (cols.next());
        return rslt;
    }

    /**
     * Left-multiplies the matrix by a vector. The multiplier is modified in place.
     * 
     * 
     * left = left*L
     *
     * @param L The lower triangular matrix
     * @param left The array (in/out parameter). Its Length  must be equal or 
     * less than the number of rows of the matrix. 
     */
    public static void lmul(final Matrix L, final DataBlock left) {
        int n = L.nrows_;
        double[] data = L.data_;

        double[] x = left.getData();
        int xinc = left.getIncrement();
        int xbeg = left.getStartPosition();
        int xend = left.getEndPosition();
        if (xinc == 1) {
            for (int xi = xbeg, idx = 0; xi < xend; ++xi, idx += n + 1) {
                double t = 0.0;
                for (int xj = xi, idx2 = idx; xj < xend; ++xj, ++idx2) {
                    t += data[idx2] * x[xj];
                }
                x[xi] = t;
            }
        } else {
            for (int xi = xbeg, idx = 0; xi != xend; xi += xinc, idx += n + 1) {
                double t = 0.0;
                for (int xj = xi, idx2 = idx; xj != xend; xj += xinc, ++idx2) {
                    t += data[idx2] * x[xj];
                }
                x[xi] = t;
            }
        }
    }

    /**
     * Left-multiplies the matrix by a vector. The multiplier is modified in place.
     * Unoptimised form
     * 
     * left = left*L
     *
     * @param L The lower triangular matrix
     * @param left The array (in/out parameter). Its Length  must be equal or 
     * less than the number of rows of the matrix. 
     */
    @Deprecated
    public static void lmul2(final Matrix L, final DataBlock left) {
        int n = L.nrows_;
        double[] data = L.data_;
        int nb = left.getLength();

        for (int i = 0, idx = 0; i < nb; ++i, idx += n + 1) {
            double t = 0.0;
            for (int j = i, idx2 = idx; j < nb; ++j, ++idx2) {
                t += data[idx2] * left.get(j);
            }
            left.set(i, t);
        }
    }

    /**
     * Left-multiplies the matrix by a vector. The multiplier is modified in place.
     * The DataBlock form should be preferred
     * 
     * left = left*L
     *
     * @param L The lower triangular matrix
     * @param left The array (in/out parameter). Its Length  must be equal or 
     * less than the number of rows of the matrix. 
     */
    @Deprecated
    public static void lmul(final Matrix L, final double[] left) {
        lmul(L, new DataBlock(left));
    }

     /**
     * Left-multiplies the matrix by a sub-matrix. The sub-matrix is modified in place.
     * 
     * left = left*L
     *
     * @param L The lower triangular matrix
     * @param left The sub-matrix (in/out parameter). Its number of rows must be equal or 
     * less than the number of rows of the triangular matrix. 
     */
    public static void lmul(final Matrix L, final SubMatrix left) {
        DataBlockIterator rows = left.rows();
        DataBlock cur = rows.getData();
        do {
            lmul(L, cur);
        } while (rows.next());
    }
    
    /**
     * Solves the set of equations x*L = b
     * Forward substitution, row version.
     * @param L The lower triangular matrix
     * @param b In-Out parameter. On entry, it contains b. On exit, it contains x.
     * @throws MatrixException The exception is thrown when the system cannot be solved
     */
    public static void lsolve(final Matrix L, final DataBlock b)
            throws MatrixException {
        lsolve(L, b, 0);
    }

    /**
     * Solves the set of equations x*L = b
     * Forward substitution, column version.
     * @param L The lower triangular matrix
     * @param b In-Out parameter. On entry, it contains b. On exit, it contains x.
     * @throws MatrixException The exception is thrown when the system cannot be solved
     */
    @Deprecated
    public static void lsolve2(final Matrix L, final DataBlock b)
            throws MatrixException {
        lsolve2(L, b, 0);
    }
    
    /**
     * Solves the set of equations x*L = b
     * Forward substitution, column version.
     * 
     * When L contains zeroes on the diagonal, the system is either unsolvable or
     * under-determined. Unsolvable systems will throw exceptions.
     * In the case of under-determined systems, the x corresponding to the 0 in the diagonal
     * is set to 0.
     * 
     * @param L The lower triangular matrix
     * @param b In-Out parameter. On entry, it contains b. On exit, it contains x.
     * @param zero Small positive value identifying 0. Can be 0.
     * @throws MatrixException The exception is thrown when the system cannot be solved
     */
    public static void lsolve(final Matrix L, final DataBlock b, double zero)
            throws MatrixException {
        int n = L.nrows_;
        double[] data = L.data_;
        double[] x = b.getData();
        int xinc = b.getIncrement();
        int xend = b.getEndPosition();
        int nb = b.getLength();

        for (int xi = xend - xinc, i = nb * n - 1; i >= 0; i -= n + 1, xi -= xinc) {
            double t = x[xi];
            for (int j = i + 1, xj = xi + xinc; xj != xend; ++j, xj += xinc) {
                t -= x[xj] * data[j];
            }
            if (Math.abs(t) <= zero) {
                x[xi] = 0;
            } else {
                double d = data[i];
                if (d == 0) {
                    throw new MatrixException(MatrixException.Singular);
                }
                x[xi] = t / d;
            }
        }
    }

    /**
     * Solves the set of equations x*L = b
     * Forward substitution, row version.
     * @param L The lower triangular matrix
     * @param b In-Out parameter. On entry, it contains b. On exit, it contains x.
     * @param zero Small positive value identifying 0. Can be 0.
     * @throws MatrixException The exception is thrown when the system cannot be solved
     */
    @Deprecated
    public static void lsolve2(final Matrix L, final DataBlock b, double zero)
            throws MatrixException {
        int n = L.ncols_;
        double[] data = L.data_;
        int nb = b.getLength();

        for (int i = nb - 1, idx = (n + 1) * i; i >= 0; --i, idx -= n + 1) {
            int idx2 = idx + 1;
            double t = b.get(i);
            for (int j = i + 1; j < nb; ++j, ++idx2) {
                t -= b.get(j) * data[idx2];
            }
            double d = data[idx];
            if (Math.abs(t) <= zero) {
                b.set(i, 0);
            } else {
                if (d == 0) {
                    throw new MatrixException(MatrixException.Singular);
                }
                b.set(i, t / d);
            }
        }
    }

    /**
     * Solves the set of equations x*L = b where x and b are vectors with a
     * Length less than or equal to the number of rows of the matrix. The
     * solution is returned in place i.e. the solution x replaces the right hand
     * side b.
     *
     * @param L The lower triangular matrix
     * @param b On entry the left hand side of the equation. Contains the
     * solution x on returning.
     * @throws MatrixException Thrown when the Length of b is larger than the
     * number of rows of the matrix.
     */
    public static void lsolve(final Matrix L, final double[] b)
            throws MatrixException {
        lsolve(L, new DataBlock(b));
    }

    /**
     * Solves the set of equations X*L = B
     *
     * @param L The lower triangular matrix
     * @param B On entry the left hand side of the equation. Contains the
     * solution x on returning.
     * @param zero Small positive value identifying 0. Can be 0.
     * @throws MatrixException Thrown when the Length of b is larger than the
     * number of rows of the matrix.
     */
    public static void lsolve(final Matrix L, final SubMatrix B, double zero)
            throws MatrixException {
        // if (left == null)
        // throw new ArgumentNullException("left");
        DataBlockIterator rows = B.rows();
        DataBlock cur = rows.getData();
        do {
            lsolve(L, cur, zero);
        } while (rows.next());
    }

    /**
     * Solves the set of equations X*L = B
     *
     * @param L The Lower triangular matrix
     * @param B in/out parameter. Contains B on entry, X on exit.
     * @throws MatrixException
     */
    public static void lsolve(final Matrix L, final SubMatrix B)
            throws MatrixException {
        lsolve(L, B, 0);
    }

    /**
     * Computes r = L*r
     * The method right-multiplies the matrix by a vector. The Length of the
     * vector must be equal or less than the number of rows of the matrix. The
     * multiplier is modified in place.
     * Column version
     *
     * @param L The lower triangular matrix
     * @param r An array of double
     */
    @Deprecated
    public static void rmul2(final Matrix L, final DataBlock r) {
        int nb = r.getLength();
        int n = L.ncols_;
        double[] data = L.data_;

        double[] x = r.getData();
        int xinc = r.getIncrement();
        int xbeg = r.getStartPosition();
        int xend = r.getEndPosition();

        for (int i = nb - 1, xi = xend; i >= 0; i--) {
            double t = 0.0;
            for (int xj = xbeg, idx = i; xj != xi; xj += xinc, idx += n) {
                t += data[idx] * x[xj];
            }
            xi -= xinc;
            x[xi] = t;
        }
    }

    /**
     * Computes r = L*r
     * The method right-multiplies the matrix by a vector. The Length of the
     * vector must be equal or less than the number of rows of the matrix. The
     * multiplier is modified in place.
     * Column version
     *
     * @param L The lower triangular matrix
     * @param r An array of double
     */
    public static void rmul(final Matrix L, final DataBlock r) {
        int nb = r.getLength();
        int nr = L.nrows_;
        double[] data = L.data_;

        double[] x = r.getData();
        int xinc = r.getIncrement();
        int xend = r.getEndPosition();

        // by column
        if (xinc == 1) {
            for (int li = nr * nb - 1, xi = xend - 1; li >= 0; li -= nr + 1, --xi) {
                double z = x[xi];
                if (z != 0) {
                    x[xi] = data[li] * z;
                    for (int xj = xi + 1, idx = li + 1; xj < xend; ++xj, ++idx) {
                        x[xj] += data[idx] * z;
                    }
                }
            }
        } else {
            for (int li = nr * nb - 1, xi = xend - xinc; li >= 0; li -= nr + 1, xi -= xinc) {
                double z = x[xi];
                if (z != 0) {
                    x[xi] = data[li] * z;
                    for (int xj = xi + xinc, idx = li + 1; xj != xend; xj += xinc, ++idx) {
                        x[xj] += data[idx] * z;
                    }
                }
            }
        }
    }

    /**
     * Computes r = L*r
     * The method right-multiplies the matrix by a vector. The Length of the
     * vector must be equal or less than the number of rows of the matrix. The
     * multiplier is modified in place.
     * Column version
     *
     * @param L The lower triangular matrix
     * @param r An array of double
     */
    public static void rmul(final Matrix L, final double[] r) {
        rmul(L, new DataBlock(r));
    }

    /**
     * Computes R = L*R
     * The method right-multiplies the matrix by a sub-matrix. The
     * multiplier is modified in place.
     * Column version
     *
     * @param L The lower triangular matrix
     * @param R The sub-matrix (in/out parameter)
     */
    public static void rmul(final Matrix L, final SubMatrix R) {
        DataBlockIterator cols = R.columns();
        DataBlock cur = cols.getData();
        do {
            rmul(L, cur);
        } while (cols.next());
    }

    /**
     * Solves the set of equations Lx = b where x and b are vectors with a
     * Length less than or equal to the number of rows of the matrix. The
     * solution is returned in place i.e. the solution x replaces the right hand
     * side b. Row version
     *
     * @param L L. Lower triangular matrix
     * @param b On entry the right hand side of the equation. Contains the
     * solution x on returning.
     * @param zero Small positive value identifying 0. Can be 0.
     * @throws MatrixException Thrown when the system cannot be solved
     */
    public static void rsolve2(final Matrix L, final DataBlock b, double zero)
            throws MatrixException {
        int n = L.nrows_;
        double[] data = L.data_;

        double[] x = b.getData();
        int xbeg = b.getStartPosition();
        int xinc = b.getIncrement();
        int xend = b.getEndPosition();

        for (int i = 0, xi = xbeg; xi != xend; ++i, xi += xinc) {
            int idx = i;
            double t = x[xi];
            for (int xj = xbeg; xj != xi; xj += xinc, idx += n) {
                t -= x[xj] * data[idx];
            }
            if (Math.abs(t) > zero) {
                double d = data[idx];
                if (d == 0) {
                    throw new MatrixException(MatrixException.Singular);
                }
                x[xi] = t / d;
            }
        }
    }
    
    /**
     * Solves the set of equations Lx = b where x and b are vectors with a
     * Length less than or equal to the number of rows of the matrix. The
     * solution is returned in place i.e. the solution x replaces the right hand
     * side b. Column version
     *
     * @param L L. Lower triangular matrix
     * @param b On entry the right hand side of the equation. Contains the
     * solution x on returning.
     * @throws MatrixException Thrown when the system cannot be solved
     */
    public static void rsolve(final Matrix L, final DataBlock b){
        rsolve(L, b, 0);
    }

    /**
     * Solves the set of equations Lx = b where x and b are vectors with a
     * Length less than or equal to the number of rows of the matrix. The
     * solution is returned in place i.e. the solution x replaces the right hand
     * side b. Row version
     *
     * @param L L. Lower triangular matrix
     * @param b On entry the right hand side of the equation. Contains the
     * solution x on returning.
     * @throws MatrixException Thrown when the system cannot be solved
     */
    public static void rsolve2(final Matrix L, final DataBlock b){
        rsolve2(L, b, 0);
    }

    /**
     * Solves the set of equations Lx = b where x and b are vectors with a
     * Length less than or equal to the number of rows of the matrix. The
     * solution is returned in place i.e. the solution x replaces the right hand
     * side b. Column version
     *
     * @param L L. Lower triangular matrix
     * @param b On entry the right hand side of the equation. Contains the
     * solution x on returning.
     * @param zero Small positive value identifying 0. Can be 0.
     * @throws MatrixException Thrown when the system cannot be solved
     */
    public static void rsolve(final Matrix L, final DataBlock b, double zero)
            throws MatrixException {
        int n = L.nrows_;
        double[] data = L.data_;

        double[] x = b.getData();
        int xbeg = b.getStartPosition();
        int xinc = b.getIncrement();
        int xend = b.getEndPosition();

        if (xinc == 1) {
            for (int i = 0, xi = xbeg; xi < xend; i += n + 1, ++xi) {
                double t = x[xi];
                if (Math.abs(t) > zero) {
                    double d = data[i];
                    if (d == 0) {
                        throw new MatrixException(MatrixException.Singular);
                    }
                    double c = t / d;
                    x[xi] = c;
                    for (int xj = xi + 1, j = i + 1; xj < xend; ++xj, ++j) {
                        x[xj] -= c * data[j];
                    }
                }
            }
        } else {
            for (int i = 0, xi = xbeg; xi != xend; i += n + 1, xi += xinc) {
                double t = x[xi];
                if (Math.abs(t) > zero) {
                    double d = data[i];
                    if (d == 0) {
                        throw new MatrixException(MatrixException.Singular);
                    }
                    double c = t / d;
                    x[xi] = c;
                    for (int xj = xi + xinc, j = i + 1; xj != xend; xj += xinc, ++j) {
                        x[xj] -= c * data[j];
                    }
                }
            }
        }
    }

    /**
     * Solves the set of equations Ax = b where x and b are vectors with a
     * Length less than or equal to the number of rows of the matrix. The
     * solution is returned in place i.e. the solution x replaces the right hand
     * side b.
     *
     * @param lower
     * @param b On entry the right hand side of the equation. Contains the
     * solution x on returning.
     * @throws MatrixException Thrown when the Length of b is larger than the
     * number of rows of the matrix.
     */
    public static void rsolve(final Matrix lower, final double[] b)
            throws MatrixException {
        rsolve(lower, new DataBlock(b));
    }

    /**
     * Solves the set of equations LX = B where X and B are matrices with a
     * number of rows less than or equal to the number of columns of L. The
     * solution is returned in place i.e. the solution X replaces the right hand
     * side B.
     * Column version
     *
     * @param L L. Lower triangular matrix
     * @param B On entry the right hand side of the equation. Contains the
     * solution X on returning.
     * @throws MatrixException Thrown when the system cannot be solved.
     */
    public static void rsolve(final Matrix L, final SubMatrix B)
            throws MatrixException {
        rsolve(L, B, 0);
    }
    
    /**
     * Solves the set of equations LX = B where X and B are matrices with a
     * number of rows less than or equal to the number of columns of L. The
     * solution is returned in place i.e. the solution X replaces the right hand
     * side B.
     * Column version
     *
     * @param L L. Lower triangular matrix
     * @param B On entry the right hand side of the equation. Contains the
     * solution X on returning.
     * @param zero Small positive value identifying 0. Can be 0.
     * @throws MatrixException Thrown when the system cannot be solved.
     */
    public static void rsolve(final Matrix L, final SubMatrix B, final double zero)
            throws MatrixException {
        // if (B == null)
        // throw new ArgumentNullException("B");
        DataBlockIterator cols = B.columns();
        DataBlock cur = cols.getData();
        do {
            rsolve(L, cur, zero);
        } while (cols.next());
    }

    private LowerTriangularMatrix() {
    }
}
