/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices;

import demetra.math.Constants;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.data.LogSign;
import jdplus.math.matrices.lapack.TRMV;
import jdplus.math.matrices.lapack.TRSV;
import jdplus.random.RandomNumberGenerator;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class LowerTriangularMatrix {

    public void randomize(Matrix M, RandomNumberGenerator rng) {
        M.set((r, c) -> (c > r) ? 0 : rng.nextDouble());
    }

    @Deprecated
    public void lsolve(final Matrix L, final Matrix B) throws MatrixException {
        int nc = B.getColumnsCount();
        if (nc != L.getRowsCount()) {
            throw new MatrixException(MatrixException.DIM);
        }
        DataBlockIterator rows = B.rowsIterator();
        while (rows.hasNext()) {
            DataBlock c = rows.next();
            TRSV.Ltsolve(L, c.getStorage(), c.getStartPosition(), c.getIncrement());
        }
    }

    /**
     * y := L*x or x = iL*y
     * @param L
     * @param x 
     */
    public void iLx(Matrix L, DataBlock x){
        TRSV.Lsolve(L, x.getStorage(), x.getStartPosition(), x.getIncrement());
    }
    
    /**
     * y := x*L or y' = L'*x or  x = iLt*y
     * @param L
     * @param x 
     */
    public void iLtx(Matrix L, DataBlock x){
        TRSV.Ltsolve(L, x.getStorage(), x.getStartPosition(), x.getIncrement());
    }

    /**
     * X*L=M or X = M*iL (iL = L^-1) or Z = iLt*N or Lt*Z=N
     * @param L
     * @param M
     * @throws MatrixException 
     */
    public void MiL(final Matrix L, final Matrix M) throws MatrixException {
        int nc = M.getColumnsCount();
        if (nc != L.getRowsCount()) {
            throw new MatrixException(MatrixException.DIM);
        }
        DataBlockIterator rows = M.rowsIterator();
        while (rows.hasNext()) {
            DataBlock c = rows.next();
            TRSV.Ltsolve(L, c.getStorage(), c.getStartPosition(), c.getIncrement());
        }
    }

    /**
     * L*X=M or X = iL*M
     * @param L
     * @param M
     * @throws MatrixException 
     */
    public void iLM(final Matrix L, final Matrix M) throws MatrixException {
        int nr = M.getRowsCount();
        if (nr != L.getRowsCount()) {
            throw new MatrixException(MatrixException.DIM);
        }
        DataBlockIterator cols = M.columnsIterator();
        while (cols.hasNext()) {
            DataBlock c = cols.next();
            TRSV.Lsolve(L, c.getStorage(), c.getStartPosition(), c.getIncrement());
        }
    }

    /**
     * X*L'=M or L*X'=M' or X' = iL*M' or Z = iL*N
     * @param L
     * @param M
     * @throws MatrixException 
     */
    public void MiLt(final Matrix L, final Matrix M) throws MatrixException {
        int nr = M.getColumnsCount();
        if (nr != L.getRowsCount()) {
            throw new MatrixException(MatrixException.DIM);
        }
        DataBlockIterator rows = M.rowsIterator();
        while (rows.hasNext()) {
            DataBlock r = rows.next();
            TRSV.Lsolve(L, r.getStorage(), r.getStartPosition(), r.getIncrement());
        }
    }

    /**
     * L*X'=M' or X*L'=M or X = M*iLt or IL*M' = X'
     * @param L
     * @param M
     * @throws MatrixException 
     */
    public void iLtM(final Matrix L, final Matrix M) throws MatrixException {
        int nr = M.getRowsCount();
        if (nr != L.getRowsCount()) {
            throw new MatrixException(MatrixException.DIM);
        }
        DataBlockIterator cols = M.columnsIterator();
        while (cols.hasNext()) {
            DataBlock c = cols.next();
            TRSV.Ltsolve(L, c.getStorage(), c.getStartPosition(), c.getIncrement());
        }
    }

    @Deprecated
    public void rsolve(final Matrix L, final Matrix M) throws MatrixException {
        int nr = M.getRowsCount(), nc = M.getColumnsCount();
        if (nr != L.getRowsCount()) {
            throw new MatrixException(MatrixException.DIM);
        }
        double[] pb = M.getStorage();
        int start = M.getStartPosition(), lda = M.getColumnIncrement();
        int bmax = start + nc * lda;
        for (int b = start; b < bmax; b += lda) {
            TRSV.Lsolve(L, pb, b, 1);
        }
    }

    /**
     * XL = M or L'X' = M' or Z = iLi*N  where N = M'
     * @param L
     * @param M
     * @throws MatrixException 
     */
    public void iLMt(final Matrix L, final Matrix M) throws MatrixException {
        int nr = M.getRowsCount(), nc = M.getColumnsCount();
        if (nc != L.getRowsCount()) {
            throw new MatrixException(MatrixException.DIM);
        }
        double[] pb = M.getStorage();
        int start = M.getStartPosition(), lda = M.getColumnIncrement();
        int bmax = start + nr;
        for (int b = start; b < bmax; ++b) {
            TRSV.Ltsolve(L, pb, b, lda);
        }
    }

    /**
     * Computes r = L*r The method right-multiplies the matrix by a vector.The
     * Length of the vector must be equal or less than the number of rows of the
     * matrix. The multiplier is modified in place. Column version
     *
     * @param L The lower triangular matrix
     * @param r An array of double
     */
    @Deprecated
    public void rmul(Matrix L, DataBlock r) {
        TRMV.Lx(L, r.getStorage(), r.getStartPosition(), r.getIncrement());
    }
    
    /**
     * x := L*x
     * @param L
     * @param x 
     */
    public void Lx(Matrix L, DataBlock x){
        TRMV.Lx(L, x.getStorage(), x.getStartPosition(), x.getIncrement());
    }
    
    /**
     * x := x*L' or z := L*z with z=x' 
     * This method is strictly the same as Lx
     * @param L
     * @param x 
     */
    public void xLt(Matrix L, DataBlock x){
        TRMV.Lx(L, x.getStorage(), x.getStartPosition(), x.getIncrement());
    }

    /**
     * x := x*L or z := L'z with z=x'
     * @param L
     * @param x 
     */
    public void xL(Matrix L, DataBlock x){
        TRMV.Ltx(L, x.getStorage(), x.getStartPosition(), x.getIncrement());
    }
    
    /**
     * x := L'x
     * This method is strictly the same as xL
     * @param L
     * @param x 
     */
    public void Ltx(Matrix L, DataBlock x){
        TRMV.Ltx(L, x.getStorage(), x.getStartPosition(), x.getIncrement());
    }
    
    /**
     * M := L*M
     * @param L
     * @param M 
     */
    public void LM(Matrix L, Matrix M){
        DataBlockIterator cols = M.columnsIterator();
        while (cols.hasNext())
            Lx(L, cols.next());
    }
    
    /**
     * M := M*L or L'N = N (N = M') 
     * @param L
     * @param M 
     */
    public void ML(Matrix L, Matrix M){
        DataBlockIterator rows = M.rowsIterator();
        while (rows.hasNext())
            Ltx(L, rows.next());
    }

    /**
     * M := L'*M
     * @param L
     * @param M 
     */
    public void LtM(Matrix L, Matrix M){
        DataBlockIterator cols = M.columnsIterator();
        while (cols.hasNext())
            Ltx(L, cols.next());
    }

    /**
     * M := M*L' or LN = N (N = M') 
     * @param L
     * @param M 
     */
    public void MLt(Matrix L, Matrix M){
        DataBlockIterator rows = M.rowsIterator();
        while (rows.hasNext())
            Lx(L, rows.next());
    }

    /**
     * Computes l = l*L The method left-multiplies the matrix by a vector.The
     * Length of the vector must be equal to the number of rows of the matrix.
     * The multiplier is modified in place. Column version
     *
     * @param L The lower triangular matrix
     * @param l An array of double
     */
    @Deprecated
    public void lmul(Matrix L, DataBlock l) {
        // l = l *L <=> L'l' = l'
        TRMV.Ltx(L, l.getStorage(), l.getStartPosition(), l.getIncrement());
    }

    /**
     * B=L*B
     *
     * @param L
     * @param B
     */
    @Deprecated
    public void rmul(final Matrix L, final Matrix B) {
        int nr = B.getRowsCount(), nc = B.getColumnsCount();
        if (nr != L.getRowsCount()) {
            throw new MatrixException(MatrixException.DIM);
        }
        double[] pb = B.getStorage();
        int start = B.getStartPosition(), lda = B.getColumnIncrement();
        int bmax = start + nc * lda;
        for (int b = start; b < bmax; b += lda) {
            TRMV.Lx(L, pb, b, 1);
        }
    }

    /**
     * B=B*L
     *
     * @param L
     * @param B
     */
    @Deprecated
    public void lmul(final Matrix L, final Matrix B) {
        // B=B*L <=> L'B' = B'
        int nc = B.getColumnsCount();
        if (nc != L.getRowsCount()) {
            throw new MatrixException(MatrixException.DIM);
        }
        DataBlockIterator rows = B.rowsIterator();
        while (rows.hasNext()) {
            DataBlock c = rows.next();
            TRMV.Ltx(L, c.getStorage(), c.getStartPosition(), c.getIncrement());
        }
    }

    public void solve(Matrix L, final DataBlock b, double zero) throws MatrixException {
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
                    for (int xj = xi + xinc, j = i + 1; xj != xend; xj += xinc, ++j) {
                        if (Math.abs(data[j]) > zero) {
                            throw new MatrixException(MatrixException.SINGULAR);
                        }
                    }
                    x[xi] = 0;
                } else {
                    double c = t / d;
                    x[xi] = c;
                    for (int xj = xi + xinc, j = i + 1; xj != xend; xj += xinc, ++j) {
                        x[xj] -= c * data[j];
                    }
                }
            } else {
                x[xi] = 0;
            }
        }
    }

    /**
     * Solves L*x=b
     *
     * @param L
     * @param b
     * @throws MatrixException
     */
    public void rsolve(Matrix L, final DataBlock b) throws MatrixException {
        TRSV.Lsolve(L, b.getStorage(), b.getStartPosition(), b.getIncrement());
    }

    /**
     * solves xL=b (or L'x = b)
     *
     * @param L
     * @param x
     * @throws MatrixException
     */
    public void lsolve(Matrix L, DataBlock x) throws MatrixException {
        TRSV.Ltsolve(L, x.getStorage(), x.getStartPosition(), x.getIncrement());
    }

    public void toLower(Matrix M) {
        int m = M.getRowsCount(), n = M.getColumnsCount();
        if (m != n) {
            throw new MatrixException(MatrixException.SQUARE);
        }
        if (n == 1) {
            return;
        }
        double[] x = M.getStorage();
        int lda = M.getColumnIncrement(), start = M.getStartPosition();
        for (int c = 0, ic = start; c < n; ic += lda, ++c) {
            int id = ic + c;
            for (int iu = ic; iu < id; ++iu) {
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
    public Matrix inverse(final Matrix L) throws MatrixException {
        if (L.diagonal().anyMatch(x -> x == 0)) {
            throw new MatrixException(MatrixException.SINGULAR);
        }
        int n = L.getRowsCount();
        if (n != L.getColumnsCount()) {
            throw new MatrixException(MatrixException.SQUARE);
        }

        Matrix IL = Matrix.identity(n);
        rsolve(L, IL);
        return IL;
    }

    public LogSign logDeterminant(Matrix L) {
        return LogSign.of(L.diagonal());
    }

    public double determinant(Matrix L) {
        LogSign ls = logDeterminant(L);
        if (ls == null) {
            return 0;
        }
        double val = Math.exp(ls.getValue());
        return ls.isPositive() ? val : -val;
    }

}
