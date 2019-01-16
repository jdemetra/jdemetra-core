/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths.matrices;

import demetra.maths.MatrixException;
import demetra.maths.matrices.spi.LowerTriangularMatrixAlgorithms;
import java.util.concurrent.atomic.AtomicReference;
import demetra.data.DataBlock;
import demetra.data.LogSign;
import demetra.util.ServiceLookup;
import demetra.random.RandomNumberGenerator;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class LowerTriangularMatrix {

    private final AtomicReference<LowerTriangularMatrixAlgorithms> IMPL = ServiceLookup.firstMutable(LowerTriangularMatrixAlgorithms.class);

    public void setImplementation(LowerTriangularMatrixAlgorithms algorithms) {
        IMPL.set(algorithms);
    }

    public LowerTriangularMatrixAlgorithms getImplementation() {
        return IMPL.get();
    }

    public void randomize(Matrix M, RandomNumberGenerator rng) {
        M.set((r, c) -> (c > r) ? 0 : rng.nextDouble());
    }

    public void rsolve(Matrix M, DataBlock x, double zero) throws MatrixException {
        IMPL.get().rsolve(M, x, zero);
    }

    public void rsolve(Matrix M, DataBlock x) throws MatrixException {
        IMPL.get().rsolve(M, x);
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
     * Solves the set of equations X*L = B
     *
     * @param L The lower triangular matrix
     * @param B On entry the left hand side of the equation. Contains the
     * solution x on returning.
     * @param zero Small positive value identifying 0. Can be 0.
     * @throws MatrixException Thrown when the Length of b is larger than the
     * number of rows of the matrix.
     */
    public void lsolve(final Matrix L, final Matrix B, double zero)
            throws MatrixException {
        IMPL.get().lsolve(L, B, zero);
    }

    public void lsolve(final Matrix L, final Matrix B)
            throws MatrixException {
        IMPL.get().lsolve(L, B);
    }

    /**
     * Solves the set of equations LX = B where X and B are matrices with a
     * number of rows less than or equal to the number of columns of L. The
     * solution is returned in place i.e. the solution X replaces the right hand
     * side B. Column version
     *
     * @param L L. Lower triangular matrix
     * @param B On entry the right hand side of the equation. Contains the
     * solution X on returning.
     * @param zero Small positive value identifying 0. Can be 0.
     * @throws MatrixException Thrown when the system cannot be solved.
     */
    public void rsolve(final Matrix L, final Matrix B, final double zero)
            throws MatrixException {
        IMPL.get().rsolve(L, B, zero);
    }

    public void rsolve(final Matrix L, final Matrix B)
            throws MatrixException {
        IMPL.get().rsolve(L, B);
    }

    /**
     * Computes B = L * B
     *
     * @param L
     * @param B
     */
    public void rmul(final Matrix L, final Matrix B) {
        IMPL.get().rmul(L, B);
    }

    /**
     * Computes B = B * L
     *
     * @param L
     * @param B
     * @throws MatrixException
     */
    public void lmul(final Matrix L, final Matrix B) {
        IMPL.get().lmul(L, B);
    }

    public void toLower(Matrix M) {
        int m = M.getColumnsCount(), n = M.getRowsCount();
        if (m == 1) {
            return;
        }
        int rinc = M.getRowIncrement(), cinc = M.getColumnIncrement(), start = M.getStartPosition();
        double[] x = M.getStorage();
        int q = Math.min(m, n);
        for (int c = 0, id = start; c < q; ++c, id += rinc + cinc) {
            for (int r = c + 1, iu = id; r < m; ++r) {
                iu += cinc;
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
        return IMPL.get().inverse(L);
    }
    
    public LogSign logDeterminant(Matrix L){
        return LogSign.of(L.diagonal());
    }
    
    public double determinant(Matrix L){
        LogSign ls=logDeterminant(L);
        if (ls == null)
            return 0;
        double val=Math.exp(ls.getValue());
        return ls.isPositive() ? val : -val;
    }

}
