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

package ec.tstoolkit.maths.matrices;

import ec.tstoolkit.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class Gauss extends LuDecomposition {

    @Override
    public void decompose(Matrix m) {
        init(m, true);
        gauss();
    }

    @Override
    public void decompose(SubMatrix m) {
        init(new Matrix(m), false);
        gauss();
    }

    /// <summary>
    /// The method implements the Gauss algorithm for LU-decomposition of a square matrix.
    /// </summary>
    private void gauss() {
        for (int k = 0, kn = 0; k < n_; k++, kn++) {
            // Find pivot.
            int p = k;
            double pmax = Math.abs(lu_[kn + k]);
            for (int i = k + 1, idx = i + k * n_; i < n_; i++, idx++) {
                double cur = Math.abs(lu_[idx]);
                if (cur > pmax) {
                    p = i;
                    pmax = cur;
                }
            }
            // Exchange if necessary.
            if (p != k) {
                for (int j = 0, pj = p, kj = k; j < n_; ++j, pj += n_, kj += n_) {
                    double tmp = lu_[pj];
                    lu_[pj] = lu_[kj];
                    lu_[kj] = tmp;
                }
                int t = piv_[p];
                piv_[p] = piv_[k];
                piv_[k] = t;

                pivsign_ = -pivsign_;
            }
            // Compute multipliers and eliminate k-th column.
            double kk = lu_[kn + k * n_];
            if (Math.abs(kk) < getEpsilon()) {
                throw new MatrixException("LU decomposition: Singular matrix");
            }

            for (int i = k + 1, idx = i; i < n_; i++, idx++) {
                lu_[idx + k * n_] /= kk;

                for (int j = k + 1; j < n_; j++) {
                    lu_[idx + j * n_] -= lu_[idx + k * n_] * lu_[kn + j * n_];
                }
            }
        }
    }
}
