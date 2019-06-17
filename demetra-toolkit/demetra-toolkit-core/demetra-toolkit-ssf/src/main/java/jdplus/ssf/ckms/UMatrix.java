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
package jdplus.ssf.ckms;

import demetra.design.Development;

/**
 * We consider the matrix <br>
 * <code>| R Z | 
 *       | K L |</code>
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class UMatrix {

    double R, Z;
    double[] K, L;
    private static final double EPS = 1e-15;

    UMatrix() {
    }

    // fails if ...
    boolean triangularize() {
	// Compute 2-norm of k-th row .
	double jsigma = -Z * Z;
	//
	if (-jsigma <= EPS)
	    return true;
	double xJx = jsigma + R * R;
	if (xJx <= 0)
	    return false;

	double jnrm = Math.sqrt(xJx);
	double v = -jsigma / (R + jnrm), w = Z;

	R = jnrm;
	Z = 0;

	double beta = 2 / (jsigma + v * v);

	// updating the remaining rows:
	// A P = A - w v', with w = b * A J v

	int nr = K.length;
	for (int k = 0; k < nr; ++k) {
	    double s = (K[k] * v - L[k] * w) * beta;
	    K[k] -= s * v;
	    L[k] -= s * w;
	}
	return true;
    }
}
