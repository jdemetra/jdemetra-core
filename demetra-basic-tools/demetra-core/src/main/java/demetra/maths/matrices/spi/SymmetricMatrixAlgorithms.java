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
import demetra.design.ServiceDefinition;
import demetra.design.ThreadSafe;
import demetra.maths.matrices.Matrix;
import demetra.random.RandomNumberGenerator;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@ThreadSafe
@ServiceDefinition(isSingleton = true)
public interface SymmetricMatrixAlgorithms {

    void randomize(Matrix M, RandomNumberGenerator rng);

    /**
     * Computes xx' and stores the results in m. The routines doesn't verify the
     * conditions on the dimensions.
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
     * Computes XX' and stores the results in m. The routines doesn't verify the
     * conditions on the dimensions copyOf the sub-matrices.
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
     * @param L The lower triangular matrix (L). The routine just use the lower
     * part copyOf the input matrix.
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

    Matrix inverse(final Matrix S);
}
