/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.maths.matrices.spi;

import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.design.Algorithm;
import demetra.maths.matrices.Matrix;
import demetra.maths.MatrixException;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Algorithm
public interface LowerTriangularMatrixAlgorithms {

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
     * @throws MatrixException Thrown when the Length of b is larger than the
     * number of rows of the matrix.
     */
    default void lsolve(final Matrix L, final Matrix B, double zero) throws MatrixException {
        DataBlockIterator rows = B.rowsIterator();
        while (rows.hasNext()) {
            lsolve(L, rows.next(), zero);
        }
    }

    default void lsolve(final Matrix L, final Matrix B) throws MatrixException {
        lsolve(L, B, 0);
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
    default void rsolve(final Matrix L, final Matrix B, final double zero) throws MatrixException {
        DataBlockIterator columns = B.columnsIterator();
        while (columns.hasNext()) {
            rsolve(L, columns.next(), zero);
        }
    }

    default void rsolve(final Matrix L, final Matrix B) throws MatrixException {
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
     * Computes the inverse of a triangular matrix R = L^-1
     *
     * @param L The triangular matrix being inverted
     * @return The inverse
     * @throws MatrixException when the matrix is non invertible (some elements
     * of the diagonal are 0).
     */
    default Matrix inverse(final Matrix L) throws MatrixException {
        int n = L.getRowsCount();
        Matrix IL = Matrix.identity(n);
        rsolve(L, IL);
        return IL;
    }
}
