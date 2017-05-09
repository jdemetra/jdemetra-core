/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths.matrices;

import demetra.maths.matrices.spi.UpperTriangularMatrixAlgorithms;
import java.util.concurrent.atomic.AtomicReference;
import demetra.data.DataBlock;
import demetra.random.IRandomNumberGenerator;
import demetra.utilities.ServiceLookup;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class UpperTriangularMatrix {

    private final AtomicReference<UpperTriangularMatrixAlgorithms> IMPL = ServiceLookup.firstMutable(UpperTriangularMatrixAlgorithms.class);

    public void setImplementation(UpperTriangularMatrixAlgorithms algorithms) {
        IMPL.set(algorithms);
    }

    public UpperTriangularMatrixAlgorithms getImplementation() {
        return IMPL.get();
    }

    public void toUpper(Matrix S) {
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

    public void randomize(Matrix M, IRandomNumberGenerator rng) {
        M.set((r, c) -> (r < c) ? 0 : rng.nextDouble());
    }

    public void rsolve(Matrix M, DataBlock x, double zero) throws MatrixException {
        IMPL.get().rsolve(M, x, zero);
    }

    public void rsolve(Matrix M, DataBlock x) throws MatrixException {
        IMPL.get().rsolve(M, x, 0);
    }

    public void lsolve(Matrix M, DataBlock x, double zero) throws MatrixException {
        IMPL.get().lsolve(M, x, zero);
    }

    public void lsolve(Matrix M, DataBlock x) throws MatrixException {
        IMPL.get().lsolve(M, x);
    }

    public void rmul(Matrix M, DataBlock x) {
        IMPL.get().rmul(M, x);
    }

    public void lmul(Matrix M, DataBlock x) {
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
    public void lsolve(final Matrix U, final Matrix B, double zero)
            throws MatrixException {
        IMPL.get().lsolve(U, B, zero);
    }

    public void lsolve(final Matrix U, final Matrix B)
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
    public void rsolve(final Matrix U, final Matrix B, final double zero)
            throws MatrixException {
        IMPL.get().rsolve(U, B, zero);
    }

    public void rsolve(final Matrix U, final Matrix B)
            throws MatrixException {
        IMPL.get().rsolve(U, B);
    }

    /**
     * Computes B = U * B
     *
     * @param U
     * @param B
     */
    public void rmul(final Matrix U, final Matrix B) {
        IMPL.get().rmul(U, B);
    }

    /**
     * Computes B = B * U
     *
     * @param U
     * @param B
     * @throws MatrixException
     */
    public void lmul(final Matrix U, final Matrix B) {
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
    public Matrix inverse(final Matrix U) throws MatrixException {
        return IMPL.get().inverse(U);
    }
}
