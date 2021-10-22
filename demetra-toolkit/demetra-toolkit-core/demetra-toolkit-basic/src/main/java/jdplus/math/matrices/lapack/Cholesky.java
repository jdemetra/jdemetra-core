/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.MatrixException;
import jdplus.math.matrices.SymmetricMatrix;

/**
 * Modified version of POTF2, which computes the Cholesky factorization of a
 * real symmetric positive definite matrix A. This version allows the
 * factorization of (quasi-)singular symmetric matrices. Redundant rows/columns
 * are set to 0
 *
 * The factorization has the form A = U**T * U , if UPLO = 'U', or A = L * L**T,
 * if UPLO = 'L', where U is an upper triangular matrix and L is lower
 * triangular.
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class Cholesky implements SymmetricMatrix.CholeskyProcessor {

    /**
     * Upper cholesky. The lower part of L is not referenced (neither used nor
     * modified)
     *
     * @param U in/out matrix
     */
    @Override
    public void ucholesky(FastMatrix U) {
        ucholesky(U, 0);
    }

    @Override
    public void ucholesky(FastMatrix U, double zero) {
        int n = U.getRowsCount();
        if (n == 0) {
            return;
        }
        double[] pu = U.getStorage();
        int lda = U.getColumnIncrement(), start = U.getStartPosition();
        if (n == 1) {
            double d = pu[start];
            if (d <= -zero) {
                throw new MatrixException(MatrixException.CHOLESKY);
            }
            if (d < zero) {
                pu[start] = 0;
            } else {
                pu[start] = Math.sqrt(d);
            }
            return;
        }
        int rend = start + n * lda; // end of the first row
        for (int j = 1, jd = start, jc = start; j <= n; ++j, jd += lda + 1, jc += lda) {
            double ajj = pu[jd];
            for (int i = jc; i < jd; ++i) {
                double d = pu[i];
                ajj -= d * d;
            }
            if (ajj <= -zero || Double.isNaN(ajj)) {
                throw new MatrixException(MatrixException.CHOLESKY);
            } else if (ajj < zero) {
                pu[jd] = 0;
                // compute elements J+1:N of row j
                if (j < n) {
                    for (int jcc = jc + lda; jcc < rend; jcc += lda) {
                        double s = 0;
                        int ic1 = jcc;
                        for (int ic0 = jc; ic0 < jd; ++ic0, ++ic1) {
                            s += pu[ic0] * pu[ic1];
                        }
                        if (Math.abs(pu[ic1] - s) > zero) {
                            throw new MatrixException(MatrixException.CHOLESKY);
                        } else {
                            pu[ic1] = 0;
                        }
                    }
                }
            } else {
                ajj = Math.sqrt(ajj);
                pu[jd] = ajj;
                // compute elements J+1:N of row j
                if (j < n) {
                    for (int jcc = jc + lda; jcc < rend; jcc += lda) {
                        double s = 0;
                        int ic1 = jcc;
                        for (int ic0 = jc; ic0 < jd; ++ic0, ++ic1) {
                            s += pu[ic0] * pu[ic1];
                        }
                        pu[ic1] = (pu[ic1] - s) / ajj;
                    }
                }
            }
        }
    }

    /**
     * Lower cholesky. L can be quasi-singular The upper part of L is not
     * referenced (neither used nor modified)
     *
     * @param L in/out matrix
     * @param zero quasi-zero value. Can be zero (in this case, singular
     * matrices will always generate an exception
     */
    @Override
    public void lcholesky(FastMatrix L, double zero) {
        int n = L.getRowsCount();
        if (n == 0) {
            return;
        }
        double[] pl = L.getStorage();
        int lda = L.getColumnIncrement(), start = L.getStartPosition();
        if (n == 1) {
            double d = pl[start];
            if (d <= -zero) {
                throw new MatrixException(MatrixException.CHOLESKY);
            }
            if (d < zero) {
                pl[start] = 0;
            } else {
                pl[start] = Math.sqrt(d);
            }
            return;
        }
        int dinc = 1 + lda, end = start + n * dinc;
        for (int idiag = start, irow = start, cend = start + n; idiag != end; ++irow, idiag += dinc, cend += lda) {
            // compute aii;
            double aii = pl[idiag];
            for (int j = irow; j != idiag; j += lda) {
                double x = pl[j];
                aii -= x * x;
            }
            if (aii <= -zero) { // negative
                throw new MatrixException(MatrixException.CHOLESKY);
            } else if (aii < zero) { // quasi-zero
                pl[idiag] = 0;
                // compute elements i+1 : n of column i
                for (int jx = irow; jx != idiag; jx += lda) {
                    double temp = pl[jx];
                    if (temp != 0) {
                        for (int ia = jx + 1, iy = idiag + 1; iy < cend; ++ia, ++iy) {
                            pl[iy] -= temp * pl[ia];
                        }
                    }
                }
                for (int iy = idiag + 1; iy < cend; ++iy) {
                    if (Math.abs(pl[iy]) > zero) {
                        throw new MatrixException(MatrixException.CHOLESKY);
                    } else {
                        pl[iy] = 0;
                    }
                }
            } else {
                aii = Math.sqrt(aii);
                pl[idiag] = aii;
                // compute elements i+1 : n of column i
                for (int jx = irow; jx != idiag; jx += lda) {
                    double temp = pl[jx];
                    if (temp != 0) {
                        for (int ia = jx + 1, iy = idiag + 1; iy < cend; ++ia, ++iy) {
                            pl[iy] -= temp * pl[ia];
                        }
                    }
                }
                double scale = 1 / aii;
                for (int iy = idiag + 1; iy < cend; ++iy) {
                    pl[iy] *= scale;
                }
            }
        }
    }

}
