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

package demetra.x11;

import demetra.data.DataBlock;
import demetra.design.Development;
import demetra.maths.polynomials.Polynomial;

/**
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class FalseAsymmetricHenderson7 implements IEndPointsProcessor {

    private double calc(Polynomial w, DataBlock x, int k, int m) {
	double r = w.get(0) * x.get(k);
	for (int i = 1; i < m; ++i)
	    r += w.get(i) * (x.get(k + i) + x.get(k - i));
	return r;
    }

    @Override
    public void process(DataBlock in, DataBlock out) {

	int n = in.length();
	for (int i = 5; i >= 0; --i) {
	    Polynomial w = TrendCycleFilterFactory.makeHendersonFilter(2 * i + 1)
		    .coefficientsAsPolynomial();
	    double s = 0;
	    for (int j = 0; j <= i; ++j) {
		out.set(i, calc(w, in, i, i));
		out.set(n - i - 1, calc(w, in, n - i - 1, i));
	    }
	}
    }
}
