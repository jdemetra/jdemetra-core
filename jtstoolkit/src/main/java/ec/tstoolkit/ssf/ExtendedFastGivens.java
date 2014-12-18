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

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.SubMatrix;

/**
 * 
 * @author Jean Palate See Saligari / Snyder
 * 
 */
@Development(status = Development.Status.Preliminary)
public final class ExtendedFastGivens {

    // X.W.X' = L.V.L'
    /**
     * 
     * @param X
     * @param W
     * @return
     */
    public static boolean process(final SubMatrix X, final DataBlock W)
    {
	DataBlockIterator xcols = X.columns();
	DataBlock Xj = xcols.getData();
	int nr = X.getRowsCount();
	do {
	    int j = xcols.getPosition();
	    double w = W.get(j);
	    if (w != 0) {
		int jmax = Math.min(j, nr);
		for (int i = 0; i < jmax; ++i) {
		    double xi = Xj.get(0);
		    if (xi != 0) {
			Xj.set(0, 0);
			Xj = Xj.drop(1, 0);
			DataBlock Li = X.column(i).drop(i + 1, 0);
			double l = W.get(i);
			if (l == 0) {
			    Li.product(Xj, 1 / xi);
			    if (Double.isInfinite(w))
				W.set(i, Double.POSITIVE_INFINITY);
			    else
				W.set(i, w * xi * xi);
			    w = 0;
			    Xj.set(0);
			    break;
			} else if (Double.isInfinite(l))
			    Xj.addAY(-xi, Li);
			else if (Double.isInfinite(w)) {
			    w = l / (xi * xi);
			    W.set(i, Double.POSITIVE_INFINITY);
			    for (int k = 0; k < nr - i - 1; ++k) {
				double lk = Li.get(k);
				double xk = Xj.get(k);
				Xj.add(k, -xi * lk);
				Li.set(k, xk / xi);
			    }
			} else {
			    // normal case
			    double nl = l + w * xi * xi;
			    double rl = l / nl;
			    W.set(i, nl);
			    double c = w * xi / nl;
			    int nk = Li.getLength();
			    for (int k = 0; k < nk; ++k) {
				double lk = Li.get(k);
				double xk = Xj.get(k);
				Xj.add(k, -xi * lk);
				Li.set(k, rl * lk + c * xk);
			    }
			    w *= rl;
			}
		    } else
			Xj = Xj.drop(1, 0);
		}
		if (j < nr) {
		    double xj = Xj.get(j);
		    if (xj != 0 && xj != 1)
			Xj.drop(j, 0).mul(1 / xj);
		    if (! Double.isInfinite(w))
			w *= xj * xj;
		}
		W.set(j, w);

	    }
	} while (xcols.next());

	int n = W.getLength() - nr;
	if (n > 0)
	    W.extract(nr, n, 1).set(0);

	X.diagonal().set(1);
	return true;
    }

    private ExtendedFastGivens() {
    }
}
