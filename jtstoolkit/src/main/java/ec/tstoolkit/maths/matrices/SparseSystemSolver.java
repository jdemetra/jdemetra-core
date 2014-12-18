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
import java.util.Arrays;

/**
 * This class is a translation of the code used in Seats (routine MLTSOL)
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public final class SparseSystemSolver {

    private SparseSystemSolver() {
        // static class
    }
    
    /**
     * Compute X such that M * X = B (or X = M^-1 * B)
     * 
     * @param A On entry, the matrix A contains M | B. On exit, 
     * it contains M | X.
     * @return
     */
    public static boolean solve(Matrix A) {
	int n = A.getRowsCount(), nl = A.getColumnsCount(), l = nl - n;
	int[] m = new int[nl];
        Arrays.fill(m, -1);

	for (int i = 0; i < n; ++i) {
	    double u = Double.MIN_VALUE;
	    int icur = -1;

	    for (int k = 0; k < n; ++k)
		if (m[k] == -1) // unused
		{
		    double a = A.get(i, k);
		    double absa = Math.abs(a);
		    if (absa > u) {
			icur = k;
			u = absa;
		    }
		}
	    if (icur < 0)
		return false;
	    m[icur] = i;
	    double pivot = 1 / A.get(i, icur);
	    if (Math.abs(pivot) <= 1e-15)
		pivot = 0;
	    for (int j = 0; j < n; ++j) {
		if (j != i) {
		    double a = A.get(j, icur);
		    if (Math.abs(a) >= 1e-13) {
			double fac = pivot * a;
			for (int k = 0; k < nl; ++k) {
			    double aik = A.get(i, k);
			    if (Math.abs(aik) <= 1e-15) {
				A.set(i, k, 0);
				aik = 0;
			    }
			    if (m[k] == -1)
				A.add(j, k, -fac * aik);
			}
		    }
		}
	    }
	    for (int k = 0; k < nl; ++k)
		if (m[k] == -1)
		    A.mul(i, k, pivot);
	}
	double[] b = new double[n];
	for (int k = n; k < nl; ++k) {
	    for (int i = 0; i < n; ++i)
		if (m[i] != -1)
		    b[i] = A.get(m[i], k);
	    for (int i = 0; i < n; ++i)
		A.set(i, k, b[i]);
	}
	return true;
    }
}
