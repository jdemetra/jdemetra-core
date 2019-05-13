/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demetra.maths.matrices;

import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
class LowerTriangularCanonicalMatrix {

    void lsolve(final CanonicalMatrix L, final FastMatrix B, double zero) throws MatrixException {
        DataBlockIterator rows = B.rowsIterator();
        while (rows.hasNext()) {
            lsolve(L, rows.next(), zero);
        }
    }

    void lsolve(final CanonicalMatrix L, final FastMatrix B) throws MatrixException {
        lsolve(L, B, 0);
    }

    void rsolve(final CanonicalMatrix L, final FastMatrix B, final double zero) throws MatrixException {
        DataBlockIterator columns = B.columnsIterator();
        while (columns.hasNext()) {
            rsolve(L, columns.next(), zero);
        }
    }

    void rsolve(final CanonicalMatrix L, final FastMatrix B) throws MatrixException {
        rsolve(L, B, 0);
    }

    void rmul(final CanonicalMatrix L, final FastMatrix B) {
        DataBlockIterator columns = B.columnsIterator();
        while (columns.hasNext()) {
            rmul(L, columns.next());
        }
    }

    void rsolve(CanonicalMatrix L, final DataBlock b, double zero) throws MatrixException {
        double[] data = L.getStorage();
        double[] x = b.getStorage();
        int xbeg = b.getStartPosition();
        int xinc = b.getIncrement();
        int xend = b.getEndPosition();
        int nr = L.getRowsCount();
        int dinc = nr + 1;

        for (int i = 0, xi = xbeg; xi != xend; i += dinc, xi += xinc) {
            double t = x[xi];
            if (Math.abs(t) > zero) {
                double d = data[i];
                if (d == 0) {
                    for (int xj = xi + xinc, j = i + 1; xj < xend; ++xj, ++j) {
                        if (Math.abs(data[j]) > zero) {
                            throw new MatrixException(MatrixException.SINGULAR);
                        }
                    }
                    x[xi] = 0;
                } else {
                    double c = t / d;
                    x[xi] = c;
                    for (int xj = xi + xinc, j = i + 1; xj < xend; ++xj, ++j) {
                        x[xj] -= c * data[j];
                    }
                }
            } else {
                x[xi] = 0;
            }
        }
    }

    void rsolve(CanonicalMatrix L, final DataBlock b) throws MatrixException {
        rsolve(L, b, 0);
    }

    void lsolve(CanonicalMatrix L, DataBlock b, double zero) throws MatrixException {
        double[] data = L.getStorage();
        int nr = L.getRowsCount();
        double[] x = b.getStorage();
        int xinc = b.getIncrement();
        int xbeg = b.getStartPosition();
        int xend = b.getEndPosition();

        for (int i = L.getLastPosition(), xi = xend; xi > xbeg; i -= 1 + nr) {
            int xd = xi - 1;
            double t = x[xd];
            for (int xj = xi, idx = i + 1; xj != xend; xj += xinc, ++idx) {
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

    void lsolve(CanonicalMatrix M, DataBlock x) throws MatrixException {
        lsolve(M, x, 0);
    }

    void rmul(CanonicalMatrix L, DataBlock r) {
        double[] data = L.getStorage();
        int nr = L.getRowsCount();

        double[] x = r.getStorage();
        int xinc = r.getIncrement();
        int xend = r.getEndPosition();

        for (int li = L.getLastPosition(), xi = xend - xinc; li >= 0; li -= nr + 1, xi -= xinc) {
            double z = x[xi];
            if (z != 0) {
                x[xi] = data[li] * z;
                for (int xj = xi + xinc, idx = li + 1; xj != xend; xj += xinc, ++idx) {
                    x[xj] += data[idx] * z;
                }
            }
        }
    }

    void toLower(CanonicalMatrix S) {
        int m = S.getRowsCount(), n = S.getColumnsCount();
        if (n == 1) {
            return;
        }
        double[] x = S.getStorage();
        int n1 = m + 1;
        int idmax = Math.min(m, n) * n1;
        int max = x.length;
        for (int id = 0; id < idmax; id += n1) {
            for (int iu = id + m; iu < max; iu += m) {
                x[iu] = 0;
            }
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
    CanonicalMatrix inverse(final CanonicalMatrix L) throws MatrixException {
        int n = L.getRowsCount();
        CanonicalMatrix IL = CanonicalMatrix.identity(n);
        rsolve(L, IL);
        return IL;
    }

}
