/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.matrices;

import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.data.LogSign;
import jdplus.maths.matrices.decomposition.CroutDoolittle;
import jdplus.random.RandomNumberGenerator;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class SymmetricMatrix {

    public void randomize(FastMatrix M, RandomNumberGenerator rng) {
        if (!M.isSquare()) {
            throw new MatrixException(MatrixException.SQUARE);
        }
        int n = M.getRowsCount();
        if (n == 1) {
            return;
        }
        int rinc = M.getRowIncrement(), cinc = M.getColumnIncrement(), start = M.getStartPosition();
        double[] x = M.getStorage();
        for (int c = 0, id = start; c < n; ++c, id += rinc + cinc) {
            x[id] = rng.nextDouble();
            for (int r = c + 1, il = id, iu = id; r < n; ++r) {
                il += rinc;
                iu += cinc;
                double q = rng.nextDouble();
                x[iu] = q;
                x[il] = q;
            }
        }
    }

    public void lcholesky(FastMatrix M, double zero) {
        if (M.isCanonical()) {
            SymmetricCanonicalMatrix.lcholesky2(M.asCanonical(), zero);
        } else if (M.getRowIncrement() == 1) {
            lcholesky_1(M, zero);
        } else {
            lcholesky_def(M, zero);
        }
    }

    private void lcholesky_1(FastMatrix M, double zero) {
        double[] data = M.getStorage();
        int n = M.getRowsCount(), cinc = M.getColumnIncrement(), dinc = 1 + cinc;
        int start = M.getStartPosition(), end = start + n * dinc;
        for (int idiag = start, irow = start, cend = start + n; idiag != end; ++irow, idiag += dinc, cend += cinc) {
            // compute aii;
            double aii = data[idiag];
            for (int j = irow; j != idiag; j += cinc) {
                double x = data[j];
                aii -= x * x;
            }
            if (aii < -zero) { // negative
                throw new MatrixException(MatrixException.CHOLESKY);
            } else if (aii <= zero) { // quasi-zero
                data[idiag] = 0;
                // compute elements i+1 : n of column i
                for (int jx = irow; jx != idiag; jx += cinc) {
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
                for (int jx = irow; jx != idiag; jx += cinc) {
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
        LowerTriangularMatrix.toLower(M);
    }

    private void lcholesky_def(FastMatrix M, double zero) {
        double[] data = M.getStorage();
        int n = M.getRowsCount(), rinc = M.getRowIncrement(), cinc = M.getColumnIncrement(), dinc = rinc + cinc;
        int start = M.getStartPosition(), end = start + n * dinc;
        for (int idiag = start, irow = start, cend = start + n * rinc; idiag != end; irow += rinc, idiag += dinc, cend += cinc) {
            // compute aii;
            double aii = data[idiag];
            for (int j = irow; j != idiag; j += cinc) {
                double x = data[j];
                aii -= x * x;
            }
            if (aii < -zero) { // negative
                throw new MatrixException(MatrixException.CHOLESKY);
            } else if (aii <= zero) { // quasi-zero
                data[idiag] = 0;
                // compute elements i+1 : n of column i
                for (int jx = irow; jx != idiag; jx += cinc) {
                    double temp = data[jx];
                    if (temp != 0) {
                        for (int ia = jx + rinc, iy = idiag + rinc; iy != cend; ia += rinc, iy += rinc) {
                            data[iy] -= temp * data[ia];
                        }
                    }
                }
                for (int iy = idiag + rinc; iy != cend; iy += rinc) {
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
                for (int jx = irow; jx != idiag; jx += cinc) {
                    double temp = data[jx];
                    if (temp != 0) {
                        for (int ia = jx + rinc, iy = idiag + rinc; iy != cend; ia += rinc, iy += rinc) {
                            data[iy] -= temp * data[ia];
                        }
                    }
                }
                for (int iy = idiag + rinc; iy != cend; iy += rinc) {
                    data[iy] /= aii;
                }
            }
        }
        LowerTriangularMatrix.toLower(M);
    }

    public void xxt(DataBlock x, FastMatrix M) {
        int nr = x.length(), xinc = x.getIncrement();
        int mcinc = M.getColumnIncrement(), mrinc = M.getRowIncrement();
        double[] px = x.getStorage(), pm = M.getStorage();
        if (xinc == 1) {
            for (int i = 0, ix = x.getStartPosition(), im = M.getStartPosition(); i < nr; ++i, ++ix, im += mrinc + mcinc) {
                for (int j = i, kx = ix, km = im, ks = im; j < nr; ++j, ++kx, km += mrinc, ks += mcinc) {
                    double z = px[ix] * px[kx];
                    pm[km] = z;
                    if (ks != km) {
                        pm[ks] = z;
                    }
                }
            }
        } else {
            for (int i = 0, ix = x.getStartPosition(), im = M.getStartPosition(); i < nr; ++i, ix += xinc, im += mrinc + mcinc) {
                for (int j = i, kx = ix, km = im, ks = im; j < nr; ++j, kx += xinc, km += mrinc, ks += mcinc) {
                    double z = px[ix] * px[kx];
                    pm[km] = z;
                    if (ks != km) {
                        pm[ks] = z;
                    }
                }
            }
        }
    }

    public void XXt(final FastMatrix X, final FastMatrix M) {
        int nr = X.getRowsCount(), nc = X.getColumnsCount(), xcinc = X.getColumnIncrement(), xrinc = X.getRowIncrement();
        int mcinc = M.getColumnIncrement(), mrinc = M.getRowIncrement();
        double[] px = X.getStorage(), pm = M.getStorage();
        if (xcinc != 1) {
            for (int i = 0, ix = X.getStartPosition(), im = M.getStartPosition(); i < nr; ++i, ix += xrinc, im += mrinc + mcinc) {
                for (int j = i, kx = ix, km = im, ks = im; j < nr; ++j, kx += xrinc, km += mrinc, ks += mcinc) {
                    double z = 0;
                    for (int c = 0, jx = ix, lx = kx; c < nc; ++c, jx += xcinc, lx += xcinc) {
                        z += px[jx] * px[lx];
                    }
                    pm[km] = z;
                    if (ks != km) {
                        pm[ks] = z;
                    }
                }
            }
        } else {
            for (int i = 0, ix = X.getStartPosition(), im = M.getStartPosition(); i < nr; ++i, ix += xrinc, im += mrinc + mcinc) {
                for (int j = i, kx = ix, km = im, ks = im; j < nr; ++j, kx += xrinc, km += mrinc, ks += mcinc) {
                    double z = 0;
                    for (int c = 0, jx = ix, lx = kx; c < nc; ++c, ++jx, ++lx) {
                        z += px[jx] * px[lx];
                    }
                    pm[km] = z;
                    if (ks != km) {
                        pm[ks] = z;
                    }
                }
            }
        }
    }

    public void LLt(FastMatrix L, FastMatrix M) {
        if (L.isCanonical() && M.isCanonical()) {
            SymmetricCanonicalMatrix.LLt(L.asCanonical(), M.asCanonical());
        } else {
            int nr = L.getRowsCount(), nc = L.getColumnsCount(), lcinc = L.getColumnIncrement(), lrinc = L.getRowIncrement();
            int mcinc = M.getColumnIncrement(), mrinc = M.getRowIncrement();
            double[] pl = L.getStorage(), pm = M.getStorage();
            if (lcinc == 1) {
                for (int i = 0, ix = L.getStartPosition(), im = M.getStartPosition(); i < nr; ++i, ix += lrinc, im += mrinc + mcinc) {
                    for (int j = i, kx = ix, km = im, ks = im; j < nr; ++j, kx += lrinc, km += mrinc, ks += mcinc) {
                        double z = 0;
                        for (int jx = ix, lx = kx; jx <= im; ++jx, ++lx) {
                            z += pl[jx] * pl[lx];
                        }
                        pm[km] = z;
                        if (ks != km) {
                            pm[ks] = z;
                        }
                    }
                }
            } else {
                for (int i = 0, ix = L.getStartPosition(), im = M.getStartPosition(); i < nr; ++i, ix += lrinc, im += mrinc + mcinc) {
                    for (int j = i, kx = ix, km = im, ks = im; j < nr; ++j, kx += lrinc, km += mrinc, ks += mcinc) {
                        double z = 0;
                        int max = im + lcinc;
                        for (int jx = ix, lx = kx; jx != max; jx += lcinc, lx += lcinc) {
                            z += pl[jx] * pl[lx];
                        }
                        pm[km] = z;
                        if (ks != km) {
                            pm[ks] = z;
                        }
                    }
                }
            }
        }
    }

    public void UUt(FastMatrix U, FastMatrix M) {
        if (U.isCanonical() && M.isCanonical()) {
            SymmetricCanonicalMatrix.UUt(U.asCanonical(), M.asCanonical());
        } else {
            int nr = U.getRowsCount(), nc = U.getColumnsCount(), lcinc = U.getColumnIncrement(), lrinc = U.getRowIncrement();
            int mcinc = M.getColumnIncrement(), mrinc = M.getRowIncrement();
            double[] pl = U.getStorage(), pm = M.getStorage();
            for (int i = 0, ix = U.getStartPosition(), imax = ix + nc * lcinc, im = M.getStartPosition(); i < nr; ++i, ix += lrinc + lcinc, im += mrinc + mcinc, imax += lrinc) {
                // ix = position of the first item of row i, imax = end of row i
                for (int j = i, kx = ix, ixc = ix, km = im, ks = im; j < nr; ++j, kx += lrinc + lcinc, km += mrinc, ks += mcinc, ixc += lcinc) {
                    // kx = position of the first item of column k ixc first used item of row i
                    double z = 0;
                    for (int jx = ixc, lx = kx; jx != imax; jx += lcinc, lx += lcinc) {
                        z += pl[jx] * pl[lx];
                    }
                    pm[km] = z;
                    if (ks != km) {
                        pm[ks] = z;
                    }
                }
            }
        }
    }

    public void XtSX(FastMatrix S, FastMatrix X, FastMatrix M) {
        if (S.isCanonical() && X.isCanonical() && M.isCanonical()) {
            SymmetricCanonicalMatrix.XtSX(S.asCanonical(), X.asCanonical(), M.asCanonical());
        } else {
            FastMatrix SX = S.times(X);
            DataBlockIterator rows = SX.columnsIterator(), cols = X.columnsIterator(), mcols = M.columnsIterator();
            int c = 0;
            while (cols.hasNext()) {
                int idx = c;
                rows.reset(c++);
                DataBlock mcol = mcols.next();
                DataBlock col = cols.next();
                while (rows.hasNext()) {
                    mcol.set(idx++, rows.next().dot(col));
                }
            }
            SymmetricMatrix.fromLower(M);
        }
    }

    public Matrix xxt(final DataBlock x) {
        Matrix M = Matrix.square(x.length());
        xxt(x, M);
        return M;
    }

    public void XtX(final FastMatrix X, final FastMatrix M) {
        XXt(X.transpose(), M);
    }

    public void UtU(final FastMatrix U, final FastMatrix M) {
        LLt(U.transpose(), M);
    }

    public void LtL(final FastMatrix L, final FastMatrix M) {
        UUt(L.transpose(), M);
    }

    public Matrix inverse(FastMatrix S) {
        try {
            Matrix lower = S.deepClone();
            lcholesky(lower);
            lower = LowerTriangularCanonicalMatrix.inverse(lower);
            return LtL(lower);
        } catch (MatrixException e) {
            CroutDoolittle cr = new CroutDoolittle();
            cr.decompose(S);
            Matrix I = Matrix.identity(S.getRowsCount());
            cr.solve(I);
            return I;
        }
    }

    public Matrix XXt(final FastMatrix X) {
        if (X.isCanonical()) {
            return SymmetricCanonicalMatrix.XXt(X.asCanonical());
        }
        Matrix M = Matrix.square(X.getRowsCount());
        XXt(X, M);
        return M;
    }

    public Matrix XtX(final FastMatrix X) {
        if (X.isCanonical()) {
            return SymmetricCanonicalMatrix.XtX(X.asCanonical());
        }
        Matrix M = Matrix.square(X.getColumnsCount());
        XXt(X.transpose(), M);
        return M;
    }

    public Matrix LLt(final FastMatrix L) {
        if (L.isCanonical()) {
            return SymmetricCanonicalMatrix.LLt(L.asCanonical());
        }

        Matrix M = Matrix.square(L.getRowsCount());
        LLt(L, M);
        return M;
    }

    public Matrix UtU(final FastMatrix U) {
        if (U.isCanonical()) {
            return SymmetricCanonicalMatrix.UtU(U.asCanonical());
        }
        Matrix M = Matrix.square(U.getColumnsCount());
        LLt(U.transpose(), M);
        return M;
    }

    public Matrix LtL(final FastMatrix L) {
        if (L.isCanonical()) {
            return SymmetricCanonicalMatrix.LtL(L.asCanonical());
        }
        Matrix M = Matrix.square(L.getRowsCount());
        UUt(L.transpose(), M);
        return M;
    }

    public Matrix UUt(final FastMatrix U) {
        if (U.isCanonical()) {
            return SymmetricCanonicalMatrix.UUt(U.asCanonical());
        }
        Matrix M = Matrix.square(U.getColumnsCount());
        UUt(U, M);
        return M;
    }

    /**
     * Returns XSX'
     *
     * @param X
     * @param S
     * @return
     */
    public Matrix XSXt(final FastMatrix S, final FastMatrix X) {
        if (S.isCanonical() && X.isCanonical()) {
            return SymmetricCanonicalMatrix.XSXt(S.asCanonical(), X.asCanonical());
        }
        return XtSX(S, X.transpose());
    }

    /**
     * M = X'SX
     *
     * @param X
     * @param S
     * @param M
     */
    public void XSXt(final FastMatrix S, final FastMatrix X, final FastMatrix M) {
        if (S.isCanonical() && X.isCanonical() && M.isCanonical()) {
            SymmetricCanonicalMatrix.XSXt(S.asCanonical(), X.asCanonical(), M.asCanonical());
        } else {
            XtSX(S, X.transpose(), M);
        }
    }

    /**
     * Returns X'SX
     *
     * @param X
     * @param S
     * @return
     */
    public Matrix XtSX(final FastMatrix S, final FastMatrix X) {
        if (S.isCanonical() && X.isCanonical()) {
            return SymmetricCanonicalMatrix.XtSX(S.asCanonical(), X.asCanonical());
        }
        int n = X.getColumnsCount();
        Matrix M = Matrix.square(n);
        XtSX(S, X, M);
        return M;
    }

    public void lcholesky(final FastMatrix M) {
        lcholesky(M, 0);
    }

    public void reenforceSymmetry(FastMatrix S) {
        if (!S.isSquare()) {
            throw new MatrixException(MatrixException.SQUARE);
        }
        if (S.isCanonical()) {
            SymmetricCanonicalMatrix.reenforceSymmetry(S.asCanonical());
        } else {
            int n = S.getRowsCount();
            if (n == 1) {
                return;
            }
            int rinc = S.getRowIncrement(), cinc = S.getColumnIncrement(), start = S.getStartPosition();
            double[] x = S.getStorage();
            for (int c = 0, id = start; c < n; ++c, id += rinc + cinc) {
                for (int r = c + 1, il = id, iu = id; r < n; ++r) {
                    il += rinc;
                    iu += cinc;
                    double q = (x[iu] + x[il]) / 2;
                    x[iu] = q;
                    x[il] = q;
                }
            }
        }
    }

    public void fromLower(FastMatrix S) {
        if (!S.isSquare()) {
            throw new MatrixException(MatrixException.SQUARE);
        }
        if (S.isCanonical()) {
            SymmetricCanonicalMatrix.fromLower(S.asCanonical());
        } else {
            int n = S.getRowsCount();
            if (n == 1) {
                return;
            }
            int rinc = S.getRowIncrement(), cinc = S.getColumnIncrement(), start = S.getStartPosition();
            double[] x = S.getStorage();
            for (int c = 0, id = start; c < n; ++c, id += rinc + cinc) {
                for (int r = c + 1, il = id, iu = id; r < n; ++r) {
                    il += rinc;
                    iu += cinc;
                    x[iu] = x[il];
                }
            }
        }
    }

    public void fromUpper(FastMatrix S) {
        if (!S.isSquare()) {
            throw new MatrixException(MatrixException.SQUARE);
        }
        if (S.isCanonical()) {
            SymmetricCanonicalMatrix.fromUpper(S.asCanonical());
        } else {
            int n = S.getRowsCount();
            if (n == 1) {
                return;
            }
            int rinc = S.getRowIncrement(), cinc = S.getColumnIncrement(), start = S.getStartPosition();
            double[] x = S.getStorage();
            for (int c = 0, id = start; c < n; ++c, id += rinc + cinc) {
                for (int r = c + 1, il = id, iu = id; r < n; ++r) {
                    il += rinc;
                    iu += cinc;
                    x[il] = x[iu];
                }
            }
        }
    }

    public LogSign logDeterminant(FastMatrix S) {
        FastMatrix s = S.deepClone();
        try {
            lcholesky(s);
            DataBlock diagonal = s.diagonal();
            LogSign ls = LogSign.of(diagonal);
            return new LogSign(ls.getValue() * 2, true);
        } catch (MatrixException e) {
            return FastMatrix.logDeterminant(S);
        }
    }

    public double determinant(FastMatrix L) {
        LogSign ls = logDeterminant(L);
        if (ls == null) {
            return 0;
        }
        double val = Math.exp(ls.getValue());
        return ls.isPositive() ? val : -val;
    }

}
