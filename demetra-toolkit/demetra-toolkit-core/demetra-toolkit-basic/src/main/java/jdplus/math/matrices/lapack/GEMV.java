/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

import jdplus.math.matrices.DataPointer;
import jdplus.math.matrices.RPointer;
import jdplus.math.matrices.CPointer;
import jdplus.math.matrices.FastMatrix;
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
    public void apply(double alpha, FastMatrix A, DataPointer x, double beta, DataPointer y, MatrixTransformation ta) {
        int m = A.getRowsCount(), n = A.getColumnsCount();
        if (m == 0 || n == 0 || (alpha == 0 && beta == 1)) {
            return;
        }
        int xinc=x.inc();
        // first compute y=beta*y
        if (ta == MatrixTransformation.None) {
            y.mul(m, beta);
            if (alpha != 0) {
                // form y+=alpha*A*x
                int jxmax = x.pos() + xinc * n;
                CPointer acol = new CPointer(A.getStorage(), A.getStartPosition());
                for (int jx = x.pos(); jx < jxmax; jx += xinc, acol.move(A.getColumnIncrement())) {
                    double tmp = alpha * x.p()[jx];
                    y.addAX(m, tmp, acol);
                }
            }
        } else {
            // first compute beta*y
            y.mul(n, beta);
            // form y+=alpha*A'*x
            int jxmax = x.pos() + xinc * m;
            DataPointer arow = new RPointer(A.getStorage(), A.getStartPosition(), A.getColumnIncrement());
            for (int jx = x.pos(); jx < jxmax; jx += xinc, arow.next()) {
                double tmp = alpha * x.p()[jx];
                y.addAX(n, tmp, arow);
            }
        }
    }

}
