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

package demetra.maths.matrices.internal;


import demetra.design.Development;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.MatrixException;
import jdplus.maths.matrices.FastMatrix;

/**
 * LU decomposition by means of the Crout-Doolittle algorithm
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class CroutDoolittle extends AbstractLuDecomposition {

    @Override
    public void decompose(FastMatrix m) {
        init(m);
        croutdoolittle();
    }

    /// <summary>
    /// The method implements the Crout-Doolittle algorithm for LU-decomposition
    /// of a square matrix.
    /// </summary>
    private void croutdoolittle() {
        // Use a "left-looking", dot-product, Crout/Doolittle algorithm.

        double[] LUcolj = new double[n];

        // Outer loop.
        for (int j = 0; j < n; j++) {

            // Make a copy of the j-th column to localize references.

            for (int i = 0, idx = j * n; i < n; i++, idx++) {
                LUcolj[i] = lu[idx];
            }

            // Apply previous transformations.

            for (int i = 0, idx = 0; i < n; i++, idx++) {
                // Most of the time is spent in the following dot product.

                int kmax = i <= j ? i : j;
                double s = 0.0;
                for (int k = 0; k < kmax; k++) {
                    s += lu[idx + k * n] * LUcolj[k];
                }

                lu[idx + j * n] = LUcolj[i] -= s;
            }

            // Find pivot and exchange if necessary.

            int p = j;
            for (int i = j + 1; i < n; i++) {
                if (Math.abs(LUcolj[i]) > Math.abs(LUcolj[p])) {
                    p = i;
                }
            }

            if (p != j) {
                for (int k = 0, pk = p, jk = j; k < n; ++k, pk += n, jk += n) {
                    double t = lu[pk];
                    lu[pk] = lu[jk];
                    lu[jk] = t;
                }
                int kk = piv[p];
                piv[p] = piv[j];
                piv[j] = kk;
                pivSign = -pivSign;
            }

            // Compute multipliers.
            int jj = j * n + j;

            if (Math.abs(lu[jj]) < eps) {
                throw new MatrixException(MatrixException.SINGULAR);
            }
            for (int i = j + 1; i < n; i++) {
                lu[j * n + i] /= lu[jj];
            }
        }
    }
}
