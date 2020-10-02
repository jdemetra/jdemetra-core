/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.math.matrices.decomposition;

import demetra.math.Constants;
import jdplus.math.matrices.LowerTriangularMatrix;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.MatrixException;

/**
 * Cholesky decomposition with complete pivoting.
 * Based on Lapack (DPSTF2)
 *
 * @author PALATEJ
 */
public class CholeskyWithPivoting {

    private Matrix M;
    private int[] piv;
    private int rank, pvt;
    private double ajj, tol;

    /**
     * Lower Cholesky (P'*S*P = L*L') where P is the pivoting matrix
     *
     * @param S Original symmetric matrix
     * @param tol User-defined tolerance. Default tolerance is used when tol is
     * negative
     * @return True when a full decomposition has bee achieved, false if the
     * matrix is rank-deficient
     */
    public boolean decompose(Matrix S, double tol) {
        initialize(S, tol);
        int n = M.getColumnsCount();
        double[] pm = M.getStorage();
        double[] v = new double[n];
        double[] w = new double[n];
        for (int j = 0; j < n; ++j) {
            for (int i = j; i < n; ++i) {
                if (j > 0) {
                    double mij = M.get(i, j - 1);
                    v[i] += mij * mij;
                }
                w[i] = M.get(i, i) - v[i];
            }
            if (j > 0) { // pvt computed in the initialization
                ajj = w[j];
                pvt = j;
                for (int k = j + 1; k < n; ++k) {
                    if (w[k] > ajj) {
                        ajj = w[k];
                        pvt = k;
                    }
                }
                if (ajj < tol) {
                    M.set(j, j, ajj);
                    rank = j;
                    return false;
                }
            }

            if (pvt != j) { // pivoting is needed
                // diag
                M.set(pvt, pvt, M.get(j, j));
                // row
                for (int k = 0; k < j; ++k) {
                    double tmp = M.get(j, k);
                    M.set(j, k, M.get(pvt, k));
                    M.set(pvt, k, tmp);
                }
                // column
                for (int k = pvt + 1; k < n; ++k) {
                    double tmp = M.get(k, j);
                    M.set(k, j, M.get(k, pvt));
                    M.set(k, pvt, tmp);
                }
                for (int k = j + 1; k < pvt; ++k) {
                    double tmp = M.get(k, j);
                    M.set(k, j, M.get(pvt, k));
                    M.set(pvt, k, tmp);
                }
                
                // dot products
                double tmp = v[pvt];
                v[pvt] = v[j];
                v[j] = tmp;

                // pivot
                int itmp = piv[pvt];
                piv[pvt] = piv[j];
                piv[j] = itmp;
            }
            ajj = Math.sqrt(ajj);
            M.set(j, j, ajj);
            // compute elements j+1 : n of column j
            for (int k = 0; k < j; ++k) {
                double tmp = M.get(j, k);
                if (tmp != 0) {
                    for (int l = j + 1; l < n; ++l) {
                        M.add(l, j, -tmp * M.get(l, k));
                    }
                }
            }
            double scale=1/ajj;
            for (int l = j + 1; l < n; ++l) {
                M.mul(l, j, scale);
            }

//                for (int jx = irow; jx != idiag; jx += lda) {
//                    double temp = pl[jx];
//                    if (temp != 0) {
//                        for (int ia = jx + 1, iy = idiag + 1; iy < cend; ++ia, ++iy) {
//                            pl[iy] -= temp * pl[ia];
//                        }
//                    }
//                }
//                for (int iy = idiag + 1; iy < cend; ++iy) {
//                    pl[iy] /= aii;
//                }
        }
        rank=n;
        return true;
    }


    private boolean initialize(Matrix S, double eps) {
        int n = S.getColumnsCount();
        if (S.getRowsCount() != n) {
            throw new MatrixException(MatrixException.SQUARE);
        }
        M = S.deepClone();
        piv = new int[n];
        double[] pm = M.getStorage();
        pvt = 0;
        ajj = pm[0];
        for (int i = 1, j = n + 1; i < n; ++i, j += n + 1) {
            piv[i] = i;
            double d = pm[j];
            if (d > ajj) {
                pvt = i;
                ajj = d;
            }
        }
        if (ajj <= 0) {
            rank = 0;
            return false;
        }
        if (tol < 0) {
            tol = ajj * Constants.getEpsilon() * n;
        } else {
            tol = eps;
        }
        return true;
    }

    public int[] getPivot() {
        return piv;
    }

    public int getRank() {
        return rank;
    }
    
    public Matrix getL(){
        LowerTriangularMatrix.toLower(M);
        return M;
    }

}
