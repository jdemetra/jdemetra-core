/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths.matrices;

import demetra.maths.matrices.spi.SymmetricMatrixAlgorithms;
import java.util.concurrent.atomic.AtomicReference;
import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.data.LogSign;
import demetra.data.accumulator.DoubleAccumulator;
import demetra.random.IRandomNumberGenerator;
import demetra.util.ServiceLookup;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class SymmetricMatrix {

    private final AtomicReference<SymmetricMatrixAlgorithms> IMPL = ServiceLookup.firstMutable(SymmetricMatrixAlgorithms.class);

    public void setImplementation(SymmetricMatrixAlgorithms algorithms) {
        IMPL.set(algorithms);
    }

    public SymmetricMatrixAlgorithms getImplementation() {
        return IMPL.get();
    }

    // Static calls to the current implementation
    public void randomize(Matrix M, IRandomNumberGenerator rng) {
        IMPL.get().randomize(M, rng);
    }

    /**
     * Computes xx' and stores the results in m. The routines doesn't verify the
     * conditions on the dimensions.
     *
     * @param x r column array
     * @param M r x r sub-matrix.
     */
    public void xxt(final DataBlock x, final Matrix M) {
        IMPL.get().xxt(x, M);
    }

    public Matrix xxt(final DataBlock x) {
        return IMPL.get().xxt(x);
    }

    /**
     * Computes XX' and stores the results in m. The routines doesn't verify the
     * conditions on the dimensions copyOf the sub-matrices.
     *
     * @param X r x c sub-matrix
     * @param M r x r sub-matrix.
     */
    public void XXt(final Matrix X, final Matrix M) {
        IMPL.get().XXt(X, M);
    }

    public void XtX(final Matrix X, final Matrix M) {
        IMPL.get().XtX(X, M);
    }

    public Matrix XXt(final Matrix X) {
        return IMPL.get().XXt(X);
    }

    public Matrix XtX(final Matrix X) {
        return IMPL.get().XtX(X);
    }

    public Matrix robustXtX(final Matrix X, DoubleAccumulator acc) {
        int n = X.getColumnsCount();
        Matrix z = Matrix.square(n);
        DataBlockIterator rows = X.columnsIterator(), columns = X.columnsIterator();
        int irow = 0;
        while (rows.hasNext()) {
            DataBlock row = rows.next();
            columns.reset(irow);
            acc.reset();
            row.robustDot(columns.next(), acc);
            z.set(irow, irow, acc.sum());
            int icol = irow;
            while (columns.hasNext()) {
                icol++;
                acc.reset();
                row.robustDot(columns.next(), acc);
                double val = acc.sum();
                z.set(irow, icol, val);
                z.set(icol, irow, val);
            }
            irow++;
        }
        return z;
    }

    /**
     * Computes L*L'
     *
     * @param L The lower triangular matrix (L). The routine just use the lower
     * part copyOf the input matrix.
     * @param M Output. Will contain LLt after the function call
     */
    public void LLt(final Matrix L, final Matrix M) {
        IMPL.get().LLt(L, M);
    }

    public void UUt(final Matrix U, final Matrix M) {
        IMPL.get().UUt(U, M);
    }

    public void UtU(final Matrix U, final Matrix M) {
        IMPL.get().UtU(U, M);
    }

    public void LtL(final Matrix L, final Matrix M) {
        IMPL.get().LtL(L, M);
    }

    public Matrix LLt(final Matrix L) {
        return IMPL.get().LLt(L);
    }

    public Matrix UtU(final Matrix U) {
        return IMPL.get().UtU(U);
    }

    public Matrix LtL(final Matrix L) {
        return IMPL.get().LtL(L);
    }

    public Matrix UUt(final Matrix U) {
        return IMPL.get().UUt(U);
    }

    /**
     * M = XSX'
     *
     * @param X
     * @param S
     * @param M
     */
    public void XSXt(final Matrix S, final Matrix X, final Matrix M) {
        IMPL.get().XSXt(S, X, M);
    }

    /**
     * Returns XSX'
     *
     * @param X
     * @param S
     * @return
     */
    public Matrix XSXt(final Matrix S, final Matrix X) {
        return IMPL.get().XSXt(S, X);
    }

    /**
     * M = X'SX
     *
     * @param X
     * @param S
     * @param M
     */
    public void XtSX(final Matrix S, final Matrix X, final Matrix M) {
        IMPL.get().XtSX(S, X, M);
    }

    /**
     * Returns X'SX
     *
     * @param X
     * @param S
     * @return
     */
    public Matrix XtSX(final Matrix S, final Matrix X) {
        return IMPL.get().XtSX(S, X);
    }

    public void lcholesky(final Matrix M, final double zero) {
        IMPL.get().lcholesky(M, zero);
    }

    public void lcholesky(final Matrix M) {
        IMPL.get().lcholesky(M);
    }

    public Matrix inverse(Matrix S) {
        return IMPL.get().inverse(S);
    }

    public void reenforceSymmetry(Matrix S) {
        if (!S.isSquare()) {
            throw new MatrixException(MatrixException.SQUARE);
        }
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

    public void fromLower(Matrix S) {
        if (!S.isSquare()) {
            throw new MatrixException(MatrixException.SQUARE);
        }
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

    public void fromUpper(Matrix S) {
        if (!S.isSquare()) {
            throw new MatrixException(MatrixException.SQUARE);
        }
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
    
    public LogSign logDeterminant(Matrix S){
        Matrix s = S.deepClone();
        try{
            lcholesky(s);
            DataBlock diagonal = s.diagonal();
            LogSign ls = LogSign.of(diagonal);
            return new LogSign(ls.getValue()*2, true);
        }catch (MatrixException e){
            return Matrix.logDeterminant(S);
        }
    }
    
    public double determinant(Matrix L){
        LogSign ls=logDeterminant(L);
        if (ls == null)
            return 0;
        double val=Math.exp(ls.getValue());
        return ls.isPositive() ? val : -val;
    }

}
