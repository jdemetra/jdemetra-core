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
 * LU decomposition by means of the Crout-Doolittle algorithm
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class CroutDoolittle extends LuDecomposition {

    @Override
    public void decompose(Matrix m) {
        init(m, true);
        croutdoolittle();
    }

    @Override
    public void decompose(SubMatrix m) {
        init(new Matrix(m), false);
        croutdoolittle();
    }

    /// <summary>
    /// The method implements the Crout-Doolittle algorithm for LU-decomposition
    /// of a square matrix.
    /// </summary>
    private void croutdoolittle() {
        // Use a "left-looking", dot-product, Crout/Doolittle algorithm.

        double[] LUcolj = new double[n_];

        // Outer loop.
        for (int j = 0; j < n_; j++) {

            // Make a copy of the j-th column to localize references.

            for (int i = 0, idx = j * n_; i < n_; i++, idx++) {
                LUcolj[i] = lu_[idx];
            }

            // Apply previous transformations.

            for (int i = 0, idx = 0; i < n_; i++, idx++) {
                // Most of the time is spent in the following dot product.

                int kmax = i <= j ? i : j;
                double s = 0.0;
                for (int k = 0; k < kmax; k++) {
                    s += lu_[idx + k * n_] * LUcolj[k];
                }

                lu_[idx + j * n_] = LUcolj[i] -= s;
            }

            // Find pivot and exchange if necessary.

            int p = j;
            for (int i = j + 1; i < n_; i++) {
                if (Math.abs(LUcolj[i]) > Math.abs(LUcolj[p])) {
                    p = i;
                }
            }

            if (p != j) {
                for (int k = 0, pk = p, jk = j; k < n_; ++k, pk += n_, jk += n_) {
                    double t = lu_[pk];
                    lu_[pk] = lu_[jk];
                    lu_[jk] = t;
                }
                int kk = piv_[p];
                piv_[p] = piv_[j];
                piv_[j] = kk;
                pivsign_ = -pivsign_;
            }

            // Compute multipliers.
            int jj = j * n_ + j;

            if (Math.abs(lu_[jj]) < getEpsilon()) {
                throw new MatrixException("LU decomposition: Singular matrix");
            }
            for (int i = j + 1; i < n_; i++) {
                lu_[j * n_ + i] /= lu_[jj];
            }
        }
    }
}
