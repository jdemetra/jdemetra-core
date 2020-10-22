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


package jdplus.x11.filter;

import nbbrd.design.Development;
import jdplus.math.linearfilters.LinearFilterException;
import jdplus.math.linearfilters.SymmetricFilter;


/**
 * Creates the filters for Trend-Cycle estimation
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Release)
public final class X11TrendCycleFilterFactory {

    /**
     * The method returns the appropriate default Henderson filter for the
     * Trend-Cycle estimation. The length depends on the frequency of the
     * series: H(13) for monthly series, H(5) for quarterly series.
     * 
     * @param frequency The annual frequency of the series
     * @return The corresponding Henderson filter.
     */
    public static SymmetricFilter defaultHendersonFilterForFrequency(
	    final int frequency) {
	int len = frequency;
	if (frequency % 2 == 0)
	    ++len;

	return makeHendersonFilter(len);
    }

    /**
     * Creates an Henderson filter for a given length
     * 
     * @param length The length of the Henderson filter. Should be an odd number
     * @return The corresponding Henderson filter
     */
    public static SymmetricFilter makeHendersonFilter(int length) {
	if (length % 2 == 0)
	    throw new LinearFilterException("Invalid length for Henderson filter. Should be odd");
	int m = length / 2;
	double[] c = new double[m + 1];
	int n = m + 2;

	double n2 = n * n;
	for (int i = 0; i < m + 1; i++) {
	    double ii = (i - m) * (i - m);
	    double up = 315 * (n2 - n * 2 + 1 - (ii));
	    up *= n2 - ii;
	    up *= n2 + n * 2 + 1 - ii;
	    up *= n2 * 3 - 16 - ii * 11;
	    double down = n * 8;
	    down *= n2 - 1;
	    down *= n2 * 4 - 1;
	    down *= n2 * 4 - 9;
	    down *= n2 * 4 - 25;
	    c[m - i] = up / down;
	}
	return SymmetricFilter.ofInternal(c);
    }

    /**
     * The method returns the appropriate trend filter for the given frequency.
     * The trend filter is a simple (centred) filter with equals weights.
     * @param frequency The annual frequency of the series
     * @return A symmetric filter of order 2 x frequency.if frequency is even or
     *         of order frequency if frequency is odd.
     * For example, the trend filter for quarterly series is:
     * 0.125 0.25 0.25 0.25 0.125  
     * 
     */
    public static SymmetricFilter makeTrendFilter(final int frequency) {
	if (frequency % 2 == 0)
	    return X11FilterFactory.makeSymmetricFilter(2, frequency);
	else
	    return X11FilterFactory.makeSymmetricFilter(frequency);
    }

}
