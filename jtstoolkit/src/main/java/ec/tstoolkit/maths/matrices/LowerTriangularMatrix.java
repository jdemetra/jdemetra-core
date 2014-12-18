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
public final class LowerTriangularMatrix {


    /**
     *
     * @param lower
     * @return
     * @throws MatrixException
     */
    public static Matrix inverse(final Matrix lower) throws MatrixException {
        int n = lower.ncols_;
        Matrix rslt = Matrix.identity(n);
        DataBlockIterator cols = rslt.columns();
        DataBlock cur = cols.getData();
        do {
            rsolve(lower, cur);
        } while (cols.next());
        return rslt;
    }

    /**
     * Computes l = l*L
     *
     * @param L
     * @param b
     */
    public static void lmul(final Matrix L, final DataBlock l) {
        int n = L.nrows_;
        double[] data = L.data_;

        double[] x = l.getData();
        int xinc = l.getIncrement();
        int xbeg = l.getStartPosition();
        int xend = l.getEndPosition();
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
     * The method left-multiplies the matrix with a vector. The Length of the
     * vector must be equal or less than the number of rows of the matrix. The
     * multiplier is modified in place.<br>
     *
     * @param lower
     * @param left An array of double
     */
    public static void lmul(final Matrix lower, final double[] left) {
        lmul(lower, new DataBlock(left));
    }

    /**
     *
     * @param lower
     * @param left
     */
    public static void lmul(final Matrix lower, final SubMatrix left) {
        // if (left == null)
        // throw new ArgumentNullException("left");
        DataBlockIterator rows = left.rows();
        DataBlock cur = rows.getData();
        do {
            lmul(lower, cur);
        } while (rows.next());
    }
    
    public static void lsolve(final Matrix L, final DataBlock b)
            throws MatrixException {
        lsolve(L, b, 0);
    }

    public static void lsolve2(final Matrix L, final DataBlock b)
            throws MatrixException {
        lsolve2(L, b, 0);
    }
    /**
     * Solves the set of equations x*L = b
     *
     * @param L
     * @param b
     * @throws MatrixException
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
     *
     * @param lower
     * @param b
     * @throws MatrixException
     */
    public static void lsolve2(final Matrix lower, final DataBlock b, double zero)
            throws MatrixException {
        /*
         * if (lower == null) throw new ArgumentNullException("lower"); if (b ==
         * null) throw new ArgumentNullException("b");
         */
        int n = lower.ncols_;
        double[] data = lower.data_;
        int nb = b.getLength();
        // if (nb > n)
        // throw new MatrixException(MatrixException.IncompatibleDimensions);

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
     * @param lower
     * @param b On entry the left hand side of the equation. Contains the
     * solution x on returning.
     * @throws MatrixException Thrown when the Length of b is larger than the
     * number of rows of the matrix.
     */
    public static void lsolve(final Matrix lower, final double[] b)
            throws MatrixException {
        lsolve(lower, new DataBlock(b));
    }

    /**
     * Solves the set of equations x*L = B
     *
     * @param L
     * @param B
     * @throws MatrixException
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

    public static void lsolve(final Matrix L, final SubMatrix B)
            throws MatrixException {
        lsolve(L, B, 0);
    }

     /**
     * Computes r = L * r
     *
     * @param L
     * @param r
     */
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
//	int nb = r.getLength();
//	int n = L.ncols_;
//        DataBlockIterator cols;
//        if (n==nb)
//            cols=L.columns();
//        else
//            cols=L.subMatrix(0, nb, 0, nb).columns();
//        DataBlock x=r.clone();
//        cols.end();
//        DataBlock col=cols.getCurrent();
//        x.shrink(nb, 0);
//        col.shrink(nb, 0);
//        do
//        {
//            x.expand(1, 0);
//            col.expand(1, 0);
//            double z=x.get(0);
//            x.set(0, 0);
//            x.addAY(z, col);
//        }
//        while (cols.previous());
    }

    /**
     * The method right-multiplies the matrix with a vector. The Length of the
     * vector must be equal or less than the number of rows of the matrix. The
     * multiplier is modified in place<br> IncompatibleDimensionsException
     * Thrown when the Length of the vector exceeds the number of rows of the
     * matrix
     *
     * @param lower
     * @param r An array of double
     */
    public static void rmul(final Matrix lower, final double[] r) {
        // if (r == null)
        // throw new ArgumentNullException("r");

        rmul(lower, new DataBlock(r));
    }

    /**
     *
     * @param lower
     * @param rightmatrix
     */
    public static void rmul(final Matrix lower, final SubMatrix rightmatrix) {
        // if (rightmatrix == null)
        // throw new ArgumentNullException("rightmatrix");
        DataBlockIterator cols = rightmatrix.columns();
        DataBlock cur = cols.getData();
        do {
            rmul(lower, cur);
        } while (cols.next());
    }

    /**
     * Solves the set of equations Lx = b where x and b are vectors with a
     * Length less than or equal to the number of rows of the matrix. The
     * solution is returned in place i.e. the solution x replaces the right hand
     * side b.
     *
     * @param lower L. Lower triangular matrix
     * @param b On entry the right hand side of the equation. Contains the
     * solution x on returning.
     * @throws MatrixException Thrown when the Length of b is larger than the
     * number of rows of the matrix.
     */
    public static void rsolve2(final Matrix L, final DataBlock b, double zero)
            throws MatrixException {
        /*
         * if (lower == null) throw new ArgumentNullException("lower"); if (b ==
         * null) throw new ArgumentNullException("b");
         */
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
    
    public static void rsolve(final Matrix L, final DataBlock b){
        rsolve(L, b, 0);
    }

    public static void rsolve2(final Matrix L, final DataBlock b){
        rsolve2(L, b, 0);
    }

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
     *
     * @param L L. Lower triangular matrix
     * @param b On entry the right hand side of the equation. Contains the
     * solution X on returning.
     * @throws MatrixException Thrown when the number of rows of B is larger
     * than the number of columns of the matrix.
     */
    public static void rsolve(final Matrix L, final SubMatrix B)
            throws MatrixException {
        rsolve(L, B, 0);
    }
    
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
