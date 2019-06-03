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

package jdplus.maths.matrices.decomposition;

import demetra.design.Development;
import demetra.maths.Constants;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.MatrixException;
import jdplus.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class Gauss extends AbstractLuDecomposition {

    @Override
    public void decompose(FastMatrix m) {
        init(m);
        gauss();
    }


    /// <summary>
    /// The method implements the Gauss algorithm for LU-decomposition of a square matrix.
    /// </summary>
    private void gauss() {
        for (int k = 0, kn = 0; k < n; k++, kn++) {
            // Find pivot.
            int p = k;
            double pmax = Math.abs(lu[kn + k]);
            for (int i = k + 1, idx = i + k * n; i < n; i++, idx++) {
                double cur = Math.abs(lu[idx]);
                if (cur > pmax) {
                    p = i;
                    pmax = cur;
                }
            }
            // Exchange if necessary.
            if (p != k) {
                for (int j = 0, pj = p, kj = k; j < n; ++j, pj += n, kj += n) {
                    double tmp = lu[pj];
                    lu[pj] = lu[kj];
                    lu[kj] = tmp;
                }
                int t = piv[p];
                piv[p] = piv[k];
                piv[k] = t;

                pivSign = -pivSign;
            }
            // Compute multipliers and eliminate k-th column.
            double kk = lu[kn + k * n];
            if (Math.abs(kk) < eps) {
                throw new MatrixException(MatrixException.SINGULAR);
            }

            for (int i = k + 1, idx = i; i < n; i++, idx++) {
                lu[idx + k * n] /= kk;

                for (int j = k + 1; j < n; j++) {
                    lu[idx + j * n] -= lu[idx + k * n] * lu[kn + j * n];
                }
            }
        }
    }
}
