/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jd.maths.matrices;

import jd.data.DataBlock;
import jd.data.DataBlockIterator;
import demetra.data.LogSign;
import demetra.random.RandomNumberGenerator;

/**
 *
 * @author Jean Palate <jean.paal.UtilityClass
 */
@lombok.experimental.UtilityClass
public class LowerTriangularMatrix {

    public void randomize(FastMatrix M, RandomNumberGenerator rng) {
        M.set((r, c) -> (c > r) ? 0 : rng.nextDouble());
    }
 
    // Matrix versions
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
    public void lsolve(final FastMatrix L, final FastMatrix B, double zero) throws MatrixException {
        DataBlockIterator rows = B.rowsIterator();
        while (rows.hasNext()) {
            lsolve(L, rows.next(), zero);
        }
    }

    public void lsolve(final FastMatrix L, final FastMatrix B) throws MatrixException {
        lsolve(L, B, 0);
    }

    /**
     * Solves the set of equations LX = B where X and B are matrices with a
     * number of rows less than or equal to the number of columns of L. The
     * solution is returned in place i.e. the solution X replaces the right hand
     * side B. Column version
     *
     * @param L L. Lower triangular matrix
     * @param B On entry the right hand side of the equation. Contains the
     * solution X on returning.
     * @param zero Small positive value identifying 0. Can be 0.
     * @throws MatrixException Thrown when the system cannot be solved.
     */
    public void rsolve(final FastMatrix L, final FastMatrix B, final double zero) throws MatrixException {
        DataBlockIterator columns = B.columnsIterator();
        while (columns.hasNext()) {
            rsolve(L, columns.next(), zero);
        }
    }

    public void rsolve(final FastMatrix L, final FastMatrix B) throws MatrixException {
        rsolve(L, B, 0);
    }

    /**
     * Computes B = L * B
     *
     * @param L
     * @param B
     */
    public void rmul(final FastMatrix L, final FastMatrix B) {
        DataBlockIterator columns = B.columnsIterator();
        while (columns.hasNext()) {
            rmul(L, columns.next());
        }
    }

    /**
     * Computes B = B * L
     *
     * @param L
     * @param B
     * @throws MatrixException
     */
    public void lmul(final FastMatrix L, final FastMatrix B) {
        DataBlockIterator rows = B.rowsIterator();
        while (rows.hasNext()) {
            lmul(L, rows.next());
        }
    }

    /**
     * Computes the inverse of a triangular matrix R = L^-1
     *
     * @param L The triangular matrix being inverted
     * @return The inverse
     * @throws MatrixException when the matrix is non invertible (some elements
     * of the diagonal are 0).
     */
    public FastMatrix inverse(final FastMatrix L) throws MatrixException {
        int n = L.getRowsCount();
        FastMatrix IL = CanonicalMatrix.identity(n);
        rsolve(L, IL);
        return IL;
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
    public void rsolve(FastMatrix L, final DataBlock b, double zero) throws MatrixException {
        if (L.getRowIncrement() == 1) {
            rsolve_column(L, b, zero);
        } else {
            rsolve_row(L, b, zero);
        }
    }

    public void rsolve(FastMatrix M, DataBlock x) throws MatrixException {
        rsolve(M, x, 0);
    }

    /**
     * Solves the set of equations x*L = b Forward substitution, column version.
     *
     * When L contains zeroes on the diagonal, the system is either unsolvable
     * or under-determined. Unsolvable systems will throw exceptions. In the
     * case of under-determined systems, the x corresponding to the 0 in the
     * diagonal is set to 0.
     *
     * @param L The lower triangular matrix
     * @param b In-Out parameter. On entry, it contains b. On exit, it contains
     * x.
     * @param zero Small positive value identifying 0. Can be 0.
     * @throws MatrixException The exception is thrown when the system cannot be
     * solved
     */
    public void lsolve(FastMatrix L, DataBlock x, double zero) throws MatrixException {
        if (L.getRowIncrement() == 1) {
            lsolve_column(L, x, zero);
        } else {
            lsolve_row(L, x, zero);
        }
    }

    public void lsolve(FastMatrix M, DataBlock x) throws MatrixException {
        lsolve(M, x, 0);
    }

    /**
     * Computes r = L*r The method right-multiplies the matrix by a vector. The
     * Length of the vector must be equal or less than the number of rows of the
     * matrix. The multiplier is modified in place. Column version
     *
     * @param L The lower triangular matrix
     * @param x
     * @param r An array of double
     */
    public void rmul(FastMatrix L, DataBlock x) {
        if (L.getRowIncrement() == 1) {
            rmul_column(L, x);
        } else {
            rmul_row(L, x);
        }
    }
    
    public void lmul(FastMatrix L, DataBlock x) {
        if (L.getRowIncrement() == 1) {
            lmul_column(L, x);
        } else {
            lmul_row(L, x);
        }
    }

    public void toLower(FastMatrix M) {
        int m = M.getColumnsCount(), n = M.getRowsCount();
        if (m == 1) {
            return;
        }
        int rinc = M.getRowIncrement(), cinc = M.getColumnIncrement(), start = M.getStartPosition();
        double[] x = M.getStorage();
        int q = Math.min(m, n);
        for (int c = 0, id = start; c < q; ++c, id += rinc + cinc) {
            for (int r = c + 1, iu = id; r < m; ++r) {
                iu += cinc;
                x[iu] = 0;
            }
        }
    }

    
    public LogSign logDeterminant(FastMatrix L){
        return LogSign.of(L.diagonal());
    }
    
    public double determinant(FastMatrix L){
        LogSign ls=logDeterminant(L);
        if (ls == null)
            return 0;
        double val=Math.exp(ls.getValue());
        return ls.isPositive() ? val : -val;
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
    public void rsolve_row(FastMatrix L, final DataBlock b, double zero) throws MatrixException {
        double[] data = L.getStorage();
        double[] x = b.getStorage();
        int xbeg = b.getStartPosition();
        int xinc = b.getIncrement();
        int xend = b.getEndPosition();

        int rinc = L.getRowIncrement(), cinc = L.getColumnIncrement();

        if (xinc == 1 && cinc == 1) {
            for (int i = L.getStartPosition(), xi = xbeg; xi < xend; i += rinc, ++xi) {
                int idx = i;
                double t = x[xi];
                for (int xj = xbeg; xj < xi; ++xj, ++idx) {
                    t -= x[xj] * data[idx];
                }
                if (Math.abs(t) > zero) {
                    double d = data[idx];
                    if (d == 0) {
                        throw new MatrixException(MatrixException.SINGULAR);
                    }
                    x[xi] = t / d;
                } else {
                    x[xi] = 0;
                }
            }
        } else {
            for (int i = L.getStartPosition(), xi = xbeg; xi != xend; i += rinc, xi += xinc) {
                int idx = i;
                double t = x[xi];
                for (int xj = xbeg; xj != xi; xj += xinc, idx += cinc) {
                    t -= x[xj] * data[idx];
                }
                if (Math.abs(t) > zero) {
                    double d = data[idx];
                    if (d == 0) {
                        throw new MatrixException(MatrixException.SINGULAR);
                    }
                    x[xi] = t / d;
                } else {
                    x[xi] = 0;
                }
            }
        }
    }

    public void rsolve_column(final FastMatrix L, final DataBlock b, double zero) throws MatrixException {
        double[] data = L.getStorage();

        double[] x = b.getStorage();
        int xbeg = b.getStartPosition();
        int xinc = b.getIncrement();
        int xend = b.getEndPosition();

        int rinc = L.getRowIncrement(), cinc = L.getColumnIncrement();
        int dinc = rinc + cinc;

        if (xinc == 1 && rinc == 1) {
            for (int i = L.getStartPosition(), xi = xbeg; xi < xend; i += dinc, ++xi) {
                double t = x[xi];
                if (Math.abs(t) > zero) {
                    double d = data[i];
                    if (d == 0) {
                        for (int xj = xi + xinc, j = i + rinc; xj < xend; ++xj, ++j) {
                            if (Math.abs(data[j]) > zero) {
                                throw new MatrixException(MatrixException.SINGULAR);
                            }
                        }
                        x[xi] = 0;
                    } else {
                        double c = t / d;
                        x[xi] = c;
                        for (int xj = xi + xinc, j = i + rinc; xj < xend; ++xj, ++j) {
                            x[xj] -= c * data[j];
                        }
                    }
                } else {
                    x[xi] = 0;
                }
            }
        } else {
            for (int i = L.getStartPosition(), xi = xbeg; xi != xend; i += dinc, xi += xinc) {
                double t = x[xi];
                if (Math.abs(t) > zero) {
                    double d = data[i];
                    if (d == 0) {
                        for (int xj = xi + xinc, j = i + rinc; xj != xend; xj += xinc, j += rinc) {
                            if (Math.abs(data[j]) > zero) {
                                throw new MatrixException(MatrixException.SINGULAR);
                            }
                        }
                        x[xi] = 0;
                    } else {
                        double c = t / d;
                        x[xi] = c;
                        for (int xj = xi + xinc, j = i + rinc; xj != xend; xj += xinc, j += rinc) {
                            x[xj] -= c * data[j];
                        }
                    }
                } else {
                    x[xi] = 0;
                }
            }
        }
    }


    public void lsolve_row(final FastMatrix L, final DataBlock b, double zero)
            throws MatrixException {
        double[] data = L.getStorage();

        double[] x = b.getStorage();
        int xinc = b.getIncrement();
        int xbeg = b.getStartPosition() - xinc;
        int xend = b.getEndPosition() - xinc;

        int rinc = L.getRowIncrement(), cinc = L.getColumnIncrement();

        if (xinc == 1 && cinc == 1) {
            for (int i = L.getLastPosition(), xi = xend; xi > xbeg; i -= rinc + cinc, --xi) {
                double t = x[xi];
                if (Math.abs(t) > zero) {
                    double d = data[i];
                    if (d == 0) {
                        throw new MatrixException(MatrixException.SINGULAR);
                    }
                    double c = t / d;
                    x[xi] = c;
                    for (int xj = xi - 1, j = i - cinc; xj > xbeg; --xj, --j) {
                        x[xj] -= c * data[j];
                    }
                } else {
                    x[xi] = 0;
                }
            }
        } else {
            for (int i = L.getLastPosition(), xi = xend; xi != xbeg; i -= rinc + cinc, xi -= xinc) {
                double t = x[xi];
                if (Math.abs(t) > zero) {
                    double d = data[i];
                    if (d == 0) {
                        throw new MatrixException(MatrixException.SINGULAR);
                    }
                    double c = t / d;
                    x[xi] = c;
                    for (int xj = xi - xinc, j = i - cinc; xj != xbeg; xj -= xinc, j -= cinc) {
                        x[xj] -= c * data[j];
                    }
                } else {
                    x[xi] = 0;
                }
            }
        }
    }

    /**
     * Solves the set of equations x*L = b Forward substitution, row version.
     *
     * @param L The lower triangular matrix
     * @param b In-Out parameter. On entry, it contains b. On exit, it contains
     * x.
     * @param zero Small positive value identifying 0. Can be 0.
     * @throws MatrixException The exception is thrown when the system cannot be
     * solved
     */
    public void lsolve_column(final FastMatrix L, final DataBlock b, double zero)
            throws MatrixException {
        double[] data = L.getStorage();
        int rinc = L.getRowIncrement(), cinc = L.getColumnIncrement();
        double[] x = b.getStorage();
        int xinc = b.getIncrement();
        int xbeg = b.getStartPosition();
        int xend = b.getEndPosition();

        if (xinc == 1 && rinc == 1) {
            for (int i = L.getLastPosition(), xi = xend; xi > xbeg; i -= cinc + rinc) {
                int xd = xi - 1;
                double t = x[xd];
                for (int xj = xi, idx = i + 1; xj < xend; ++xj, ++idx) {
                    t -= x[xj] * data[idx];
                }
                if (Math.abs(t) > zero) {
                    double d = data[i];
                    if (d == 0) {
                        throw new MatrixException(MatrixException.SINGULAR);
                    }
                    x[xd] = t / d;
                } else {
                    x[xd] = 0;
                }

                xi = xd;
            }
        } else {
            for (int i = L.getLastPosition(), xi = xend; xi != xbeg; i -= cinc + rinc) {
                int idx = i;
                int xd = xi - xinc;
                double t = x[xd];
                for (int xj = xi; xj != xend; xj += xinc) {
                    idx += rinc;
                    t -= x[xj] * data[idx];
                }
                if (Math.abs(t) > zero) {
                    double d = data[i];
                    if (d == 0) {
                        throw new MatrixException(MatrixException.SINGULAR);
                    }
                    x[xd] = t / d;
                } else {
                    x[xd] = 0;
                }

                xi = xd;
            }
        }
    }


    void rmul_column(final FastMatrix L, final DataBlock r) {
        double[] data = L.getStorage();
        int rinc = L.getRowIncrement(), cinc = L.getColumnIncrement();

        double[] x = r.getStorage();
        int xinc = r.getIncrement();
        int xend = r.getEndPosition();

        if (xinc == 1 && rinc == 1) {
            for (int li = L.getLastPosition(), xi = xend - 1; li >= L.getStartPosition(); li -= rinc + cinc, xi -= xinc) {
                double z = x[xi];
                if (z != 0) {
                    x[xi] = data[li] * z;
                    for (int xj = xi + 1, idx = li + 1; xj != xend; ++xj, ++idx) {
                        x[xj] += data[idx] * z;
                    }
                }
            }
        } else {
            for (int li = L.getLastPosition(), xi = xend - xinc; li >= L.getStartPosition(); li -= rinc + cinc, xi -= xinc) {
                double z = x[xi];
                if (z != 0) {
                    x[xi] = data[li] * z;
                    for (int xj = xi + xinc, idx = li + rinc; xj != xend; xj += xinc, idx += rinc) {
                        x[xj] += data[idx] * z;
                    }
                }
            }
        }
    }

    void rmul_row(final FastMatrix L, final DataBlock r) {
        double[] data = L.getStorage();
        int rinc = L.getRowIncrement(), cinc = L.getColumnIncrement();

        double[] x = r.getStorage();
        int xinc = r.getIncrement();
        int xbeg = r.getStartPosition() - xinc;
        int xend = r.getEndPosition() - xinc;

        if (xinc == 1 && cinc == 1) {
            for (int xi = xend, li = L.getLastPosition(); xi > xbeg; --xi, li -= rinc + 1) {
                double z = 0;
                for (int xj = xi, k = li; xj > xbeg; --xj, --k) {
                    z += x[xj] * data[k];
                }
                x[xi] = z;
            }
        } else {
            for (int xi = xend, li = L.getLastPosition(); xi != xbeg; xi -= xinc, li -= rinc + cinc) {
                double z = 0;
                for (int xj = xi, k = li; xj != xbeg; xj -= xinc, k -= cinc) {
                    z += x[xj] * data[k];
                }
                x[xi] = z;
            }
        }
    }

    void lmul_column(final FastMatrix L, final DataBlock r) {
        double[] data = L.getStorage();
        int rinc = L.getRowIncrement(), cinc = L.getColumnIncrement();

        double[] x = r.getStorage();
        int xinc = r.getIncrement();
        int xbeg = r.getStartPosition();
        int xend = r.getEndPosition();

        if (xinc == 1 && rinc == 1) {
            for (int li = L.getStartPosition(), xi = xbeg; xi < xend; li += cinc + 1, ++xi) {
                double z = 0;
                for (int xj = xi, k = li; xj < xend; ++xj, ++k) {
                    z += data[k] * x[xj];
                }
                x[xi] = z;
            }
        } else {
            for (int li = L.getStartPosition(), xi = xbeg; xi != xend; li += cinc + rinc, xi += xinc) {
                double z = 0;
                for (int xj = xi, k = li; xj != xend; xj += xinc, k += rinc) {
                    z += data[k] * x[xj];
                }
                x[xi] = z;
            }
        }
    }

    void lmul_row(final FastMatrix L, final DataBlock r) {
        double[] data = L.getStorage();
        int rinc = L.getRowIncrement(), cinc = L.getColumnIncrement();

        double[] x = r.getStorage();
        int xinc = r.getIncrement();
        int xbeg = r.getStartPosition();
        int xend = r.getEndPosition();

        if (xinc == 1 && cinc == 1) {
            for (int xi = xbeg, li = L.getStartPosition(); xi < xend; ++xi, li += rinc) {
                double z = x[xi];
                if (z != 0) {
                    int k = li;
                    for (int xj = xbeg; xj < xi; ++xj, ++k) {
                        x[xj] += data[k] * z;
                    }
                    x[xi] = data[k] * z;
                }
            }
        } else {
            for (int xi = xbeg, li = L.getStartPosition(); xi != xend; xi += xinc, li += rinc) {
                double z = x[xi];
                if (z != 0) {
                    int k = li;
                    for (int xj = xbeg; xj != xi; xj += xinc, k += cinc) {
                        x[xj] += data[k] * z;
                    }
                    x[xi] = data[k] * z;
                }
            }
        }
    }

}
