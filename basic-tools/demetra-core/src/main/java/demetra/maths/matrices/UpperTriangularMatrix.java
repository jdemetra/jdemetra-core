/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths.matrices;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.maths.matrices.impl.FastUpperTriangularMatrixAlgorithms;
import demetra.random.IRandomNumberGenerator;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class UpperTriangularMatrix {

    public static interface Algorithms {

        void rsolve(Matrix M, DataBlock x, double zero) throws MatrixException;

        default void rsolve(Matrix M, DataBlock x) throws MatrixException {
            rsolve(M, x, 0);
        }

        void lsolve(Matrix M, DataBlock x, double zero) throws MatrixException;

        default void lsolve(Matrix M, DataBlock x) throws MatrixException {
            lsolve(M, x, 0);
        }

        void rmul(Matrix M, DataBlock x);

        void lmul(Matrix M, DataBlock x);

        // Matrix versions
        /**
         * Solves the set of equations X*L = B
         *
         * @param L The lower triangular matrix
         * @param B On entry the left hand side of the equation. Contains the
         * solution x on returning.
         * @param zero Small positive value identifying 0. Can be 0.
         * @throws MatrixException Thrown when the Length of b is larger than
         * the number of rows of the matrix.
         */
        default void lsolve(final Matrix L, final Matrix B, double zero)
                throws MatrixException {
            DataBlockIterator rows = B.rowsIterator();
            while (rows.hasNext()) {
                lsolve(L, rows.next(), zero);
            }
        }

        default void lsolve(final Matrix L, final Matrix B)
                throws MatrixException {
            lsolve(L, B, 0);
        }

        /**
         * Solves the set of equations LX = B where X and B are matrices with a
         * number of rows less than or equal to the number of columns of L. The
         * solution is returned in place i.e. the solution X replaces the right
         * hand side B. Column version
         *
         * @param L L. Lower triangular matrix
         * @param B On entry the right hand side of the equation. Contains the
         * solution X on returning.
         * @param zero Small positive value identifying 0. Can be 0.
         * @throws MatrixException Thrown when the system cannot be solved.
         */
        default void rsolve(final Matrix L, final Matrix B, final double zero)
                throws MatrixException {
            DataBlockIterator columns = B.columnsIterator();
            while (columns.hasNext()) {
                rsolve(L, columns.next(), zero);
            }
        }

        default void rsolve(final Matrix L, final Matrix B)
                throws MatrixException {
            rsolve(L, B, 0);
        }

        /**
         * Computes B = L * B
         *
         * @param L
         * @param B
         */
        default void rmul(final Matrix L, final Matrix B) {
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
        default void lmul(final Matrix L, final Matrix B) {
            DataBlockIterator rows = B.rowsIterator();
            while (rows.hasNext()) {
                lmul(L, rows.next());
            }
        }

        /**
         * Computes the inverse of a triangular matrix R = U^-1
         *
         * @param U The upper matrix being inverted
         * @return The inverse
         * @throws MatrixException when the matrix is non invertible (some
         * elements of the diagonal are 0).
         */
        default Matrix inverse(final Matrix U) throws MatrixException {
            int n = U.getRowsCount();
            Matrix IU = Matrix.identity(n);
            rsolve(U, IU);
            return IU;
        }

    }

    private final static AtomicReference<Algorithms> IMPL = new AtomicReference<>(FastUpperTriangularMatrixAlgorithms.INSTANCE);

    public static void setImplementation(Algorithms algorithms) {
        IMPL.set(algorithms);
    }

    public static Algorithms getImplementation() {
        return IMPL.get();
    }

    private UpperTriangularMatrix() {
    }

    public static void toUpper(Matrix S) {
        int m = S.getColumnsCount(), n = S.getRowsCount();
        if (n == 1) {
            return;
        }
        int rinc = S.getRowIncrement(), cinc = S.getColumnIncrement(), start = S.getStartPosition();
        double[] x = S.getStorage();
        int q = Math.min(m, n);
        for (int c = 0, id = start; c < q; ++c, id += rinc + cinc) {
            for (int r = c + 1, il = id; r < n; ++r) {
                il += rinc;
                x[il] = 0;
            }
        }
    }

    public static void randomize(Matrix M, IRandomNumberGenerator rng) {
        M.set((r, c) -> (r < c) ? 0 : rng.nextDouble());
    }

    public static void rsolve(Matrix M, DataBlock x, double zero) throws MatrixException {
        IMPL.get().rsolve(M, x, zero);
    }

    public static void rsolve(Matrix M, DataBlock x) throws MatrixException {
        IMPL.get().rsolve(M, x, 0);
    }

    public static void lsolve(Matrix M, DataBlock x, double zero) throws MatrixException {
        IMPL.get().lsolve(M, x, zero);
    }

    public static void lsolve(Matrix M, DataBlock x) throws MatrixException {
        IMPL.get().lsolve(M, x);
    }

    public static void rmul(Matrix M, DataBlock x) {
        IMPL.get().rmul(M, x);
    }

    public static void lmul(Matrix M, DataBlock x) {
        IMPL.get().lmul(M, x);
    }

    // Matrix versions
    /**
     * Solves the set of equations X*U = B
     *
     * @param U The upper triangular matrix
     * @param B On entry the left hand side of the equation. Contains the
     * solution x on returning.
     * @param zero Small positive value identifying 0. Can be 0.
     * @throws MatrixException Thrown when the Length of b is larger than the
     * number of rows of the matrix.
     */
    public static void lsolve(final Matrix U, final Matrix B, double zero)
            throws MatrixException {
        IMPL.get().lsolve(U, B, zero);
    }

    public static void lsolve(final Matrix U, final Matrix B)
            throws MatrixException {
        IMPL.get().lsolve(U, B);
    }

    /**
     * Solves the set of equations LX = B where X and B are matrices with a
     * number of rows less than or equal to the number of columns of U. The
     * solution is returned in place i.e. the solution X replaces the right hand
     * side B. Column version
     *
     * @param U U. Upper triangular matrix
     * @param B On entry the right hand side of the equation. Contains the
     * solution X on returning.
     * @param zero Small positive value identifying 0. Can be 0.
     * @throws MatrixException Thrown when the system cannot be solved.
     */
    public static void rsolve(final Matrix U, final Matrix B, final double zero)
            throws MatrixException {
        IMPL.get().rsolve(U, B, zero);
    }

    public static void rsolve(final Matrix U, final Matrix B)
            throws MatrixException {
        IMPL.get().rsolve(U, B);
    }

    /**
     * Computes B = U * B
     *
     * @param U
     * @param B
     */
    public static void rmul(final Matrix U, final Matrix B) {
        IMPL.get().rmul(U, B);
    }

    /**
     * Computes B = B * U
     *
     * @param U
     * @param B
     * @throws MatrixException
     */
    public static void lmul(final Matrix U, final Matrix B) {
        IMPL.get().lmul(U, B);
    }

    /**
     * Computes the inverse of a triangular matrix R = U^-1
     *
     * @param U The triangular matrix being inverted
     * @return The inverse
     * @throws MatrixException when the matrix is non invertible (some elements
     * of the diagonal are 0).
     */
    public static Matrix inverse(final Matrix U) throws MatrixException {
        return IMPL.get().inverse(U);
    }

}
