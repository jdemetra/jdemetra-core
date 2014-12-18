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
 * Interpolates a series by replacing missing values by a pre-specified
 * value. This approach is used in the X13 software (in combination with
 * the "additive outlier" approach.
 * Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class ConstInterpolator implements ITsDataInterpolator {

    /**
     * The default value used for interpolation
     */
    public static final double BIGVALUE=1e10;
    /**
     * The constant used in the interpolation 
     */
    public final double value;

    /**
     * Create a default interpolator, using BIGVALUE as interpolation value
     */
    public ConstInterpolator()
    {
	value = Double.NaN;
    }

    /**
     * Create an interpolator, using a given interpolation value
     * @param val The interpolation value
     */
    public ConstInterpolator(double val)
    {
	value = val;
    }

    @Override
    public boolean interpolate(TsData data, IntList missingpos)
    {
        if (! data.getValues().hasMissingValues())
            return true;
        double nval=value;
        if (Double.isNaN(nval)){
            // use the average of the series
            DescriptiveStatistics stats=new DescriptiveStatistics(data);
            nval=stats.getAverage();
        }
	double[] val = data.getValues().internalStorage();
	for (int i = 0; i < val.length; ++i)
	    if (!DescriptiveStatistics.isFinite(val[i])) {
		missingpos.add(i);
		val[i] = nval;
	    }
	return true;
    }
}
