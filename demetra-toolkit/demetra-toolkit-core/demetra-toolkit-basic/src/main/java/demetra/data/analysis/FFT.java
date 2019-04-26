/*
 * Copyright 2017 National Bank copyOf Belgium
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
package demetra.data.analysis;

import demetra.design.Development;
import demetra.maths.Complex;
import demetra.maths.Constants;
import demetra.util.Arrays2;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.experimental.UtilityClass
public class FFT {

    private final double[] COS_ARRAY;
    private final double[] SIN_ARRAY;

    static {
	COS_ARRAY = new double[16];
	SIN_ARRAY = new double[16];
	double twopi = Constants.TWOPI;
	double n = 2;
	for (int i = 0; i < 16; ++i) {
	    COS_ARRAY[i] = Math.cos(twopi / n);
	    SIN_ARRAY[i] = Math.sin(twopi / n);
	    n *= 2;
	}
    }

    /**
     * 
     * @param data
     */
    public void backTransform(final Complex[] data) {
	transform(data, true);
    }

    /**
     * 
     * @param data
     */
    public void transform(final Complex[] data)
    {
	transform(data, false);
    }

    private void transform(final Complex[] data, final boolean back) {
	final int n = data.length;
	for (int i = 0, j = 0; i < n; ++i) {
	    if (j > i)
		Arrays2.swap(data, i, j);
	    int q = n >> 1;
	    while (q >= 1 && j >= q) {
		j -= q;
		q >>= 1;
	    }
	    j += q;
	}
	// Danielson-Lanzcos routine
	int m = 1, s = 0;
	// external loop
	while (m < n) {
	    int tm = m << 1;
	    Complex wm = Complex.cart(COS_ARRAY[s], back ? -SIN_ARRAY[s]
		    : SIN_ARRAY[s]);
	    Complex w = Complex.ONE;
	    // internal loops
	    for (int j = 0; j < m; ++j) {
		for (int k = j; k < n; k += tm) {
		    int l = k + m;
		    Complex t = w.times(data[l]);
		    Complex u = data[k];
		    data[k] = u.plus(t);
		    data[l] = u.minus(t);
		}
		w = w.times(wm);
	    }
	    m = tm;
	    ++s;
	}
	if (back) {
	    final double v = 1.0 / n;
	    for (int i = 0; i < n; ++i)
		data[i] = data[i].times(v);
	}
    }
}