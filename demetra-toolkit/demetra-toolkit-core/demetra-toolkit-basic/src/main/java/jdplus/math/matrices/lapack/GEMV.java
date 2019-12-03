/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.MatrixTransformation;

/**
 * DGEMV performs one of the matrix-vector operations
 *
 * y := alpha*A*x + beta*y, or y := alpha*A**T*x + beta*y,
 *
 * where alpha and beta are scalars, x and y are vectors and A is an m by n
 * matrix.
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class GEMV {

    /**
     * y = alpha*A*x + beta*y
     *
     * @param alpha
     * @param A
     * @param x
     * @param beta
     * @param y
     * @param ta
     */
    public void apply(double alpha, Matrix A, DataPointer x, double beta, DataPointer y, MatrixTransformation ta) {
        int m = A.getRowsCount(), n = A.getColumnsCount();
        if (m == 0 || n == 0 || (alpha == 0 && beta == 1)) {
            return;
        }
        int xinc=x.inc();
        // first compute y=beta*y
        if (ta == MatrixTransformation.None) {
            SCAL.apply(m, beta, y);
            if (alpha != 0) {
                // form y+=alpha*A*x
                int jxmax = x.pos + xinc * n;
                DataPointer acol = new CPointer(A.getStorage(), A.getStartPosition());
                for (int jx = x.pos; jx < jxmax; jx += xinc, acol.pos += A.getColumnIncrement()) {
                    double tmp = alpha * x.p[jx];
                    AXPY.apply(m, tmp, acol, y);
                }
            }
        } else {
            // first compute beta*y
            SCAL.apply(n, beta, y);
            // form y+=alpha*A'*x
            int jxmax = x.pos + xinc * m;
            DataPointer arow = new RPointer(A.getStorage(), A.getStartPosition(), A.getColumnIncrement());
            for (int jx = x.pos; jx < jxmax; jx += xinc, ++arow.pos) {
                double tmp = alpha * x.p[jx];
                AXPY.apply(n, tmp, arow, y);
            }
        }
    }

}
