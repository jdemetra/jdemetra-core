/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices;

import demetra.data.DoubleSeqCursor;
import demetra.design.Algorithm;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.data.LogSign;
import jdplus.math.matrices.lapack.Cholesky;
import jdplus.math.matrices.lapack.SYRK;
import jdplus.math.matrices.decomposition.CroutDoolittle;
import jdplus.math.matrices.decomposition.LUDecomposition;
import jdplus.random.MersenneTwister;
import jdplus.random.RandomNumberGenerator;
import nbbrd.service.Mutability;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class SymmetricMatrix {

    public static void XtSX() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private final SymmetricMatrixLoader.CholeskyProcessor CHOLESKY = new SymmetricMatrixLoader.CholeskyProcessor();

    @Algorithm
    @ServiceDefinition(quantifier = Quantifier.SINGLE,
            mutability = Mutability.CONCURRENT,
            fallback = Cholesky.class)
    public static interface CholeskyProcessor {

        void lcholesky(Matrix L, double zero);

        void ucholesky(Matrix U, double zero);

        /**
         * Lower cholesky. The upper part of L is not referenced (neither used
         * nor modified)
         *
         * @param L in/out matrix
         */
        default void lcholesky(Matrix L) {
            lcholesky(L, 0);
        }

        default void ucholesky(Matrix U) {
            ucholesky(U, 0);
        }
    }

    /**
     * Fill a square matrix with random numbers ( in a symmetric way)
     *
     * @param S The square matrix. Symmetric on exit
     * @param rng The random number generator
     */
    public void randomize(Matrix S, RandomNumberGenerator rng) {
        if (!S.isSquare()) {
            throw new MatrixException(MatrixException.SQUARE);
        }
        int n = S.getRowsCount(), start = S.getStartPosition(), lda = S.getColumnIncrement();
        if (rng == null) {
            rng = MersenneTwister.fromSystemNanoTime();
        }
        double[] x = S.getStorage();
        int max = start + lda * n;
        for (int id = start; id < max; id += lda + 1) {
            x[id] = rng.nextDouble();
            for (int il = id + 1, iu = id + lda; iu < max; il++, iu += lda) {
                double q = rng.nextDouble();
                x[iu] = q;
                x[il] = q;
            }
        }
    }

    /**
     * Cholesky decomposition of a symmetric matrix. Only the lower part of the
     * matrix is used (and modified) The matrix can be singular
     *
     * @param L
     * @param zero
     * @exception MatrixException
     */
    public void lcholesky(Matrix L, double zero) {
        CHOLESKY.get().lcholesky(L, zero);
    }

    /**
     * Cholesky decomposition of a symmetric matrix. Only the lower part of the
     * matrix is used (and modified) An exception is thrown if the matrix is
     * singular
     *
     * @param L
     * @exception MatrixException
     */
    public void lcholesky(final Matrix L) {
        CHOLESKY.get().lcholesky(L, 0);
    }

    public void ucholesky(Matrix M, double zero) {
        CHOLESKY.get().ucholesky(M, zero);
    }

    public void ucholesky(final Matrix M) {
        CHOLESKY.get().ucholesky(M, 0);
    }

    public void LLt(Matrix L, Matrix M) {
        int nr = L.getRowsCount(), nc = L.getColumnsCount(), lcinc = L.getColumnIncrement();
        int mcinc = M.getColumnIncrement();
        double[] pl = L.getStorage(), pm = M.getStorage();
        for (int i = 0, ix = L.getStartPosition(), im = M.getStartPosition(); i < nr; ++i, ix += 1, im += 1 + mcinc) {
            for (int j = i, kx = ix, km = im, ks = im; j < nr; ++j, kx++, km++, ks += mcinc) {
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

    public void UUt(Matrix U, Matrix M) {
        int nr = U.getRowsCount(), nc = U.getColumnsCount(), lcinc = U.getColumnIncrement();
        int mcinc = M.getColumnIncrement();
        double[] pl = U.getStorage(), pm = M.getStorage();
        for (int i = 0, ix = U.getStartPosition(), imax = ix + nc * lcinc, im = M.getStartPosition(); i < nr; ++i, ix += 1 + lcinc, im += 1 + mcinc, imax++) {
            // ix = position of the first item of row i, imax = end of row i
            for (int j = i, kx = ix, ixc = ix, km = im, ks = im; j < nr; ++j, kx += 1 + lcinc, km++, ks += mcinc, ixc += lcinc) {
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

    public void LtL(Matrix L, Matrix M) {
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

    public void UtU(Matrix U, Matrix M) {
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

    public Matrix inverse(Matrix S) {
        try {
            Matrix lower = S.deepClone();
            lcholesky(lower);
            lower = LowerTriangularMatrix.inverse(lower);
            return LtL(lower);
        } catch (MatrixException e) {
            LUDecomposition lu = CroutDoolittle.decompose(S);
            Matrix I = Matrix.identity(S.getRowsCount());
            lu.solve(I);
            return I;
        }
    }
    
    /**
     * Solve Sx =y using Cholesky decomposition
     * @param S A symmetric matrix. On exit, it contains the cholesky factor
     * @param y On entry y; on exit x
     */
    public void solve(final Matrix S, DataBlock y){
        lcholesky(S);
        LowerTriangularMatrix.solveLx(S, y);
        LowerTriangularMatrix.solvexL(S, y);
    }

    public Matrix XXt(final Matrix X) {
        int nr = X.getRowsCount();
        Matrix S = Matrix.square(nr);
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

    public Matrix XtX(final Matrix X) {
        int nr = X.getRowsCount(), nc = X.getColumnsCount();
        Matrix M = Matrix.square(nc);
        double[] px = X.getStorage(), pm = M.getStorage();
        int xstart = X.getStartPosition(), xinc = X.getColumnIncrement();
        int xmax = xstart + xinc * nc;
        for (int c = 0, mpos = 0, x0 = xstart, x1 = xstart + nr; c < nc; ++c, x0 += xinc, x1 += xinc) {
            mpos += c;
            for (int xpos = x0; xpos < xmax; xpos += xinc) {
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

    public Matrix UUt(final Matrix U) {
        Matrix M = Matrix.square(U.getColumnsCount());
        UUt(U, M);
        return M;
    }

    public Matrix LLt(final Matrix L) {
        Matrix M = Matrix.square(L.getRowsCount());
        LLt(L, M);
        return M;
    }

    public Matrix UtU(final Matrix U) {
        Matrix M = Matrix.square(U.getColumnsCount());
        UtU(U, M);
        return M;
    }

    public Matrix LtL(final Matrix L) {
        Matrix M = Matrix.square(L.getColumnsCount());
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
    public Matrix XSXt(final Matrix S, final Matrix X) {
        Matrix XSX = Matrix.square(X.getRowsCount());
        XSXt(S, X, XSX);
        return XSX;
    }

    public void XSXt(final Matrix S, final Matrix X, final Matrix M) {
        Matrix XS = GeneralMatrix.AB(X, S);
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
    public Matrix XtSX(final Matrix S, final Matrix X) {
        int n = X.getColumnsCount();
        Matrix M = Matrix.square(n);
        XtSX(S, X, M);
        return M;
    }

    public void XtSX(Matrix S, Matrix X, Matrix M) {
        Matrix SX = GeneralMatrix.AB(S, X);
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

    public void reenforceSymmetry(Matrix S) {
        if (!S.isSquare()) {
            throw new MatrixException(MatrixException.SQUARE);
        }
        int n = S.getRowsCount(), lda=S.getColumnIncrement(), start=S.start;
        if (n == 1) {
            return;
        }
        double[] x = S.getStorage();
        int del = lda + 1;
        int max = start+lda*n;
        for (int id = start; id < max; id += del) {
            for (int il = id + 1, iu = id + lda; iu < max; il++, iu += lda) {
                double q=(x[iu]+ x[il])/2;
                x[il] = q;
                x[iu] = q;
            }
        }
    }

    public LogSign logDeterminant(Matrix S) {
        Matrix s = S.deepClone();
        lcholesky(s);
        DataBlock diagonal = s.diagonal();
        LogSign ls = LogSign.of(diagonal);
        return new LogSign(ls.getValue() * 2, true);
    }

    public double determinant(Matrix L) {
        LogSign ls = logDeterminant(L);
        if (ls == null) {
            return 0;
        }
        return Math.exp(ls.getValue());
    }

    public void fromLower(Matrix S) {
        if (!S.isSquare()) {
            throw new MatrixException(MatrixException.SQUARE);
        }
        int n = S.getRowsCount(), lda=S.getColumnIncrement(), start=S.start;
        if (n == 1) {
            return;
        }
        double[] x = S.getStorage();
        int del = lda + 1;
        int max = start+lda*n;
        for (int id = start; id < max; id += del) {
            for (int il = id + 1, iu = id + lda; iu < max; il++, iu += lda) {
                x[iu] = x[il];
            }
        }
    }

    public void fromUpper(Matrix S) {
        if (!S.isSquare()) {
            throw new MatrixException(MatrixException.SQUARE);
        }
        int n = S.getRowsCount(), lda=S.getColumnIncrement(), start=S.start;
        if (n == 1) {
            return;
        }
        double[] x = S.getStorage();
        int del = lda + 1;
        int max = start+lda*n;
        for (int id = start; id < max; id += del) {
            for (int il = id + 1, iu = id + lda; iu < max; il++, iu += lda) {
                x[il] = x[iu];
            }
        }
    }

    private void lcholesky_1(Matrix M, double zero) {
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

    public void xxt(DataBlock x, Matrix M) {
        int nr = x.length(), xinc = x.getIncrement();
        int mcinc = M.getColumnIncrement();
        double[] px = x.getStorage(), pm = M.getStorage();
        if (xinc == 1) {
            for (int i = 0, ix = x.getStartPosition(), im = M.getStartPosition(); i < nr; ++i, ++ix, im += 1 + mcinc) {
                for (int j = i, kx = ix, km = im, ks = im; j < nr; ++j, ++kx, km++, ks += mcinc) {
                    double z = px[ix] * px[kx];
                    pm[km] = z;
                    if (ks != km) {
                        pm[ks] = z;
                    }
                }
            }
        } else {
            for (int i = 0, ix = x.getStartPosition(), im = M.getStartPosition(); i < nr; ++i, ix += xinc, im += 1 + mcinc) {
                for (int j = i, kx = ix, km = im, ks = im; j < nr; ++j, kx += xinc, km++, ks += mcinc) {
                    double z = px[ix] * px[kx];
                    pm[km] = z;
                    if (ks != km) {
                        pm[ks] = z;
                    }
                }
            }
        }
    }

    public void XXt(final Matrix X, final Matrix M) {
        int nr = X.getRowsCount(), nc = X.getColumnsCount(), xcinc = X.getColumnIncrement();
        int mcinc = M.getColumnIncrement();
        double[] px = X.getStorage(), pm = M.getStorage();
        if (xcinc != 1) {
            for (int i = 0, ix = X.getStartPosition(), im = M.getStartPosition(); i < nr; ++i, ix++, im += 1 + mcinc) {
                for (int j = i, kx = ix, km = im, ks = im; j < nr; ++j, kx++, km += 1, ks += mcinc) {
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
            for (int i = 0, ix = X.getStartPosition(), im = M.getStartPosition(); i < nr; ++i, ix++, im += 1 + mcinc) {
                for (int j = i, kx = ix, km = im, ks = im; j < nr; ++j, kx++, km++, ks += mcinc) {
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

    public Matrix xxt(final DataBlock x) {
        Matrix M = Matrix.square(x.length());
        xxt(x, M);
        return M;
    }

    public void XtX(final Matrix X, final Matrix M) {
        SYRK.lapply(false, 1, X, 0, M);
        fromLower(M);
    }
    
//    /**
//     * Apply permutations on the given symmetric matrix.
//     * The permutation matrix is defined by P[i, j] = P[pvt[j], j]=1
//     * (which means that PSPt[i, j] = S[pvt[i], pvt[j]])
//     * @param X
//     * @param pvt 
//     */
//    public void PSPt(final Matrix X, final int[] pvt){
//        // not optimized !
//        
//    }

}
