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
package demetra.data;

import demetra.design.Development;
import demetra.utilities.IntList;

/**
 * Interpolates a series by replacing missing values by a pre-specified
 * value. This approach is used in the X13 software (in combination with
 * the "additive outlier" approach.
 * Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class ConstInterpolator implements IDataInterpolator {

    /**
     * The default value used for interpolation
     */
    public static final double BIGVALUE = 1e10;
    /**
     * The constant used in the interpolation
     */
    public final double value;

    /**
     * Create a default interpolator, using the average as interpolation value
     */
    public ConstInterpolator() {
        value = Double.NaN;
    }

    /**
     * Create an interpolator, using a given interpolation value
     *
     * @param val The interpolation value
     */
    public ConstInterpolator(double val) {
        value = val;
    }

    @Override
    public double[] interpolate(DoubleSequence data, IntList missingpos) {
        double[] ndata = data.toArray();
        double nval = value;
        if (!Double.isFinite(nval)) { // use the average
            nval = Doubles.averageWithMissing(data);
        }
        for (int i = 0; i < ndata.length; ++i) {
            if (!Double.isFinite(ndata[i])) {
                missingpos.add(i);
                ndata[i] = nval;
            }
        }
        return ndata;
    }
}
