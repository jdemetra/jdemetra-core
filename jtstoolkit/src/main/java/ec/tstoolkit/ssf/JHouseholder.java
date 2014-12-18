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
package ec.tstoolkit.ssf;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
class JHouseholder {
    // fails if ...

    private static final double m_eps = 1e-15;

    public boolean triangularize(final Matrix m, final int ISsf,
	    final int maxRow) {
	int nr = m.getRowsCount(), nc = m.getColumnsCount();
	int n = Math.min(nr, nc);
	if (n > maxRow)
	    n = maxRow;
	if (n == nr)
	    --n;
	double[] v = new double[nc];
	for (int r = 0; r < n; ++r) {
	    // Compute 2-norm of k-th row .
	    double jsigma = 0, sigma = 0;
	    for (int c = r + 1; c < nc; ++c) {
		double tmp = m.get(r, c);
		if (c < ISsf)
		    jsigma += tmp * tmp;
		else
		    jsigma -= tmp * tmp;
		sigma += tmp * tmp;
		v[c] = tmp;
	    }

	    //
	    if (sigma <= m_eps)
		continue;
	    double d = m.get(r, r);
	    double xJx = jsigma + d * d;
	    if (xJx <= 0)
		return false;

	    double jnrm = Math.sqrt(xJx);
	    if (d < 0)
		v[r] = d - jnrm;
	    else
		v[r] = -jsigma / (d + jnrm);

	    m.set(r, r, jnrm);
	    for (int c = r + 1; c < nc; ++c)
		m.set(r, c, 0);

	    double beta = 2 / (jsigma + v[r] * v[r]);

	    // updating the remaining rows:
	    // A P = A - w v', with w = b * A J v

	    for (int k = r + 1; k < nr; ++k) {
		double s = 0;
		for (int c = r; c < nc; ++c)
		    if (v[c] != 0)
			if (c < ISsf)
			    s += m.get(k, c) * v[c];
			else
			    s -= m.get(k, c) * v[c];

		s *= beta;
		for (int c = r; c < nc; ++c)
		    if (v[c] != 0)
			m.set(k, c, m.get(k, c) - s * v[c]);
	    }
	}
	return true;
    }
}
