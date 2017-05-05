/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths.matrices;

import java.util.concurrent.atomic.AtomicReference;
import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.data.DoubleAccumulator;
import demetra.maths.matrices.impl.FastSymmetricMatrixAlgorithms;
import demetra.random.IRandomNumberGenerator;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class SymmetricMatrix {

    public static interface Algorithms {

        void randomize(Matrix M, IRandomNumberGenerator rng);

        /**
         * Computes xx' and stores the results in m. The routines doesn't verify
         * the conditions on the dimensions.
         *
         * @param x r column array
         * @param M r x r sub-matrix.
         */
        void xxt(final DataBlock x, final Matrix M);

        default Matrix xxt(final DataBlock x) {
            Matrix M = Matrix.square(x.length());
            xxt(x, M);
            return M;
        }

        /**
         * Computes XX' and stores the results in m. The routines doesn't verify
         * the conditions on the dimensions copyOf the sub-matrices.
         *
         * @param X r x c sub-matrix
         * @param M r x r sub-matrix.
         */
        void XXt(final Matrix X, final Matrix M);

        default void XtX(final Matrix X, final Matrix M) {
            XXt(X.transpose(), M);
        }

        default Matrix XXt(final Matrix X) {
            Matrix M = Matrix.square(X.getRowsCount());
            XXt(X, M);
            return M;
        }

        default Matrix XtX(final Matrix X) {
            Matrix M = Matrix.square(X.getColumnsCount());
            XXt(X.transpose(), M);
            return M;
        }

        /**
         * Computes L*L'
         *
         * @param L The lower triangular matrix (L). The routine just use the
         * lower part copyOf the input matrix.
         * @param M Output. Will contain LLt after the function call
         */
        void LLt(final Matrix L, final Matrix M);

        void UUt(final Matrix U, final Matrix M);

        default void UtU(final Matrix U, final Matrix M) {
            LLt(U.transpose(), M);
        }

        default void LtL(final Matrix L, final Matrix M) {
            UUt(L.transpose(), M);
        }

        default Matrix LLt(final Matrix L) {
            Matrix M = Matrix.square(L.getRowsCount());
            LLt(L, M);
            return M;
        }

        default Matrix UtU(final Matrix U) {
            Matrix M = Matrix.square(U.getColumnsCount());
            LLt(U.transpose(), M);
            return M;
        }

        default Matrix LtL(final Matrix L) {
            Matrix M = Matrix.square(L.getRowsCount());
            UUt(L.transpose(), M);
            return M;
        }

        default Matrix UUt(final Matrix U) {
            Matrix M = Matrix.square(U.getColumnsCount());
            UUt(U, M);
            return M;
        }

        /**
         * M = XSX'
         *
         * @param X
         * @param S
         * @param M
         */
        void XtSX(final Matrix S, final Matrix X, final Matrix M);

        /**
         * Returns XSX'
         *
         * @param X
         * @param S
         * @return
         */
        default Matrix XSXt(final Matrix S, final Matrix X) {
            return XtSX(S, X.transpose());
        }

        /**
         * M = X'SX
         *
         * @param X
         * @param S
         * @param M
         */
        default void XSXt(final Matrix S, final Matrix X, final Matrix M) {
            XtSX(S, X.transpose(), M);
        }

        /**
         * Returns X'SX
         *
         * @param X
         * @param S
         * @return
         */
        default Matrix XtSX(final Matrix S, final Matrix X) {
            int n = X.getColumnsCount();
            Matrix M = Matrix.square(n);
            XtSX(S, X, M);
            return M;
        }

        void lcholesky(final Matrix M, final double zero);

        default void lcholesky(final Matrix M) {
            lcholesky(M, 0);
        }
    }

    private final static AtomicReference<Algorithms> IMPL = new AtomicReference<>(FastSymmetricMatrixAlgorithms.INSTANCE);

    public static void setImplementation(Algorithms algorithms) {
        IMPL.set(algorithms);
    }

    public static Algorithms getImplementation() {
        return IMPL.get();
    }

    private SymmetricMatrix() {
    }

    // Static calls to the current implementation
    public static void randomize(Matrix M, IRandomNumberGenerator rng) {
        IMPL.get().randomize(M, rng);
    }

    /**
     * Computes xx' and stores the results in m. The routines doesn't verify the
     * conditions on the dimensions.
     *
     * @param x r column array
     * @param M r x r sub-matrix.
     */
    public static void xxt(final DataBlock x, final Matrix M) {
        IMPL.get().xxt(x, M);
    }

    public static Matrix xxt(final DataBlock x) {
        return IMPL.get().xxt(x);
    }

    /**
     * Computes XX' and stores the results in m. The routines doesn't verify the
     * conditions on the dimensions copyOf the sub-matrices.
     *
     * @param X r x c sub-matrix
     * @param M r x r sub-matrix.
     */
    public static void XXt(final Matrix X, final Matrix M) {
        IMPL.get().XXt(X, M);
    }

    public static void XtX(final Matrix X, final Matrix M) {
        IMPL.get().XtX(X, M);
    }

    public static Matrix XXt(final Matrix X) {
        return IMPL.get().XXt(X);
    }

    public static Matrix XtX(final Matrix X) {
        return IMPL.get().XtX(X);
    }

    public static Matrix robustXtX(final Matrix X, DoubleAccumulator acc) {
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
    public static void LLt(final Matrix L, final Matrix M) {
        IMPL.get().LLt(L, M);
    }

    public static void UUt(final Matrix U, final Matrix M) {
        IMPL.get().UUt(U, M);
    }

    public static void UtU(final Matrix U, final Matrix M) {
        IMPL.get().UtU(U, M);
    }

    public static void LtL(final Matrix L, final Matrix M) {
        IMPL.get().LtL(L, M);
    }

    public static Matrix LLt(final Matrix L) {
        return IMPL.get().LLt(L);
    }

    public static Matrix UtU(final Matrix U) {
        return IMPL.get().UtU(U);
    }

    public static Matrix LtL(final Matrix L) {
        return IMPL.get().LtL(L);
    }

    public static Matrix UUt(final Matrix U) {
        return IMPL.get().UUt(U);
    }

    /**
     * M = XSX'
     *
     * @param X
     * @param S
     * @param M
     */
    public static void XSXt(final Matrix S, final Matrix X, final Matrix M) {
        IMPL.get().XSXt(S, X, M);
    }

    /**
     * Returns XSX'
     *
     * @param X
     * @param S
     * @return
     */
    public static Matrix XSXt(final Matrix S, final Matrix X) {
        return IMPL.get().XSXt(S, X);
    }

    /**
     * M = X'SX
     *
     * @param X
     * @param S
     * @param M
     */
    public static void XtSX(final Matrix S, final Matrix X, final Matrix M) {
        IMPL.get().XtSX(S, X, M);
    }

    /**
     * Returns X'SX
     *
     * @param X
     * @param S
     * @return
     */
    public static Matrix XtSX(final Matrix S, final Matrix X) {
        return IMPL.get().XtSX(S, X);
    }

    public static void lcholesky(final Matrix M, final double zero) {
        IMPL.get().lcholesky(M, zero);
    }

    public static void lcholesky(final Matrix M) {
        IMPL.get().lcholesky(M);
    }

    public static void reenforceSymmetry(Matrix S) {
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

    public static void fromLower(Matrix S) {
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

    public static void fromUpper(Matrix S) {
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

}
