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

import demetra.design.Development;
import demetra.maths.linearfilters.LinearFilterException;
import demetra.maths.linearfilters.SymmetricFilter;
import demetra.maths.polynomials.Polynomial;

/**
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
public final class FilterFactory {

    /*
     * The method returns a symmetric filter based on the value of the global
     * Moving Seasonality Ratio
     * 
     * @param rsm The global Moving Seasonality Ratio
     * 
     * @return The corresponding symmetric filter. (3x3, 3x5 or 3x9)
     */
    /**
     * 
     * @param rsm
     * @return
     */
    public static SymmetricFilter makeFilterForGlobalRSM(final double rsm)
    {
	if (rsm < 2.5)
	    return makeSymmetricFilter(3, 3);
	else if (rsm >= 2.5 && rsm < 3.5)
	    return null;
	else if (rsm >= 3.5 && rsm < 5.5)
	    return makeSymmetricFilter(3, 5);
	else if (rsm >= 5.5 && rsm < 6.5)
	    return null;
	else
	    return makeSymmetricFilter(3, 9);
    }

    /**
     * The method returns the appropriate symmetric filter for the given Month
     * for Cyclical Dominance value. The MCD corresponds to the first month
     * where the ratio I/C is smaller than 1.0.
     * 
     * @param data
     *            An array of double values, the ratios I/C
     * @return The corresponding symmetricFilter. Is never null.
     */
    public static SymmetricFilter makeMCDFilter(final double[] data) {
	int idx = -1;
	for (int i = data.length - 1; i >= 0; i--)
	    if (data[i] >= 1.0) {
		idx = i + 2; // add one for zero-based index; add another to
		// include value itself
		break;
	    }

	if (idx == -1)
	    return makeSymmetricFilter(0);

	if (idx > 6)
	    return makeSymmetricFilter(2, 6);
	else if (idx % 2 == 0)
	    return makeSymmetricFilter(2, idx);
	else
	    return makeSymmetricFilter(idx / 2);
    }

    /**
     * Creates a simple symmetric filter with identical weights. ( 1/length,...,
     * 1/length).
     * 
     * @param length Length of the filter. Should be odd
     * @return The corresponding symmetric filter
     */
    public static SymmetricFilter makeSymmetricFilter(final int length) {
	if (length % 2 == 0)
	    throw new LinearFilterException(
		    "Invalid length for Henderson filter. Should be odd");
	double[] c = new double[length / 2 + 1];
	double w = 1.0 / length;
	for (int i = 0; i < c.length; i++)
	    c[i] = w;
	return SymmetricFilter.of(c);
    }

    /**
     * Creates a m x n symmetric filter. The sum of m and n should be even.
     * The length of the final filter is (m+n-1).
     * 
     * @param m The length of the first filter.
     * @param n The length of the second filter
     * @return The corresponding symmetric filter
     */
    public static SymmetricFilter makeSymmetricFilter(final int m, final int n) {
	Polynomial M = simpleFilter(m);
	Polynomial N = simpleFilter(n);
	return SymmetricFilter.createFromWeights(M.times(N));

    }

    static Polynomial simpleFilter(int len) {
	double[] c = new double[len];
	double w = 1.0 / len;
	for (int i = 0; i < len; i++)
	    c[i] = w;
	return Polynomial.of(c);
    }

    // / <summary>
    // / The method returns filterchain that corresponds with the filter for the
    // global
    // / Moving Seasonality Ratio
    // / </summary>
    // / <param name="rsm">The global Moving Seasonality Ratio</param>
    // / <returns>A FilterChain</returns>

    // / <summary>
    // /
    // / </summary>
    // / <param name="data"></param>
    // / <returns></returns>

    private FilterFactory() {
    }

    // / <summary>
    // / The method returns the appropriate FilterChain containing asymmetric
    // Musgrave filters.
    // / </summary>
    // / <param name="length">The length of the filter</param>
    // / <param name="midpoint">The midpoint of the filter</param>
    // / <param name="x11">Indicates whether X11 or X12 Musgrave filters are
    // demanded.</param>
    // / <param name="frequency">Frequency of the series to be filtered</param>
    // / <returns></returns>
    /*
     * public static FiniteFilter[] makeHendersonFilterChain(final int length,
     * final int midpoint, final boolean x11, final int frequency) {
     * List<IFiniteFilter> fc = new ArrayList<IFiniteFilter>(); // special
     * coefficients for X11-ARIMA if (x11 && length == 2) for (int i = 0; i <
     * midpoint; i++) fc.add(MusgraveFilter.X11MusgraveFilter(midpoint, i));
     * else for (int i = 0; i < midpoint; i++) fc.add(new MusgraveFilter(length,
     * midpoint, i, frequency)); return fc; }
     */

    // / <summary>
    // / The property returns an averaging stable seasonality filter.
    // / </summary>
    /*
     * public static IFiniteFilter stable() { return new
     * AveragingStableSeasonalityFilter(); }*
     * 
     * // / <summary> // / The method returns a Henderson filter of length l. L
     * must not be even // / and smaller than or equal to 101. This method is
     * used to set a user // defined // / Trend filter in the
     * ExecutionParameters. // / </summary> // / <param name="l">The length of
     * the Henderson filter (l .LE. 101 and (l // mod 2) != 0) </param> // /
     * <returns>A Henderson filter of the requested length</returns> public
     * static IFiniteFilter trendMA(final int l) { if (l % 2 == 0) throw new
     * X11Exception("The parameter to TrendMA must be odd"); if (l > 101) throw
     * new X11Exception( "The parameter must be equal to or less than 101");
     * return HendersonFilters.instance.create(l); }
     */

}
