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


package ec.tstoolkit.timeseries.simplets;

import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.utilities.IntList;

/**
 * Interpolation of missing values by using the average of its n non missing
 * values. At the beginning or at the end of the series, the nearest non
 * missing value is replicated.
 * This approach is used in the first steps of Tramo.
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class AverageInterpolator implements ITsDataInterpolator
{
//    /**
//     * 
//     * @param y
//     * @return
//     */
//    public static int[] cleanMissings(double[] y)
//    {
//	IntList missings = new IntList(y.length);
//	if (!cleanMissings(y, missings))
//	    throw new TsException(TsException.TS_EMPTY, "Averageinterpolator");
//	else if (missings.isEmpty())
//	    return null;
//	else {
//            return missings.toArray();
//	}
//    }

    /**
     * 
     * @param y
     * @param missings
     * @return
     */
    public static boolean cleanMissings(double[] y, IntList missings)
    {
	// starting at the end of the series
	int ny = y.length, i0 = -1, i1 = -1;
	for (int i = ny - 1; i >= 0; --i) {
	    if (!DescriptiveStatistics.isFinite(y[i])) {
		if (i1 == -1)
		    i1 = i + 1;
		i0 = i - 1;
		if (missings != null)
		    missings.add(i);
	    } else if (i0 != -1) // finishing run of missing values
	    {
		// interpolating
		double v = (i1 >= ny) ? y[i0] : (y[i1] + y[i0]) / 2;
		for (int j = i0 + 1; j < i1; ++j)
		    y[j] = v;
		i0 = i1 = -1;
	    }
	}
	if (i1 > 0) {
	    if (i1 >= ny)
		return false;
	    for (int j = 0; j < i1; ++j)
		y[j] = y[i1];
	}
	return true;
    }

    @Override
    public boolean interpolate(TsData data, IntList missingpos)
    {
	return cleanMissings(data.getValues().internalStorage(), missingpos);
    }

}
