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
package jdplus.maths.matrices;

import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import demetra.data.DoubleSeqCursor;
import demetra.data.LogSign;
import jdplus.maths.matrices.decomposition.CroutDoolittle;
import jdplus.random.MersenneTwister;
import demetra.random.RandomNumberGenerator;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
class SymmetricCanonicalMatrix {

    void randomize(CanonicalMatrix S, RandomNumberGenerator rng) {
        if (!S.isSquare()) {
            throw new MatrixException(MatrixException.SQUARE);
        }
        int n = S.getRowsCount();
        if (rng == null) {
            rng = MersenneTwister.fromSystemNanoTime();
        }
        double[] x = S.getStorage();
        int n1 = n + 1;
        int max = x.length;
        for (int id = 0; id < max; id += n1) {
            x[id] = rng.nextDouble();
            for (int il = id + 1, iu = id + n; iu < max; il++, iu += n) {
                double q = rng.nextDouble();
                x[iu] = q;
                x[il] = q;
            }
        }
    }

    void lcholesky(CanonicalMatrix M, double zero) {
        double[] data = M.getStorage();
        int n = M.getRowsCount(), dinc = 1 + n;
        int end = n * dinc;
        for (int idiag = 0, irow = 0, cend = n; idiag != end; ++irow, idiag += dinc, cend += n) {
            // compute aii;
            double aii = data[idiag];
            for (int j = irow; j != idiag; j += n) {
                double x = data[j];
                aii -= x * x;
            }
            if (aii < -zero) { // negative
                throw new MatrixException(MatrixException.CHOLESKY);
            } else if (aii <= zero) { // quasi-zero
                data[idiag] = 0;
                // compute elements i+1 : n of column i
                for (int jx = irow; jx != idiag; jx += n) {
                    double temp = data[jx];
                    if (temp != 0) {
                        for (int ia = jx + 1, iy = idiag + 1; iy < cend; ++ia, ++iy) {
                            data[iy] -= temp * data[ia];
                        }
                    }
                }
                for (int iy = idiag + 1; iy < cend; ++iy) {
                    if (Math.abs(data[iy]) > zero) {
                        throw new MatrixException(MatrixException.CHOLESKY);
                    } else {
                        data[iy] = 0;
                    }
                }
            } else {
                aii = Math.sqrt(aii);
                data[idiag] = aii;
                // compute elements i+1 : n of column i
                for (int jx = irow; jx != idiag; jx += n) {
                    double temp = data[jx];
                    if (temp != 0) {
                        for (int ia = jx + 1, iy = idiag + 1; iy < cend; ++ia, ++iy) {
                            data[iy] -= temp * data[ia];
                        }
                    }
                }
                for (int iy = idiag + 1; iy < cend; ++iy) {
                    data[iy] /= aii;
                }
            }
        }
        LowerTriangularCanonicalMatrix.toLower(M);
    }

    void lcholesky(final CanonicalMatrix M) {
        lcholesky(M, 0);
    }

    void lcholesky2(CanonicalMatrix M, double zero) {
        // gaxpy implementation
        double[] x = M.getStorage();
        int n = M.getRowsCount(), dinc = 1 + n;
        for (int j = 0, jdiag = 0, jend = n; j < n; jdiag += dinc, jend += n, ++j) {
            // compute elements j : n of column j
            for (int i = j; i < jdiag; i += n) {
                double temp = x[i];
                if (temp != 0) {
                    for (int icur = i, jcur = jdiag; jcur < jend; ++icur, ++jcur) {
                        x[jcur] -= temp * x[icur];
                    }
                }
            }
            double ajj = x[jdiag];
            if (ajj < -zero) {
                throw new MatrixException(MatrixException.CHOLESKY);
            } else if (ajj <= zero) { // quasi-zero
                for (int jcur = jdiag + 1; jcur < jend; ++jcur) {
                    if (Math.abs(x[jcur]) > zero) {
                        throw new MatrixException(MatrixException.LDL);
                    } else {
                        x[jcur] = 0;
                    }
                }
                x[jdiag] = 0;
            } else {    // normal case
                ajj = Math.sqrt(ajj);
                x[jdiag] = ajj;
                for (int jcur = jdiag + 1; jcur < jend; ++jcur) {
                    x[jcur] /= ajj;
                }
            }
        }
        LowerTriangularCanonicalMatrix.toLower(M);
    }

    void lcholesky2(final CanonicalMatrix M) {
        lcholesky2(M, 0);
    }

    void LLt(CanonicalMatrix L, CanonicalMatrix M) {
        int n = L.getRowsCount(), d = n + 1;
        double[] pl = L.getStorage(), pm = M.getStorage();
        for (int i = 0, ix = 0, im = 0; i < n; ++i, ++ix, im += d) {
            for (int j = i, kx = ix, km = im, ks = im; j < n; ++j, ++kx, ++km, ks += n) {
                double z = 0;
                int max = im + n;
                for (int jx = ix, lx = kx; jx != max; jx += n, lx += n) {
                    z += pl[jx] * pl[lx];
                }
                pm[km] = z;
                if (ks != km) {
                    pm[ks] = z;
                }
            }
        }
    }

    void UUt(CanonicalMatrix U, CanonicalMatrix M) {
        int n = U.getRowsCount(), d = n + 1;
        double[] pl = U.getStorage(), pm = M.getStorage();
        for (int i = 0, ix = 0, imax = ix + n * n, im = 0; i < n; ++i, ix += d, im += d, ++imax) {
            // ix = position of the first item of row i, imax = end of row i
            for (int j = i, kx = ix, ixc = ix, km = im, ks = im; j < n; ++j, kx += d, ++km, ks += n, ixc += n) {
                // kx = position of the first item of column k ixc first used item of row i
                double z = 0;
                for (int jx = ixc, lx = kx; jx != imax; jx += n, lx += n) {
                    z += pl[jx] * pl[lx];
                }
                pm[km] = z;
                if (ks != km) {
                    pm[ks] = z;
                }
            }
        }
    }

    void LtL(CanonicalMatrix L, CanonicalMatrix M) {
        int n = L.getRowsCount();
        double[] pl = L.getStorage(), pm = M.getStorage();
        for (int r = 0, mpos = 0, x0 = 0, x1 = n; r < n; ++r, x0 += n, x1 += n) {
            mpos += r;
            for (int c = r, xpos = x0; c < n; xpos += n, ++c) {
                double s = 0;
                for (int xcur = x0 + c, ycur = xpos + c; xcur < x1; ++xcur, ++ycur) {
                    s += pl[xcur] * pl[ycur];
                }
                pm[mpos++] = s;
            }
        }
        fromLower(M);
    }

    void UtU(CanonicalMatrix U, CanonicalMatrix M) {
        int n = U.getRowsCount();
        double[] pu = U.getStorage(), pm = M.getStorage();
        for (int r = 0, mpos = 0, x0 = 0; r < n; ++r, x0 += n) {
            mpos += r;
            for (int c = r, xpos = x0, x1 = x0 + r; c < n; xpos += n, ++c, ++x1) {
                double s = 0;
                for (int xcur = x0, ycur = xpos; xcur <= x1; ++xcur, ++ycur) {
                    s += pu[xcur] * pu[ycur];
                }
                pm[mpos++] = s;
            }
        }
        fromLower(M);

    }

    CanonicalMatrix inverse(CanonicalMatrix S) {
        try {
            CanonicalMatrix lower = S.deepClone();
            lcholesky(lower);
            lower = LowerTriangularCanonicalMatrix.inverse(lower);
            return LtL(lower);
        } catch (MatrixException e) {
            CroutDoolittle cr = new CroutDoolittle();
            cr.decompose(S);
            CanonicalMatrix I = CanonicalMatrix.identity(S.getRowsCount());
            cr.solve(I);
            return I;
        }
    }

    CanonicalMatrix XXt(final CanonicalMatrix X) {
        int nr = X.getRowsCount();
        CanonicalMatrix S = CanonicalMatrix.square(nr);
        double[] sx = S.getStorage();
        double[] px = X.getStorage();
        int xmax = px.length;
        // Raw gaxpy implementation
        for (int x0 = 0; x0 < xmax;) {
            int x1 = x0 + nr;
            for (int pos = 0, ypos = x0, c = 0; c < nr; ++ypos, ++x0, ++c) {
                double yc = px[ypos];
                if (yc != 0) {
                    pos += c;
                    for (int xpos = x0; xpos < x1; ++pos, ++xpos) {
                        sx[pos] += yc * px[xpos];
                    }
                } else {
                    pos += nr;
                }
            }
        }
        fromLower(S);
        return S;
    }

    CanonicalMatrix XtX(final CanonicalMatrix X) {
        int nr = X.getRowsCount(), nc = X.getColumnsCount();
        CanonicalMatrix M = CanonicalMatrix.square(nc);
        double[] px = X.getStorage(), pm = M.getStorage();
        int xmax = px.length;
        for (int c = 0, mpos = 0, x0 = 0, x1 = nr; c < nc; ++c, x0 += nr, x1 += nr) {
            mpos += c;
            for (int xpos = x0; xpos < xmax; xpos += nr) {
                double s = 0;
                for (int xcur = x0, ycur = xpos; xcur < x1; ++xcur, ++ycur) {
                    s += px[xcur] * px[ycur];
                }
                pm[mpos++] = s;
            }
        }
        fromLower(M);
        return M;
    }

    CanonicalMatrix UUt(final CanonicalMatrix U) {
        CanonicalMatrix M = CanonicalMatrix.square(U.getColumnsCount());
        UUt(U, M);
        return M;
    }

    CanonicalMatrix LLt(final CanonicalMatrix L) {
        CanonicalMatrix M = CanonicalMatrix.square(L.getRowsCount());
        LLt(L, M);
        return M;
    }

    CanonicalMatrix UtU(final CanonicalMatrix U) {
        CanonicalMatrix M = CanonicalMatrix.square(U.getColumnsCount());
        UtU(U, M);
        return M;
    }

    CanonicalMatrix LtL(final CanonicalMatrix L) {
        CanonicalMatrix M = CanonicalMatrix.square(L.getColumnsCount());
        LtL(L, M);
        return M;
    }

    /**
     * Returns XSX'
     *
     * @param X
     * @param S
     * @return
     */
    CanonicalMatrix XSXt(final CanonicalMatrix S, final CanonicalMatrix X) {
        CanonicalMatrix XSX = CanonicalMatrix.square(X.getRowsCount());
        XSXt(S, X, XSX);
        return XSX;
    }

    void XSXt(final CanonicalMatrix S, final CanonicalMatrix X, final CanonicalMatrix M) {
        CanonicalMatrix XS = X.times(S);
        DataBlockIterator xsrows = XS.rowsIterator(), xtcols = X.rowsIterator(), mcols = M.columnsIterator();
        int c = 0;
        while (xtcols.hasNext()) {
            DataBlock mcol = mcols.next();
            DataBlock col = xtcols.next();
            DoubleSeqCursor.OnMutable mcursor = mcol.cursor();
            mcursor.moveTo(c);
            xsrows.reset(c++);
            while (xsrows.hasNext()) {
                mcursor.setAndNext(xsrows.next().dot(col));
            }
        }
        fromLower(M);
    }

    /**
     * Returns X'SX
     *
     * @param X
     * @param S
     * @return
     */
    CanonicalMatrix XtSX(final CanonicalMatrix S, final CanonicalMatrix X) {
        int n = X.getColumnsCount();
        CanonicalMatrix M = CanonicalMatrix.square(n);
        XtSX(S, X, M);
        return M;
    }

    void XtSX(CanonicalMatrix S, CanonicalMatrix X, CanonicalMatrix M) {
        CanonicalMatrix SX = S.times(X);
        DataBlockIterator sxcols = SX.columnsIterator(), xtrows = X.columnsIterator(), mcols = M.columnsIterator();
        int c = 0;
        while (sxcols.hasNext()) {
            DataBlock mcol = mcols.next();
            DataBlock col = sxcols.next();
            DoubleSeqCursor.OnMutable mcursor = mcol.cursor();
            mcursor.moveTo(c);
            xtrows.reset(c++);
            while (xtrows.hasNext()) {
                mcursor.setAndNext(xtrows.next().dot(col));
            }
        }
        fromLower(M);
    }

    void reenforceSymmetry(CanonicalMatrix S) {
        if (!S.isSquare()) {
            throw new MatrixException(MatrixException.SQUARE);
        }
        int n = S.getRowsCount();
        if (n == 1) {
            return;
        }
        double[] x = S.getStorage();
        int n1 = n + 1;
        int max = x.length;
        for (int id = 0; id < max; id += n1) {
            for (int il = id + 1, iu = id + n; iu < max; il++, iu += n) {
                double q = (x[iu] + x[il]) / 2;
                x[iu] = q;
                x[il] = q;
            }
        }
    }

    LogSign logDeterminant(CanonicalMatrix S) {
        CanonicalMatrix s = S.deepClone();
        try {
            lcholesky(s);
            DataBlock diagonal = s.diagonal();
            LogSign ls = LogSign.of(diagonal);
            return new LogSign(ls.getValue() * 2, true);
        } catch (MatrixException e) {
            return FastMatrix.logDeterminant(S);
        }
    }

    double determinant(CanonicalMatrix L) {
        LogSign ls = logDeterminant(L);
        if (ls == null) {
            return 0;
        }
        double val = Math.exp(ls.getValue());
        return ls.isPositive() ? val : -val;
    }

    void fromLower(CanonicalMatrix S) {
        if (!S.isSquare()) {
            throw new MatrixException(MatrixException.SQUARE);
        }
        int n = S.getRowsCount();
        if (n == 1) {
            return;
        }
        double[] x = S.getStorage();
        int n1 = n + 1;
        int max = x.length;
        for (int id = 0; id < max; id += n1) {
            for (int il = id + 1, iu = id + n; iu < max; il++, iu += n) {
                x[iu] = x[il];
            }
        }
    }

    void fromUpper(CanonicalMatrix S) {
        if (!S.isSquare()) {
            throw new MatrixException(MatrixException.SQUARE);
        }
        int n = S.getRowsCount();
        if (n == 1) {
            return;
        }
        double[] x = S.getStorage();
        int n1 = n + 1;
        int max = x.length;
        for (int id = 0; id < max; id += n1) {
            for (int il = id + 1, iu = id + n; iu < max; il++, iu += n) {
                x[il] = x[iu];
            }
        }
    }

    ////////////////////////////////////////
    // Auxiliary routines
    /**
     * S=S+X*X'. Only the lower part is upgraded
     *
     * @param S
     * @param x
     */
    private void laddXXt(final CanonicalMatrix S, final DataBlock x) {
        double[] sx = S.getStorage();
        double[] px = x.getStorage();
        int xinc = x.getIncrement();
        int x0 = x.getStartPosition(), x1 = x.getEndPosition();
        int n = S.getRowsCount();

        // Raw gaxpy implementation
        for (int pos = 0, ypos = x0, c = 0; c < n; ypos += xinc, x0 += xinc, ++c) {
            double yc = px[ypos];
            if (yc != 0) {
                pos += c;
                if (xinc == 1) {
                    for (int xpos = x0; xpos < x1; ++pos, ++xpos) {
                        sx[pos] += yc * px[xpos];
                    }
                } else {
                    for (int xpos = x0; xpos != x1; ++pos, xpos += xinc) {
                        sx[pos] += yc * px[xpos];
                    }
                }
            } else {
                pos += n;
            }
        }
    }

    void addXXt(final CanonicalMatrix S, final DataBlock x) {
        laddXXt(S, x);
        fromLower(S);
    }

}
