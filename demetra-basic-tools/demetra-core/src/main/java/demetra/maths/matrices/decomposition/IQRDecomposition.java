/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package demetra.maths.matrices.decomposition;

import demetra.data.DataBlock;
import demetra.design.Development;
import demetra.data.DoubleSequence;
import demetra.maths.matrices.Matrix;
import demetra.maths.MatrixException;

/**
 * Decomposes a matrix A as A = applyQ*R
 * where applyQ is an orthogonal transformation and R is upper triangular.
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface IQRDecomposition {

    void setPrecision(double eps);

    double getPrecision();

    /**
     * @param m A matrix
     */
    void decompose(Matrix m) throws MatrixException;

    /**
     * Gets the R matrix
     *
     * @param compact True if the R matrix doesn't contain entries corresponding to redundant variables,
     * false otherwise
     * @return
     */
    Matrix r(boolean compact);

    /**
     * Gets the diagonal of the R matrix
     *
     * @param compact True if the diagonal doesn't contain entries corresponding to redundant variables,
     * false otherwise. Redundant variables are given by unused
     * @return
     */
    DoubleSequence rdiagonal(boolean compact);

    int rank();

    int[] dimensions();

    boolean isFullRank();

    int[] used();

    int[] unused();

    /**
     * Minimizes || b - A x || = || b - QR x || = || applyQ'b - R x ||
     * = || applyQ'b(1) - R x || + || applyQ'b(2) ||
     * The solution is obtained by solving applyQ'b(1) = R x
     * applyQ'b(2) are the residuals
     *
     * @param x
     * @param b
     * @param res
     * @throws MatrixException
     */
    void leastSquares(DoubleSequence x, DataBlock b, DataBlock res)
            throws MatrixException;

    /**
     * Solves a linear system. The underlying matrix must be square and full rank.
     *
     * @param x
     * @return
     */
    default boolean solve(DataBlock x) {
        if (!isFullRank()) {
            return false;
        }
        int[] dim = dimensions();
        if (dim[0] != dim[1] || x.length() != dim[0]) {
            return false;
        }
        DataBlock b = DataBlock.make(dim[0]);
        try {
            leastSquares(x, b, null);
            x.copy(b);
            return true;
        } catch (MatrixException err) {
            return false;
        }
    }
    
    /**
     * In place computation of applyQ'*x
     * @param x 
     */
    void applyQt(DataBlock x);

    /**
     * In place computation of applyQ*x
     *
     * @param x
     */
    void applyQ(DataBlock x);

}
