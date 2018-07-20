/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or â€“ as soon they will be approved 
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

package ec.satoolkit.x11;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.linearfilters.FiniteFilter;
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;

/**
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SeasonalFilterFactory {

    /**
     *
     */
    public static final SymmetricFilter S3X1 = FilterFactory
	    .makeSymmetricFilter(3, 1);
    /**
     *
     */
    public static final SymmetricFilter S3X3 = FilterFactory
	    .makeSymmetricFilter(3, 3);
    /**
     *
     */
    public static final SymmetricFilter S3X5 = FilterFactory
	    .makeSymmetricFilter(3, 5);
    /**
     *
     */
    public static final SymmetricFilter S3X9 = FilterFactory
	    .makeSymmetricFilter(3, 9);
    /**
     *
     */
    public static final SymmetricFilter S3X15 = FilterFactory
	    .makeSymmetricFilter(3, 15);
    
    public static final AsymmetricEndPoints endPoints(int len){
        switch (len){
            case 1: return new AsymmetricEndPoints(FC1);
            case 2: return new AsymmetricEndPoints(FC3);
            case 3: return new AsymmetricEndPoints(FC5);
            case 5: return new AsymmetricEndPoints(FC9);
            default: return null;
        }
    }

    private static final double[] ma1x0 = { 0.39, 0.61 },
	    ma2x1 = { 3.0 / 27, 7.0 / 27, 10.0 / 27, 7.0 / 27 },
	    ma2x0 = { 5.0 / 27, 11.0 / 27, 11.0 / 27 },
	    ma3x2 = { 4.0 / 60, 8.0 / 60, 13.0 / 60, 13.0 / 60, 13.0 / 60,
		    9.0 / 60 },
	    ma3x1 = { 4.0 / 60, 11.0 / 60, 15.0 / 60, 15.0 / 60, 15.0 / 60 },
	    ma3x0 = { 9.0 / 60, 17.0 / 60, 17.0 / 60, 17.0 / 60 },
	    // { 35.0/1026, 75.0/1026, 114.0, 75.0/1026, 116.0, 75.0/1026,
	    // 75.0/1026,
	    // 117.0, 75.0/1026, 119.0, 75.0/1026, 120.0, 75.0/1026, 121.0,
	    // 75.0/1026,
	    // 123.0, 75.0/1026, 86.0, 75.0/1026 };
	    ma5x4 = { 0.034, 0.073, 0.111, 0.113, 0.114, 0.116, 0.117, 0.118,
		    0.12, 0.084 },
	    // { 35.0/1026, 77.0/1026, 116.0/1026, 120.0/1026, 126.0/1026,
	    // 131.0/1026, 135.0/1026, 141.0/1026, 145.0/1026 }
	    ma5x3 = { 0.034, 0.075, 0.113, 0.117, 0.123, 0.128, 0.132, 0.137,
		    0.141 },
	    // { 33.0/1026, 81.0/1026, 126.0/1026, 136.0/1026, 147.0/1026,
	    // 158.0/1026, 167.0/1026, 177.0/1026 }
	    ma5x2 = { 0.032, 0.079, 0.123, 0.133, 0.143, 0.154, 0.163, 0.173 },
	    // { 29.0/1026, 94.0/1026, 148.0/1026, 164.0/1026, 181.0/1026,
	    // 197.0/1026, 213.0/1026 }
	    ma5x1 = { 0.028, 0.092, 0.144, 0.160, 0.176, 0.192, 0.208 },
	    // { 52.0/1026, 115.0/1026, 177.0/1026, 202.0/1026, 227.0/1026,
	    // 252.0/1026 }
	    ma5x0 = { 0.051, 0.112, 0.173, 0.197, 0.221, 0.246 }, ma8x0 = {
		    0.02222, 0.04444, 0.06667, 0.06667, 0.16, 0.16, 0.16, 0.16,
		    0.16 }, ma8x1 = { 0.0222, 0.04444, 0.06667, 0.06667,
		    0.06667, 0.14667, 0.14667, 0.14667, 0.14667, 0.14667 },
	    ma8x2 = { 0.02223, 0.04444, 0.06667, 0.06667, 0.06667, 0.06667,
		    0.13333, 0.13333, 0.13333, 0.13333, 0.13333 }, ma8x3 = {
		    0.02221, 0.04444, 0.06667, 0.06667, 0.06667, 0.06667,
		    0.06667, 0.12, 0.12, 0.12, 0.12, 0.12 }, ma8x4 = { 0.02219,
		    0.04444, 0.06667, 0.06667, 0.06667, 0.06667, 0.06667,
		    0.06667, 0.10667, 0.10667, 0.10667, 0.10667, 0.10667 },
	    ma8x5 = { 0.02222, 0.04444, 0.06667, 0.06667, 0.06667, 0.06667,
		    0.06667, 0.06667, 0.06667, 0.09333, 0.09333, 0.09333,
		    0.09333, 0.09333 }, ma8x6 = { 0.0222, 0.04444, 0.06667,
		    0.06667, 0.06667, 0.06667, 0.06667, 0.06667, 0.06667,
		    0.06667, 0.08, 0.08, 0.08, 0.08, 0.08 }, ma8x7 = { 0.0222,
		    0.04444, 0.06667, 0.06667, 0.06667, 0.06667, 0.06667,
		    0.06667, 0.06667, 0.06667, 0.06667, 0.07111, 0.07111,
		    0.07111, 0.07111, 0.04889 };

    static final FiniteFilter M_1X0 = new FiniteFilter(ma1x0, -1);

    static final FiniteFilter M_2X1 = new FiniteFilter(ma2x1, -2);

    static final FiniteFilter M_2X0 = new FiniteFilter(ma2x0, -2);

    static final FiniteFilter M_3X2 = new FiniteFilter(ma3x2, -3);
    static final FiniteFilter M_3X1 = new FiniteFilter(ma3x1, -3);
    static final FiniteFilter M_3X0 = new FiniteFilter(ma3x0, -3);
    static final FiniteFilter M_5X4 = new FiniteFilter(ma5x4, -5);
    static final FiniteFilter M_5X3 = new FiniteFilter(ma5x3, -5);
    static final FiniteFilter M_5X2 = new FiniteFilter(ma5x2, -5);
    static final FiniteFilter M_5X1 = new FiniteFilter(ma5x1, -5);
    static final FiniteFilter M_5X0 = new FiniteFilter(ma5x0, -5);
    static final FiniteFilter M_8X0 = new FiniteFilter(ma8x0, -8);
    static final FiniteFilter M_8X1 = new FiniteFilter(ma8x1, -8);
    static final FiniteFilter M_8X2 = new FiniteFilter(ma8x2, -8);
    static final FiniteFilter M_8X3 = new FiniteFilter(ma8x3, -8);
    static final FiniteFilter M_8X4 = new FiniteFilter(ma8x4, -8);
    static final FiniteFilter M_8X5 = new FiniteFilter(ma8x5, -8);
    static final FiniteFilter M_8X6 = new FiniteFilter(ma8x6, -8);
    static final FiniteFilter M_8X7 = new FiniteFilter(ma8x7, -8);
    static final FiniteFilter[] FC1 = new FiniteFilter[] { M_1X0 };
    static final FiniteFilter[] FC3 = new FiniteFilter[] { M_2X1, M_2X0 };
    static final FiniteFilter[] FC5 = new FiniteFilter[] { M_3X2, M_3X1, M_3X0 };
    static final FiniteFilter[] FC9 = new FiniteFilter[] { M_5X4, M_5X3, M_5X2,
	    M_5X1, M_5X0 };
    static final FiniteFilter[] FC15 = new FiniteFilter[] { M_8X7, M_8X6,
	    M_8X5, M_8X4, M_8X3, M_8X2, M_8X1, M_8X0 };
    static final DefaultSeasonalFilteringStrategy C_S3X1 = new DefaultSeasonalFilteringStrategy(
	    S3X1, new AsymmetricEndPoints(FC1), "3x1");
    static final DefaultSeasonalFilteringStrategy C_S3X3 = new DefaultSeasonalFilteringStrategy(
	    S3X3, new AsymmetricEndPoints(FC3), "3x3");
    static final DefaultSeasonalFilteringStrategy C_S3X5 = new DefaultSeasonalFilteringStrategy(
	    S3X5, new AsymmetricEndPoints(FC5), "3x5");
    static final DefaultSeasonalFilteringStrategy C_S3X9 = new DefaultSeasonalFilteringStrategy(
	    S3X9, new AsymmetricEndPoints(FC9), "3x9");

    static final DefaultSeasonalFilteringStrategy C_S3X15 = new DefaultSeasonalFilteringStrategy(
	    S3X15, new AsymmetricEndPoints(FC15), "3x15");

    /**
     *
     * @param option
     * @return
     */
    public static DefaultSeasonalFilteringStrategy getDefaultFilteringStrategy(
	    final SeasonalFilterOption option) {
	switch (option) {
	case S3X1:
	    return C_S3X1;
	case S3X3:
	    return C_S3X3;
	case S3X5:
	    return C_S3X5;
	case S3X9:
	    return C_S3X9;
	case S3X15:
	    return C_S3X15;
	default:
	    return null;
	}
    }

//    /**
//     * The method returns an array of filters that corresponds to the filter for
//     * the global Moving Seasonality Ratio
//     * 
//     * @param rms
//     * @return The corresponding array of filters. Can be null.
//     */
//    public static FiniteFilter[] getEndPointsFiltersForGlobalRMS(
//	    final double rms) {
//	if (rms < 2.5)
//	    return FC3;
//	else if (rms >= 2.5 && rms < 3.5)
//	    return null;
//	else if (rms >= 3.5 && rms < 5.5)
//	    return FC5;
//	else if (rms >= 5.5 && rms < 6.5)
//	    return null;
//	else
//	    return FC9;
//    }
//
//    /**
//     * 
//     * @param rms
//     * @return
//     */
//    public static SymmetricFilter getFilterForGlobalRMS(final double rms)
//    {
//	if (rms < 2.5)
//	    return S3X3;
//	else if (rms >= 2.5 && rms < 3.5)
//	    return null;
//	else if (rms >= 3.5 && rms < 5.5)
//	    return S3X5;
//	else if (rms >= 5.5 && rms < 6.5)
//	    return null;
//	else
//	    return S3X9;
//    }

    /**
     * 
     * @param rms
     * @return
     */
    public static IFiltering getFilteringStrategyForGlobalRMS(final double rms)
    {
	if (rms < 2.5)
	    return C_S3X3;
	else if (rms >= 2.5 && rms < 3.5)
	    return null;
	else if (rms >= 3.5 && rms < 5.5)
	    return C_S3X5;
	else if (rms >= 5.5 && rms < 6.5)
	    return null;
	else
	    return C_S3X9;
    }

}
