/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.maths.matrices.internal;

import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.MatrixException;
import demetra.maths.matrices.spi.LowerTriangularMatrixAlgorithms;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = LowerTriangularMatrixAlgorithms.class)
public final class FastLowerTriangularMatrixAlgorithms implements LowerTriangularMatrixAlgorithms {

    public FastLowerTriangularMatrixAlgorithms() {
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
    @Override
    public void rsolve(final Matrix L, final DataBlock b, double zero) throws MatrixException {
        if (L.getRowIncrement() == 1) {
            rsolve_column(L, b, zero);
        } else {
            rsolve_row(L, b, zero);
        }
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
    public void rsolve_row(final Matrix L, final DataBlock b, double zero) throws MatrixException {
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

    public void rsolve_column(final Matrix L, final DataBlock b, double zero) throws MatrixException {
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

    @Override
    public void lsolve(Matrix L, DataBlock x, double zero) throws MatrixException {
        if (L.getRowIncrement() == 1) {
            lsolve_column(L, x, zero);
        } else {
            lsolve_row(L, x, zero);
        }
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
    public void lsolve_row(final Matrix L, final DataBlock b, double zero)
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
    public void lsolve_column(final Matrix L, final DataBlock b, double zero)
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

    /**
     * Computes r = L*r The method right-multiplies the matrix by a vector. The
     * Length of the vector must be equal or less than the number of rows of the
     * matrix. The multiplier is modified in place. Column version
     *
     * @param L The lower triangular matrix
     * @param x
     * @param r An array of double
     */
    @Override
    public void rmul(Matrix L, DataBlock x) {
        if (L.getRowIncrement() == 1) {
            rmul_column(L, x);
        } else {
            rmul_row(L, x);
        }
    }

    public void rmul_column(final Matrix L, final DataBlock r) {
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

    public void rmul_row(final Matrix L, final DataBlock r) {
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

    public void lmul_column(final Matrix L, final DataBlock r) {
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

    public void lmul_row(final Matrix L, final DataBlock r) {
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

    @Override
    public void lmul(Matrix L, DataBlock x) {
        if (L.getRowIncrement() == 1) {
            lmul_column(L, x);
        } else {
            lmul_row(L, x);
        }
    }

}
