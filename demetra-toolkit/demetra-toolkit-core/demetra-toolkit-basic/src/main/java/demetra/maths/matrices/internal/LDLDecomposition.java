/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths.matrices.internal;

import demetra.maths.MatrixException;
import demetra.maths.matrices.LowerTriangularMatrix;
import demetra.maths.matrices.Matrix;
import demetra.data.DoubleSeq;

/**
 * Computes for a given symmetric matrix X a LDL decomposition, which is defined
 * by X = L D L', where L is a lower triangular matrix with ones on its diagonal
 * and D is diagonal matrix. Alternatively, we can define the decomposition with
 * D composed of -1,0 or 1 and L with any positive values on its diagonal
 *
 * @author palatej
 */
public class LDLDecomposition {

    private Matrix M;

    public void decompose(Matrix S, double zero) {
        M = S.deepClone();
        double[] data = M.getStorage();
        int n = M.getRowsCount(), cinc = M.getColumnIncrement(), dinc = 1 + cinc;
        int start = M.getStartPosition(), end = start + n * dinc;
        for (int idiag = start, irow = start, cend = start + n; idiag != end; ++irow, idiag += dinc, cend += cinc) {
            // compute aii;
            double aii = data[idiag];
            for (int j = irow, k = start; j != idiag; j += cinc, k += dinc) {
                double x = data[j];
                if (data[k] != 0) {
                    aii -= x * x * data[k];
                }
            }
            if (aii >= -zero && aii <= zero) { // quasi-zero
                data[idiag] = 0;
                // compute elements i+1 : n of column i
                for (int jx = irow, k = start; jx != idiag; jx += cinc, k += dinc) {
                    double temp = data[jx] * data[k];
                    if (temp != 0) {
                        for (int ia = jx + 1, iy = idiag + 1; iy < cend; ++ia, ++iy) {
                            data[iy] -= temp * data[ia];
                        }
                    }
                }
                for (int iy = idiag + 1; iy < cend; ++iy) {
                    if (Math.abs(data[iy]) > zero) {
                        throw new MatrixException(MatrixException.LDL);
                    } else {
                        data[iy] = 0;
                    }
                }
            } else {
                data[idiag] = aii;
                // compute elements i+1 : n of column i
                for (int jx = irow, k = start; jx != idiag; jx += cinc, k += dinc) {
                    double temp = data[jx] * data[k];
                    if (temp != 0) {
                        for (int ia = jx + 1, iy = idiag + 1; iy < cend; ++ia, ++iy) {
                            data[iy] -= temp * data[ia];
                        }
                    }
                }
                for (int iy = idiag + 1; iy < cend; ++iy) {
                    data[iy] /= aii;
                }
            }
        }
        LowerTriangularMatrix.toLower(M);
    }

    Matrix L() {
        Matrix L = M.deepClone();
        L.diagonal().set(1);
        return L;
    }

    Matrix D() {
        return Matrix.diagonal(M.diagonal());
    }

    DoubleSeq diagonal() {
        return M.diagonal().unmodifiable();
    }

}
